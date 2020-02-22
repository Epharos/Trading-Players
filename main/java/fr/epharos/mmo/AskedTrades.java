package fr.epharos.mmo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.DEDICATED_SERVER)
public class AskedTrades 
{
	public ServerPlayerEntity asker, responder;
	public int timeOut;
	
	public static List<AskedTrades> pendings = new ArrayList<AskedTrades>();
	
	public AskedTrades(ServerPlayerEntity a, ServerPlayerEntity r)
	{
		asker = a;
		responder = r;
		timeOut = 1200;
		Trading._LOGGER.info(a.getName().getString() + " sent a trade request to " + responder.getName().getString());
		pendings.add(this);
	}
	
	/**
	 * Simply decrements the pending request timer
	 */
	public void timeIsRunningOut()
	{
		timeOut--;
	}
	
	/**
	 * Checks timer for all pending requests and remove it from the {@link AskedTrades#pendings} if timer has reached 0
	 */
	public static void checkPendings()
	{
		for(Iterator<AskedTrades> it = pendings.iterator() ; it.hasNext() ;)
		{
			AskedTrades ask = it.next();
			if(ask.timeOut <= 0)
			{
				ask.asker.sendMessage(new TranslationTextComponent("chat.trade.noanswer.asker", ask.responder.getName().getString()));
				ask.responder.sendMessage(new TranslationTextComponent("chat.trade.noanswer.responder", ask.asker.getName().getString()));
				it.remove();
			}
		}
	}
}
