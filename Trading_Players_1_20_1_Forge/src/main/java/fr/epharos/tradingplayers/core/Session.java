package fr.epharos.tradingplayers.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.network.chat.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import fr.epharos.tradingplayers.event.EventSessionCreated;
import fr.epharos.tradingplayers.event.EventSessionDestroyed;
import fr.epharos.tradingplayers.network.PacketHandler;
import fr.epharos.tradingplayers.network.PacketUpdateTradingStateClient;
import fr.epharos.tradingplayers.screen.TradingContainer;
import fr.epharos.tradingplayers.util.TradingPlayersConfig;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;

public class Session implements Container
{
	private final ServerPlayer player, trader;
	private boolean playerAcceptance = false, traderAcceptance = false;
	private final ItemStackHandler playerStacks, traderStacks;
	
	private static final Map<ServerPlayer, Session> registeredSessions = new HashMap<>();
	
	/**
	 * Initiates a new trading Session and register it to the Hashmap
	 * 
	 * @param p 
	 * 		The player starting the trade
	 * @param t 
	 * 		The player he trades with
	 */
	private Session(@NonNull ServerPlayer p, @NonNull ServerPlayer t)
	{
		player = p;
		trader = t;
		
		playerStacks = new ItemStackHandler(36);
		traderStacks = new ItemStackHandler(36);
		
		Session.registeredSessions.put(p, this);
		Session.registeredSessions.put(t, this);
	}
	
	/**
	 * Creates the trading Session
	 * 
	 * @param p 
	 * 		The player starting the trade
	 * @param t 
	 * 		The player he trades with
	 */
	public static void createSession(@NonNull ServerPlayer p, @NonNull ServerPlayer t)
	{
		Session session = new Session(p, t);
		session.start();
		
		MinecraftForge.EVENT_BUS.post(new EventSessionCreated(p, t, session));
		
		TradingPlayers._LOGGER.info("Trading session started (" + p.getDisplayName().getString() + " <-> " + t.getDisplayName().getString() + ")");
	}
	
	/**
	 * Starts the trade by opening the screen to both players
	 */
	private void start()
	{
		TradingPlayers.openTradingScreen(this.player);
		TradingPlayers.openTradingScreen(this.trader);
	}
	
	/**
	 * Destroys the trading Session for both players involved with the player
	 * 
	 * @param player 
	 * 		One of the two players involved in a trading Session
	 * @see {@link #getPlayerSession(ServerPlayer)}
	 */
	public static void destroyPlayerSession(ServerPlayer player)
	{
		Session.getPlayerSession(player).ifPresent(s -> s.destroySession());
	}
	
	/**
	 * Destroys the Session, unregisters it and sends a message (processed or cancelled) to the players involved in the trade
	 */
	public void destroySession()
	{
		MinecraftForge.EVENT_BUS.post(new EventSessionDestroyed.Pre(player, trader, this.playerAcceptance && this.traderAcceptance ? EventSessionDestroyed.TradeStatus.PROCESSING : EventSessionDestroyed.TradeStatus.CANCELING, this));
		
		this.player.closeContainer();
		this.trader.closeContainer();
		
		if(playerAcceptance && traderAcceptance)
		{
			player.displayClientMessage(Component.translatable("chat.trade.success", trader.getName().getString()), true);
			trader.displayClientMessage(Component.translatable("chat.trade.success", player.getName().getString()), true);
			TradingPlayers._LOGGER.info("Trade processed (" + player.getName().getString() + " <-> " + trader.getName().getString() + ")");
		}
		else
		{
			player.displayClientMessage(Component.translatable("chat.trade.cancelled", trader.getName().getString()), true);
			trader.displayClientMessage(Component.translatable("chat.trade.cancelled", player.getName().getString()), true);
			this.putItemsBack(player);
			this.putItemsBack(trader);
			TradingPlayers._LOGGER.info("Trade cancelled (" + player.getName().getString() + " <-> " + trader.getName().getString() + ")");
		}
		
		registeredSessions.put(player, null);
		registeredSessions.put(trader, null);
		
		MinecraftForge.EVENT_BUS.post(new EventSessionDestroyed.Post(player, trader, this.playerAcceptance && this.traderAcceptance ? EventSessionDestroyed.TradeStatus.PROCESSING : EventSessionDestroyed.TradeStatus.CANCELING, this));
	}

