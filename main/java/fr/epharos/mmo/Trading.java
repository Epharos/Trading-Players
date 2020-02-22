package fr.epharos.mmo;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(Trading._MODID)
public class Trading
{
	public static final String _MODID = "trade";
	public static final Logger _LOGGER = LogManager.getLogger(_MODID); //Output logger
	public static final String _PROTOCOL_VERSION = String.valueOf(1);
	public static final SimpleChannel _CHANNEL = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Trading._MODID, "trading"))
			.networkProtocolVersion(() -> Trading._PROTOCOL_VERSION)
			.clientAcceptedVersions(Trading._PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(Trading._PROTOCOL_VERSION::equals)
			.simpleChannel();
	
	public static ContainerType<?> tradingContainer;
	public static Proxy proxy; 

    public Trading() 
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);  
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        _LOGGER.info("Common Setup");
        proxy = DistExecutor.runForDist(() -> ClientProxy::new , () -> ServerProxy::new );
        Trading.registerNetworkPackets();
    }

    private void clientSetup(final FMLClientSetupEvent event) 
    {
    	_LOGGER.info("Client Setup");
    }
    
    private void serverSetup(final FMLDedicatedServerSetupEvent event)
    {
    	_LOGGER.info("Server Setup");
    }
    
    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event)
    {
    	CommandTrading.register(event.getCommandDispatcher());
    	_LOGGER.info("Commands have been registered !");
    }
    
    @SubscribeEvent
    public void serverTick(ServerTickEvent event)
    {   	
    	DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
	    	for(Iterator<AskedTrades> it = AskedTrades.pendings.iterator() ; it.hasNext() ; )
	    	{
	    		it.next().timeIsRunningOut();
	    	}
	    	
	    	AskedTrades.checkPendings();
    	});	
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents 
    {
        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
        	event.getRegistry().registerAll(
        				tradingContainer = IForgeContainerType.create(ContainerTrading::new).setRegistryName(new ResourceLocation(Trading._MODID, "container_trading"))
        			);
        	
//        	for(Iterator<ContainerType<?>> it = event.getRegistry().iterator() ; it.hasNext() ;)
//        		_LOGGER.debug("Container enregistrés :" +it.next().getRegistryName());
        	
        	_LOGGER.info("Containers have been registered !");
        }
    }
    
    /**
     * Opens Trading Screen to the player
     * @param p
     * 		The player to open the trading screen to
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public static void openTradingScreen(ServerPlayerEntity p)
    {
    	NetworkHooks.openGui(p, new INamedContainerProvider() 
    		{
				public Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity player) 
				{
					return new ContainerTrading(windowID, player);
				}
	
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Trade");
				}
    		});
    }
    
    /**
     * Registers the packets used by the mod
     */
    public static void registerNetworkPackets()
    {
    	_CHANNEL.messageBuilder(PacketUpdateTradingInventory.class, 0)
    		.encoder(PacketUpdateTradingInventory::writePacketData)
    		.decoder(PacketUpdateTradingInventory::readPacketData)
    		.consumer(PacketUpdateTradingInventory::handlePacket)
    		.add();
    	
    	_CHANNEL.messageBuilder(PacketTraderName.class, 1)
			.encoder(PacketTraderName::writePacketData)
			.decoder(PacketTraderName::readPacketData)
			.consumer(PacketTraderName::handlePacket)
			.add();
    	
    	_LOGGER.info("Packets have been registered !");
    }
}
