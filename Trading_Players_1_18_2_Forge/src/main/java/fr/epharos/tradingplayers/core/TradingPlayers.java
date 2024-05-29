package fr.epharos.tradingplayers.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.epharos.tradingplayers.command.TradeCommand;
import fr.epharos.tradingplayers.network.PacketHandler;
import fr.epharos.tradingplayers.registries.ContainerRegistry;
import fr.epharos.tradingplayers.screen.TradingContainer;
import fr.epharos.tradingplayers.screen.TradingScreen;
import fr.epharos.tradingplayers.util.PendingTrade;
import fr.epharos.tradingplayers.util.TradingPlayersConfig;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkHooks;

@Mod(TradingPlayers._MODID)
public class TradingPlayers
{
	public static final String _MODID = "tradingplayers"; //Mod ID
	public static final Logger _LOGGER = LogManager.getLogger(_MODID); //Output logger	

    public TradingPlayers() 
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        
        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ModLoadingContext.get().registerConfig(Type.SERVER, TradingPlayersConfig.SPEC, "tradingplayers-config.toml");
        ContainerRegistry.CONTAINERS.register(bus);
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    	PacketHandler.init();
    }
    
    private void clientSetup(final FMLClientSetupEvent event) 
    {
    	MenuScreens.register(ContainerRegistry.TRADING_CONTAINER.get(), TradingScreen::new);
    }
    
    @SubscribeEvent
    public void serverTick(ServerTickEvent event)
    {   	
    	DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> PendingTrade.checkPendings());
    }
    
    @SubscribeEvent
    public void registerCommand(RegisterCommandsEvent event)
    {
    	TradeCommand.register(event.getDispatcher());
    }
    
    /**
     * Opens Trading Screen to the player
     * @param p
     * 		The player to open the trading screen to
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public static void openTradingScreen(ServerPlayer p)
    {
    	MenuProvider provider = new MenuProvider() 
    	{
			public AbstractContainerMenu createMenu(int containerID, Inventory playerInv, Player player) {
				return new TradingContainer(containerID, playerInv);
			}

			public Component getDisplayName() 
			{
				return new TranslatableComponent("trade.name");
			}
    	};
    	
    	NetworkHooks.openGui(p, provider);
    }
}