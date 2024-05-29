package fr.epharos.tradingplayers.event;

import fr.epharos.tradingplayers.core.Session;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public abstract class EventSessionDestroyed extends Event
{
	private final ServerPlayer player, trader;
	private final TradeStatus status;
	private final Session session;
	
	public EventSessionDestroyed(ServerPlayer p, ServerPlayer t, TradeStatus s, Session ses)
	{
		this.player = p;
		this.trader = t;
		this.status = s;
		this.session = ses;
	}
	
	public ServerPlayer getPlayer() 
	{
		return player;
	}

	public ServerPlayer getTrader() 
	{
		return trader;
	}
	
	public TradeStatus getStatus()
	{
		return status;
	}
	
	public Session getSession()
	{
		return session;
	}
	
	public static class Pre extends EventSessionDestroyed
	{
		/**
		 * This event is fired before the trade is being processed/canceled and before the session is being destroyed
		 */
		public Pre(ServerPlayer p, ServerPlayer t, TradeStatus s, Session ses) 
		{
			super(p, t, s, ses);
		}
	}
	
	public static class Post extends EventSessionDestroyed
	{
		/**
		 * This event is fired after the trade is being processed/canceled and just before the session is being destroyed
		 */
		public Post(ServerPlayer p, ServerPlayer t, TradeStatus s, Session ses) 
		{
			super(p, t, s, ses);
		}
	}
	
	public enum TradeStatus
	{
		PROCESSING,
		CANCELING;
	}
}
