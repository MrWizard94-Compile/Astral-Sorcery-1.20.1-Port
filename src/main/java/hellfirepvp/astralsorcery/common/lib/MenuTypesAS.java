package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.container.ContainerAltarAttunement;
import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerAltarDiscovery;
import hellfirepvp.astralsorcery.common.container.ContainerAltarRadiance;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for all Astral Sorcery menu (container) types.
 *
 * <p>1.16 -> 1.20 changes:
 * ContainerType -> MenuType,
 * IForgeContainerType -> IForgeMenuType</p>
 */
public class MenuTypesAS {

    private MenuTypesAS() {}

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<MenuType<ContainerAltarDiscovery>> ALTAR_DISCOVERY =
            MENU_TYPES.register("altar_discovery",
                    () -> IForgeMenuType.create(ContainerAltarDiscovery::fromNetwork));

    public static final RegistryObject<MenuType<ContainerAltarAttunement>> ALTAR_ATTUNEMENT =
            MENU_TYPES.register("altar_attunement",
                    () -> IForgeMenuType.create(ContainerAltarAttunement::fromNetwork));

    public static final RegistryObject<MenuType<ContainerAltarConstellation>> ALTAR_CONSTELLATION =
            MENU_TYPES.register("altar_constellation",
                    () -> IForgeMenuType.create(ContainerAltarConstellation::fromNetwork));

    public static final RegistryObject<MenuType<ContainerAltarRadiance>> ALTAR_RADIANCE =
            MENU_TYPES.register("altar_radiance",
                    () -> IForgeMenuType.create(ContainerAltarRadiance::fromNetwork));

    // TODO: TOME, OBSERVATORY menus when those containers are ported
}
