/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.FocusPerk;
import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;
import hellfirepvp.astralsorcery.common.perk.node.SmallPerk;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyAevitas;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyArmara;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDiscidia;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyEvorsio;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVicio;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Builds the default Astral Sorcery perk tree structure.
 * Defines all root perks, key perks, small/major perks and their connections.
 *
 * <p>The tree is organized in a radial layout with 5 constellation roots
 * (Discidia, Armara, Vicio, Aevitas, Evorsio) at cardinal/intercardinal
 * positions, connected through small and major perks to central key perks.</p>
 *
 * <p>This class is called once during server init to populate the PerkTree
 * singleton. In a future iteration this could be data-driven via JSON.</p>
 */
public final class PerkTreeData {

    private PerkTreeData() {}

    /**
     * Builds and registers the entire perk tree into the PerkTree singleton.
     */
    public static void buildTree() {
        PerkTree.clearForTesting();

        // ======================================================================
        // Root perks (center, one per major constellation)
        // ======================================================================
        RootPerk rootDiscidia = new RootPerk(AstralSorcery.key("root_discidia"), 0, -30, AstralSorcery.key("discidia"));
        rootDiscidia.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE.getKey(), ModifierType.ADDITION, 1.0f));

        RootPerk rootArmara = new RootPerk(AstralSorcery.key("root_armara"), 28, -9, AstralSorcery.key("armara"));
        rootArmara.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR.getKey(), ModifierType.ADDITION, 2.0f));

        RootPerk rootVicio = new RootPerk(AstralSorcery.key("root_vicio"), 17, 24, AstralSorcery.key("vicio"));
        rootVicio.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(), ModifierType.ADDED_MULTIPLY, 0.05f));

        RootPerk rootAevitas = new RootPerk(AstralSorcery.key("root_aevitas"), -17, 24, AstralSorcery.key("aevitas"));
        rootAevitas.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH.getKey(), ModifierType.ADDITION, 2.0f));

        RootPerk rootEvorsio = new RootPerk(AstralSorcery.key("root_evorsio"), -28, -9, AstralSorcery.key("evorsio"));
        rootEvorsio.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(), ModifierType.ADDED_MULTIPLY, 0.10f));

        PerkTree.register(rootDiscidia);
        PerkTree.register(rootArmara);
        PerkTree.register(rootVicio);
        PerkTree.register(rootAevitas);
        PerkTree.register(rootEvorsio);

        // ======================================================================
        // Discidia branch: Root → Small → Major → Key
        // ======================================================================
        SmallPerk discSmall1 = smallPerk("disc_s1", 0, -36, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.5f);
        SmallPerk discSmall2 = smallPerk("disc_s2", 4, -42, PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE, 0.05f);
        MajorPerk discMajor = majorPerk("disc_m1", 0, -48, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 1.0f);
        KeyDiscidia keyDiscidia = new KeyDiscidia(0, -56);

        PerkTree.register(discSmall1);
        PerkTree.register(discSmall2);
        PerkTree.register(discMajor);
        PerkTree.register(keyDiscidia);

        connect(rootDiscidia, discSmall1);
        connect(discSmall1, discSmall2);
        connect(discSmall2, discMajor);
        connect(discMajor, keyDiscidia);

        // ======================================================================
        // Armara branch
        // ======================================================================
        SmallPerk armaSmall1 = smallPerk("arma_s1", 34, -12, PerkAttributeTypesAS.ATTR_TYPE_ARMOR, 1.0f);
        SmallPerk armaSmall2 = smallPerk("arma_s2", 40, -16, PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS, 0.5f);
        MajorPerk armaMajor = majorPerk("arma_m1", 46, -12, PerkAttributeTypesAS.ATTR_TYPE_ARMOR, 2.0f);
        KeyArmara keyArmara = new KeyArmara(54, -12);

        PerkTree.register(armaSmall1);
        PerkTree.register(armaSmall2);
        PerkTree.register(armaMajor);
        PerkTree.register(keyArmara);

        connect(rootArmara, armaSmall1);
        connect(armaSmall1, armaSmall2);
        connect(armaSmall2, armaMajor);
        connect(armaMajor, keyArmara);

        // ======================================================================
        // Vicio branch
        // ======================================================================
        SmallPerk viciSmall1 = smallPerk("vici_s1", 20, 30, PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED, 0.03f);
        SmallPerk viciSmall2 = smallPerk("vici_s2", 24, 36, PerkAttributeTypesAS.ATTR_TYPE_REACH, 0.5f);
        MajorPerk viciMajor = majorPerk("vici_m1", 20, 42, PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED, 0.05f);
        KeyVicio keyVicio = new KeyVicio(20, 50);

        PerkTree.register(viciSmall1);
        PerkTree.register(viciSmall2);
        PerkTree.register(viciMajor);
        PerkTree.register(keyVicio);

        connect(rootVicio, viciSmall1);
        connect(viciSmall1, viciSmall2);
        connect(viciSmall2, viciMajor);
        connect(viciMajor, keyVicio);

        // ======================================================================
        // Aevitas branch
        // ======================================================================
        SmallPerk aevSmall1 = smallPerk("aev_s1", -20, 30, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 1.0f);
        SmallPerk aevSmall2 = smallPerk("aev_s2", -24, 36, PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL, 0.02f);
        MajorPerk aevMajor = majorPerk("aev_m1", -20, 42, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 2.0f);
        KeyAevitas keyAevitas = new KeyAevitas(-20, 50);

        PerkTree.register(aevSmall1);
        PerkTree.register(aevSmall2);
        PerkTree.register(aevMajor);
        PerkTree.register(keyAevitas);

        connect(rootAevitas, aevSmall1);
        connect(aevSmall1, aevSmall2);
        connect(aevSmall2, aevMajor);
        connect(aevMajor, keyAevitas);

        // ======================================================================
        // Evorsio branch
        // ======================================================================
        SmallPerk evorSmall1 = smallPerk("evor_s1", -34, -12, PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.05f);
        SmallPerk evorSmall2 = smallPerk("evor_s2", -40, -16, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.05f);
        MajorPerk evorMajor = majorPerk("evor_m1", -46, -12, PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.10f);
        KeyEvorsio keyEvorsio = new KeyEvorsio(-54, -12);

        PerkTree.register(evorSmall1);
        PerkTree.register(evorSmall2);
        PerkTree.register(evorMajor);
        PerkTree.register(keyEvorsio);

        connect(rootEvorsio, evorSmall1);
        connect(evorSmall1, evorSmall2);
        connect(evorSmall2, evorMajor);
        connect(evorMajor, keyEvorsio);

        // ======================================================================
        // Cross-connections between roots (inner ring)
        // ======================================================================
        connect(rootDiscidia, rootArmara);
        connect(rootArmara, rootVicio);
        connect(rootVicio, rootAevitas);
        connect(rootAevitas, rootEvorsio);
        connect(rootEvorsio, rootDiscidia);

        // ======================================================================
        // Focus perks at the center (accessible from any root)
        // ======================================================================
        FocusPerk focusStarlight = new FocusPerk(AstralSorcery.key("focus_starlight"), 0, 0, AstralSorcery.key("astralis"));
        focusStarlight.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_STARLIGHT_COLLECTION.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.20f));
        focusStarlight.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
        PerkTree.register(focusStarlight);

        // Connect focus to all roots
        connect(focusStarlight, rootDiscidia);
        connect(focusStarlight, rootArmara);
        connect(focusStarlight, rootVicio);
        connect(focusStarlight, rootAevitas);
        connect(focusStarlight, rootEvorsio);
    }

    // =========================================================================
    // Factory helpers
    // =========================================================================

    @Nonnull
    private static SmallPerk smallPerk(@Nonnull String name, int x, int y,
                                        @Nonnull hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType type,
                                        float value) {
        SmallPerk perk = new SmallPerk(AstralSorcery.key(name), x, y);
        perk.addModifier(new PerkAttributeModifier(type.getKey(), ModifierType.ADDITION, value));
        return perk;
    }

    @Nonnull
    private static MajorPerk majorPerk(@Nonnull String name, int x, int y,
                                        @Nonnull hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType type,
                                        float value) {
        MajorPerk perk = new MajorPerk(AstralSorcery.key(name), x, y);
        perk.addModifier(new PerkAttributeModifier(type.getKey(), ModifierType.ADDITION, value));
        return perk;
    }

    private static void connect(@Nonnull AbstractPerk a,
                                 @Nonnull AbstractPerk b) {
        PerkTree.connect(a.getKey(), b.getKey());
    }
}
