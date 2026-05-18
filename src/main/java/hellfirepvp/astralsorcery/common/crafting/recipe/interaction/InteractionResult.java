/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.interaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Abstract result produced when a {@link hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction}
 * recipe completes.
 *
 * <p>1.16 → 1.20: World → Level, PacketBuffer → FriendlyByteBuf; JEI methods removed.</p>
 */
public abstract class InteractionResult {

    private final ResourceLocation id;

    protected InteractionResult(ResourceLocation id) {
        this.id = id;
    }

    public final ResourceLocation getId() {
        return id;
    }

    public abstract void doResult(Level level, Vector3 at);

    public abstract void read(JsonObject json) throws JsonParseException;

    public abstract void write(JsonObject json);

    public abstract void read(FriendlyByteBuf buf);

    public abstract void write(FriendlyByteBuf buf);
}
