package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.LightNetworkBuffer;
import hellfirepvp.astralsorcery.common.data.world.RockCrystalBuffer;
import hellfirepvp.astralsorcery.common.data.world.StorageNetworkBuffer;
import hellfirepvp.astralsorcery.common.data.world.base.WorldCacheDomain;

/**
 * World-persistent data domain and save key constants.
 *
 * <p>Populated by {@link hellfirepvp.astralsorcery.common.registry.RegistryData#init()}
 * during {@code FMLCommonSetupEvent}. Must be called before any world data is accessed.</p>
 *
 * <p>1.16 used ObserverLib's WorldCacheDomain. The port provides an equivalent
 * in {@link WorldCacheDomain} that wraps Minecraft's {@code SavedData} system.</p>
 */
public final class DataAS {

    private DataAS() {}

    /** The Astral Sorcery world cache domain. All AS data keys belong to this domain. */
    public static WorldCacheDomain DOMAIN_AS;

    /** Per-world starlight transmission network state. Full impl in Phase C. */
    public static WorldCacheDomain.SaveKey<LightNetworkBuffer> KEY_STARLIGHT_NETWORK;

    /** Per-world storage network (chalice fluid links). */
    public static WorldCacheDomain.SaveKey<StorageNetworkBuffer> KEY_STORAGE_NETWORK;

    /** Per-world rock crystal cluster positions for shrine seeding. */
    public static WorldCacheDomain.SaveKey<RockCrystalBuffer> KEY_ROCK_CRYSTAL_BUFFER;

    public static void init() {
        DOMAIN_AS = new WorldCacheDomain(AstralSorcery.key("as_domain"));

        KEY_STARLIGHT_NETWORK = DOMAIN_AS.register(
                new WorldCacheDomain.SaveKey<>("starlight_network"),
                k -> new LightNetworkBuffer(k)
        );

        KEY_STORAGE_NETWORK = DOMAIN_AS.register(
                new WorldCacheDomain.SaveKey<>("storage_network"),
                k -> new StorageNetworkBuffer(k)
        );

        KEY_ROCK_CRYSTAL_BUFFER = DOMAIN_AS.register(
                new WorldCacheDomain.SaveKey<>("rock_crystal_buffer"),
                k -> new RockCrystalBuffer(k)
        );
    }
}
