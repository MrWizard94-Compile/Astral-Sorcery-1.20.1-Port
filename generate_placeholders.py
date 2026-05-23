"""
Generate 16x16 placeholder PNG textures for Astral Sorcery Port.
Uses only Python stdlib (struct + zlib) to create valid PNG files.
"""
import os
import struct
import zlib

BASE = os.path.join("src", "main", "resources", "assets", "astralsorcery", "textures")

def make_png(r, g, b, filepath):
    """Create a 16x16 solid-color PNG file using raw PNG chunk construction."""
    width, height = 16, 16

    def chunk(chunk_type, data):
        c = chunk_type + data
        crc = struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)
        return struct.pack(">I", len(data)) + c + crc

    # PNG signature
    sig = b'\x89PNG\r\n\x1a\n'

    # IHDR: width, height, bit depth 8, color type 2 (RGB), compression 0, filter 0, interlace 0
    ihdr_data = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    ihdr = chunk(b'IHDR', ihdr_data)

    # IDAT: raw image data with filter byte 0 per row
    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'  # filter byte: None
        for x in range(width):
            raw_data += struct.pack("BBB", r, g, b)

    compressed = zlib.compress(raw_data)
    idat = chunk(b'IDAT', compressed)

    # IEND
    iend = chunk(b'IEND', b'')

    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'wb') as f:
        f.write(sig + ihdr + idat + iend)


# Color definitions for block textures
# Format: (texture_name, R, G, B)
block_textures = [
    # Marble variants - light grey family
    ("marble_raw",       0xCC, 0xCC, 0xCC),
    ("marble_arch",      0xBB, 0xBB, 0xCC),
    ("marble_bricks",    0xC0, 0xC0, 0xC8),
    ("marble_chiseled",  0xC8, 0xC8, 0xD0),
    ("marble_engraved",  0xB8, 0xB8, 0xC4),
    ("marble_runed",     0xB0, 0xB4, 0xC8),
    ("marble_pillar",    0xC4, 0xC4, 0xD4),
    ("marble_pillar_top",0xD0, 0xD0, 0xD8),

    # Black marble - dark grey
    ("black_marble_raw", 0x55, 0x55, 0x55),

    # Infused wood variants - brown family
    ("infused_wood",          0x8B, 0x69, 0x14),
    ("infused_wood_arch",     0x7E, 0x5F, 0x12),
    ("infused_wood_column",   0x9A, 0x72, 0x16),
    ("infused_wood_engraved", 0x70, 0x55, 0x10),
    ("infused_wood_enriched", 0xA0, 0x7B, 0x1A),
    ("infused_wood_planks",   0x8F, 0x6E, 0x18),

    # Ore blocks
    ("rock_crystal_ore",    0x88, 0x88, 0x99),  # Stone grey with slight blue
    ("aquamarine_sand_ore", 0xE8, 0xD0, 0x70),  # Sandy yellow

    # Light/utility blocks
    ("illuminator",         0xFF, 0xF0, 0xAA),  # Warm yellow glow

    # Altar/machine blocks - grey-blue
    ("altar",               0x77, 0x99, 0xAA),
    ("attunement_altar",    0x88, 0xAA, 0xBB),

    # Crystal blocks - blue-white
    ("collector_crystal",           0xAA, 0xD4, 0xE6),
    ("celestial_collector_crystal", 0xBB, 0xDD, 0xF0),

    # Optics - translucent blue-ish
    ("lens",  0x99, 0xBB, 0xDD),
    ("prism", 0xAA, 0xCC, 0xEE),
    ("relay", 0x88, 0xAA, 0xCC),

    # Machines/structures - grey-blue
    ("well",             0x77, 0x88, 0x99),
    ("infuser",          0x66, 0x88, 0xAA),
    ("ritual_pedestal",  0x88, 0x99, 0xBB),
    ("chalice",          0xAA, 0x88, 0x66),  # Bronze/copper
    ("telescope",        0x77, 0x88, 0xAA),
    ("gateway",          0x99, 0x77, 0xCC),  # Purple-ish for portal
    ("fountain",         0x66, 0xAA, 0xCC),  # Water blue
    ("observatory",      0x88, 0x99, 0xAA),
    ("tree_beacon",      0x55, 0xAA, 0x55),  # Green for tree
]

