package fr.epharos.mmo;

import java.util.Collection;
import java.util.Iterator;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandTrading 
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
				Commands.literal("trade")
				.then(
						Commands.literal("send")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> askPlayerForTrade(ctx.getSource(), EntityArgument.getPlayers(ctx, "player")))))
				
				.then(
						Commands.literal("accept")
						.executes(ctx -> acceptTrade(ctx.getSource())))
				.then(
						Commands.literal("refuse")
						.executes(ctx -> refuseTrade(ctx.getSource())))
				);
		
	}
	
	/**
	 * Tries to refuse a trade
	 * @param src
	 * 		The player who used "/trade refuse"
	 * @return
	 * @throws CommandSyntaxException
	 */
	private static int refuseTrade(CommandSource src) throws CommandSyntaxException 
	{
		for(Iterator<AskedTrades> it = AskedTrades.pendings.iterator() ; it.hasNext() ; )
		{
			AskedTrades ask = it.next();
			
			if(ask.responder == src.asPlayer())
			{
				ask.asker.sendMessage(new TranslationTextComponent("chat.trade.refused.asker", ask.responder.getName().getString()));
				src.sendFeedback(new TranslationTextComponent("chat.trade.refused.responder", ask.asker.getName().getString()), false);
				it.remove();
				return 1;
			}
		}
		
		src.sendFeedback(new TranslationTextComponent("chat.trade.error.nopending"), false);
		return 0;
	}

	/**
	 * Tries to accept a trade
	 * @param src
	 * 		The player who used "/trade accept"
	 * @return
	 * @throws CommandSyntaxException
	 */
	private static int acceptTrade(CommandSource src) throws CommandSyntaxException 
	{
		for(Iterator<AskedTrades> it = AskedTrades.pendings.iterator() ; it.hasNext() ; )
		{
			AskedTrades ask = it.next();
			
			if(ask.responder == src.asPlayer())
			{
				if(ask.responder.getDistanceSq(ask.asker) <= 64.0d)
				{
					Session.createSession(ask.asker, ask.responder);
					it.remove();
					return 1;
				}
			}
		}
		
		src.sendErrorMessage(new TranslationTextComponent("chat.trade.error.nopending"));
		return 0;
	}
	
	/**
	 * Check if a players are both available for a trade
	 * @param src
	 * 		The player who used "/trade send [player]"
	 * @param player
	 * 		The player asked
	 * @return
	 */
	private static boolean inAvailableForTrade(CommandSource src, ServerPlayerEntity player)
	{
		for(Iterator<AskedTrades> it = AskedTrades.pendings.iterator() ; it.hasNext() ;)
		{
			AskedTrades ask = it.next();
			
			try 
			{
				if(ask.asker == src.asPlayer())
				{
					src.sendErrorMessage(new TranslationTextComponent("chat.trade.error.hasasked"));
					return false;
				}
				else if(ask.responder == src.asPlayer())
				{
					src.sendErrorMessage(new TranslationTextComponent("chat.trade.error.haspending"));
					return false;
				}
				
				if(ask.asker == player)
				{
					src.sendErrorMessage(new TranslationTextComponent("chat.trade.error.player.haspending", src.asPlayer().getName().getString()));
					return false;
				}
				else if(ask.responder == player)
				{
					src.sendErrorMessage(new TranslationTextComponent("chat.trade.error.player.haspending", src.asPlayer().getName().getString()));
					return false;
				}
			} catch (CommandSyntaxException e) 
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			if(Session.registeredSessions.containsKey(src.asPlayer()))
			{
				if(Session.registeredSessions.get(src.asPlayer()) != null)
				{
					return false;
				}
			}
			
			if(Session.registeredSessions.containsKey(player))
			{
				if(Session.registeredSessions.get(player) != null)
				{
					src.sendErrorMessage(new TranslationTextComponent("chat.trade.error.istrading", src.asPlayer().getName().getString()));
					return false;
				}
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	static boolean flag = true;

	/**
	 * After all checks, send trading request
	 * @param src
	 * 		The player who used "/trade send [player]"
	 * @param player
	 * 		The player asked
	 * @return
	 * @throws CommandSyntaxException
	 */
	private static int askPlayerForTrade(CommandSource src, Collection<? extends ServerPlayerEntity> player) throws CommandSyntaxException
	{
		if(player.size() != 1)
			return 0;
		
		player.forEach(p -> {
			try 
			{
				if(src.asPlayer() == p)
				{
					src.sendErrorMessage(new TranslationTextComponent("chat.trade.error.themselves"));
					flag = false;
					return;
				}
				
				if(!inAvailableForTrade(src, p))
				{
					flag = false;
					return;
				}
				
				new AskedTrades(src.asPlayer(), p);
				p.sendMessage(new TranslationTextComponent("chat.trade.invite.receiver", src.asPlayer().getName().getString()));
			} 
			catch (CommandSyntaxException e) 
			{
				e.printStackTrace();
			}
		});
		
		if(!flag)
		{
			flag = true;
			return 0;
		}
		
		player.forEach(p -> {
			src.sendFeedback(new TranslationTextComponent("chat.trade.invite.sender", p.getName().getString()), false);
		});
		return 0;
	}
}
