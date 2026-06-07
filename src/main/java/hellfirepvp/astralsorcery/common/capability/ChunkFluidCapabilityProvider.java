package hellfirepvp.astralsorcery.common.capability;

import hellfirepvp.astralsorcery.common.data.world.ChunkFluidEntry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability provider for per-chunk fluid data (lightwell system).
 * Tracks starlight fluid availability per chunk for lightwell production.
 *
 * <p>1.16 -> 1.20 changes: same as PlayerCapabilityProvider.</p>
 */
public class ChunkFluidCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<ChunkFluidEntry> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    @Nonnull
    private final ChunkFluidEntry data = new ChunkFluidEntry();
    @Nonnull
    private final LazyOptional<ChunkFluidEntry> optional = LazyOptional.of(() -> data);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, optional);
    }

    @Nonnull
    @Override
    public CompoundTag serializeNBT() {
        return data.writeToNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.readFromNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
