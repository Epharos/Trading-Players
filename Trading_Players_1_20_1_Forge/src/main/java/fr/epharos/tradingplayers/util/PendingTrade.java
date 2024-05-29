package fr.epharos.tradingplayers.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

@OnlyIn(Dist.DEDICATED_SERVER)
public class PendingTrade
{
	private final ServerPlayer player, trader;
	private final long expiry;
	
	public static final List<PendingTrade> pendingTrades = new ArrayList<>();
	public static final Map<ServerPlayer, List<UUID>> ignoredPlayers = new HashMap<>();
	
	public PendingTrade(@NonNull ServerPlayer p, @NonNull ServerPlayer t)
	{
		this.player = p;
		this.trader = t;
		this.expiry = System.currentTimeMillis() + (long)(TradingPlayersConfig.PENDING_TIME.get() * 1000);
		
		PendingTrade.pendingTrades.add(this);
	}
	
	public static DistExecutor.SafeRunnable checkPendings()
	{		
		return new DistExecutor.SafeRunnable() 
		{
			private static final long serialVersionUID = 1L;

			public void run() 
			{
				for(Iterator<PendingTrade> it = PendingTrade.pendingTrades.iterator() ; it.hasNext() ;)
				{
					PendingTrade pt = it.next();
					
					if(pt.expiry <= Util.getMillis())
					{
						pt.player.displayClientMessage(Component.translatable("chat.trade.noanswer.player", pt.trader.getDisplayName().getString()), true);
						pt.trader.displayClientMessage(Component.translatable("chat.trade.noanswer.trader", pt.player.getDisplayName().getString()), true);
						it.remove();
					}
				}
			}
		};
	}
	
	public static void ignorePlayer(ServerPlayer player, ServerPlayer ignored)
	{
		if(!PendingTrade.ignoredPlayers.containsKey(player))
			PendingTrade.ignoredPlayers.put(player, new ArrayList<>());
		
		if(!PendingTrade.isIgnoredBy(ignored, player))
			PendingTrade.ignoredPlayers.get(player).add(ignored.getUUID());
	}
	
	public static void unignorePlayer(ServerPlayer player, ServerPlayer ignored)
	{
		if(!PendingTrade.ignoredPlayers.containsKey(player))
			return;
		
		for(Iterator<UUID> it = PendingTrade.ignoredPlayers.get(player).iterator() ; it.hasNext() ;)
		{
			if(it.next() == ignored.getUUID())
			{
				it.remove();
				return;
			}
		}
	}
	
	public static boolean isIgnoredBy(ServerPlayer ignored, ServerPlayer player)
	{
		if(!PendingTrade.ignoredPlayers.containsKey(player))
			return false;
		
		return PendingTrade.ignoredPlayers.get(player).contains(ignored.getUUID());
	}
	
	public ServerPlayer getPlayer() 
	{
		return player;
	}

	public ServerPlayer getTrader() 
	{
		return trader;
	}
}
