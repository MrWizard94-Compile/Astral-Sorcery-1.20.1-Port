package hellfirepvp.astralsorcery.client.resource;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ReloadableResource {

    void invalidateAndReload();

}
