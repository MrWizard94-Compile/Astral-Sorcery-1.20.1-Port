#!/usr/bin/env python3
"""
Expands star imports in Java source files to explicit imports.

For own packages (hellfirepvp.*): scans the source tree to build class lists.
For static star imports: scans the class body to get field/constant names.
For external packages: uses hardcoded class lists.
"""
import re
import os
import sys
from pathlib import Path
from collections import defaultdict

SRC_ROOT = Path("src/main/java")

# ── External package class lists ──────────────────────────────────────────────

JAVA_UTIL = {
    "ArrayList", "ArrayDeque", "Arrays", "Collection", "Collections", "Comparator",
    "Deque", "EnumMap", "EnumSet", "HashMap", "HashSet", "IdentityHashMap",
    "Iterator", "LinkedHashMap", "LinkedHashSet", "LinkedList", "List",
    "Map", "Objects", "Optional", "PriorityQueue", "Properties", "Queue",
    "Random", "Set", "Stack", "TreeMap", "TreeSet", "UUID", "WeakHashMap",
    "NoSuchElementException", "ConcurrentModificationException", "Locale",
}

JAVA_UTIL_FUNCTION = {
    "BiConsumer", "BiFunction", "BiPredicate", "BooleanSupplier", "Consumer",
    "DoubleConsumer", "DoubleFunction", "DoublePredicate", "DoubleSupplier",
    "Function", "IntConsumer", "IntFunction", "IntPredicate", "IntSupplier",
    "LongConsumer", "LongFunction", "LongPredicate", "LongSupplier",
    "ObjDoubleConsumer", "ObjIntConsumer", "ObjLongConsumer", "Predicate",
    "Supplier", "UnaryOperator", "BinaryOperator",
    "ToDoubleFunction", "ToIntFunction", "ToLongFunction",
    "ToDoubleBiFunction", "ToIntBiFunction", "ToLongBiFunction",
}

JAVA_UTIL_STREAM = {
    "Collectors", "Stream", "IntStream", "LongStream", "DoubleStream",
    "StreamSupport", "Collector",
}

JAVA_AWT = {
    "Color", "Dimension", "Font", "FontMetrics", "Graphics", "Graphics2D",
    "GraphicsConfiguration", "GraphicsDevice", "GraphicsEnvironment",
    "Image", "Insets", "LayoutManager", "MediaTracker", "Point",
    "Polygon", "Rectangle", "RenderingHints", "Shape", "Stroke", "Toolkit",
}

COM_GOOGLE_GSON = {
    "Gson", "GsonBuilder", "JsonArray", "JsonDeserializationContext",
    "JsonDeserializer", "JsonElement", "JsonNull", "JsonObject",
    "JsonParseException", "JsonPrimitive", "JsonSerializationContext",
    "JsonSerializer", "JsonSyntaxException", "TypeAdapter", "TypeAdapterFactory",
}

NET_MC_NBT = {
    "CompoundTag", "ListTag", "StringTag", "IntTag", "LongTag", "FloatTag",
    "DoubleTag", "ByteTag", "ShortTag", "ByteArrayTag", "IntArrayTag",
    "LongArrayTag", "Tag", "EndTag", "NbtUtils", "NbtAccounter",
}

NET_MC_WORLD_ITEM = {
    "Item", "ItemStack", "Items", "BlockItem", "ArmorItem", "SwordItem",
    "PickaxeItem", "AxeItem", "ShovelItem", "HoeItem", "BowItem",
    "CrossbowItem", "TieredItem", "CreativeModeTab", "ItemUtils",
    "ItemCooldowns", "ArmorMaterial",
}

NET_MC_GEOM_BUILDERS = {
    "CubeDefinition", "CubeDeformation", "CubeListBuilder", "LayerDefinition",
    "MeshDefinition", "PartDefinition", "PartPose",
}

NET_MINECRAFTFORGE_EVENT_LIVING = {
    "LivingAttackEvent", "LivingDamageEvent", "LivingDeathEvent",
    "LivingDropsEvent", "LivingEntityUseItemEvent", "LivingEvent",
    "LivingFallEvent", "LivingHealEvent", "LivingHurtEvent",
    "LivingJumpEvent", "LivingKnockBackEvent",
}

NET_MINECRAFTFORGE_EVENTBUS_API = {
    "IEventBus", "Event", "SubscribeEvent", "BusBuilder",
    "EventPriority", "Cancelable", "ListenerList",
}

