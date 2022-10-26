package fr.epharos.tradingplayers.registries;

import fr.epharos.tradingplayers.core.TradingPlayers;
import fr.epharos.tradingplayers.screen.TradingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ContainerRegistry 
{
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister
			.create(ForgeRegistries.CONTAINERS, TradingPlayers._MODID);
	
	public static final RegistryObject<MenuType<TradingContainer>> TRADING_CONTAINER = CONTAINERS
			.register("trading_container", () -> new MenuType<>(TradingContainer::new));
}
