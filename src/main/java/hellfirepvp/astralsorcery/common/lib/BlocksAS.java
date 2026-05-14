package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlocksAS {

    private BlocksAS() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AstralSorcery.MODID);

    // Entries will be registered as block classes are ported in Phase 5.
    // Pattern: public static final RegistryObject<BlockFoo> FOO = BLOCKS.register("foo", BlockFoo::new);
}