EXTERNAL_PACKAGES = {
    "java.util":                             JAVA_UTIL,
    "java.util.function":                    JAVA_UTIL_FUNCTION,
    "java.util.stream":                      JAVA_UTIL_STREAM,
    "java.awt":                              JAVA_AWT,
    "com.google.gson":                       COM_GOOGLE_GSON,
    "net.minecraft.nbt":                     NET_MC_NBT,
    "net.minecraft.world.item":              NET_MC_WORLD_ITEM,
    "net.minecraft.client.model.geom.builders": NET_MC_GEOM_BUILDERS,
    "net.minecraftforge.event.entity.living": NET_MINECRAFTFORGE_EVENT_LIVING,
    "net.minecraftforge.eventbus.api":       NET_MINECRAFTFORGE_EVENTBUS_API,
}

# ── Own-package scanning ───────────────────────────────────────────────────────

def pkg_to_path(pkg: str) -> Path:
    return SRC_ROOT / Path(*pkg.split("."))

def scan_own_package(pkg: str) -> set[str]:
    """Return set of top-level class names declared in own package (non-recursive)."""
    pkg_dir = pkg_to_path(pkg)
    names = set()
    if not pkg_dir.is_dir():
        return names
    for f in pkg_dir.iterdir():
        if f.suffix == ".java":
            names.add(f.stem)
    return names

def get_static_members(fqn: str) -> set[str]:
    """
    For a static star import like `hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Properties`,
    split into outer class FQN + inner class name, scan the outer class's Java source,
    and extract the inner class's public static fields/methods/constants.
    """
    # Try to find the source file by stripping trailing class names
    parts = fqn.split(".")
    # Walk from right to left to find the .java file
    for i in range(len(parts), 0, -1):
        candidate = SRC_ROOT / Path(*parts[:i]).with_suffix(".java")
        if candidate.exists():
            src = candidate.read_text(encoding="utf-8", errors="replace")
            # Find the inner class (parts[i:] joined by ".")
            inner = ".".join(parts[i:]) if i < len(parts) else None
            return _extract_static_members(src, inner)
    return set()

def _extract_static_members(src: str, inner_class: str | None) -> set[str]:
    """Extract public static field/constant names from a class body."""
    # If inner class specified, first isolate its body
    if inner_class:
        pattern = re.compile(
            r"(?:public|protected|private)?\s+(?:static\s+)?(?:final\s+)?(?:class|interface|enum)\s+"
            + re.escape(inner_class) + r"\s*(?:extends[^{]*)?\s*\{",
            re.DOTALL
        )
        m = pattern.search(src)
        if m:
            # Extract the body of this class (simple brace counting)
            start = m.end()
            depth = 1
            i = start
            while i < len(src) and depth > 0:
                if src[i] == "{":
                    depth += 1
                elif src[i] == "}":
                    depth -= 1
                i += 1
            src = src[start:i-1]

    # Find all identifiers that look like constants/fields declared with `public static`
    names = set()
    # Static fields: public static [final] Type NAME
    for m in re.finditer(
        r"(?:public\s+)?static\s+(?:final\s+)?(?:\w+(?:<[^>]*>)?)\s+([A-Z][A-Z0-9_]*)\s*[=;]",
        src
    ):
        names.add(m.group(1))
    # Enum constants (all-caps not required, but usually uppercase)
    for m in re.finditer(r"(?:^|\n)\s+([A-Z][A-Z0-9_]*)\s*(?:\([^)]*\))?\s*[,;]", src):
        names.add(m.group(1))
    # Nested classes/interfaces
    for m in re.finditer(r"(?:public|private|protected)?\s+(?:static\s+)?(?:class|interface|enum)\s+(\w+)", src):
        names.add(m.group(1))
    return names

# ── File parsing helpers ───────────────────────────────────────────────────────

def strip_strings_and_comments(src: str) -> str:
    """Remove string literals and comments from Java source to avoid false matches."""
    result = []
    i = 0
    while i < len(src):
        # Line comment
        if src[i:i+2] == "//":
            while i < len(src) and src[i] != "\n":
                i += 1
        # Block comment
        elif src[i:i+2] == "/*":
            i += 2
            while i < len(src) and src[i-2:i] != "*/":
                i += 1
        # String literal
        elif src[i] == '"':
            i += 1
            while i < len(src):
                if src[i] == "\\" and i+1 < len(src):
                    i += 2
                elif src[i] == '"':
                    i += 1
                    break
                else:
                    i += 1
        # Char literal
        elif src[i] == "'":
            i += 1
            while i < len(src):
                if src[i] == "\\" and i+1 < len(src):
                    i += 2
                elif src[i] == "'":
                    i += 1
                    break
                else:
                    i += 1
        else:
            result.append(src[i])
            i += 1
    return "".join(result)

