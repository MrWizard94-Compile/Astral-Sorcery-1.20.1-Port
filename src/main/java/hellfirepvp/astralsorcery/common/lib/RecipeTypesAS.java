package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeTypesAS {

    private RecipeTypesAS() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, AstralSorcery.MODID);

    // 5 entries: well, infusion, block_transmutation, altar, liquid_interaction
    // Registered when recipe classes are ported in Phase 6.
}
