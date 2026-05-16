/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Generates loot tables for all AS blocks.
 */
public class AstralLootTableProvider extends LootTableProvider {

    public AstralLootTableProvider(@Nonnull PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ASBlockLoot::new, LootContextParamSets.BLOCK)
        ));
    }

    /**
     * Block loot sub-provider. Defines what each AS block drops when broken.
     */
    public static class ASBlockLoot extends BlockLootSubProvider {

        protected ASBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            // Marble variants — self-drop
            dropSelf(BlocksAS.MARBLE_RAW.get());
            dropSelf(BlocksAS.MARBLE_BRICKS.get());
            dropSelf(BlocksAS.MARBLE_PILLAR.get());
            dropSelf(BlocksAS.MARBLE_CHISELED.get());
            dropSelf(BlocksAS.MARBLE_ARCH.get());
            dropSelf(BlocksAS.MARBLE_ENGRAVED.get());
            dropSelf(BlocksAS.MARBLE_RUNED.get());
            dropSelf(BlocksAS.BLACK_MARBLE_RAW.get());

            // Wood variants — self-drop
            dropSelf(BlocksAS.INFUSED_WOOD.get());
            dropSelf(BlocksAS.INFUSED_WOOD_PLANKS.get());
            dropSelf(BlocksAS.INFUSED_WOOD_COLUMN.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ARCH.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ENGRAVED.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ENRICHED.get());

            // Functional blocks — self-drop
            dropSelf(BlocksAS.ALTAR.get());
            dropSelf(BlocksAS.ATTUNEMENT_ALTAR.get());
            dropSelf(BlocksAS.WELL.get());
            dropSelf(BlocksAS.LENS.get());
            dropSelf(BlocksAS.PRISM.get());
            dropSelf(BlocksAS.RELAY.get());
            dropSelf(BlocksAS.CHALICE.get());
            dropSelf(BlocksAS.TELESCOPE.get());
            dropSelf(BlocksAS.INFUSER.get());
            dropSelf(BlocksAS.RITUAL_PEDESTAL.get());
            dropSelf(BlocksAS.GATEWAY.get());
            dropSelf(BlocksAS.FOUNTAIN.get());
            dropSelf(BlocksAS.ILLUMINATOR.get());
            dropSelf(BlocksAS.COLLECTOR_CRYSTAL.get());
            dropSelf(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get());

            // Slabs drop 2 when double-placed
            add(BlocksAS.MARBLE_SLAB.get(), createSlabItemTable(BlocksAS.MARBLE_SLAB.get()));
            dropSelf(BlocksAS.MARBLE_STAIRS.get());

            // Ore blocks — drop items
            add(BlocksAS.ROCK_CRYSTAL_ORE.get(),
                    createOreDrop(BlocksAS.ROCK_CRYSTAL_ORE.get(), ItemsAS.ROCK_CRYSTAL.get()));
            add(BlocksAS.AQUAMARINE_ORE.get(),
                    createOreDrop(BlocksAS.AQUAMARINE_ORE.get(), ItemsAS.AQUAMARINE.get()));
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
