package fr.epharos.tradingplayers.event;

import fr.epharos.tradingplayers.core.Session;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class EventSessionCreated extends Event 
{
	private final ServerPlayer player, trader;
	private final Session session;
	
	/**
	 * This event takes place when a trading session is created
	 * @see fr.epharos.tradingplayers.core.Session#createSession(ServerPlayer, ServerPlayer)
	 */
	public EventSessionCreated(ServerPlayer p, ServerPlayer t, Session s)
	{
		this.player = p;
		this.trader = t;
		this.session = s;
	}

	public ServerPlayer getPlayer() 
	{
		return player;
	}

	public ServerPlayer getTrader() 
	{
		return trader;
	}
	
	public Session getSession()
	{
		return session;
	}
}
