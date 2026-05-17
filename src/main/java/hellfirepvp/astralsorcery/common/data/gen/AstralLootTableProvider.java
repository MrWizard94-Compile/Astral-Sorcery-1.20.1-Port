/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.gen;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
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
 * Generates loot table JSON files for all Astral Sorcery blocks.
 * Most blocks are simple self-drops; slabs use the standard
 * double-drop table.
 */
public class AstralLootTableProvider extends LootTableProvider {

    public AstralLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(AstralBlockLoot::new, LootContextParamSets.BLOCK)
        ));
    }

    /**
     * Block loot sub-provider that defines drop behaviour for
     * every registered Astral Sorcery block.
     */
    public static class AstralBlockLoot extends BlockLootSubProvider {

        protected AstralBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            // --- Marble family ---
            dropSelf(BlocksAS.MARBLE_RAW.get());
            dropSelf(BlocksAS.MARBLE_ARCH.get());
            dropSelf(BlocksAS.MARBLE_BRICKS.get());
            dropSelf(BlocksAS.MARBLE_CHISELED.get());
            dropSelf(BlocksAS.MARBLE_ENGRAVED.get());
            dropSelf(BlocksAS.MARBLE_RUNED.get());
            dropSelf(BlocksAS.MARBLE_PILLAR.get());
            dropSelf(BlocksAS.BLACK_MARBLE_RAW.get());

            // --- Infused wood family ---
            dropSelf(BlocksAS.INFUSED_WOOD.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ARCH.get());
            dropSelf(BlocksAS.INFUSED_WOOD_COLUMN.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ENGRAVED.get());
            dropSelf(BlocksAS.INFUSED_WOOD_ENRICHED.get());
            dropSelf(BlocksAS.INFUSED_WOOD_PLANKS.get());

            // --- Special ---
            dropSelf(BlocksAS.ILLUMINATOR.get());

            // --- Tile entity blocks ---
            dropSelf(BlocksAS.ALTAR.get());
            dropSelf(BlocksAS.ATTUNEMENT_ALTAR.get());
            dropSelf(BlocksAS.COLLECTOR_CRYSTAL.get());
            dropSelf(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get());
            dropSelf(BlocksAS.LENS.get());
            dropSelf(BlocksAS.PRISM.get());
            dropSelf(BlocksAS.RELAY.get());
            dropSelf(BlocksAS.WELL.get());
            dropSelf(BlocksAS.INFUSER.get());
            dropSelf(BlocksAS.RITUAL_PEDESTAL.get());
            dropSelf(BlocksAS.CHALICE.get());
            dropSelf(BlocksAS.TELESCOPE.get());
            dropSelf(BlocksAS.GATEWAY.get());
            dropSelf(BlocksAS.FOUNTAIN.get());
            dropSelf(BlocksAS.OBSERVATORY.get());
            dropSelf(BlocksAS.TREE_BEACON.get());

            // --- Slab (drops 2 when full) ---
            add(BlocksAS.MARBLE_SLAB.get(), createSlabItemTable(BlocksAS.MARBLE_SLAB.get()));

            // --- Stairs ---
            dropSelf(BlocksAS.MARBLE_STAIRS.get());

            // --- Ores (self-drop; silk touch handling can be added later) ---
            dropSelf(BlocksAS.ROCK_CRYSTAL_ORE.get());
            dropSelf(BlocksAS.AQUAMARINE_ORE.get());
        }

        @Override
        @Nonnull
        protected Iterable<Block> getKnownBlocks() {
            return BlocksAS.BLOCKS.getEntries().stream()
                    .map(RegistryObject::get)::iterator;
        }
    }
}
