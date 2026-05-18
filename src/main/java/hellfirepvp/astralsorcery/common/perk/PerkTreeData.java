/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.FocusPerk;
import hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk;
import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;
import hellfirepvp.astralsorcery.common.perk.node.SmallPerk;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyAddEnchantment;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyAevitas;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyAlcara;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyAreaOfEffect;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyArmara;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyBleed;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyBootes;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyChargeBalancing;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyCheatDeath;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyCleanseBadPotions;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyCullingAttack;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDamageArmor;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDamageEffects;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDigTypes;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDisarm;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDiscidia;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyEntityReach;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyEvorsio;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyFornax;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyGelu;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyGrowables;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyHorologium;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyLastBreath;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyLightningArc;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyLucerna;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyMagnetDrops;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyMantleFlight;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyMending;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyMineralis;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyNoArmor;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyNoKnockback;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyOctans;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyPelotrio;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyProjectileDistance;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyProjectileProximity;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyRampage;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyReducedFood;
import hellfirepvp.astralsorcery.common.perk.node.key.KeySpawnLights;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyStepAssist;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyStoneEnrichment;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyTreeConnector;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyUlteria;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVicio;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVoidTrash;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVorux;
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

        // ======================================================================
        // Outer constellation branches (extended paths from key perks)
        // ======================================================================

        // --- Discidia → Lucerna (offense → starlight mastery) ---
        SmallPerk discOuter1 = smallPerk("disc_o1", 4, -62, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.5f);
        SmallPerk discOuter2 = smallPerk("disc_o2", 8, -68, PerkAttributeTypesAS.ATTR_TYPE_STARLIGHT_COLLECTION, 0.10f);
        MajorPerk discOuterMajor = majorPerk("disc_om1", 12, -74,
                PerkAttributeTypesAS.ATTR_TYPE_STARLIGHT_COLLECTION, 0.15f);
        KeyLucerna keyLucerna = new KeyLucerna(16, -80);

        PerkTree.register(discOuter1);
        PerkTree.register(discOuter2);
        PerkTree.register(discOuterMajor);
        PerkTree.register(keyLucerna);

        connect(keyDiscidia, discOuter1);
        connect(discOuter1, discOuter2);
        connect(discOuter2, discOuterMajor);
        connect(discOuterMajor, keyLucerna);

        // --- Armara → Fornax (defense → fire/elemental mastery) ---
        SmallPerk armaOuter1 = smallPerk("arma_o1", 60, -8, PerkAttributeTypesAS.ATTR_TYPE_ARMOR, 1.0f);
        SmallPerk armaOuter2 = smallPerk("arma_o2", 66, -4, PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST, 0.10f);
        MajorPerk armaOuterMajor = majorPerk("arma_om1", 72, 0,
                PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST, 0.20f);
        KeyFornax keyFornax = new KeyFornax(78, 4);

        PerkTree.register(armaOuter1);
        PerkTree.register(armaOuter2);
        PerkTree.register(armaOuterMajor);
        PerkTree.register(keyFornax);

        connect(keyArmara, armaOuter1);
        connect(armaOuter1, armaOuter2);
        connect(armaOuter2, armaOuterMajor);
        connect(armaOuterMajor, keyFornax);

        // --- Vicio → Octans (speed → aquatic mastery) ---
        SmallPerk viciOuter1 = smallPerk("vici_o1", 24, 56, PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED, 0.03f);
        SmallPerk viciOuter2 = smallPerk("vici_o2", 28, 62, PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL, 0.03f);
        MajorPerk viciOuterMajor = majorPerk("vici_om1", 32, 68,
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED, 0.05f);
        KeyOctans keyOctans = new KeyOctans(36, 74);

        PerkTree.register(viciOuter1);
        PerkTree.register(viciOuter2);
        PerkTree.register(viciOuterMajor);
        PerkTree.register(keyOctans);

        connect(keyVicio, viciOuter1);
        connect(viciOuter1, viciOuter2);
        connect(viciOuter2, viciOuterMajor);
        connect(viciOuterMajor, keyOctans);

        // --- Aevitas → Bootes (life → farming/husbandry mastery) ---
        SmallPerk aevOuter1 = smallPerk("aev_o1", -24, 56, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 1.0f);
        SmallPerk aevOuter2 = smallPerk("aev_o2", -28, 62, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.10f);
        MajorPerk aevOuterMajor = majorPerk("aev_om1", -32, 68,
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.15f);
        KeyBootes keyBootes = new KeyBootes(-36, 74);

        PerkTree.register(aevOuter1);
        PerkTree.register(aevOuter2);
        PerkTree.register(aevOuterMajor);
        PerkTree.register(keyBootes);

        connect(keyAevitas, aevOuter1);
        connect(aevOuter1, aevOuter2);
        connect(aevOuter2, aevOuterMajor);
        connect(aevOuterMajor, keyBootes);

        // --- Evorsio → Mineralis (breaking → deep mining mastery) ---
        SmallPerk evorOuter1 = smallPerk("evor_o1", -60, -8, PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.08f);
        SmallPerk evorOuter2 = smallPerk("evor_o2", -66, -4, PerkAttributeTypesAS.ATTR_TYPE_REACH, 1.0f);
        MajorPerk evorOuterMajor = majorPerk("evor_om1", -72, 0,
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.15f);
        KeyMineralis keyMineralis = new KeyMineralis(-78, 4);

        PerkTree.register(evorOuter1);
        PerkTree.register(evorOuter2);
        PerkTree.register(evorOuterMajor);
        PerkTree.register(keyMineralis);

        connect(keyEvorsio, evorOuter1);
        connect(evorOuter1, evorOuter2);
        connect(evorOuter2, evorOuterMajor);
        connect(evorOuterMajor, keyMineralis);

        // --- Horologium (rare) — accessible from Lucerna + Mineralis outer ring ---
        SmallPerk horoPath1 = smallPerk("horo_p1", -30, -80, PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT, 0.05f);
        SmallPerk horoPath2 = smallPerk("horo_p2", -16, -84, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.05f);
        MajorPerk horoMajor = majorPerk("horo_m1", 0, -88,
                PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT, 0.10f);
        KeyHorologium keyHorologium = new KeyHorologium(0, -96);

        PerkTree.register(horoPath1);
        PerkTree.register(horoPath2);
        PerkTree.register(horoMajor);
        PerkTree.register(keyHorologium);

        // Horologium connects from both Lucerna and Mineralis paths
        connect(keyLucerna, horoPath2);
        connect(keyMineralis, horoPath1);
        connect(horoPath1, horoMajor);
        connect(horoPath2, horoMajor);
        connect(horoMajor, keyHorologium);

        // ======================================================================
        // Minor constellation branches (rare key perks)
        // ======================================================================

        // --- Pelotrio (revive) — accessible from Aevitas outer branch ---
        SmallPerk peloPath1 = smallPerk("pelo_p1", -40, 60, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 1.0f);
        SmallPerk peloPath2 = smallPerk("pelo_p2", -44, 66, PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL, 0.03f);
        KeyPelotrio keyPelotrio = new KeyPelotrio(-48, 72);

        PerkTree.register(peloPath1);
        PerkTree.register(peloPath2);
        PerkTree.register(keyPelotrio);

        connect(keyBootes, peloPath1);
        connect(peloPath1, peloPath2);
        connect(peloPath2, keyPelotrio);

        // --- Alcara (nighttime regen + perk amplifier) — between Lucerna and Fornax ---
        SmallPerk alcaPath1 = smallPerk("alca_p1", 40, -60, PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT, 0.05f);
        SmallPerk alcaPath2 = smallPerk("alca_p2", 48, -54, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.5f);
        KeyAlcara keyAlcara = new KeyAlcara(56, -48);

        PerkTree.register(alcaPath1);
        PerkTree.register(alcaPath2);
        PerkTree.register(keyAlcara);

        connect(keyLucerna, alcaPath1);
        connect(alcaPath1, alcaPath2);
        connect(alcaPath2, keyAlcara);

        // --- Gelu (frost defense) — between Fornax and Octans outer ring ---
        SmallPerk geluPath1 = smallPerk("gelu_p1", 64, 24, PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST, 0.10f);
        SmallPerk geluPath2 = smallPerk("gelu_p2", 68, 32, PerkAttributeTypesAS.ATTR_TYPE_ARMOR, 1.0f);
        KeyGelu keyGelu = new KeyGelu(72, 40);

        PerkTree.register(geluPath1);
        PerkTree.register(geluPath2);
        PerkTree.register(keyGelu);

        connect(keyFornax, geluPath1);
        connect(geluPath1, geluPath2);
        connect(geluPath2, keyGelu);

        // --- Ulteria (loot/experience) — between Bootes and Mineralis outer ring ---
        SmallPerk ultePathA = smallPerk("ulte_p1", -64, 24, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.10f);
        SmallPerk ultePathB = smallPerk("ulte_p2", -68, 32, PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.05f);
        KeyUlteria keyUlteria = new KeyUlteria(-72, 40);

        PerkTree.register(ultePathA);
        PerkTree.register(ultePathB);
        PerkTree.register(keyUlteria);

        connect(keyBootes, ultePathA);
        connect(ultePathA, ultePathB);
        connect(ultePathB, keyUlteria);

        // --- Vorux (berserker) — between Discidia key and Horologium ---
        SmallPerk voruxPath1 = smallPerk("vorux_p1", -10, -90, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.05f);
        SmallPerk voruxPath2 = smallPerk("vorux_p2", -20, -94, PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE, 0.05f);
        KeyVorux keyVorux = new KeyVorux(-30, -98);

        PerkTree.register(voruxPath1);
        PerkTree.register(voruxPath2);
        PerkTree.register(keyVorux);

        connect(keyHorologium, voruxPath1);
        connect(voruxPath1, voruxPath2);
        connect(voruxPath2, keyVorux);

        // ======================================================================
        // Gem Socket perks (one per main branch + central)
        // ======================================================================

        GemSocketPerk gemDiscidia = gemSocketPerk("gem_discidia", 8, -48);
        PerkTree.register(gemDiscidia);
        connect(discMajor, gemDiscidia);

        GemSocketPerk gemArmara = gemSocketPerk("gem_armara", 50, -18);
        PerkTree.register(gemArmara);
        connect(armaMajor, gemArmara);

        GemSocketPerk gemVicio = gemSocketPerk("gem_vicio", 26, 44);
        PerkTree.register(gemVicio);
        connect(viciMajor, gemVicio);

        GemSocketPerk gemAevitas = gemSocketPerk("gem_aevitas", -26, 44);
        PerkTree.register(gemAevitas);
        connect(aevMajor, gemAevitas);

        GemSocketPerk gemEvorsio = gemSocketPerk("gem_evorsio", -50, -18);
        PerkTree.register(gemEvorsio);
        connect(evorMajor, gemEvorsio);

        // Central gem socket accessible from starlight focus
        GemSocketPerk gemCentral = gemSocketPerk("gem_central", 0, -14);
        PerkTree.register(gemCentral);
        connect(focusCombat, gemCentral);

        // ======================================================================
        // Additional focus perks (one per non-covered constellation)
        // ======================================================================

        // Defensive focus (Armara constellation)
        FocusPerk focusDefense = new FocusPerk(AstralSorcery.key("focus_defense"), 8, 4, AstralSorcery.key("armara"));
        focusDefense.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        focusDefense.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST.getKey(),
                ModifierType.ADDITION, 0.20f));
        PerkTree.register(focusDefense);
        connect(focusStarlight, focusDefense);

        // Speed focus (Vicio constellation)
        FocusPerk focusSpeed = new FocusPerk(AstralSorcery.key("focus_speed"), -8, 4, AstralSorcery.key("vicio"));
        focusSpeed.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
        focusSpeed.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_REACH.getKey(),
                ModifierType.ADDITION, 1.0f));
        PerkTree.register(focusSpeed);
        connect(focusStarlight, focusSpeed);

        // Life focus (Aevitas constellation)
        FocusPerk focusLife = new FocusPerk(AstralSorcery.key("focus_life"), 0, 8, AstralSorcery.key("aevitas"));
        focusLife.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        focusLife.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL.getKey(),
                ModifierType.ADDITION, 0.05f));
        PerkTree.register(focusLife);
        connect(focusStarlight, focusLife);

        // Mining focus (Evorsio constellation)
        FocusPerk focusMining = new FocusPerk(AstralSorcery.key("focus_mining"), -8, -8, AstralSorcery.key("evorsio"));
        focusMining.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        focusMining.addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
        PerkTree.register(focusMining);
        connect(focusStarlight, focusMining);

        // ======================================================================
        // Outer ring cross-connections (linking outer key branches)
        // ======================================================================

        // Bridge between Fornax and Octans outer branches
        SmallPerk outerBridgeFO = smallPerk("outer_fo", 56, 38,
                PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST, 0.05f);
        PerkTree.register(outerBridgeFO);
        connect(armaOuterMajor, outerBridgeFO);
        connect(viciOuterMajor, outerBridgeFO);

        // Bridge between Octans and Bootes outer branches
        SmallPerk outerBridgeOB = smallPerk("outer_ob", 0, 72,
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL, 0.02f);
        PerkTree.register(outerBridgeOB);
        connect(viciOuterMajor, outerBridgeOB);
        connect(aevOuterMajor, outerBridgeOB);

        // Bridge between Bootes and Mineralis outer branches
        SmallPerk outerBridgeBM = smallPerk("outer_bm", -56, 38,
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.05f);
        PerkTree.register(outerBridgeBM);
        connect(aevOuterMajor, outerBridgeBM);
        connect(evorOuterMajor, outerBridgeBM);

        // Bridge between Mineralis and Lucerna outer branches
        SmallPerk outerBridgeML = smallPerk("outer_ml", -38, -72,
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.05f);
        PerkTree.register(outerBridgeML);
        connect(evorOuterMajor, outerBridgeML);
        connect(discOuterMajor, outerBridgeML);

        // Bridge between Lucerna and Fornax outer branches
        SmallPerk outerBridgeLF = smallPerk("outer_lf", 48, -44,
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.03f);
        PerkTree.register(outerBridgeLF);
        connect(discOuterMajor, outerBridgeLF);
        connect(armaOuterMajor, outerBridgeLF);

        // ======================================================================
        // Extended key perk branches: the 29 new gameplay-effect perks
        // Each gets a short 2-small-perk approach path off a thematically
        // related existing key perk.
        // ======================================================================

        // --- Combat branch off KeyDiscidia ---
        SmallPerk discExt1 = smallPerk("disc_ext1", -4, -60, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.3f);
        SmallPerk discExt2 = smallPerk("disc_ext2", -8, -66, PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE, 0.03f);
        KeyBleed keyBleed = new KeyBleed(-12, -72);
        PerkTree.register(discExt1); PerkTree.register(discExt2); PerkTree.register(keyBleed);
        connect(keyDiscidia, discExt1); connect(discExt1, discExt2); connect(discExt2, keyBleed);

        SmallPerk discExt3 = smallPerk("disc_ext3", 4, -60, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.03f);
        SmallPerk discExt4 = smallPerk("disc_ext4", 8, -66, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.3f);
        KeyRampage keyRampage = new KeyRampage(12, -72);
        PerkTree.register(discExt3); PerkTree.register(discExt4); PerkTree.register(keyRampage);
        connect(keyDiscidia, discExt3); connect(discExt3, discExt4); connect(discExt4, keyRampage);

        SmallPerk discExt5 = smallPerk("disc_ext5", -4, -74, PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER, 0.05f);
        KeyCullingAttack keyCulling = new KeyCullingAttack(-8, -80);
        PerkTree.register(discExt5); PerkTree.register(keyCulling);
        connect(keyBleed, discExt5); connect(discExt5, keyCulling);

        SmallPerk discExt6 = smallPerk("disc_ext6", 4, -74, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.5f);
        KeyDamageArmor keyDamageArmor = new KeyDamageArmor(8, -80);
        PerkTree.register(discExt6); PerkTree.register(keyDamageArmor);
        connect(keyRampage, discExt6); connect(discExt6, keyDamageArmor);

        SmallPerk discExt7 = smallPerk("disc_ext7", 0, -86, PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE, 0.04f);
        KeyLightningArc keyLightningArc = new KeyLightningArc(0, -92);
        PerkTree.register(discExt7); PerkTree.register(keyLightningArc);
        connect(keyCulling, discExt7); connect(keyDamageArmor, discExt7); connect(discExt7, keyLightningArc);

        SmallPerk discExt8 = smallPerk("disc_ext8", -14, -78, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.4f);
        KeyAreaOfEffect keyAreaOfEffect = new KeyAreaOfEffect(-18, -84);
        PerkTree.register(discExt8); PerkTree.register(keyAreaOfEffect);
        connect(keyCulling, discExt8); connect(discExt8, keyAreaOfEffect);

        SmallPerk discExt9 = smallPerk("disc_ext9", -20, -66, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.04f);
        KeyNoArmor keyNoArmor = new KeyNoArmor(-24, -72);
        PerkTree.register(discExt9); PerkTree.register(keyNoArmor);
        connect(keyDiscidia, discExt9); connect(discExt9, keyNoArmor);

        SmallPerk discExt10 = smallPerk("disc_ext10", 20, -66, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.3f);
        KeyDamageEffects keyDamageEffects = new KeyDamageEffects(24, -72);
        PerkTree.register(discExt10); PerkTree.register(keyDamageEffects);
        connect(keyDiscidia, discExt10); connect(discExt10, keyDamageEffects);

        // Projectile off Lucerna (ranged/vision theme)
        SmallPerk lucExt1 = smallPerk("luc_ext1", 20, -84, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.03f);
        KeyProjectileDistance keyProjDist = new KeyProjectileDistance(24, -90);
        PerkTree.register(lucExt1); PerkTree.register(keyProjDist);
        connect(keyLucerna, lucExt1); connect(lucExt1, keyProjDist);

        SmallPerk lucExt2 = smallPerk("luc_ext2", 26, -84, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.3f);
        KeyProjectileProximity keyProjProx = new KeyProjectileProximity(32, -90);
        PerkTree.register(lucExt2); PerkTree.register(keyProjProx);
        connect(keyLucerna, lucExt2); connect(lucExt2, keyProjProx);

        // --- Defense/utility off KeyArmara ---
        SmallPerk armaExt1 = smallPerk("arma_ext1", 58, -16, PerkAttributeTypesAS.ATTR_TYPE_ARMOR, 0.5f);
        SmallPerk armaExt2 = smallPerk("arma_ext2", 64, -20, PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST, 0.05f);
        KeyNoKnockback keyNoKnockback = new KeyNoKnockback(70, -24);
        PerkTree.register(armaExt1); PerkTree.register(armaExt2); PerkTree.register(keyNoKnockback);
        connect(keyArmara, armaExt1); connect(armaExt1, armaExt2); connect(armaExt2, keyNoKnockback);

        SmallPerk armaExt3 = smallPerk("arma_ext3", 58, -6, PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS, 0.5f);
        KeyDisarm keyDisarm = new KeyDisarm(64, -2);
        PerkTree.register(armaExt3); PerkTree.register(keyDisarm);
        connect(keyArmara, armaExt3); connect(armaExt3, keyDisarm);

        // --- Speed/mobility off KeyVicio ---
        SmallPerk viciExt1 = smallPerk("vici_ext1", 28, 54, PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED, 0.03f);
        KeyStepAssist keyStepAssist = new KeyStepAssist(36, 60);
        PerkTree.register(viciExt1); PerkTree.register(keyStepAssist);
        connect(keyVicio, viciExt1); connect(viciExt1, keyStepAssist);

        SmallPerk viciExt2 = smallPerk("vici_ext2", 14, 54, PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED, 0.02f);
        KeyMantleFlight keyMantleFlight = new KeyMantleFlight(8, 60);
        PerkTree.register(viciExt2); PerkTree.register(keyMantleFlight);
        connect(keyVicio, viciExt2); connect(viciExt2, keyMantleFlight);

        // --- Life/sustain off KeyAevitas ---
        SmallPerk aevExt1 = smallPerk("aev_ext1", -28, 54, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 0.5f);
        KeyMending keyMending = new KeyMending(-36, 60);
        PerkTree.register(aevExt1); PerkTree.register(keyMending);
        connect(keyAevitas, aevExt1); connect(aevExt1, keyMending);

        SmallPerk aevExt2 = smallPerk("aev_ext2", -14, 54, PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL, 0.02f);
        KeyCleanseBadPotions keyCleanse = new KeyCleanseBadPotions(-8, 60);
        PerkTree.register(aevExt2); PerkTree.register(keyCleanse);
        connect(keyAevitas, aevExt2); connect(aevExt2, keyCleanse);

        SmallPerk aevExt3 = smallPerk("aev_ext3", -20, 54, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 1.0f);
        KeyReducedFood keyReducedFood = new KeyReducedFood(-16, 62);
        PerkTree.register(aevExt3); PerkTree.register(keyReducedFood);
        connect(keyAevitas, aevExt3); connect(aevExt3, keyReducedFood);

        // --- Mining/tools off KeyEvorsio ---
        SmallPerk evorExt1 = smallPerk("evor_ext1", -58, -16, PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.05f);
        KeyDigTypes keyDigTypes = new KeyDigTypes(-66, -20);
        PerkTree.register(evorExt1); PerkTree.register(keyDigTypes);
        connect(keyEvorsio, evorExt1); connect(evorExt1, keyDigTypes);

        SmallPerk evorExt2 = smallPerk("evor_ext2", -58, -6, PerkAttributeTypesAS.ATTR_TYPE_REACH, 0.5f);
        KeyEntityReach keyEntityReach = new KeyEntityReach(-66, -2);
        PerkTree.register(evorExt2); PerkTree.register(keyEntityReach);
        connect(keyEvorsio, evorExt2); connect(evorExt2, keyEntityReach);

        // --- Nature off KeyBootes ---
        SmallPerk booExt1 = smallPerk("boo_ext1", -40, 78, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.05f);
        KeyGrowables keyGrowables = new KeyGrowables(-48, 84);
        PerkTree.register(booExt1); PerkTree.register(keyGrowables);
        connect(keyBootes, booExt1); connect(booExt1, keyGrowables);

        SmallPerk booExt2 = smallPerk("boo_ext2", -28, 78, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.05f);
        KeyTreeConnector keyTreeConn = new KeyTreeConnector(-20, 84);
        PerkTree.register(booExt2); PerkTree.register(keyTreeConn);
        connect(keyBootes, booExt2); connect(booExt2, keyTreeConn);

        SmallPerk booExt3 = smallPerk("boo_ext3", -34, 80, PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL, 0.02f);
        KeyMagnetDrops keyMagnetDrops = new KeyMagnetDrops(-40, 88);
        PerkTree.register(booExt3); PerkTree.register(keyMagnetDrops);
        connect(keyBootes, booExt3); connect(booExt3, keyMagnetDrops);

        // --- Mining spoils off KeyMineralis ---
        SmallPerk minExt1 = smallPerk("min_ext1", -82, 8, PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED, 0.05f);
        KeyStoneEnrichment keyStoneEnrich = new KeyStoneEnrichment(-88, 12);
        PerkTree.register(minExt1); PerkTree.register(keyStoneEnrich);
        connect(keyMineralis, minExt1); connect(minExt1, keyStoneEnrich);

        SmallPerk minExt2 = smallPerk("min_ext2", -82, -2, PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE, 0.05f);
        KeyVoidTrash keyVoidTrash = new KeyVoidTrash(-88, -6);
        PerkTree.register(minExt2); PerkTree.register(keyVoidTrash);
        connect(keyMineralis, minExt2); connect(minExt2, keyVoidTrash);

        // --- Death-cheating off KeyPelotrio (survival theme) ---
        SmallPerk peloExt1 = smallPerk("pelo_ext1", -52, 76, PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH, 0.5f);
        KeyCheatDeath keyCheatDeath = new KeyCheatDeath(-56, 82);
        PerkTree.register(peloExt1); PerkTree.register(keyCheatDeath);
        connect(keyPelotrio, peloExt1); connect(peloExt1, keyCheatDeath);

        // --- Low-health rampage off KeyVorux ---
        SmallPerk voruxExt1 = smallPerk("vorux_ext1", -34, -102, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE, 0.3f);
        KeyLastBreath keyLastBreath = new KeyLastBreath(-40, -106);
        PerkTree.register(voruxExt1); PerkTree.register(keyLastBreath);
        connect(keyVorux, voruxExt1); connect(voruxExt1, keyLastBreath);

        // --- Utility off KeyAlcara (enchantment/starlight theme) ---
        SmallPerk alcaExt1 = smallPerk("alca_ext1", 60, -52, PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT, 0.05f);
        KeyAddEnchantment keyAddEnch = new KeyAddEnchantment(64, -58);
        PerkTree.register(alcaExt1); PerkTree.register(keyAddEnch);
        connect(keyAlcara, alcaExt1); connect(alcaExt1, keyAddEnch);

        // --- Misc off KeyHorologium (time/charge theme) ---
        SmallPerk horoExt1 = smallPerk("horo_ext1", 4, -100, PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT, 0.05f);
        KeySpawnLights keySpawnLights = new KeySpawnLights(8, -106);
        PerkTree.register(horoExt1); PerkTree.register(keySpawnLights);
        connect(keyHorologium, horoExt1); connect(horoExt1, keySpawnLights);

        SmallPerk horoExt2 = smallPerk("horo_ext2", -4, -100, PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED, 0.03f);
        KeyChargeBalancing keyChargeBalance = new KeyChargeBalancing(-8, -106);
        PerkTree.register(horoExt2); PerkTree.register(keyChargeBalance);
        connect(keyHorologium, horoExt2); connect(horoExt2, keyChargeBalance);
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

    @Nonnull
    private static GemSocketPerk gemSocketPerk(@Nonnull String name, int x, int y) {
        return new GemSocketPerk(AstralSorcery.key(name), x, y);
    }

    private static void connect(@Nonnull AbstractPerk a,
                                 @Nonnull AbstractPerk b) {
        PerkTree.connect(a.getKey(), b.getKey());
    }
}
