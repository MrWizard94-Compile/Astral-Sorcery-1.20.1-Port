package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.base.Mods;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

import static hellfirepvp.astralsorcery.common.base.Mods.ASTRAL_SORCERY;
import static hellfirepvp.astralsorcery.common.base.Mods.CURIOS;

public class TagsAS {

    private TagsAS() {}

    public static class Blocks {

        public static final TagKey<Block> MARBLE = blockTagForge("marble");
        public static final TagKey<Block> ORES = blockTagForge("ores");

        private Blocks() {}
    }

    public static class Items {

        public static final TagKey<Item> CURIOS_NECKLACE = itemTag(CURIOS, "necklace");
        public static final TagKey<Item> FORGE_GEM_AQUAMARINE = itemTagForge("gems/aquamarine");
        public static final TagKey<Item> DUSTS_STARDUST = itemTag(ASTRAL_SORCERY, "stardust");
        public static final TagKey<Item> INGOTS_STARMETAL = itemTag(ASTRAL_SORCERY, "starmetal");
        public static final TagKey<Item> COLORED_LENS = itemTag(ASTRAL_SORCERY, "colored_lens");

        private Items() {}
    }

    @Nonnull
    private static TagKey<Block> blockTagForge(@Nonnull String name) {
        return blockTag(Mods.FORGE, name);
    }

    @Nonnull
    private static TagKey<Block> blockTag(@Nonnull Mods mod, @Nonnull String name) {
        return BlockTags.create(new ResourceLocation(mod.getModId(), name));
    }

    @Nonnull
    private static TagKey<Item> itemTagForge(@Nonnull String name) {
        return itemTag(Mods.FORGE, name);
    }

    @Nonnull
    private static TagKey<Item> itemTag(@Nonnull Mods mod, @Nonnull String name) {
        return ItemTags.create(new ResourceLocation(mod.getModId(), name));
    }
}
