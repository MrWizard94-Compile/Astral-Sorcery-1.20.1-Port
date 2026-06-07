package hellfirepvp.astralsorcery.common.fluid;

import hellfirepvp.astralsorcery.common.crafting.nojson.LiquidStarlightCraftingRegistry;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.core.BlockPos;
import javax.annotation.Nonnull;

public class BlockLiquidStarlight extends LiquidBlock {

    public BlockLiquidStarlight() {
        super(() -> (FlowingFluid) FluidsAS.LIQUID_STARLIGHT.get(),
                Properties.of()
                        .noCollission()
                        .strength(100.0F)
                        .lightLevel(state -> 15)
                        .noLootTable()
                        .replaceable());
    }

    @Override
    public void entityInside(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if (state.getValue(LEVEL) != 0) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
        } else if (entity instanceof ItemEntity itemEntity) {
            LiquidStarlightCraftingRegistry.tryCraft(itemEntity, pos);
            if (!level.isClientSide() && itemEntity.getItem().isEmpty()) {
                itemEntity.discard();
            }
        }
    }
}
