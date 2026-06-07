package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.GatewayHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Block entity for the Celestial Gateway.
 * A teleportation node in the gateway network for cross-dimension travel.
 * Stores display name and multiblock validity; registers/unregisters
 * with {@link GatewayHandler} on placement and removal.
 *
 * <p>On first tick, registers with the GatewayHandler to join the
 * teleportation network. On removal, unregisters so the network
 * stays consistent. The gateway ID is persistent via NBT.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * GatewayHelper -> GatewayHandler (SavedData)</p>
 */
public class BlockEntityGateway extends BlockEntityTick {

    /** How often to re-validate the multiblock structure (ticks). */
    private static final int STRUCTURE_CHECK_INTERVAL = 60;

    @Nonnull
    private UUID gatewayId = UUID.randomUUID();

    @Nullable
    private String displayName = null;

    @Nonnull
    private DyeColor color = DyeColor.WHITE;

    private boolean structureValid = false;
    private boolean registeredInNetwork = false;

    public BlockEntityGateway(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.GATEWAY.get(), pos, state);
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide()) {
            registerInNetwork();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // Client-side: portal vortex particles handled by renderer
            return;
        }

        // Periodically validate structure
        if (getTicksExisted() % STRUCTURE_CHECK_INTERVAL == 0) {
            boolean wasValid = structureValid;
            structureValid = validateStructure();
            if (wasValid != structureValid) {
                markForUpdate();
            }
        }
    }

    /**
     * Registers this gateway in the server-wide network.
     * Called on first tick and when loaded from NBT.
     */
    private void registerInNetwork() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) return;
        if (registeredInNetwork) return;

        ServerLevel serverLevel = (ServerLevel) level;
        GatewayHandler handler = GatewayHandler.get(serverLevel.getServer());
        handler.registerGateway(gatewayId, serverLevel.dimension(), worldPosition, displayName);
        registeredInNetwork = true;
        AstralSorcery.log.debug("Gateway BE registered: {} at {}", gatewayId, worldPosition);
    }

    /**
     * Unregisters this gateway from the network.
     * Called when the block is broken or the BE is removed.
     */
    private void unregisterFromNetwork() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) return;
        if (!registeredInNetwork) return;

        ServerLevel serverLevel = (ServerLevel) level;
        GatewayHandler handler = GatewayHandler.get(serverLevel.getServer());
        handler.unregisterGateway(gatewayId);
        registeredInNetwork = false;
        AstralSorcery.log.debug("Gateway BE unregistered: {} at {}", gatewayId, worldPosition);
    }

    @Override
    public void setRemoved() {
        unregisterFromNetwork();
        super.setRemoved();
    }

    private static final TagKey<Block> MARBLE_BLOCKS =
            BlockTags.create(new ResourceLocation(AstralSorcery.MODID, "marble_blocks"));

    /**
     * Validates the gateway multiblock structure.
     * Requires open sky and 4 AS marble blocks at cardinal positions.
     */
    private boolean validateStructure() {
        Level level = getLevel();
        if (level == null) return false;

        // Must have open sky above for starlight reception
        if (!level.canSeeSky(worldPosition.above())) {
            return false;
        }

        // Check for the 4 AS marble pillars at cardinal positions
        BlockPos[] pillarPositions = {
                worldPosition.offset(2, 0, 0),
                worldPosition.offset(-2, 0, 0),
                worldPosition.offset(0, 0, 2),
                worldPosition.offset(0, 0, -2)
        };

        for (BlockPos pillar : pillarPositions) {
            if (!level.getBlockState(pillar).is(MARBLE_BLOCKS)) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    public UUID getGatewayId() {
        return gatewayId;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String name) {
        this.displayName = name;
        // Update display name in the network as well
        if (!isClientSide() && registeredInNetwork) {
            Level level = getLevel();
            if (level instanceof ServerLevel serverLevel) {
                GatewayHandler handler = GatewayHandler.get(serverLevel.getServer());
                handler.unregisterGateway(gatewayId);
                handler.registerGateway(gatewayId, serverLevel.dimension(), worldPosition, name);
            }
        }
        markForUpdate();
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    @Nonnull
    public DyeColor getColor() {
        return color;
    }

    public void setColor(@Nonnull DyeColor color) {
        this.color = color;
        markForUpdate();
    }

    /**
     * Stores the given DyeColor in the item's NBT so the placed gateway can read it.
     * Called by the change_gateway_color crafting recipe.
     */
    public static void setItemColor(@Nonnull ItemStack stack, @Nonnull DyeColor color) {
        stack.getOrCreateTag().putInt("gatewayColor", color.getId());
    }

    @Nullable
    public static DyeColor getItemColor(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("gatewayColor")) return null;
        return DyeColor.byId(tag.getInt("gatewayColor"));
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        if (compound.contains("gatewayId")) {
            this.gatewayId = compound.getUUID("gatewayId");
        }
        if (compound.contains("displayName")) {
            this.displayName = compound.getString("displayName");
        } else {
            this.displayName = null;
        }
        if (compound.contains("gatewayColor")) {
            this.color = DyeColor.byId(compound.getInt("gatewayColor"));
        }
        this.structureValid = compound.getBoolean("structureValid");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.putUUID("gatewayId", gatewayId);
        String dn = displayName;
        if (dn != null) {
            compound.putString("displayName", dn);
        }
        compound.putInt("gatewayColor", color.getId());
        compound.putBoolean("structureValid", structureValid);
    }

}
