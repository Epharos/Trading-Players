package fr.epharos.tradingplayers.network;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import fr.epharos.tradingplayers.screen.TradingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketTraderName 
{
	public UUID playerID;
	
	public PacketTraderName(UUID i)
	{
		this.playerID = i;
	}
	
	public static void encodePacket(PacketTraderName packet, FriendlyByteBuf buf)
	{
		buf.writeUUID(packet.playerID);
	}
	
	public static PacketTraderName decodePacket(FriendlyByteBuf buf)
	{
		return new PacketTraderName(buf.readUUID());
	}

	public boolean handlePacket(CustomPayloadEvent.Context ctx)
	{
		final AtomicBoolean flag = new AtomicBoolean(false);

		if(Minecraft.getInstance().screen instanceof TradingScreen s)
		{
			s.traderHead = ((AbstractClientPlayer)(Minecraft.getInstance().level.getPlayerByUUID(this.playerID))).getSkin().texture();
		}

		flag.set(true);

		ctx.setPacketHandled(true);
		return flag.get();
	}
}
