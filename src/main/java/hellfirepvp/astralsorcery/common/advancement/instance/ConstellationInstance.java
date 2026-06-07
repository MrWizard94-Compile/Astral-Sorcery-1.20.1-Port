/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConstellationInstance extends AbstractCriterionTriggerInstance {

    private boolean constellationMajor = false;
    private boolean constellationWeak  = false;
    private boolean constellationMinor = false;
    private final Set<IConstellation> constellations = new HashSet<>();

    private ConstellationInstance(@Nonnull ResourceLocation id, @Nonnull ContextAwarePredicate player) {
        super(id, player);
    }

    public static ConstellationInstance any(@Nonnull ResourceLocation type) {
        return new ConstellationInstance(type, ContextAwarePredicate.ANY);
    }

    public static ConstellationInstance anyMajor(@Nonnull ResourceLocation type) {
        ConstellationInstance i = new ConstellationInstance(type, ContextAwarePredicate.ANY);
        i.constellationMajor = true;
        return i;
    }

    public static ConstellationInstance anyWeak(@Nonnull ResourceLocation type) {
        ConstellationInstance i = new ConstellationInstance(type, ContextAwarePredicate.ANY);
        i.constellationWeak = true;
        return i;
    }

    public static ConstellationInstance anyMinor(@Nonnull ResourceLocation type) {
        ConstellationInstance i = new ConstellationInstance(type, ContextAwarePredicate.ANY);
        i.constellationMinor = true;
        return i;
    }

    public static ConstellationInstance anyOf(@Nonnull ResourceLocation type, IConstellation... cst) {
        ConstellationInstance i = new ConstellationInstance(type, ContextAwarePredicate.ANY);
        i.constellations.addAll(Arrays.asList(cst));
        return i;
    }

    @Override
    @Nonnull
    public JsonObject serializeToJson(@Nonnull SerializationContext context) {
        JsonObject out = super.serializeToJson(context);
        if (this.constellationMajor) out.addProperty("major", true);
        if (this.constellationWeak)  out.addProperty("weak",  true);
        if (this.constellationMinor) out.addProperty("minor", true);
        if (!this.constellations.isEmpty()) {
            JsonArray names = new JsonArray();
            for (IConstellation cst : this.constellations) {
                names.add(cst.getRegistryName().toString());
            }
            out.add("constellations", names);
        }
        return out;
    }

    @Nonnull
    public static ConstellationInstance deserialize(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
        ConstellationInstance instance = new ConstellationInstance(id, ContextAwarePredicate.ANY);
        instance.constellationMajor = GsonHelper.getAsBoolean(json, "major", false);
        instance.constellationWeak  = GsonHelper.getAsBoolean(json, "weak",  false);
        instance.constellationMinor = GsonHelper.getAsBoolean(json, "minor", false);
        JsonArray names = GsonHelper.getAsJsonArray(json, "constellations", null);
        for (int idx = 0; names != null && idx < names.size(); idx++) {
            String key = names.get(idx).getAsString();
            IConstellation cst = ConstellationRegistry.getConstellation(new ResourceLocation(key));
            if (cst == null) {
                throw new IllegalArgumentException(
                        "Unknown constellation '" + key + "' at constellations[" + idx + "]");
            }
            instance.constellations.add(cst);
        }
        return instance;
    }

    public boolean test(@Nonnull IConstellation discovered) {
        if (constellationMajor && !(discovered instanceof IMajorConstellation)) return false;
        if (constellationWeak  && (!(discovered instanceof IWeakConstellation) || discovered instanceof IMajorConstellation)) return false;
        if (constellationMinor && !(discovered instanceof IMinorConstellation)) return false;
        return constellations.isEmpty() || constellations.contains(discovered);
    }
}
