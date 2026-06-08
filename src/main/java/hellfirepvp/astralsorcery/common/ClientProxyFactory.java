/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Holds the one reference to {@code ClientProxy} so that it never appears in
 * {@code AstralSorcery.class}'s constant pool.
 *
 * <p>{@code AstralSorcery.java} uses:
 * <pre>
 *   DistExecutor.safeRunForDist(() -> ClientProxyFactory::create, () -> CommonProxy::new)
 * </pre>
 * The outer lambda only references this class (not {@code ClientProxy}), so the
 * dev-mode {@code RuntimeDistCleaner} does not intercept it on a dedicated server.
 * On the server the dist cleaner strips {@link #create()} (it is {@code @OnlyIn(Dist.CLIENT)}),
 * removing the {@code ClientProxy} reference from this class's constant pool.
 * {@code safeRunForDist} never calls the client supplier on a server, so the
 * method handle {@code ClientProxyFactory::create} is never resolved.</p>
 */
public final class ClientProxyFactory {

    private ClientProxyFactory() {}

    @OnlyIn(Dist.CLIENT)
    public static CommonProxy create() {
        return new hellfirepvp.astralsorcery.client.ClientProxy();
    }
}
