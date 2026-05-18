/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.interaction;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Registry of named {@link InteractionResult} factories for
 * {@link hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction} recipes.
 *
 * <p>1.16 → 1.20: ResourceLocation import moved to net.minecraft.resources.</p>
 */
public class InteractionResultRegistry {

    public static final ResourceLocation ID_DROP_ITEM   = AstralSorcery.key("drop_item");
    public static final ResourceLocation ID_SPAWN_ENTITY = AstralSorcery.key("spawn_entity");

    private static final Map<ResourceLocation, Supplier<InteractionResult>> REGISTRY = new HashMap<>();

    private InteractionResultRegistry() {}

    public static void register(ResourceLocation key, Supplier<InteractionResult> supplier) {
        REGISTRY.put(key, supplier);
    }

    public static Collection<ResourceLocation> getKeys() {
        return REGISTRY.keySet();
    }

    public static Collection<String> getKeysAsStrings() {
        return REGISTRY.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList());
    }

    @Nullable
    public static InteractionResult create(ResourceLocation key) {
        Supplier<InteractionResult> supplier = REGISTRY.get(key);
        return supplier != null ? supplier.get() : null;
    }

    static {
        register(ID_DROP_ITEM,    ResultDropItem::new);
        register(ID_SPAWN_ENTITY, ResultSpawnEntity::new);
    }
}
