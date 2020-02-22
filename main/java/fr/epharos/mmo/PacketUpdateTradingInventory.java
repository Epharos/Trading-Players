package fr.epharos.mmo;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateTradingInventory
{
	public int action;
	
	public PacketUpdateTradingInventory(int a)
	{
		action = a;
	}
	
	public static void writePacketData(PacketUpdateTradingInventory packet, PacketBuffer buffer)
	{
		buffer.writeInt(packet.action);
	}
	
	public static PacketUpdateTradingInventory readPacketData(PacketBuffer buffer)
	{
		return new PacketUpdateTradingInventory(buffer.readInt());
	}
	
	public static void handlePacket(PacketUpdateTradingInventory packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> 
		{		
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> 
			{
				switch(packet.action)
				{
				case 0 :
					if(Minecraft.getInstance().currentScreen instanceof GuiTrading)
					{
						((GuiTrading)Minecraft.getInstance().currentScreen).toggleTraderAcceptation();
					}
					break;
				case 1 :
					if(Minecraft.getInstance().currentScreen instanceof GuiTrading)
					{
						((GuiTrading)Minecraft.getInstance().currentScreen).toggleTraderAcceptation(false);
						((GuiTrading)Minecraft.getInstance().currentScreen).togglePlayerAcceptation(false);
					}
					break;
				case 2 :
					Trading._CHANNEL.sendToServer(packet);
					break;
				}
			});
			
			DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> 
			{
				switch(packet.action)
				{
				case 0:
					Session.toggleAcceptationFor(context.get().getSender());
					break;
				case 1:
					Session.toggleBothAcceptations(context.get().getSender());
					break;
				case 2:
					Session.destroySession(context.get().getSender());
					break;				
				}
			});
		});
		context.get().setPacketHandled(true);
	}
}
