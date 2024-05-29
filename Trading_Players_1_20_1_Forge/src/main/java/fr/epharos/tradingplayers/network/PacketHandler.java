package fr.epharos.tradingplayers.network;

import fr.epharos.tradingplayers.core.TradingPlayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler 
{
	private PacketHandler() { }
	
	public static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(TradingPlayers._MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	
	public static void init()
	{
		int id = 0;
		PacketHandler.INSTANCE.registerMessage(id++, PacketUpdateTradingStateClient.class,
			PacketUpdateTradingStateClient::encodePacket,
			PacketUpdateTradingStateClient::decodePacket,
			PacketUpdateTradingStateClient::handlePacket);

		PacketHandler.INSTANCE.registerMessage(id++, PacketUpdateTradingStateServer.class,
				PacketUpdateTradingStateServer::encodePacket,
				PacketUpdateTradingStateServer::decodePacket,
				PacketUpdateTradingStateServer::handlePacket);

		PacketHandler.INSTANCE.registerMessage(id++, PacketTraderName.class,
				PacketTraderName::encodePacket,
				PacketTraderName::decodePacket,
				PacketTraderName::handlePacket);
	}
}
