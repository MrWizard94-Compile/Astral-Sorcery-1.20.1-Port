package hellfirepvp.astralsorcery.mixin.accessor;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface LevelSkyDarkenAccessor {

    @Accessor("skyDarken")
    void astralsorcery$setSkyDarken(int value);
}
