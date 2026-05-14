package hellfirepvp.astralsorcery.common.base;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.versions.forge.ForgeVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum Mods {

    MINECRAFT("minecraft", true),
    FORGE(ForgeVersion.MOD_ID, true),
    ASTRAL_SORCERY(AstralSorcery.MODID, true),
    DRACONIC_EVOLUTION("draconicevolution"),
    CURIOS("curios"),
    JEI("jei"),
    BOTANIA("botania"),
    CRAFTTWEAKER("crafttweaker");

    private final String modid;
    private final boolean loaded;

    Mods(@Nonnull String modid) {
        this(modid, ModList.get().isLoaded(modid));
    }

    Mods(@Nonnull String modid, boolean loaded) {
        this.modid = modid;
        this.loaded = loaded;
    }

    @Nonnull
    public String getModId() {
        return this.modid;
    }

    public boolean isPresent() {
        return loaded;
    }

    public void executeIfPresent(@Nonnull Supplier<Runnable> execSupplier) {
        if (this.isPresent()) {
            execSupplier.get().run();
        }
    }

    @Nullable
    public <T> T getIfPresent(@Nonnull Supplier<Supplier<T>> supplierSupplier) {
        if (this.isPresent()) {
            return supplierSupplier.get().get();
        }
        return null;
    }

    @Nonnull
    public ResourceLocation key(@Nonnull String path) {
        return new ResourceLocation(this.getModId(), path);
    }

    public void sendIMC(@Nonnull String method, @Nonnull Supplier<?> thing) {
        if (this.isPresent()) {
            InterModComms.sendTo(AstralSorcery.MODID, this.getModId(), method, thing);
        }
    }

    @Nullable
    public static Mods byModId(@Nonnull String modId) {
        for (Mods mod : values()) {
            if (mod.getModId().equals(modId)) {
                return mod;
            }
        }
        return null;
    }
}
