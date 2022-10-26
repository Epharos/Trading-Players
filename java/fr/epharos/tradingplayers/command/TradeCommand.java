package fr.epharos.tradingplayers.command;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.epharos.tradingplayers.core.Session;
import fr.epharos.tradingplayers.util.PendingTrade;
import fr.epharos.tradingplayers.util.TradingPlayersConfig;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class TradeCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(
				Commands.literal("trade")
				.then(
						Commands.literal("send")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> askPlayerForTrade(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(
						Commands.literal("cancel")
						.executes(ctx -> cancelPending(ctx.getSource())))
				.then(
						Commands.literal("accept")
						.executes(ctx -> acceptTrade(ctx.getSource())))
				.then(
						Commands.literal("refuse")
						.executes(ctx -> refuseTrade(ctx.getSource())))
				.then(
						Commands.literal("ignore")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(ctx -> ignorePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				);
    }
	
	private static int ignorePlayer(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException 
	{
		if(PendingTrade.isIgnoredBy(player, source.getPlayerOrException()))
		{
			PendingTrade.unignorePlayer(source.getPlayerOrException(), player);
			source.sendSuccess(new TranslatableComponent("chat.trade.ignore.cancel", player.getDisplayName().getString()), false);
			return Command.SINGLE_SUCCESS;
		}
		
		PendingTrade.ignorePlayer(source.getPlayerOrException(), player);
		source.sendSuccess(new TranslatableComponent("chat.trade.ignore.success", player.getDisplayName().getString()), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int cancelPending(CommandSourceStack source) throws CommandSyntaxException 
	{
		for(Iterator<PendingTrade> it = PendingTrade.pendingTrades.iterator() ; it.hasNext() ;)
		{
			PendingTrade pt = it.next();
			
			if(pt.getPlayer() == source.getPlayerOrException())
			{
				pt.getPlayer().sendMessage(new TranslatableComponent("chat.trade.canceled.player", pt.getTrader().getDisplayName().getString()), Util.NIL_UUID);
				pt.getTrader().sendMessage(new TranslatableComponent("chat.trade.canceled.trader", pt.getPlayer().getDisplayName().getString()), Util.NIL_UUID);
				it.remove();
				return Command.SINGLE_SUCCESS;
			}
		}
		
		source.getPlayerOrException().sendMessage(new TranslatableComponent("chat.trade.error.nopending"), Util.NIL_UUID);
		return 0;
	}

	private static int refuseTrade(CommandSourceStack source) throws CommandSyntaxException 
	{
		for(Iterator<PendingTrade> it = PendingTrade.pendingTrades.iterator() ; it.hasNext() ;)
		{
			PendingTrade pt = it.next();
			
			if(pt.getTrader() == source.getPlayerOrException())
			{
				pt.getPlayer().sendMessage(new TranslatableComponent("chat.trade.refused.asker", pt.getTrader().getDisplayName().getString()), Util.NIL_UUID);
				pt.getTrader().sendMessage(new TranslatableComponent("chat.trade.refused.responder", pt.getPlayer().getDisplayName().getString()), Util.NIL_UUID);
				it.remove();
				return Command.SINGLE_SUCCESS;
			}
		}
		
		source.getPlayerOrException().sendMessage(new TranslatableComponent("chat.trade.error.nopending"), Util.NIL_UUID);
		return 0;
	}

	private static int acceptTrade(CommandSourceStack source) throws CommandSyntaxException 
	{
		for(Iterator<PendingTrade> it = PendingTrade.pendingTrades.iterator() ; it.hasNext() ;)
		{
			PendingTrade pt = it.next();
			
			if(pt.getTrader() == source.getPlayerOrException())
			{				
				if(pt.getPlayer().distanceToSqr(pt.getTrader()) <= TradingPlayersConfig.MAX_TRADING_RANGE.get())
				{					
					Session.createSession(pt.getPlayer(), pt.getTrader());
					it.remove();
					return Command.SINGLE_SUCCESS;
				}
				else
				{					
					source.getPlayerOrException().sendMessage(new TranslatableComponent("chat.trade.error.tofar", pt.getPlayer()), Util.NIL_UUID);
					return 0;
				}
			}
		}
		
		source.getPlayerOrException().sendMessage(new TranslatableComponent("chat.trade.error.nopending"), Util.NIL_UUID);
		return 0;
	}

	private static boolean isAvailableForTrading(CommandSourceStack src, ServerPlayer player) throws CommandSyntaxException
	{		
		if(PendingTrade.isIgnoredBy(src.getPlayerOrException(), player))
		{
			src.sendFailure(new TranslatableComponent("chat.trade.ignored", player.getDisplayName().getString()));
			return false;
		}
		
		for(Iterator<PendingTrade> it = PendingTrade.pendingTrades.iterator() ; it.hasNext() ;)
		{
			PendingTrade pt = it.next();
			
			if(pt.getPlayer() == src.getPlayerOrException())
			{
				src.sendFailure(new TranslatableComponent("chat.trade.error.hasasked"));
				return false;
			}
			else if(pt.getTrader() == src.getOnlinePlayerNames())
			{
				src.sendFailure(new TranslatableComponent("chat.trade.error.haspending"));
				return false;
			}
			
			if(pt.getPlayer() == player)
			{
				src.sendFailure(new TranslatableComponent("chat.trade.error.player.haspending", src.getPlayerOrException().getName().getString()));
				return false;
			}
			else if(pt.getTrader() == player)
			{
				src.sendFailure(new TranslatableComponent("chat.trade.error.player.haspending", src.getPlayerOrException().getName().getString()));
				return false;
			}
		}
		
		final AtomicBoolean flag = new AtomicBoolean(true);
		
		Session.getPlayerSession(src.getPlayerOrException()).ifPresent(s -> {
			flag.set(false);
		});
		
		Session.getPlayerSession(player).ifPresent(s -> {
			flag.set(false);
			src.sendFailure(new TranslatableComponent("chat.trade.error.istrading", player.getDisplayName().getString()));
		});
		
		return flag.get();
	}
	
    private static int askPlayerForTrade(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException 
    {    	
    	if(source.getPlayerOrException() == player)
    	{
    		source.sendFailure(new TranslatableComponent("chat.trade.error.themselves"));
    		return 0;
    	}
  
    	
    	if(!TradeCommand.isAvailableForTrading(source, player))
    	{
    		return 0;
    	}
    	
    	new PendingTrade(source.getPlayerOrException(), player);
    	player.sendMessage(new TranslatableComponent("chat.trade.invite.receiver", source.getPlayerOrException().getDisplayName().getString()), Util.NIL_UUID);
    	source.getPlayerOrException().sendMessage(new TranslatableComponent("chat.trade.invite.sender", player.getDisplayName().getString()), Util.NIL_UUID);
    	
		return Command.SINGLE_SUCCESS;
	}
}
