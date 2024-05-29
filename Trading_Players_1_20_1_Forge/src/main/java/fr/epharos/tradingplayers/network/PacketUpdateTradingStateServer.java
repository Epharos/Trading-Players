package fr.epharos.tradingplayers.network;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import fr.epharos.tradingplayers.core.Session;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class PacketUpdateTradingStateServer 
{
public byte action;
	
	public PacketUpdateTradingStateServer(byte b)
	{
		this.action = b;
	}
	
	public static void encodePacket(PacketUpdateTradingStateServer packet, FriendlyByteBuf buf)
	{
		buf.writeByte(packet.action);
	}
	
	public static PacketUpdateTradingStateServer decodePacket(FriendlyByteBuf buf)
	{
		return new PacketUpdateTradingStateServer(buf.readByte());
	}
	
	public static boolean handlePacket(PacketUpdateTradingStateServer packet, Supplier<NetworkEvent.Context> ctx)
	{
		final AtomicBoolean flag = new AtomicBoolean(false);

			switch(packet.action)
			{
			case 0:
				Session.toggleAcceptanceFor(ctx.get().getSender());
				break;
			case 1:
				Session.resetBothAcceptances(ctx.get().getSender());
				break;
			case 2:
				Session.destroyPlayerSession(ctx.get().getSender());
				break;
			case 3:
				Session.getPlayerSession(ctx.get().getSender()).ifPresent(s -> {
					PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new PacketTraderName(s.getTrader(ctx.get().getSender()).getUUID()));
				});
				
			}
			
			flag.set(true);
		
		ctx.get().setPacketHandled(true);
		return flag.get();
	}
}
