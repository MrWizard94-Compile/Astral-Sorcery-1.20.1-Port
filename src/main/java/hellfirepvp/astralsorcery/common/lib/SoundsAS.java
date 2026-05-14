package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundsAS {

    private SoundsAS() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AstralSorcery.MODID);

    public static final RegistryObject<SoundEvent> BLOCK_COLOREDLENS_ATTACH =
            sound("block.coloredlens.attach");

    public static final RegistryObject<SoundEvent> ALTAR_CRAFT_START =
            sound("altar.craft.start");
    public static final RegistryObject<SoundEvent> ALTAR_CRAFT_FINISH =
            sound("altar.craft.finish");
    public static final RegistryObject<SoundEvent> ALTAR_CRAFT_LOOP_T1 =
            sound("altar.craft.loop.t1");
    public static final RegistryObject<SoundEvent> ALTAR_CRAFT_LOOP_T2 =
            sound("altar.craft.loop.t2");
    public static final RegistryObject<SoundEvent> ALTAR_CRAFT_LOOP_T3 =
            sound("altar.craft.loop.t3");
    public static final RegistryObject<SoundEvent> ALTAR_CRAFT_LOOP_T4 =
            sound("altar.craft.loop.t4");
    public static final RegistryObject<SoundEvent> ALTAR_CRAFT_LOOP_T4_WAITING =
            sound("altar.craft.loop.t4.waiting");

    public static final RegistryObject<SoundEvent> ATTUNEMENT_ALTAR_IDLE =
            sound("attunement.altar.idle");
    public static final RegistryObject<SoundEvent> ATTUNEMENT_ALTAR_PLAYER_ATTUNE =
            sound("attunement.altar.player.attune");
    public static final RegistryObject<SoundEvent> ATTUNEMENT_ALTAR_ITEM_START =
            sound("attunement.altar.item.start");
    public static final RegistryObject<SoundEvent> ATTUNEMENT_ALTAR_ITEM_FINISH =
            sound("attunement.altar.item.finish");
    public static final RegistryObject<SoundEvent> ATTUNEMENT_ALTAR_ITEM_LOOP =
            sound("attunement.altar.item.loop");

    public static final RegistryObject<SoundEvent> INFUSER_CRAFT_START =
            sound("infuser.craft.start");
    public static final RegistryObject<SoundEvent> INFUSER_CRAFT_LOOP =
            sound("infuser.craft.loop");
    public static final RegistryObject<SoundEvent> INFUSER_CRAFT_FINISH =
            sound("infuser.craft.finish");

    public static final RegistryObject<SoundEvent> PERK_SEAL =
            sound("perk.seal");
    public static final RegistryObject<SoundEvent> PERK_UNSEAL =
            sound("perk.unseal");
    public static final RegistryObject<SoundEvent> PERK_UNLOCK =
            sound("perk.unlock");
    public static final RegistryObject<SoundEvent> ILLUMINATION_WAND_HIGHLIGHT =
            sound("illumination.wand.highlight");
    public static final RegistryObject<SoundEvent> ILLUMINATION_WAND_UNHIGHLIGHT =
            sound("illumination.wand.unhighlight");
    public static final RegistryObject<SoundEvent> ILLUMINATION_WAND_LIGHT =
            sound("illumination.wand.light");

    public static final RegistryObject<SoundEvent> GUI_JOURNAL_CLOSE =
            sound("gui.journal.close");
    public static final RegistryObject<SoundEvent> GUI_JOURNAL_PAGE =
            sound("gui.journal.page");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(AstralSorcery.key(name)));
    }
}
