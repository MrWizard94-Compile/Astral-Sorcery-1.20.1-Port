/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.block.tile.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.loot.CopyConstellation;
import hellfirepvp.astralsorcery.common.loot.CopyCrystalProperties;
import hellfirepvp.astralsorcery.common.loot.CopyGatewayColor;
import hellfirepvp.astralsorcery.common.loot.LinearLuckBonus;
import hellfirepvp.astralsorcery.common.loot.RandomCrystalProperty;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Generates loot tables for all AS blocks.
 * Covers all 63 registered blocks from {@link BlocksAS}.
 */
public class AstralLootTableProvider extends LootTableProvider {

    public AstralLootTableProvider(@Nonnull PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ASBlockLoot::new, LootContextParamSets.BLOCK)
        ));
    }

    public static class ASBlockLoot extends BlockLootSubProvider {

        protected ASBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            // ---- Marble family (dropSelf) ----
            dropSelf(BlocksAS.MARBLE_RAW.get());
            dropSelf(BlocksAS.MARBLE_ARCH.get());
            dropSelf(BlocksAS.MARBLE_BRICKS.get());
            dropSelf(BlocksAS.MARBLE_CHISELED.get());
            dropSelf(BlocksAS.MARBLE_ENGRAVED.get());
            dropSelf(BlocksAS.MARBLE_RUNED.get());
            dropSelf(BlocksAS.MARBLE_PILLAR.get());
            dropSelf(BlocksAS.MARBLE_STAIRS.get());
            add(BlocksAS.MARBLE_SLAB.get(), createSlabItemTable(BlocksAS.MARBLE_SLAB.get()));

            // ---- Black marble family (dropSelf) ----
            dropSelf(BlocksAS.BLACK_MARBLE_RAW.get());
            dropSelf(BlocksAS.BLACK_MARBLE_ARCH.get());
            dropSelf(BlocksAS.BLACK_MARBLE_BRICKS.get());
            dropSelf(BlocksAS.BLACK_MARBLE_CHISELED.get());
            dropSelf(BlocksAS.BLACK_MARBLE_ENGRAVED.get());
            dropSelf(BlocksAS.BLACK_MARBLE_RUNED.get());
            dropSelf(BlocksAS.BLACK_MARBLE_PILLAR.get());
            dropSelf(BlocksAS.BLACK_MARBLE_STAIRS.get());
            add(BlocksAS.BLACK_MARBLE_SLAB.get(), createSlabItemTable(BlocksAS.BLACK_MARBLE_SLAB.get()));

            // ---- Infused wood family (dropSelf) ----
            dropSelf(BlocksAS.INFUSED_WOOD.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ARCH.get());
            dropSelf(BlocksAS.INFUSED_WOOD_COLUMN.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ENGRAVED.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ENRICHED.get());
            dropSelf(BlocksAS.INFUSED_WOOD_INFUSED.get());
            dropSelf(BlocksAS.INFUSED_WOOD_PLANKS.get());
            dropSelf(BlocksAS.INFUSED_WOOD_STAIRS.get());
            add(BlocksAS.INFUSED_WOOD_SLAB.get(), createSlabItemTable(BlocksAS.INFUSED_WOOD_SLAB.get()));

            // ---- Ores: starmetal drops itself (not the ore item) ----
            dropSelf(BlocksAS.STARMETAL_ORE.get());
            dropSelf(BlocksAS.STARMETAL.get());

            // ---- Aquamarine ore: silk-touch → ore block; otherwise 1-3 aquamarine + LinearLuckBonus ----
            add(BlocksAS.AQUAMARINE_ORE.get(), createSilkTouchDispatchTable(
                    BlocksAS.AQUAMARINE_ORE.get(),
                    applyExplosionDecay(BlocksAS.AQUAMARINE_ORE.get(),
                            LootItem.lootTableItem(ItemsAS.AQUAMARINE.get())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                                    .apply(LinearLuckBonus.create()))));

            // ---- Rock crystal ore: 2-5 rolls, each a rock crystal with random properties ----
            add(BlocksAS.ROCK_CRYSTAL_ORE.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(2.0F, 5.0F))
                            .add(applyExplosionDecay(BlocksAS.ROCK_CRYSTAL_ORE.get(),
                                    LootItem.lootTableItem(ItemsAS.ROCK_CRYSTAL.get())
                                            .apply(RandomCrystalProperty.create())))));

            // ---- Glow flower: shears → self; otherwise 2-4 glowstone dust + LinearLuckBonus ----
            add(BlocksAS.GLOW_FLOWER.get(), createShearsDispatchTable(
                    BlocksAS.GLOW_FLOWER.get(),
                    applyExplosionDecay(BlocksAS.GLOW_FLOWER.get(),
                            LootItem.lootTableItem(Items.GLOWSTONE_DUST)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                    .apply(LinearLuckBonus.create()))));

            // ---- Lens and Prism: drop self + copy crystal attributes ----
            add(BlocksAS.LENS.get(), crystalBlockDrop(BlocksAS.LENS.get(),
                    CopyCrystalProperties.create()));
            add(BlocksAS.PRISM.get(), crystalBlockDrop(BlocksAS.PRISM.get(),
                    CopyCrystalProperties.create()));

            // ---- Gateway: drop self + copy color ----
            add(BlocksAS.GATEWAY.get(), crystalBlockDrop(BlocksAS.GATEWAY.get(),
                    CopyGatewayColor.create()));

            // ---- Collector crystals: drop self + copy attributes + copy constellation ----
            add(BlocksAS.COLLECTOR_CRYSTAL.get(), crystalBlockDrop(BlocksAS.COLLECTOR_CRYSTAL.get(),
                    CopyCrystalProperties.create(), CopyConstellation.create()));
            add(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get(), crystalBlockDrop(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get(),
                    CopyCrystalProperties.create(), CopyConstellation.create()));

            // ---- Celestial crystal cluster: staged drops (stage 0 → nothing) ----
            add(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get(), celestialCrystalClusterTable());

            // ---- Gem crystal cluster: drops perk gems at full growth only ----
            add(BlocksAS.GEM_CRYSTAL_CLUSTER.get(), gemCrystalClusterTable());

            // ---- Simple functional blocks (dropSelf) ----
            dropSelf(BlocksAS.ALTAR.get());
            dropSelf(BlocksAS.ATTUNEMENT_ALTAR.get());
            dropSelf(BlocksAS.WELL.get());
            dropSelf(BlocksAS.RELAY.get());
            dropSelf(BlocksAS.CHALICE.get());
            dropSelf(BlocksAS.TELESCOPE.get());
            dropSelf(BlocksAS.INFUSER.get());
            dropSelf(BlocksAS.RITUAL_PEDESTAL.get());
            dropSelf(BlocksAS.FOUNTAIN.get());
            dropSelf(BlocksAS.FOUNTAIN_PRIME_LIQUID.get());
            dropSelf(BlocksAS.FOUNTAIN_PRIME_ORE.get());
            dropSelf(BlocksAS.FOUNTAIN_PRIME_VORTEX.get());
            dropSelf(BlocksAS.OBSERVATORY.get());
            dropSelf(BlocksAS.TREE_BEACON.get());
            dropSelf(BlocksAS.SPECTRAL_RELAY.get());
            dropSelf(BlocksAS.REFRACTION_TABLE.get());
            dropSelf(BlocksAS.RITUAL_LINK.get());
            dropSelf(BlocksAS.ILLUMINATOR.get());

            // ---- Internal / special blocks: no drops ----
            add(BlocksAS.FLARE_LIGHT.get(), noDrop());
            add(BlocksAS.STRUCTURAL.get(), noDrop());
            add(BlocksAS.VANISHING.get(), noDrop());
            add(BlocksAS.TRANSLUCENT_BLOCK.get(), noDrop());
            add(BlocksAS.FLUID_LIQUID_STARLIGHT.get(), noDrop());
            add(BlocksAS.TREE_BEACON_COMPONENT.get(), noDrop());
        }

        /**
         * Builds a drop-self table with explosion condition and one or more loot functions applied to the item.
         */
        private LootTable.Builder crystalBlockDrop(Block block,
                                                    net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder... functions) {
            LootItem.Builder<?> entry = LootItem.lootTableItem(block);
            for (net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder fn : functions) {
                entry = entry.apply(fn);
            }
            return LootTable.lootTable()
                    .withPool(applyExplosionCondition(block, LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(entry)));
        }

        /**
         * Celestial crystal cluster staged drops:
         * <ul>
         *   <li>Stage 0 — nothing (no pool condition matches)</li>
         *   <li>Stage 1 — 1 stardust</li>
         *   <li>Stage 2 — 1-2 stardust</li>
         *   <li>Stage 3 — 1-2 stardust</li>
         *   <li>Stage 4 — 1 celestial crystal + 2 stardust</li>
         * </ul>
         * CopyCrystalProperties and explosion decay applied at table level (no-op on stardust).
         */
        private LootTable.Builder celestialCrystalClusterTable() {
            Block block = BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get();
            return LootTable.lootTable()
                    .apply(CopyCrystalProperties.create())
                    .apply(ApplyExplosionDecay.explosionDecay())
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.CELESTIAL_CRYSTAL.get())
                                    .when(stageCondition(block, 4))))
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST.get())
                                    .when(stageCondition(block, 1))))
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(1.0F, 2.0F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST.get())
                                    .when(stageCondition(block, 2))))
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(1.0F, 2.0F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST.get())
                                    .when(stageCondition(block, 3))))
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(2.0F))
                            .add(LootItem.lootTableItem(ItemsAS.STARDUST.get())
                                    .when(stageCondition(block, 4))));
        }

        /**
         * Gem crystal cluster drops a perk gem of the appropriate type at full growth (stage 2) only.
         */
        private LootTable.Builder gemCrystalClusterTable() {
            Block block = BlocksAS.GEM_CRYSTAL_CLUSTER.get();
            return LootTable.lootTable()
                    .apply(ApplyExplosionDecay.explosionDecay())
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.PERK_GEM_DAY.get())
                                    .when(gemStageCondition(block, BlockGemCrystalCluster.GrowthStageType.DAY_2))))
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.PERK_GEM_NIGHT.get())
                                    .when(gemStageCondition(block, BlockGemCrystalCluster.GrowthStageType.NIGHT_2))))
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(ItemsAS.PERK_GEM_SKY.get())
                                    .when(gemStageCondition(block, BlockGemCrystalCluster.GrowthStageType.SKY_2))));
        }

        private static LootItemBlockStatePropertyCondition.Builder stageCondition(Block block, int stage) {
            return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                    .setProperties(StatePropertiesPredicate.Builder.properties()
                            .hasProperty(BlockCelestialCrystalCluster.STAGE, stage));
        }

        private static LootItemBlockStatePropertyCondition.Builder gemStageCondition(
                Block block, BlockGemCrystalCluster.GrowthStageType stage) {
            return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                    .setProperties(StatePropertiesPredicate.Builder.properties()
                            .hasProperty(BlockGemCrystalCluster.STAGE, stage));
        }

        @Override
        @Nonnull
        protected Iterable<Block> getKnownBlocks() {
            return BlocksAS.BLOCKS.getEntries().stream()
                    .map(RegistryObject::get)
                    .map(block -> (Block) block)
                    ::iterator;
        }
    }
}
