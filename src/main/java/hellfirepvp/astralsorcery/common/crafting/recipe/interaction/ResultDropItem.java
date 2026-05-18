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
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Interaction result that drops an item at the reaction point.
 *
 * <p>1.16 → 1.20: World → Level, PacketBuffer → FriendlyByteBuf; JEI methods removed.</p>
 */
public class ResultDropItem extends InteractionResult {

    private ItemStack output = ItemStack.EMPTY;

    ResultDropItem() {
        super(InteractionResultRegistry.ID_DROP_ITEM);
    }

    public static ResultDropItem dropItem(ItemStack output) {
        ResultDropItem drop = new ResultDropItem();
        drop.output = output.copy();
        return drop;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    @Override
    public void doResult(Level level, Vector3 at) {
        ItemUtils.dropItemNaturally(level, at.getX(), at.getY(), at.getZ(), this.output.copy());
    }

    @Override
    public void read(JsonObject json) throws JsonParseException {
        this.output = JsonHelper.getItemStack(json, "output");
        if (this.output.isEmpty()) {
            throw new JsonParseException("Output stack must not be empty!");
        }
    }

    @Override
    public void write(JsonObject json) {
        json.add("output", JsonHelper.serializeItemStack(this.output));
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.output = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.output);
    }
}
