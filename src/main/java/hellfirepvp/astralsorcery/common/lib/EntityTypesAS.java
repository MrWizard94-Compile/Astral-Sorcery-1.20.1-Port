package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.entity.EntityCelestialCrystal;
import hellfirepvp.astralsorcery.common.entity.EntityCrystal;
import hellfirepvp.astralsorcery.common.entity.EntityDazzlingGem;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.entity.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.entity.EntityIlluminationSpark;
import hellfirepvp.astralsorcery.common.entity.EntityItemExplosionResistant;
import hellfirepvp.astralsorcery.common.entity.EntityItemHighlighted;
import hellfirepvp.astralsorcery.common.entity.EntityLiquidSpark;
import hellfirepvp.astralsorcery.common.entity.EntityNocturnalSpark;
import hellfirepvp.astralsorcery.common.entity.EntityObservatoryHelper;
import hellfirepvp.astralsorcery.common.entity.EntityShootingStar;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import hellfirepvp.astralsorcery.common.entity.EntityStarling;
import hellfirepvp.astralsorcery.common.entity.EntityStarmetal;
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

    /**
     * Shooting star — falls from sky during celestial events, drops rare loot on impact.
     */
    public static final RegistryObject<EntityType<EntityShootingStar>> SHOOTING_STAR =
            ENTITY_TYPES.register("shooting_star", () ->
                    EntityType.Builder.<EntityShootingStar>of(EntityShootingStar::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":shooting_star"));

    /**
     * Grappling hook — Evorsio perk projectile that pulls the player to the impact point.
     */
    public static final RegistryObject<EntityType<EntityGrapplingHook>> GRAPPLING_HOOK =
            ENTITY_TYPES.register("grappling_hook", () ->
                    EntityType.Builder.<EntityGrapplingHook>of(EntityGrapplingHook::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(AstralSorcery.MODID + ":grappling_hook"));

    /**
     * Highlighted item entity — glowing drop from shooting stars, rituals, etc.
     */
    public static final RegistryObject<EntityType<EntityItemHighlighted>> ITEM_HIGHLIGHTED =
            ENTITY_TYPES.register("item_highlighted", () ->
                    EntityType.Builder.<EntityItemHighlighted>of(EntityItemHighlighted::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(32)
                            .updateInterval(20)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":item_highlighted"));

    /**
     * Liquid starlight spark — ambient visual entity orbiting lightwells and chalices.
     */
    public static final RegistryObject<EntityType<EntityLiquidSpark>> LIQUID_SPARK =
            ENTITY_TYPES.register("liquid_spark", () ->
                    EntityType.Builder.<EntityLiquidSpark>of(EntityLiquidSpark::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(32)
                            .updateInterval(5)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":liquid_spark"));

    /**
     * Celestial crystal — floating crystal entity from celestial events.
     */
    public static final RegistryObject<EntityType<EntityCelestialCrystal>> CELESTIAL_CRYSTAL =
            ENTITY_TYPES.register("celestial_crystal", () ->
                    EntityType.Builder.<EntityCelestialCrystal>of(EntityCelestialCrystal::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)
                            .clientTrackingRange(64)
                            .updateInterval(5)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":celestial_crystal"));

    /**
     * Observatory helper — invisible mount entity for observatory camera control.
     */
    public static final RegistryObject<EntityType<EntityObservatoryHelper>> OBSERVATORY_HELPER =
            ENTITY_TYPES.register("observatory_helper", () ->
                    EntityType.Builder.<EntityObservatoryHelper>of(EntityObservatoryHelper::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(16)
                            .updateInterval(20)
                            .fireImmune()
                            .noSummon()
                            .build(AstralSorcery.MODID + ":observatory_helper"));

    /**
     * Starling — glowing companion entity orbiting players or ritual pedestals.
     */
    public static final RegistryObject<EntityType<EntityStarling>> STARLING =
            ENTITY_TYPES.register("starling", () ->
                    EntityType.Builder.<EntityStarling>of(EntityStarling::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)
                            .clientTrackingRange(32)
                            .updateInterval(3)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":starling"));

    /**
     * Explosion-resistant item entity — base for crystals and gems.
     */
    public static final RegistryObject<EntityType<EntityItemExplosionResistant>> ITEM_EXPLOSION_RESISTANT =
            ENTITY_TYPES.register("item_explosion_resistant", () ->
                    EntityType.Builder.<EntityItemExplosionResistant>of(EntityItemExplosionResistant::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(20)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":item_explosion_resistant"));

    /**
     * Crystal item entity — explosion-resistant, can be chiseled to split crystal properties.
     */
    public static final RegistryObject<EntityType<EntityCrystal>> ITEM_CRYSTAL =
            ENTITY_TYPES.register("item_crystal", () ->
                    EntityType.Builder.<EntityCrystal>of(EntityCrystal::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(20)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":item_crystal"));

    /**
     * Dazzling gem item entity — explosion-resistant, never despawns.
     */
    public static final RegistryObject<EntityType<EntityDazzlingGem>> ITEM_DAZZLING_GEM =
            ENTITY_TYPES.register("item_dazzling_gem", () ->
                    EntityType.Builder.<EntityDazzlingGem>of(EntityDazzlingGem::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(20)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":item_dazzling_gem"));

    /**
     * Starmetal ingot item entity — can be chiseled to produce stardust.
     */
    public static final RegistryObject<EntityType<EntityStarmetal>> ITEM_STARMETAL_INGOT =
            ENTITY_TYPES.register("item_starmetal_ingot", () ->
                    EntityType.Builder.<EntityStarmetal>of(EntityStarmetal::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(20)
                            .fireImmune()
                            .build(AstralSorcery.MODID + ":item_starmetal_ingot"));
}
