package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Crystal item entity — explosion-resistant and can be chiseled by a player
 * holding a {@link hellfirepvp.astralsorcery.common.item.ItemChisel}.
 * Chiseling splits the crystal: reduces its highest property tier and creates
 * a new inert crystal with some of those properties transferred.
 * {@code lifespan = Integer.MAX_VALUE} prevents natural despawn.
 *
 * <p>1.16 → 1.20: InteractableEntity removed — chisel interaction hooked via
 * {@link #hurt(DamageSource, float)}; uses {@link RandomSource} (Minecraft 1.20
 * replaces java.util.Random for entity-level RNG).</p>
 */
public class EntityCrystal extends EntityItemExplosionResistant {

    public EntityCrystal(EntityType<? extends EntityCrystal> type, Level level) {
        super(type, level);
        this.lifespan = Integer.MAX_VALUE;
    }

    public EntityCrystal(EntityType<? extends EntityCrystal> type, Level level,
                          double x, double y, double z, @Nonnull ItemStack stack) {
        super(type, level, x, y, z, stack);
        this.lifespan = Integer.MAX_VALUE;
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (!level().isClientSide() && source.getEntity() instanceof Player player) {
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!held.isEmpty() && held.getItem() instanceof hellfirepvp.astralsorcery.common.item.ItemChisel) {
                ItemStack thisStack = getItem();
                if (!thisStack.isEmpty() && thisStack.getItem() instanceof ItemCrystalBase base) {
                    CrystalAttributes attrs = base.getAttributes(thisStack);
                    if (attrs != null && !attrs.isEmpty()) {
                        boolean doDamage = false;
                        if (random.nextFloat() < 0.35f) {
                            int fortune = EnchantmentHelper.getItemEnchantmentLevel(
                                    Enchantments.BLOCK_FORTUNE, held);
                            doDamage = splitCrystal(base, thisStack, attrs, fortune, random);
                        }
                        if (doDamage || random.nextFloat() < 0.35f) {
                            held.hurtAndBreak(1, player,
                                    p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                        }
                        return true;
                    }
                }
            }
        }
        return super.hurt(source, amount);
    }

    private boolean splitCrystal(ItemCrystalBase base, ItemStack thisStack,
                                   CrystalAttributes attrs, int fortune,
                                   @Nonnull RandomSource rng) {
        ItemStack created = new ItemStack(base);
        if (created.isEmpty()) return false;

        int maxSplit = Mth.ceil(attrs.getTotalTierLevel() / 2f);
        if (maxSplit >= attrs.getTotalTierLevel()) return false;

        int lostModifiers = 0;
        if (maxSplit > 1 && rng.nextFloat() < (0.6f / (fortune + 1))) {
            lostModifiers++;
            if (maxSplit > 2 && rng.nextFloat() < (0.2f / (fortune + 1))) {
                lostModifiers++;
            }
        }

        CrystalAttributes resultThis = attrs;
        CrystalAttributes.Builder splitBuilder = CrystalAttributes.Builder.newBuilder(false);
        for (int i = 0; i < maxSplit; i++) {
            List<CrystalProperty> props = new ArrayList<>(resultThis.getProperties());
            if (props.isEmpty()) break;
            CrystalProperty prop = props.get(rng.nextInt(props.size()));
            resultThis = resultThis.modifyLevel(prop, -1);
            if (lostModifiers > 0) {
                lostModifiers--;
            } else {
                splitBuilder.addProperty(prop, 1);
            }
        }

        base.setAttributes(thisStack, resultThis);
        base.setAttributes(created, splitBuilder.build());
        ItemUtils.dropItemNaturally(level(), getX(), getY() + 0.25, getZ(), created);
        return true;
    }
}