	/**
	 * 
	 * @param player
	 * 		The player we want the Session he is involved in
	 * @return
	 * 		An {@link Optional} containing the {@link Session} if it exists
	 */
	public static Optional<Session> getPlayerSession(ServerPlayer player)
	{
		return Optional.ofNullable(Session.registeredSessions.get(player));
	}
	
	/**
	 * 
	 * @param p
	 * 		The player to get the slot id from
	 * @param slotID
	 * 		Which slot id to get from the player's inventory
	 * @return 
	 * 		Player slot ID
	 */
	public int getPlayerInventorySlotID(ServerPlayer p, int slotID)
	{
		return p == player ? slotID : player.getInventory().getContainerSize() + playerStacks.getSlots() + slotID;
	}
	
	/**
	 * 
	 * @param 
	 * 		Player to get his trading inventory slot id from
	 * @return
	 * 		Player's trading inventory first slot ID
	 * 
	 * @see
	 * 		ContainerTrading#putSlotInServer(PlayerEntity)
	 */
	public int getPlayerTradingSlotID(ServerPlayer p)
	{
		return p == player ? player.getInventory().getContainerSize() : player.getInventory().getContainerSize() + playerStacks.getSlots() + trader.getInventory().getContainerSize();
	}
	
	/**
	 * 
	 * @param 
	 * 		Trader to get his trading inventory slot id from
	 * @return
	 * 		Trader's trading inventory first slot ID
	 * 
	 * @see
	 * 		ContainerTrading#putSlotInServer(PlayerEntity)
	 */
	public int getTraderTradingSlotID(ServerPlayer p) {
		return p != player ? player.getInventory().getContainerSize() : player.getInventory().getContainerSize() + playerStacks.getSlots() + trader.getInventory().getContainerSize();
	}
	
	public Inventory getPlayerInventory(ServerPlayer sp)
	{
		return sp == this.player ? this.player.getInventory() : this.trader.getInventory();
	}
	
	public IItemHandler getPlayerTradingInventory(ServerPlayer sp)
	{
		return sp == this.player ? this.playerStacks : this.traderStacks;
	}

	public void clearContent() { }

	public int getContainerSize() 
	{
		return this.player.getInventory().getContainerSize() + this.trader.getInventory().getContainerSize() + this.playerStacks.getSlots() + this.traderStacks.getSlots();
	}

	public boolean isEmpty() 
	{
		return Session.isItemStackHandlerEmpty(this.playerStacks) && Session.isItemStackHandlerEmpty(this.traderStacks);
	}

	public ItemStack getItem(int slotID) 
	{
		if(slotID < player.getInventory().getContainerSize())
			return player.getInventory().getItem(slotID);
		
		slotID -= player.getInventory().getContainerSize();
		if(slotID < this.playerStacks.getSlots())
			return this.playerStacks.getStackInSlot(slotID);
		
		slotID -= this.playerStacks.getSlots();
		if(slotID < trader.getInventory().getContainerSize())
			return this.trader.getInventory().getItem(slotID);
		
		slotID -= trader.getInventory().getContainerSize();
		if(slotID < this.traderStacks.getSlots())
			return this.traderStacks.getStackInSlot(slotID);
		
		return ItemStack.EMPTY;
	}

	public ItemStack removeItem(int slotID, int count) 
	{
		if(slotID < player.getInventory().getContainerSize())
			return this.player.getInventory().removeItem(slotID, count);
		
		slotID -= player.getInventory().getContainerSize();
		if(slotID < this.playerStacks.getSlots())
			return this.playerStacks.extractItem(slotID, count, false);
		
		slotID -= this.playerStacks.getSlots();
		if(slotID < trader.getInventory().getContainerSize())
			return this.trader.getInventory().removeItem(slotID, count);
		
		slotID -= trader.getInventory().getContainerSize();
		if(slotID < this.traderStacks.getSlots())
			return this.traderStacks.extractItem(slotID, count, false);
		
		return ItemStack.EMPTY;
	}

	public ItemStack removeItemNoUpdate(int p_18951_) 
	{
		return ItemStack.EMPTY;
	}

