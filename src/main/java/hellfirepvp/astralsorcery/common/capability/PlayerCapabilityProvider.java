package hellfirepvp.astralsorcery.common.capability;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
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
 * Capability provider for player progression/research data.
 * Attached to all Player entities via {@link CapabilitySetup}.
 *
 * <p>1.16 -> 1.20 changes:
 * CapabilityManager.get(CapabilityToken) replaces @CapabilityInject,
 * ICapabilitySerializable unchanged,
 * CompoundNBT -> CompoundTag</p>
 */
public class PlayerCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    /**
     * The capability instance. Resolved at mod construction time via CapabilityToken.
     */
    public static final Capability<PlayerProgress> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    @Nonnull
    private final PlayerProgress data = new PlayerProgress();
    @Nonnull
    private final LazyOptional<PlayerProgress> optional = LazyOptional.of(() -> data);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, optional);
    }

    @Nonnull
    @Override
    public CompoundTag serializeNBT() {
        return data.writeToNBT();
    }

    @Override
    public void deserializeNBT(@Nonnull CompoundTag nbt) {
        data.readFromNBT(nbt);
    }

    /**
     * Called when the provider is invalidated (player removed from world).
     */
    public void invalidate() {
        optional.invalidate();
    }
}
