/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Pelotrio (Weak — Spectral Tools).
 *
 * <p>When the wearer attacks a mob there is a configurable chance a spectral sword
 * spawns to fight alongside them. When the wearer breaks a log or leaf with an axe,
 * a spectral axe may spawn. When breaking any block with a pickaxe, a spectral
 * pickaxe may spawn. Each spawn costs alignment charge.</p>
 *
 * <p>1.16 → 1.20 API changes:
 * ToolType removed — axe/pickaxe detection now uses instanceof checks on the item class.
 * state.isIn(tag) → state.is(tag).
 * BlockEvent.getWorld() → BlockEvent.getLevel() (returns LevelAccessor).
 * world.addEntity() → level.addFreshEntity().</p>
 */
public class MantleEffectPelotrio extends MantleEffect {

    public static final PelotrioConfig CONFIG = new PelotrioConfig();

    public MantleEffectPelotrio() {
        super(ConstellationsAS.PELOTRIO);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(this::onAttack);
        bus.addListener(this::onBreak);
    }

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    private void onAttack(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntity();
        if (attacked.level().isClientSide()) return;
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player)) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.PELOTRIO) == null) return;
        if (rand.nextFloat() >= CONFIG.chanceSpawnSword.get().floatValue()) return;
        if (!AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerSword.get())) return;

        Level level = player.level();
        EntitySpectralTool sword = new EntitySpectralTool(EntityTypesAS.SPECTRAL_TOOL.get(), level);
        sword.setPos(player.getX(), player.getY() + 1.0, player.getZ());
        sword.setOwnerUUID(player.getUUID());
        sword.setToolItem(new ItemStack(Items.IRON_SWORD));
        if (level.addFreshEntity(sword)) {
            AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerSword.get(), false);
        }
    }

    private void onBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level level) || level.isClientSide()) return;
        Player player = event.getPlayer();
        if (ItemMantle.getEffect(player, ConstellationsAS.PELOTRIO) == null) return;

        BlockState state = event.getState();
        ItemStack mainHand = player.getMainHandItem();

        if ((state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) && mainHand.getItem() instanceof AxeItem) {
            if (rand.nextFloat() < CONFIG.chanceSpawnAxe.get().floatValue()
                    && AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerAxe.get())) {
                EntitySpectralTool axe = new EntitySpectralTool(EntityTypesAS.SPECTRAL_TOOL.get(), level);
                axe.setPos(player.getX(), player.getY() + 1.0, player.getZ());
                axe.setOwnerUUID(player.getUUID());
                axe.setToolItem(new ItemStack(Items.IRON_AXE));
                if (level.addFreshEntity(axe)) {
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerAxe.get(), false);
                }
            }
            return;
        }

        if (mainHand.getItem() instanceof PickaxeItem) {
            if (rand.nextFloat() < CONFIG.chanceSpawnPickaxe.get().floatValue()
                    && AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerPickaxe.get())) {
                EntitySpectralTool pick = new EntitySpectralTool(EntityTypesAS.SPECTRAL_TOOL.get(), level);
                pick.setPos(player.getX(), player.getY() + 1.0, player.getZ());
                pick.setOwnerUUID(player.getUUID());
                pick.setToolItem(new ItemStack(Items.IRON_PICKAXE));
                if (level.addFreshEntity(pick)) {
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerPickaxe.get(), false);
                }
            }
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class PelotrioConfig extends Config {

        public ForgeConfigSpec.DoubleValue chanceSpawnSword;
        public ForgeConfigSpec.DoubleValue chanceSpawnPickaxe;
        public ForgeConfigSpec.DoubleValue chanceSpawnAxe;
        public ForgeConfigSpec.DoubleValue speedSword;
        public ForgeConfigSpec.DoubleValue speedPickaxe;
        public ForgeConfigSpec.DoubleValue speedAxe;
        public ForgeConfigSpec.DoubleValue swordDamage;
        public ForgeConfigSpec.IntValue durationSword;
        public ForgeConfigSpec.IntValue durationPickaxe;
        public ForgeConfigSpec.IntValue durationAxe;
        public ForgeConfigSpec.IntValue ticksPerSwordAttack;
        public ForgeConfigSpec.IntValue ticksPerPickaxeBlockBreak;
        public ForgeConfigSpec.IntValue ticksPerAxeLogBreak;
        public ForgeConfigSpec.IntValue chargeCostPerSword;
        public ForgeConfigSpec.IntValue chargeCostPerPickaxe;
        public ForgeConfigSpec.IntValue chargeCostPerAxe;

        public PelotrioConfig() {
            super("pelotrio");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.pelotrio");
            chanceSpawnSword = b
                    .comment("Chance per attack to spawn a spectral sword.")
                    .defineInRange("chanceSpawnSword", 0.6, 0.0, 1.0);
            chanceSpawnPickaxe = b
                    .comment("Chance per pickaxe block break to spawn a spectral pickaxe.")
                    .defineInRange("chanceSpawnPickaxe", 0.8, 0.0, 1.0);
            chanceSpawnAxe = b
                    .comment("Chance per log/leaf break with an axe to spawn a spectral axe.")
                    .defineInRange("chanceSpawnAxe", 0.8, 0.0, 1.0);
            speedSword = b
                    .comment("Movement speed of the spectral sword (used when ToolTask is ported).")
                    .defineInRange("speedSword", 2.3, 0.5, 4.5);
            speedPickaxe = b
                    .comment("Movement speed of the spectral pickaxe (used when ToolTask is ported).")
                    .defineInRange("speedPickaxe", 1.8, 0.5, 4.5);
            speedAxe = b
                    .comment("Movement speed of the spectral axe (used when ToolTask is ported).")
                    .defineInRange("speedAxe", 1.8, 0.5, 4.5);
            swordDamage = b
                    .comment("Damage per attack of the spectral sword (used when ToolTask is ported).")
                    .defineInRange("swordDamage", 4.0, 0.1, 32.0);
            durationSword = b
                    .comment("Lifetime in ticks of a spawned spectral sword.")
                    .defineInRange("durationSword", 100, 20, 500);
            durationPickaxe = b
                    .comment("Lifetime in ticks of a spawned spectral pickaxe.")
                    .defineInRange("durationPickaxe", 100, 20, 500);
            durationAxe = b
                    .comment("Lifetime in ticks of a spawned spectral axe.")
                    .defineInRange("durationAxe", 100, 20, 500);
            ticksPerSwordAttack = b
                    .comment("Minimum ticks between sword attacks (used when ToolTask is ported).")
                    .defineInRange("ticksPerSwordAttack", 6, 1, 100);
            ticksPerPickaxeBlockBreak = b
                    .comment("Ticks per pickaxe block break (used when ToolTask is ported).")
                    .defineInRange("ticksPerPickaxeBlockBreak", 4, 1, 100);
            ticksPerAxeLogBreak = b
                    .comment("Ticks per axe log break (used when ToolTask is ported).")
                    .defineInRange("ticksPerAxeLogBreak", 2, 1, 100);
            chargeCostPerSword = b
                    .comment("Alignment charge consumed per spectral sword spawned.")
                    .defineInRange("chargeCostPerSword", 250, 0, 1000);
            chargeCostPerPickaxe = b
                    .comment("Alignment charge consumed per spectral pickaxe spawned.")
                    .defineInRange("chargeCostPerPickaxe", 250, 0, 1000);
            chargeCostPerAxe = b
                    .comment("Alignment charge consumed per spectral axe spawned.")
                    .defineInRange("chargeCostPerAxe", 250, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
