package fr.epharos.tradingplayers.network;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import fr.epharos.tradingplayers.screen.TradingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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
	
	public boolean handlePacket(Supplier<NetworkEvent.Context> ctx)
	{
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		ctx.get().enqueueWork(() -> 
		{
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
				PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 2));
				break;
			}
			
			flag.set(true);
		});
		
		ctx.get().setPacketHandled(true);
		return flag.get();
	}
}
