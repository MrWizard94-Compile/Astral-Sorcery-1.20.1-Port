package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.loot.CopyConstellation;
import hellfirepvp.astralsorcery.common.loot.CopyCrystalProperties;
import hellfirepvp.astralsorcery.common.loot.LinearLuckBonus;
import hellfirepvp.astralsorcery.common.loot.RandomCrystalProperty;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class LootAS {

    private LootAS() {}

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, AstralSorcery.MODID);

    public static final ResourceLocation SHRINE_CHEST =
            AstralSorcery.key("shrine_chest");
    public static final ResourceLocation STARFALL_SHOOTING_STAR_REWARD =
            AstralSorcery.key("gameplay/starfall/shooting_star");

    // Registered directly in the outer class so these initializers run when LootAS is loaded,
    // ensuring the DeferredRegister entries exist before RegisterEvent fires.
    public static final RegistryObject<LootItemFunctionType> LINEAR_LUCK_BONUS =
            LOOT_FUNCTION_TYPES.register("linear_luck_bonus",
                    () -> new LootItemFunctionType(new LinearLuckBonus.Serializer()));

    // Singular alias — used by rock_crystal_ore.json loot table
    public static final RegistryObject<LootItemFunctionType> RANDOM_CRYSTAL_PROPERTY =
            LOOT_FUNCTION_TYPES.register("random_crystal_property",
                    () -> new LootItemFunctionType(new RandomCrystalProperty.Serializer()));

    // Plural form — used by shooting_star.json loot table
    public static final RegistryObject<LootItemFunctionType> RANDOM_CRYSTAL_PROPERTIES =
            LOOT_FUNCTION_TYPES.register("random_crystal_properties",
                    () -> new LootItemFunctionType(new RandomCrystalProperty.Serializer()));

    public static final RegistryObject<LootItemFunctionType> COPY_CRYSTAL_PROPERTIES =
            LOOT_FUNCTION_TYPES.register("copy_crystal_properties",
                    () -> new LootItemFunctionType(new CopyCrystalProperties.Serializer()));

    public static final RegistryObject<LootItemFunctionType> COPY_CONSTELLATION =
            LOOT_FUNCTION_TYPES.register("copy_constellation",
                    () -> new LootItemFunctionType(new CopyConstellation.Serializer()));
}
