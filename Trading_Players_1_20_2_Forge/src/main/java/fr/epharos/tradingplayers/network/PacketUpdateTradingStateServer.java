package fr.epharos.tradingplayers.network;

import java.util.concurrent.atomic.AtomicBoolean;

import fr.epharos.tradingplayers.core.Session;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
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
	
	public boolean handlePacket(CustomPayloadEvent.Context ctx)
	{
		final AtomicBoolean flag = new AtomicBoolean(false);

			switch(this.action)
			{
			case 0:
				Session.toggleAcceptanceFor(ctx.getSender());
				break;
			case 1:
				Session.resetBothAcceptances(ctx.getSender());
				break;
			case 2:
				Session.destroyPlayerSession(ctx.getSender());
				break;
			case 3:
				Session.getPlayerSession(ctx.getSender()).ifPresent(s -> {
					PacketHandler.INSTANCE.send(new PacketTraderName(s.getTrader(ctx.getSender()).getUUID()), PacketDistributor.PLAYER.with(ctx.getSender()));
				});
				
			}
			
			flag.set(true);
		
		ctx.setPacketHandled(true);
		return flag.get();
	}
}
