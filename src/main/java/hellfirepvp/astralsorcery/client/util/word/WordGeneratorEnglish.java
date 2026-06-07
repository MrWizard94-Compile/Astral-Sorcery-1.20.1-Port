/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.util.word;

import java.util.Random;

/**
 * Generates pronounceable English-like words by alternating vowels and consonants.
 */
public class WordGeneratorEnglish extends RandomWordGenerator {

    private static final String[] VOWELS = { "a", "e", "i", "o", "u" };
    private static final String[] CONS   = { "b", "c", "d", "f", "g", "h", "j", "k", "l", "m",
                                              "n", "p", "ph", "qu", "r", "s", "t", "v", "w",
                                              "x", "y", "z", "tt", "ch", "sh" };

    @Override
    public String generateWord(long seed, int length) {
        Random rng = new Random(seed);
        boolean useVowel = rng.nextFloat() > 0.8f;
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String[] pool = useVowel ? VOWELS : CONS;
            word.append(pool[rng.nextInt(pool.length)]);
            useVowel = !useVowel;
        }
        return word.toString();
    }
}
