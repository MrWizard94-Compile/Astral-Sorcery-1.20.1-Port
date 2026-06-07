/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.util.word;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract word generator registry. A locale-appropriate generator is chosen
 * at runtime; {@link WordGeneratorEnglish} is used as fallback.
 *
 * <p>Call {@link #init()} once during client setup, before the first
 * {@link #getGenerator()} call.</p>
 *
 * <p>1.16 → 1.20: {@code gameSettings.language} → {@code options.languageCode}.</p>
 */
@OnlyIn(Dist.CLIENT)
public abstract class RandomWordGenerator {

    private static final Map<String, RandomWordGenerator> localizedProviders = new HashMap<>();
    private static RandomWordGenerator fallback;

    @Nonnull
    public static RandomWordGenerator getGenerator() {
        if (fallback == null) {
            init();
        }
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang == null) {
            return fallback;
        }
        RandomWordGenerator gen = localizedProviders.get(lang.toLowerCase());
        return gen != null ? gen : fallback;
    }

    public abstract String generateWord(long seed, int length);

    public static void init() {
        fallback = new WordGeneratorEnglish();
        localizedProviders.put("en_us", fallback);
        localizedProviders.put("zh_cn", new WordGeneratorChinese());
    }
}
