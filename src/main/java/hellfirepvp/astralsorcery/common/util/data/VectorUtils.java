package hellfirepvp.astralsorcery.common.util.data;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Geometric helper methods that operate on {@link Vector3} instances:
 * circle point generation, velocity capping, and random spatial offsets.
 */
public class VectorUtils {

    @Nonnull
    public static List<Vector3> getCirclePositions(@Nonnull Vector3 centerOffset,
                                                    @Nonnull Vector3 axis,
                                                    double radius,
                                                    int amountOfPointsOnCircle) {
        List<Vector3> out = new LinkedList<>();
        Vector3 circleVec = axis.clone().perpendicular().normalize().multiply(radius);
        double degPerPoint = 360D / amountOfPointsOnCircle;
        for (int i = 0; i < amountOfPointsOnCircle; i++) {
            double deg = i * degPerPoint;
            out.add(circleVec.clone().rotate(Math.toRadians(deg), axis.clone()).add(centerOffset));
        }
        return out;
    }

    @Nonnull
    public static Vector3 getRandomCirclePosition(@Nonnull Vector3 centerOffset,
                                                   @Nonnull Vector3 axis,
                                                   double radius) {
        return getCirclePosition(centerOffset, axis, radius, Math.random() * 360);
    }

    @Nonnull
    public static Vector3 getCirclePosition(@Nonnull Vector3 centerOffset,
                                             @Nonnull Vector3 axis,
                                             double radius,
                                             double degree) {
        Vector3 circleVec = axis.clone().perpendicular().normalize().multiply(radius);
        return circleVec.rotate(Math.toRadians(degree), axis.clone()).add(centerOffset);
    }

    @Nonnull
    public static Vector3 limitVelocityToMinecraftLimit(@Nonnull Vector3 velocity) {
        double maxDir = Math.max(Math.abs(velocity.getX()),
                Math.max(Math.abs(velocity.getY()), Math.abs(velocity.getZ())));
        if (maxDir <= 3.9) { // SEntityVelocityPacket: 3.9 * 8000 short value limit
            return velocity;
        }
        return velocity.multiply(3.9 / maxDir);
    }

    public static void applyRandomOffset(@Nonnull Vector3 target, @Nonnull Random rand) {
        applyRandomOffset(target, rand, 1F);
    }

    public static void applyRandomOffset(@Nonnull Vector3 target, @Nonnull Random rand,
                                          float multiplier) {
        target.addX(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
        target.addY(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
        target.addZ(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
    }

    public static void applyRandomCircularOffset(@Nonnull Vector3 target, @Nonnull Random rand) {
        applyRandomOffset(target, rand, 1F);
    }

    public static void applyRandomCircularOffset(@Nonnull Vector3 target, @Nonnull Random rand,
                                                  float multiplier) {
        Vector3 v = Vector3.random().normalize().multiply(rand.nextFloat() * multiplier);
        target.addX(v.getX() * (rand.nextBoolean() ? 1 : -1));
        target.addY(v.getY() * (rand.nextBoolean() ? 1 : -1));
        target.addZ(v.getZ() * (rand.nextBoolean() ? 1 : -1));
    }
}
