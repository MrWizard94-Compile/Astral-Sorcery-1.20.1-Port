package hellfirepvp.astralsorcery.common.item.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Crystal Sword — durability and attack damage scale with crystal properties.
 * Size → durability; Cutting → attack damage and speed.
 */
public class ItemCrystalSword extends SwordItem {

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_UUID  = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    /** Base attack damage bonus for crystal sword (added to generic attack 4). */
    private static final float BASE_DAMAGE = 3.0F;
    private static final float BASE_SPEED  = -2.4F;

    public ItemCrystalSword() {
        super(CrystalToolTier.INSTANCE, (int) BASE_DAMAGE, BASE_SPEED, new Properties());
    }

    @Override
    @SuppressWarnings("null")
    public int getMaxDamage(ItemStack stack) {
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null
                ? CrystalCalculations.getToolDurability(props, super.getMaxDamage(stack))
                : super.getMaxDamage(stack);
    }

    @Override
    @Nonnull
    @SuppressWarnings("null")
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            @Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);

        CrystalProperties props = CrystalProperties.getFromStack(stack);
        if (props == null) return super.getAttributeModifiers(slot, stack);

        float mult = CrystalCalculations.getToolSpeedMultiplier(props);
        float damage = BASE_DAMAGE * mult;
        float speed  = BASE_SPEED;

        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        map.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_UUID,
                "Crystal sword damage", damage, AttributeModifier.Operation.ADDITION));
        map.put(Attributes.ATTACK_SPEED,  new AttributeModifier(ATTACK_SPEED_UUID,
                "Crystal sword speed", speed,  AttributeModifier.Operation.ADDITION));
        return map;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        if (props != null) {
            tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.size",    props.getSize()));
            tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.purity",  props.getPurity()));
            tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.cutting", props.getCutting()));
        }
    }
}
