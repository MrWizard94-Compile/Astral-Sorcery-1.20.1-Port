/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Constellation effect for <em>Bootes</em> (Weak — Animal / Herding).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Breeds adult animals in the area by resetting their love cooldown
 *       and triggering the in-love state</li>
 *   <li>Accelerates growth of baby animals in range</li>
 *   <li>Shears wool from Sheep that are not yet sheared</li>
 * </ul>
 *
 * <p>Potency scales the baby-growth acceleration amount and affects how
 * frequently animals enter the love state.</p>
 */
public class CEffectBootes extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Age ticks added to baby animals per effect tick. */
    private static final int BASE_GROWTH_ACCELERATION = 60;

    /** Chance (0-1) that an adult animal will be set in love per tick. */
    private static final double BASE_BREED_CHANCE = 0.1;

    public CEffectBootes() {
        super("bootes");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        processAnimals(serverLevel, area, potency);
    }

    /**
     * Process all animals in the area: accelerate babies, breed adults,
     * and shear sheep.
     */
    private void processAnimals(@Nonnull ServerLevel level, @Nonnull AABB area,
                                double potency) {
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);
        int growthBoost = (int) (BASE_GROWTH_ACCELERATION * potency);
        double breedChance = Math.min(1.0, BASE_BREED_CHANCE * potency);

        for (Animal animal : animals) {
            if (animal.isBaby()) {
                // Accelerate baby growth
                animal.ageUp(growthBoost);
            } else {
                // Chance to trigger breeding
                if (!animal.isInLove()
                        && animal.getAge() == 0
                        && level.getRandom().nextDouble() < breedChance) {
                    animal.setInLove(null);
                }
            }

            // Shear sheep
            if (animal instanceof Sheep sheep && !sheep.isSheared()) {
                shearSheep(level, sheep);
            }
        }
    }

    /**
     * Shear a sheep and drop its wool at its position.
     */
    private void shearSheep(@Nonnull ServerLevel level, @Nonnull Sheep sheep) {
        sheep.setSheared(true);
        // Drop wool matching the sheep's dye color (white_wool, orange_wool, etc.)
        String woolName = sheep.getColor().getName() + "_wool";
        Block woolBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft", woolName));
        if (woolBlock == null) woolBlock = Blocks.WHITE_WOOL;

        int woolCount = 1 + level.getRandom().nextInt(3);
        ItemEntity item = new ItemEntity(level,
                sheep.getX(), sheep.getY() + 1.0, sheep.getZ(),
                new ItemStack(woolBlock, woolCount));
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
