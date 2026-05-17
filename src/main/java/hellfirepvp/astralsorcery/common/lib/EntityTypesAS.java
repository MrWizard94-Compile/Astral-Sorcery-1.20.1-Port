package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.entity.EntityIlluminationSpark;
import hellfirepvp.astralsorcery.common.entity.EntityNocturnalSpark;
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

    /**
     * Illumination spark — floating light entity from Illumination Powder.
     */
    public static final RegistryObject<EntityType<EntityIlluminationSpark>> ILLUMINATION_SPARK =
            ENTITY_TYPES.register("illumination_spark", () ->
                    EntityType.Builder.<EntityIlluminationSpark>of(EntityIlluminationSpark::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(32)
                            .updateInterval(20)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":illumination_spark"));

    /**
     * Constellation flare �� projectile launched from ritual configurations.
     */
    public static final RegistryObject<EntityType<EntityFlare>> FLARE =
            ENTITY_TYPES.register("flare", () ->
                    EntityType.Builder.<EntityFlare>of(EntityFlare::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":flare"));

    /**
     * Nocturnal spark — homing hostile-seeking spark from Nocturnal Powder.
     */
    public static final RegistryObject<EntityType<EntityNocturnalSpark>> NOCTURNAL_SPARK =
            ENTITY_TYPES.register("nocturnal_spark", () ->
                    EntityType.Builder.<EntityNocturnalSpark>of(EntityNocturnalSpark::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":nocturnal_spark"));

    // TODO: Register remaining entity types as they are ported:
    // - EntityShootingStar (shooting star event)
    // - EntityGrapplingHook (evorsio grappling)
    // - EntityCrystalTool (crystal tool projectile)
    // - EntityObservatoryHelper (observatory targeting)
    // - EntityItemHighlighted (glowing item drop)
    // - EntityLiquidSpark (liquid starlight spark)
    // - EntityStarling (ritual pet entity)
    // - EntityCelestialCrystal (floating crystal drop)
}
