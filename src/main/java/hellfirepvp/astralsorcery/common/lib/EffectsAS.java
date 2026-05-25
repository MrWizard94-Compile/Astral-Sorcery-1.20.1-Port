package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.effect.EffectBleed;
import hellfirepvp.astralsorcery.common.effect.EffectCheatDeath;
import hellfirepvp.astralsorcery.common.effect.EffectDropModifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectsAS {

    private EffectsAS() {}

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AstralSorcery.MODID);

    public static final RegistryObject<MobEffect> EFFECT_BLEED =
            MOB_EFFECTS.register("bleed", EffectBleed::new);

    public static final RegistryObject<MobEffect> EFFECT_CHEAT_DEATH =
            MOB_EFFECTS.register("cheat_death", EffectCheatDeath::new);

    public static final RegistryObject<MobEffect> EFFECT_DROP_MODIFIER =
            MOB_EFFECTS.register("drop_modifier", EffectDropModifier::new);
}
