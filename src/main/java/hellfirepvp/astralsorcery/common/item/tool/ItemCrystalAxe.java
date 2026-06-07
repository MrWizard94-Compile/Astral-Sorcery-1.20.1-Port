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
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Crystal Axe — durability, attack damage, and dig speed scale with crystal properties.
 */
public class ItemCrystalAxe extends AxeItem {

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("92AB5534-4B37-11D1-9CBF-00AA00B8A733");
    private static final UUID ATTACK_SPEED_UUID  = UUID.fromString("E014E42C-4B37-11D1-9CBF-00AA00B8A733");

    private static final float BASE_DAMAGE = 5.0F;
    private static final float BASE_SPEED  = -3.0F;

    public ItemCrystalAxe() {
        super(CrystalToolTier.INSTANCE, BASE_DAMAGE, BASE_SPEED, new Properties());
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        float base = super.getDestroySpeed(stack, state);
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null ? base * CrystalCalculations.getToolSpeedMultiplier(props) : base;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null
                ? CrystalCalculations.getToolDurability(props, super.getMaxDamage(stack))
                : super.getMaxDamage(stack);
    }

    @Override
    @Nonnull
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);

        CrystalProperties props = CrystalProperties.getFromStack(stack);
        if (props == null) return super.getAttributeModifiers(slot, stack);

        float mult = CrystalCalculations.getToolSpeedMultiplier(props);
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        map.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_UUID,
                "Crystal axe damage", BASE_DAMAGE * mult, AttributeModifier.Operation.ADDITION));
        map.put(Attributes.ATTACK_SPEED,  new AttributeModifier(ATTACK_SPEED_UUID,
                "Crystal axe speed",  BASE_SPEED,          AttributeModifier.Operation.ADDITION));
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
