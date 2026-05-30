package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.item.ItemStarmetalIngot;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Starmetal ingot item entity — can be chiseled by a player holding a
 * {@link hellfirepvp.astralsorcery.common.item.ItemChisel} to produce stardust.
 * Each chisel strike has a 40% chance to produce stardust, and a chance to consume the ingot.
 *
 * <p>Fortune on the chisel reduces the chance of consuming the ingot.</p>
 *
 * <p>1.16 → 1.20: custom EntityCustomItemReplacement replaced by standard ItemEntity subclass;
 * ReflectionHelper.setSkipItemPhysicsRender removed (visual-only difference);
 * InteractableEntity interface removed — chisel interaction hooked via hurt().</p>
 */
public class EntityStarmetal extends EntityItemHighlighted {

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityStarmetal(EntityType<? extends ItemEntity> type, Level level,
                            double x, double y, double z, @Nonnull ItemStack stack) {
        super(type, level);
        setPos(x, y, z);
        setItem(stack);
        this.lifespan = stack.isEmpty() ? 6000 : Integer.MAX_VALUE;
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (!level().isClientSide() && source.getEntity() instanceof Player player) {
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!held.isEmpty() && held.getItem() instanceof hellfirepvp.astralsorcery.common.item.ItemChisel) {
                ItemStack thisStack = getItem();
                if (!thisStack.isEmpty() && thisStack.getItem() instanceof ItemStarmetalIngot) {
                    boolean doDamage = false;
                    if (random.nextFloat() < 0.4f) {
                        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, held);
                        doDamage = createStardust(fortune);
                    }
                    if (doDamage || random.nextFloat() < 0.35f) {
                        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                    }
                    return true;
                }
            }
        }
        return super.hurt(source, amount);
    }

    private boolean createStardust(int fortune) {
        ItemStack stardust = new ItemStack(ItemsAS.STARDUST.get());
        ItemUtils.dropItemNaturally(level(), getX(), getY() + 0.25, getZ(), stardust);

        float breakChance = 0.90f - Math.min(fortune, 10) * 0.06f;
        if (random.nextFloat() < breakChance) {
            ItemStack thisStack = getItem();
            thisStack.shrink(1);
            setItem(thisStack);
        }
        return true;
    }
}
