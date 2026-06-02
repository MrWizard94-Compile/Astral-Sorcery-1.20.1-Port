package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.ChunkFluidCapabilityProvider;
import hellfirepvp.astralsorcery.common.data.world.ChunkFluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Public constants for Astral Sorcery capabilities.
 *
 * <p>1.16 used {@code @CapabilityInject} to populate a {@code Capability<T>} field.
 * In 1.20 capabilities are obtained via {@link net.minecraftforge.common.capabilities.CapabilityManager#get}
 * with a {@link net.minecraftforge.common.capabilities.CapabilityToken}, exposed through
 * the provider class. This class holds the public ResourceLocation keys so other systems
 * can reference them without importing the provider directly.</p>
 */
public final class CapabilitiesAS {

    private CapabilitiesAS() {}

    public static final ResourceLocation CHUNK_FLUID_KEY = AstralSorcery.key("chunk_fluid");

    /** Delegates to {@link ChunkFluidCapabilityProvider#CAPABILITY}. */
    public static Capability<ChunkFluidEntry> CHUNK_FLUID() {
        return ChunkFluidCapabilityProvider.CAPABILITY;
    }
}
