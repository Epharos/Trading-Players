package fr.epharos.tradingplayers.network;

import fr.epharos.tradingplayers.core.TradingPlayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler 
{
	private PacketHandler() { }
	
	public static final int PROTOCOL_VERSION = 1;

	public static final SimpleChannel INSTANCE = ChannelBuilder.named(
			new ResourceLocation(TradingPlayers._MODID, "trading"))
			.serverAcceptedVersions((status, version) -> true)
			.clientAcceptedVersions((status, version) -> true)
			.networkProtocolVersion(PROTOCOL_VERSION)
			.simpleChannel();
	
	public static void init()
	{
		PacketHandler.INSTANCE.messageBuilder(PacketUpdateTradingStateClient.class, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(PacketUpdateTradingStateClient::encodePacket)
			.decoder(PacketUpdateTradingStateClient::decodePacket)
			.consumerMainThread(PacketUpdateTradingStateClient::handlePacket)
			.add();
		
		PacketHandler.INSTANCE.messageBuilder(PacketUpdateTradingStateServer.class, NetworkDirection.PLAY_TO_SERVER)
		.encoder(PacketUpdateTradingStateServer::encodePacket)
		.decoder(PacketUpdateTradingStateServer::decodePacket)
		.consumerMainThread(PacketUpdateTradingStateServer::handlePacket)
		.add();
		
		PacketHandler.INSTANCE.messageBuilder(PacketTraderName.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(PacketTraderName::encodePacket)
		.decoder(PacketTraderName::decodePacket)
		.consumerMainThread(PacketTraderName::handlePacket)
		.add();
	}
}
