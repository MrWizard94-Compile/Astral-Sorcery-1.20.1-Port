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
        // Inter-branch connector perks (bridging adjacent branches)
        // ======================================================================

        // Discidia → Armara bridge (attack speed + armor hybrid)
        SmallPerk bridgeDA = smallPerk("bridge_da", 16, -22, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.03f);
        PerkTree.register(bridgeDA);
        connect(rootDiscidia, bridgeDA);
        connect(rootArmara, bridgeDA);

        // Armara → Vicio bridge (knockback resist + speed hybrid)
        SmallPerk bridgeAV = smallPerk("bridge_av", 26, 10, PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST, 0.05f);
        PerkTree.register(bridgeAV);
        connect(rootArmara, bridgeAV);
        connect(rootVicio, bridgeAV);

        // Vicio → Aevitas bridge (reach + regen hybrid)
        SmallPerk bridgeVA = smallPerk("bridge_va", 0, 30, PerkAttributeTypesAS.ATTR_TYPE_REACH, 0.3f);
        PerkTree.register(bridgeVA);
        connect(rootVicio, bridgeVA);
        connect(rootAevitas, bridgeVA);

        // Aevitas → Evorsio bridge (health + mining hybrid)
        SmallPerk bridgeAE = smallPerk("bridge_ae", -26, 10, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 1.0f);
        PerkTree.register(bridgeAE);
        connect(rootAevitas, bridgeAE);
        connect(rootEvorsio, bridgeAE);

        // Evorsio → Discidia bridge (experience + damage hybrid)
        SmallPerk bridgeED = smallPerk("bridge_ed", -16, -22, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.03f);
        PerkTree.register(bridgeED);
        connect(rootEvorsio, bridgeED);
        connect(rootDiscidia, bridgeED);

        // ======================================================================
        // Secondary small perk paths (alternate branching from each root)
        // ======================================================================

        // Discidia alt path: crit-focused
        SmallPerk discAlt1 = smallPerk("disc_a1", -4, -36, PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE, 0.04f);
        SmallPerk discAlt2 = smallPerk("disc_a2", -8, -42, PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER, 0.1f);
        PerkTree.register(discAlt1);
        PerkTree.register(discAlt2);
        connect(rootDiscidia, discAlt1);
        connect(discAlt1, discAlt2);

        // Armara alt path: toughness-focused
        SmallPerk armaAlt1 = smallPerk("arma_a1", 32, -4, PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS, 1.0f);
        SmallPerk armaAlt2 = smallPerk("arma_a2", 38, 0, PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST, 0.1f);
        PerkTree.register(armaAlt1);
        PerkTree.register(armaAlt2);
        connect(rootArmara, armaAlt1);
        connect(armaAlt1, armaAlt2);

        // Vicio alt path: swim/step-height related
        SmallPerk viciAlt1 = smallPerk("vici_a1", 12, 28, PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED, 0.02f);
        SmallPerk viciAlt2 = smallPerk("vici_a2", 8, 34, PerkAttributeTypesAS.ATTR_TYPE_REACH, 0.4f);
        PerkTree.register(viciAlt1);
        PerkTree.register(viciAlt2);
        connect(rootVicio, viciAlt1);
        connect(viciAlt1, viciAlt2);

        // Aevitas alt path: life steal focused
        SmallPerk aevAlt1 = smallPerk("aev_a1", -12, 28, PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL, 0.03f);
        SmallPerk aevAlt2 = smallPerk("aev_a2", -8, 34, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 1.0f);
        PerkTree.register(aevAlt1);
        PerkTree.register(aevAlt2);
        connect(rootAevitas, aevAlt1);
        connect(aevAlt1, aevAlt2);

        // Evorsio alt path: experience focused
        SmallPerk evorAlt1 = smallPerk("evor_a1", -32, -4, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.08f);
        SmallPerk evorAlt2 = smallPerk("evor_a2", -38, 0, PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.05f);
        PerkTree.register(evorAlt1);
        PerkTree.register(evorAlt2);
        connect(rootEvorsio, evorAlt1);
        connect(evorAlt1, evorAlt2);

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

        // Second focus — combat efficiency
        FocusPerk focusCombat = new FocusPerk(AstralSorcery.key("focus_combat"), 0, -8, AstralSorcery.key("discidia"));
        focusCombat.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        focusCombat.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER.getKey(),
                ModifierType.ADDITION, 0.20f));
        PerkTree.register(focusCombat);

        // Connect focuses to all roots
        connect(focusStarlight, rootDiscidia);
        connect(focusStarlight, rootArmara);
        connect(focusStarlight, rootVicio);
        connect(focusStarlight, rootAevitas);
        connect(focusStarlight, rootEvorsio);

        connect(focusCombat, focusStarlight);
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
