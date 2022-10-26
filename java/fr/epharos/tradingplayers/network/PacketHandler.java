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
			new ResourceLocation(TradingPlayers._MODID, "trading"), 
			() -> PacketHandler.PROTOCOL_VERSION, 
			PacketHandler.PROTOCOL_VERSION::equals, 
			PacketHandler.PROTOCOL_VERSION::equals);
	
	public static void init()
	{
		int packetID = 0;
		PacketHandler.INSTANCE.messageBuilder(PacketUpdateTradingStateClient.class, packetID++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(PacketUpdateTradingStateClient::encodePacket)
			.decoder(PacketUpdateTradingStateClient::decodePacket)
			.consumer(PacketUpdateTradingStateClient::handlePacket)
			.add();
		
		PacketHandler.INSTANCE.messageBuilder(PacketUpdateTradingStateServer.class, packetID++, NetworkDirection.PLAY_TO_SERVER)
		.encoder(PacketUpdateTradingStateServer::encodePacket)
		.decoder(PacketUpdateTradingStateServer::decodePacket)
		.consumer(PacketUpdateTradingStateServer::handlePacket)
		.add();
		
		PacketHandler.INSTANCE.messageBuilder(PacketTraderName.class, packetID++, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(PacketTraderName::encodePacket)
		.decoder(PacketTraderName::decodePacket)
		.consumer(PacketTraderName::handlePacket)
		.add();
	}
}