# Color definitions for item textures
item_textures = [
    # Wand - magic purple/blue
    ("wand",              0x88, 0x66, 0xCC),

    # Aquamarine/crystal items - cyan family
    ("aquamarine",        0x55, 0xCC, 0xBB),
    ("rock_crystal",      0xBB, 0xDD, 0xEE),
    ("celestial_crystal", 0xCC, 0xEE, 0xFF),

    # Dust/powder - sparkly colors
    ("stardust",            0xDD, 0xDD, 0x88),  # Gold-ish
    ("starmetal_ingot",     0x55, 0x66, 0xAA),  # Dark metallic blue
    ("starmetal_dust",      0x77, 0x88, 0xBB),  # Lighter metallic blue
    ("illumination_powder", 0xFF, 0xEE, 0x88),  # Bright yellow

    # Glass/lens items
    ("glass_lens",    0xCC, 0xDD, 0xEE),  # Clear glass
    ("infused_glass", 0xAA, 0xCC, 0xEE),  # Tinted glass

    # Gem/stone items
    ("resonating_gem",  0xAA, 0x77, 0xCC),  # Purple gem
    ("formation_stone", 0x99, 0x99, 0xAA),  # Stone grey
    ("shifting_stone",  0xAA, 0x88, 0xBB),  # Shifting purple

    # Paper/knowledge items
    ("constellation_paper", 0xEE, 0xEE, 0xCC),  # Parchment
    ("knowledge_fragment",  0xDD, 0xCC, 0xAA),  # Old paper

    # Tools - brown handles with blue crystal heads
    ("linking_tool",    0x88, 0xAA, 0xCC),
    ("hand_telescope",  0x77, 0x88, 0x99),

    # Perk items - magic themed
    ("perk_seal",      0xCC, 0xAA, 0x44),  # Golden seal
    ("perk_gem_day",   0xFF, 0xDD, 0x44),  # Sun gold
    ("perk_gem_night", 0x44, 0x44, 0x99),  # Dark blue night
    ("perk_gem_sky",   0x88, 0xCC, 0xFF),  # Sky blue

    # Crystal tools - blue-white
    ("crystal_pickaxe", 0xAA, 0xCC, 0xEE),
    ("crystal_axe",     0xAA, 0xCC, 0xDD),
    ("crystal_shovel",  0xAA, 0xBB, 0xDD),
    ("crystal_sword",   0xBB, 0xCC, 0xEE),

    # Mantles - each constellation has a color theme
    ("mantle_discidia", 0xCC, 0x44, 0x44),  # Red (offense)
    ("mantle_armara",   0x44, 0x44, 0xCC),  # Blue (defense)
    ("mantle_vicio",    0x44, 0xCC, 0x44),  # Green (mobility)
    ("mantle_aevitas",  0xCC, 0xCC, 0x44),  # Yellow (life)
    ("mantle_evorsio",  0xCC, 0x77, 0x44),  # Orange (destruction)

    # Colored lens items - each tinted with their effect color
    ("colored_lens_fire",         0xFF, 0x66, 0x33),  # Fire orange
    ("colored_lens_break",        0xAA, 0xAA, 0xAA),  # Grey (break)
    ("colored_lens_growth",       0x44, 0xBB, 0x44),  # Green (growth)
    ("colored_lens_damage",       0xCC, 0x33, 0x33),  # Red (damage)
    ("colored_lens_regeneration", 0xFF, 0x88, 0xAA),  # Pink (regen)
    ("colored_lens_push",         0x88, 0x88, 0xFF),  # Blue (push)
    ("colored_lens_spectral",     0xDD, 0xBB, 0xFF),  # Light purple (spectral)
]


def main():
    count = 0

    # Generate block textures
    block_dir = os.path.join(BASE, "block")
    for name, r, g, b in block_textures:
        filepath = os.path.join(block_dir, f"{name}.png")
        make_png(r, g, b, filepath)
        count += 1
        print(f"  Created: textures/block/{name}.png  (#{r:02X}{g:02X}{b:02X})")

    # Generate item textures
    item_dir = os.path.join(BASE, "item")
    for name, r, g, b in item_textures:
        filepath = os.path.join(item_dir, f"{name}.png")
        make_png(r, g, b, filepath)
        count += 1
        print(f"  Created: textures/item/{name}.png  (#{r:02X}{g:02X}{b:02X})")

    print(f"\nDone! Created {count} placeholder textures.")
    print(f"  Block textures: {len(block_textures)}")
    print(f"  Item textures:  {len(item_textures)}")


if __name__ == "__main__":
    main()
