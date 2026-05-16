package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All Astral Sorcery entity type registrations.
 *
 * <p>1.16 → 1.20: EntityType.Builder pattern unchanged.
 * Registration via DeferredRegister. EntityClassification → MobCategory.</p>
 */
public final class EntityTypesAS {

    private EntityTypesAS() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AstralSorcery.MODID);

    // =========================================================================
    // Entity registrations
    // =========================================================================

    /**
     * Spectral tool — ghost tool entity summoned by Vicio perk effects.
     */
    public static final RegistryObject<EntityType<EntitySpectralTool>> SPECTRAL_TOOL =
            ENTITY_TYPES.register("spectral_tool", () ->
                    EntityType.Builder.<EntitySpectralTool>of(EntitySpectralTool::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":spectral_tool"));

    // TODO: Register remaining entity types as they are ported:
    // - EntityIlluminationSpark (floating light)
    // - EntityShootingStar (shooting star event)
    // - EntityFlare (constellation flare projectile)
    // - EntityGrapplingHook (evorsio grappling)
    // - EntityCrystalTool (crystal tool projectile)
    // - EntityObservatoryHelper (observatory targeting)
    // - EntityItemHighlighted (glowing item drop)
    // - EntityNocturnalSpark (nocturnal powder spark)
    // - EntityLiquidSpark (liquid starlight spark)
    // - EntityStarling (ritual pet entity)
    // - EntityCelestialCrystal (floating crystal drop)
}
