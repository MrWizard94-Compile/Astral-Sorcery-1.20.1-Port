/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Abstract base class for perk attribute converters.
 * Converters intercept attribute modifiers from perks and can transform or
 * augment them — e.g. to scale damage up near the centre of the tree.
 *
 * <p>1.16 → 1.20: dropped {@code ForgeRegistryEntry<PerkConverter>}; registration
 * is managed by a simple map in {@code PerkConverterRegistry} rather than
 * Forge's data-driven registry (which changed significantly in 1.20).</p>
 */
public abstract class PerkConverter {

    @Nonnull
    private final ResourceLocation id;

    protected PerkConverter(@Nonnull ResourceLocation id) {
        this.id = id;
    }

    @Nonnull
    public ResourceLocation getId() {
        return id;
    }

    /**
     * Converts the given modifier. Use
     * {@link PerkAttributeModifier#convertModifier} to produce the replacement.
     */
    @Nonnull
    public abstract PerkAttributeModifier convertModifier(@Nonnull Player player,
                                                           @Nonnull PlayerProgress progress,
                                                           @Nonnull PerkAttributeModifier modifier,
                                                           @Nullable AbstractPerk owningPerk);

    /**
     * Optionally produces additional modifiers derived from the given one.
     * Default: empty list.
     */
    @Nonnull
    public Collection<PerkAttributeModifier> gainExtraModifiers(@Nonnull Player player,
                                                                 @Nonnull PlayerProgress progress,
                                                                 @Nonnull PerkAttributeModifier modifier,
                                                                 @Nullable AbstractPerk owningPerk) {
        return Collections.emptyList();
    }

    public void onApply(@Nonnull Player player, @Nonnull LogicalSide side) {}
    public void onRemove(@Nonnull Player player, @Nonnull LogicalSide side) {}

    // =========================================================================
    // Ranged sub-type
    // =========================================================================

    /**
     * Creates a ranged variant that only converts modifiers whose owning perk
     * falls within {@code radius} tree-space units of {@code offset}.
     */
    @Nonnull
    public Radius asRangedConverter(@Nonnull Point.Float offset, float radius) {
        PerkConverter self = this;
        return new Radius(id, offset, radius) {
            @Nonnull
            @Override
            public PerkAttributeModifier convertModifierInRange(@Nonnull Player player,
                                                                 @Nonnull PlayerProgress progress,
                                                                 @Nonnull PerkAttributeModifier modifier,
                                                                 @Nonnull AbstractPerk owningPerk) {
                return self.convertModifier(player, progress, modifier, owningPerk);
            }

            @Nonnull
            @Override
            public Collection<PerkAttributeModifier> gainExtraModifiersInRange(@Nonnull Player player,
                                                                                @Nonnull PlayerProgress progress,
                                                                                @Nonnull PerkAttributeModifier modifier,
                                                                                @Nonnull AbstractPerk owningPerk) {
                return self.gainExtraModifiers(player, progress, modifier, owningPerk);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerkConverter other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // =========================================================================
    // Radius sub-type
    // =========================================================================

    public abstract static class Radius extends PerkConverter {

        private final float radius;
        private final Point.Float offset;

        protected Radius(@Nonnull ResourceLocation id, @Nonnull Point.Float offset, float radius) {
            super(id);
            this.offset = offset;
            this.radius = radius;
        }

        public float getRadius() { return radius; }
        public Point.Float getOffset() { return offset; }

        public Radius withNewRadius(float newRadius) {
            Radius self = this;
            return new Radius(getId(), offset, newRadius) {
                @Nonnull
                @Override
                public PerkAttributeModifier convertModifierInRange(@Nonnull Player player,
                                                                     @Nonnull PlayerProgress progress,
                                                                     @Nonnull PerkAttributeModifier modifier,
                                                                     @Nonnull AbstractPerk owningPerk) {
                    return self.convertModifierInRange(player, progress, modifier, owningPerk);
                }
                @Nonnull
                @Override
                public Collection<PerkAttributeModifier> gainExtraModifiersInRange(@Nonnull Player player,
                                                                                    @Nonnull PlayerProgress progress,
                                                                                    @Nonnull PerkAttributeModifier modifier,
                                                                                    @Nonnull AbstractPerk owningPerk) {
                    return self.gainExtraModifiersInRange(player, progress, modifier, owningPerk);
                }
            };
        }

        protected boolean canAffectPerk(@Nonnull AbstractPerk perk) {
            double dx = offset.x - perk.getX();
            double dy = offset.y - perk.getY();
            return Math.sqrt(dx * dx + dy * dy) <= radius;
        }

        @Nonnull
        @Override
        public PerkAttributeModifier convertModifier(@Nonnull Player player,
                                                      @Nonnull PlayerProgress progress,
                                                      @Nonnull PerkAttributeModifier modifier,
                                                      @Nullable AbstractPerk owningPerk) {
            if (owningPerk == null || !canAffectPerk(owningPerk)) return modifier;
            return convertModifierInRange(player, progress, modifier, owningPerk);
        }

        @Nonnull
        @Override
        public Collection<PerkAttributeModifier> gainExtraModifiers(@Nonnull Player player,
                                                                     @Nonnull PlayerProgress progress,
                                                                     @Nonnull PerkAttributeModifier modifier,
                                                                     @Nullable AbstractPerk owningPerk) {
            if (owningPerk == null || !canAffectPerk(owningPerk)) return Collections.emptyList();
            return gainExtraModifiersInRange(player, progress, modifier, owningPerk);
        }

        @Nonnull
        public abstract PerkAttributeModifier convertModifierInRange(@Nonnull Player player,
                                                                      @Nonnull PlayerProgress progress,
                                                                      @Nonnull PerkAttributeModifier modifier,
                                                                      @Nonnull AbstractPerk owningPerk);

        @Nonnull
        public abstract Collection<PerkAttributeModifier> gainExtraModifiersInRange(@Nonnull Player player,
                                                                                     @Nonnull PlayerProgress progress,
                                                                                     @Nonnull PerkAttributeModifier modifier,
                                                                                     @Nonnull AbstractPerk owningPerk);
    }
}
