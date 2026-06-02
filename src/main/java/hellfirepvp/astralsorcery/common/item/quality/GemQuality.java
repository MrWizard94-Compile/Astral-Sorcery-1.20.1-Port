package hellfirepvp.astralsorcery.common.item.quality;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum GemQuality {

    BROKEN  (ChatFormatting.GRAY,  0.1f),
    FLAWED  (ChatFormatting.GRAY,  0.35f),
    MUNDANE (ChatFormatting.WHITE, 0.5f),
    CLEAR   (ChatFormatting.AQUA,  0.6f),
    FACETED (ChatFormatting.AQUA,  0.7f),
    GLEAMING(ChatFormatting.GOLD,  0.8f),
    FLAWLESS(ChatFormatting.GOLD,  1.0f);

    private final ChatFormatting color;
    private final float degree;

    GemQuality(ChatFormatting color, float degree) {
        this.color = color;
        this.degree = degree;
    }

    public float getDegree() {
        return degree;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("item.astralsorcery.gem_quality." + name().toLowerCase(Locale.ROOT))
                .withStyle(color);
    }
}
