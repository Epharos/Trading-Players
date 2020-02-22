package fr.epharos.mmo;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketTraderName 
{
	public int playerID;
	
	public PacketTraderName(int i)
	{
		playerID = i;
	}
	
	public static void writePacketData(PacketTraderName packet, PacketBuffer buffer)
	{
		buffer.writeInt(packet.playerID);
	}
	
	public static PacketTraderName readPacketData(PacketBuffer buffer)
	{
		return new PacketTraderName(buffer.readInt());
	}
	
	public static void handlePacket(PacketTraderName packet, Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> 
		{			
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> 
			{
				if(Minecraft.getInstance().currentScreen instanceof GuiTrading)
				{
					((GuiTrading)Minecraft.getInstance().currentScreen).traderHead = ((AbstractClientPlayerEntity)Minecraft.getInstance().world.getEntityByID(packet.playerID)).getLocationSkin();
				}
			});
			
			DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> 
			{
				Session playerSession = Session.registeredSessions.get(context.get().getSender());
				Trading._CHANNEL.send(PacketDistributor.PLAYER.with(() -> context.get().getSender()),
						new PacketTraderName(playerSession.trader != context.get().getSender() ?
								playerSession.trader.getEntityId() : playerSession.player.getEntityId()));
			});
		});
		context.get().setPacketHandled(true);
	}
}
