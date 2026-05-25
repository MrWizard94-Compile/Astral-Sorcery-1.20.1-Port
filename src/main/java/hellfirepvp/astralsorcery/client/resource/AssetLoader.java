package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Utility for building mod-namespaced resource paths and loading textures
 * from the mod's asset tree.
 *
 * <p>Texture paths follow the convention:
 * {@code assets/astralsorcery/textures/<location>/<name>.png}</p>
 *
 * <p>1.16 → 1.20: Removed WavefrontObject/OBJ model loading (handled elsewhere).
 * BindableResource now uses ResourceLocation keys directly instead of sanitised
 * string keys, so no path→key mangling is needed.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class AssetLoader {

    private AssetLoader() {}

    @OnlyIn(Dist.CLIENT)
    public static BindableResource loadTexture(TextureLocation location, String... pathSegments) {
        String name = String.join("/", pathSegments);
        if (name.endsWith(".png")) {
            name = name.substring(0, name.length() - 4);
        }
        String path = "textures/" + location.getLocation() + "/" + name + ".png";
        return new BindableResource(new ResourceLocation(AstralSorcery.MODID, path));
    }

    public interface SubLocation {
        String getLocation();
    }

    public enum TextureLocation implements SubLocation {
        ITEMS("item"),
        BLOCKS("block"),
        GUI("gui"),
        MISC("misc"),
        MODEL("model"),
        EFFECT("effect"),
        ENVIRONMENT("environment"),
        CONSTELLATION("constellation");

        private final String location;

        TextureLocation(String location) {
            this.location = location;
        }

        @Override
        public String getLocation() {
            return location;
        }
    }
}