def get_used_identifiers(body: str) -> set[str]:
    """Extract capitalized identifiers that look like type names from the file body."""
    cleaned = strip_strings_and_comments(body)
    # Match identifiers that start with uppercase (type names, not variables)
    # Exclude ALL_CAPS constants
    return set(m.group(0) for m in re.finditer(r"\b([A-Z][a-zA-Z0-9_]+)\b", cleaned))

# ── Main processing ────────────────────────────────────────────────────────────

def process_file(path: Path) -> bool:
    """
    Expand star imports in a single file. Returns True if the file was modified.
    """
    src = path.read_text(encoding="utf-8", errors="replace")
    lines = src.splitlines(keepends=True)

    # Parse imports
    import_lines = []
    other_lines = []
    in_imports = False
    package_line_idx = -1

    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith("package "):
            package_line_idx = i
        elif stripped.startswith("import "):
            in_imports = True
        if in_imports and not stripped.startswith("import ") and stripped and not stripped.startswith("//"):
            if not stripped.startswith("*") and stripped != "":
                in_imports = False

    # Find star imports
    star_imports = []
    for i, line in enumerate(lines):
        stripped = line.strip()
        if re.match(r"^import\s+(static\s+)?[\w.]+\.\*;$", stripped):
            # Parse: is it static?
            m = re.match(r"^import\s+(static\s+)?([\w.]+)\.\*;$", stripped)
            if m:
                is_static = bool(m.group(1))
                pkg = m.group(2)
                star_imports.append((i, is_static, pkg))

    if not star_imports:
        return False

    # Get file body (everything after imports) for identifier scanning
    body_start = 0
    for i, line in enumerate(lines):
        if not line.strip().startswith("import ") and not line.strip().startswith("package ") and line.strip():
            body_start = i
            break

    body = "".join(lines)
    used_ids = get_used_identifiers(body)

    # Get the file's own package
    own_pkg = ""
    if package_line_idx >= 0:
        m = re.match(r"package\s+([\w.]+)\s*;", lines[package_line_idx].strip())
        if m:
            own_pkg = m.group(1)

    # Get explicitly imported names (to avoid re-adding them)
    explicit_imports = set()
    for line in lines:
        m = re.match(r"^import\s+(?:static\s+)?([\w.]+)\.(\w+)\s*;", line.strip())
        if m:
            explicit_imports.add(m.group(2))

    new_lines = list(lines)
    modified = False

    for (line_idx, is_static, pkg) in reversed(star_imports):
        # Determine the class list for this package
        if is_static:
            # Static star import: get member names from the class
            members = get_static_members(pkg)
            needed = used_ids & members
            # Generate static imports
            new_imports = sorted(f"import static {pkg}.{name};\n" for name in needed
                                 if name not in explicit_imports)
        else:
            if pkg in EXTERNAL_PACKAGES:
                class_list = EXTERNAL_PACKAGES[pkg]
            elif pkg.startswith("hellfirepvp."):
                class_list = scan_own_package(pkg)
            else:
                # Unknown external package — keep star import, add comment
                print(f"  UNKNOWN pkg {pkg} in {path} — keeping star import")
                continue

            # Determine which classes are actually used
            needed = used_ids & class_list
            # Exclude same-package classes (they don't need imports) and already-imported ones
            if own_pkg == pkg:
                needed = set()
            needed -= explicit_imports
            new_imports = sorted(f"import {pkg}.{name};\n" for name in needed)

        if not new_imports:
            # Nothing needed; just remove the star import
            new_lines[line_idx] = ""
            modified = True
            continue

        # Replace star import line with specific imports (sorted)
        new_lines[line_idx] = "".join(new_imports)
        modified = True

    if not modified:
        return False

    # Write back, removing duplicate blank lines in import section
    result = "".join(new_lines)
    # Clean up multiple consecutive blank lines (leave max 1)
    result = re.sub(r"\n{3,}", "\n\n", result)
    path.write_text(result, encoding="utf-8")
    return True


def main():
    count = 0
    for java_file in sorted(SRC_ROOT.rglob("*.java")):
        src = java_file.read_text(encoding="utf-8", errors="replace")
        if ".*;" in src and "import " in src:
            if process_file(java_file):
                print(f"  Fixed: {java_file.relative_to(SRC_ROOT)}")
                count += 1
    print(f"\nDone. Modified {count} files.")


if __name__ == "__main__":
    os.chdir(Path(__file__).parent)
    main()
