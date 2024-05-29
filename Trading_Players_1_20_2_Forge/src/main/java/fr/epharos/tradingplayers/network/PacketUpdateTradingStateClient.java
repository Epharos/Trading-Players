package fr.epharos.tradingplayers.network;

import java.util.concurrent.atomic.AtomicBoolean;

import fr.epharos.tradingplayers.screen.TradingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

public class PacketUpdateTradingStateClient 
{
	public byte action;
	
	public PacketUpdateTradingStateClient(byte b)
	{
		this.action = b;
	}
	
	public static void encodePacket(PacketUpdateTradingStateClient packet, FriendlyByteBuf buf)
	{
		buf.writeByte(packet.action);
	}
	
	public static PacketUpdateTradingStateClient decodePacket(FriendlyByteBuf buf)
	{
		return new PacketUpdateTradingStateClient(buf.readByte());
	}
	
	public boolean handlePacket(CustomPayloadEvent.Context ctx)
	{
		final AtomicBoolean flag = new AtomicBoolean(false);

			switch(this.action)
			{
			case 0:
				if(Minecraft.getInstance().screen instanceof TradingScreen s)
				{
					s.toggleTraderAcceptance();
				}
				
				break;
			case 1:
				if(Minecraft.getInstance().screen instanceof TradingScreen s)
				{
					s.setPlayerAcceptance(false);
					s.setTraderAcceptance(false);
				}
				
				break;
			case 2:
				PacketHandler.INSTANCE.send(new PacketUpdateTradingStateServer((byte) 2), PacketDistributor.SERVER.noArg());
				break;
			}
			
			flag.set(true);
		
		ctx.setPacketHandled(true);
		return flag.get();
	}
}