	public void setItem(int slotID, ItemStack stack) 
	{
		if(slotID < player.getInventory().getContainerSize())
		{
			player.getInventory().setItem(slotID, stack);
			return;
		}
		
		slotID -= player.getInventory().getContainerSize();
		if(slotID < playerStacks.getSlots())
		{
			playerStacks.setStackInSlot(slotID, stack);
			return;
		}
		
		slotID -= playerStacks.getSlots();
		if(slotID < trader.getInventory().getContainerSize())
		{
			trader.getInventory().setItem(slotID, stack);
			return;
		}
		
		slotID -= trader.getInventory().getContainerSize();
		if(slotID < traderStacks.getSlots())
		{
			traderStacks.setStackInSlot(slotID, stack);
			return;
		}
	}

	public void setChanged() { }

	public boolean stillValid(Player e) 
	{
		return (player.isAlive() ? e.distanceToSqr(player) <= TradingPlayersConfig.MAX_TRADING_RANGE.get() : false) && (trader.isAlive() ? e.distanceToSqr(trader) <= TradingPlayersConfig.MAX_TRADING_RANGE.get() : false);
	}
	
	/**
	 * Checks if an {@link ItemStackHandler} is empty or not
	 * 
	 * @param ish
	 * 		The {@link ItemStackHandler} to check
	 * @return
	 */
	public static boolean isItemStackHandlerEmpty(ItemStackHandler ish)
	{
		for(int i = 0 ; i < ish.getSlots() ; i++)
		{
			if(ish.getStackInSlot(i) != null && ish.getStackInSlot(i) != ItemStack.EMPTY)
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Toggles the acceptance state for a given player.
	 * If both acceptances are {@code true}, then the trade is processed and the session is destroyed.
	 * 
	 * @param sender
	 * 		The player who needs to get his acceptance toggled
	 */
	public static void toggleAcceptanceFor(@Nonnull ServerPlayer sender) 
	{
		Session.getPlayerSession(sender).ifPresent(s -> {
			if(sender == s.player)
			{
				s.playerAcceptance = !s.playerAcceptance;
				PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> s.trader), new PacketUpdateTradingStateClient((byte) 0));
			}
			else
			{
				s.traderAcceptance = !s.traderAcceptance;
				PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> s.player), new PacketUpdateTradingStateClient((byte) 0));
			}
			
			if(s.playerAcceptance && s.traderAcceptance)
			{
				s.processTrade();
				s.destroySession();
			}
		});
	}
	
	/**
	 * Resets both players acceptances to false
	 * 
	 * @see TradingContainer#clicked(int, int, ClickType, Player) 
	 * @param sender
	 * 		The player who clicked on a slot and needs to toggle off both acceptances
	 */
	public static void resetBothAcceptances(ServerPlayer sender)
	{
		Session.getPlayerSession(sender).ifPresent(s -> {
			s.playerAcceptance = false;
			s.traderAcceptance = false;
			
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> s.player), new PacketUpdateTradingStateClient((byte) 1));
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> s.trader), new PacketUpdateTradingStateClient((byte) 1));
		});
	}

	/**
	 * Processes the trade by transferring trading inventories to player's inventories
	 */
	private void processTrade() 
	{
		for(int i = 0 ; i < this.traderStacks.getSlots() ; i++)
		{
			if(this.traderStacks.getStackInSlot(i) == ItemStack.EMPTY)
				continue;
			
			if(!this.player.getInventory().add(this.traderStacks.getStackInSlot(i)))
				this.player.drop(this.traderStacks.getStackInSlot(i), false, true);
			
			this.traderStacks.setStackInSlot(i, ItemStack.EMPTY);
		}
		
		for(int i = 0 ; i < this.playerStacks.getSlots() ; i++)
		{
			if(this.playerStacks.getStackInSlot(i) == ItemStack.EMPTY)
				continue;
			
			if(!this.trader.getInventory().add(this.playerStacks.getStackInSlot(i)))
				this.trader.drop(this.playerStacks.getStackInSlot(i), false, true);
			
			this.playerStacks.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	public ServerPlayer getTrader(ServerPlayer player) 
	{
		return player == this.player ? this.trader : this.player;
	}
	
	public boolean getPlayerAcceptance() 
	{
		return playerAcceptance;
	}

	public boolean getTraderAcceptance() 
	{
		return traderAcceptance;
	}
	
	public void putItemsBack(ServerPlayer sp)
	{		
		for(int i = 0 ; i < this.getPlayerTradingInventory(sp).getSlots() ; i++)
		{
			this.getPlayerInventory(sp).placeItemBackInInventory(this.getPlayerTradingInventory(sp).getStackInSlot(i));
		}
	}
}
