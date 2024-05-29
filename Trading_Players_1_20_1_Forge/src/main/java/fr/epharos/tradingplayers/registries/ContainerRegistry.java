package fr.epharos.tradingplayers.registries;

import fr.epharos.tradingplayers.core.TradingPlayers;
import fr.epharos.tradingplayers.screen.TradingContainer;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ContainerRegistry 
{
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister
			.create(ForgeRegistries.MENU_TYPES, TradingPlayers._MODID);
	
	public static final RegistryObject<MenuType<TradingContainer>> TRADING_CONTAINER = CONTAINERS
			.register("trading_container", () -> new MenuType<>(TradingContainer::new, FeatureFlags.DEFAULT_FLAGS));
}
