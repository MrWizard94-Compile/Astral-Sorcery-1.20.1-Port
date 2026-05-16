/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.AstralSorcery;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

/**
 * Global loot modifier that adds Astral Sorcery drops to vanilla
 * loot tables (chests, mobs, etc.). Uses Forge's global loot
 * modifier system rather than modifying loot table JSONs directly.
 *
 * <p>Example uses:
 * <ul>
 *   <li>Stardust drops from endermen</li>
 *   <li>Constellation papers in dungeon chests</li>
 *   <li>Rock crystal fragments from deep mining</li>
 * </ul>
 *
 * <p>1.16 -> 1.20 changes:
 * Global loot modifiers now use Codec-based serialization.
 * DeferredRegister key is {@code ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS}.</p>
 */
public final class GlobalLootModifierAS {

    private GlobalLootModifierAS() {}

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    AstralSorcery.MODID);

    public static final RegistryObject<Codec<AstralLootModifier>> ASTRAL_LOOT =
            LOOT_MODIFIERS.register("astral_loot", () -> AstralLootModifier.CODEC);

    /**
     * Loot modifier that conditionally adds Astral Sorcery items to loot.
     * The drop chance and item selection are configured via the codec fields.
     */
    public static class AstralLootModifier extends LootModifier {

        /** Probability [0, 1] that an AS item is added to eligible loot. */
        private final float dropChance;

        public static final Codec<AstralLootModifier> CODEC = RecordCodecBuilder.create(instance ->
                codecStart(instance)
                        .and(Codec.floatRange(0f, 1f).fieldOf("drop_chance")
                                .forGetter(m -> m.dropChance))
                        .apply(instance, AstralLootModifier::new)
        );

        protected AstralLootModifier(@Nonnull LootItemCondition[] conditions, float dropChance) {
            super(conditions);
            this.dropChance = dropChance;
        }

        @Override
        @Nonnull
        protected ObjectArrayList<ItemStack> doApply(@Nonnull ObjectArrayList<ItemStack> generatedLoot,
                                                     @Nonnull LootContext context) {
            if (context.getRandom().nextFloat() >= dropChance) {
                return generatedLoot;
            }

            // TODO: Select an appropriate AS item based on context:
            // - Stardust for mob kills
            // - Constellation papers for dungeon chests
            // - Aquamarine shards for underground chests
            // For now, this is a structural placeholder. Item selection
            // will be wired once ItemsAS has the full set of drops.

            return generatedLoot;
        }

        @Override
        @Nonnull
        public Codec<AstralLootModifier> codec() {
            return CODEC;
        }
    }
}
