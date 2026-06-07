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
 * Chinese-locale word generator that produces a trigram constellation-style name.
 * Credit to yuanjie000.
 */
public class WordGeneratorChinese extends RandomWordGenerator {

    private static final String[] A = { "乾", "震", "坎", "艮", "坤", "巽", "离", "兑" };
    private static final String[] B = { "角", "亢", "氐", "房", "心", "尾", "箕", "斗", "牛", "女",
                                         "虚", "危", "室", "壁", "奎", "娄", "胃", "昴", "毕", "觜",
                                         "参", "井", "鬼", "柳", "星", "张", "翼", "轸" };
    private static final String SUFFIX = "座";

    @Override
    public String generateWord(long seed, int length) {
        Random rng = new Random(seed);
        return A[rng.nextInt(A.length)] + B[rng.nextInt(B.length)] + SUFFIX;
    }
}
