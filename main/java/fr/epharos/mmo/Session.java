package fr.epharos.mmo;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * This class manages all current trades on the server side.
 * It's also the Inventory that will be modified by the Container. 
 * 
 * @author Epharos
 * @version 2.0
 *
 */
@OnlyIn(Dist.DEDICATED_SERVER)
public class Session implements IInventory
{
	public ServerPlayerEntity player, trader;
	public ItemStack[] playerTradingItems = new ItemStack[45], traderTradingItems = new ItemStack[45];
	public boolean playerAcceptation = false, traderAcceptation = false;
	
	public static Map<ServerPlayerEntity, Session> registeredSessions = new HashMap<ServerPlayerEntity, Session>();
	
	public Session(@Nonnull ServerPlayerEntity p, @Nonnull ServerPlayerEntity t)
	{
		player = p;
		trader = t;
		
		playerTradingItems = this.getEmptyItemStackList(playerTradingItems.length);
		traderTradingItems = this.getEmptyItemStackList(traderTradingItems.length);
	}

	/**
	 * Create a trading session.
	 * 
	 * @param p
	 * 		A player from the trading session
	 * @param t
	 * 		Logically, the other player
	 */
	public static void createSession(@Nonnull ServerPlayerEntity p, @Nonnull ServerPlayerEntity t)
	{
		Session session = new Session(p, t);
		Session.registeredSessions.put(p, session);
		Session.registeredSessions.put(t, session);
		session.start();
		
		Trading._LOGGER.info("A trade has started : " + p.getName().getString() + " <-> " + t.getName().getString());
	}
	
	/**
	 * Starts the session by opening the trading GUI and the container
	 * 
	 * @see 
	 * 		Trading#openTradingScreen(ServerPlayerEntity)
	 */
	
	private void start()
	{
		Trading.openTradingScreen(player);
		Trading.openTradingScreen(trader);
	}
	
	/**
	 * When a trade is finished (success or cancelled), ends the trading session
	 * @param p
	 * 		A player from the trade (can be one player or the other, it will destroy for both player)
	 */
	public static void destroySession(@Nonnull ServerPlayerEntity p)
	{
		Session session = Session.registeredSessions.get(p);
		
		if(session != null)
			session.destroySession();
	}
	
	/**
	 * Destroys completely the session by closing players trading screen, sending a message to the players accordingly to the trading state (success or cancelled) and putting a null session for both players 
	 */
	private void destroySession() 
	{
		player.closeScreen();
		trader.closeScreen();
		
		if(playerAcceptation && traderAcceptation)
		{
			player.sendMessage(new TranslationTextComponent("chat.trade.success", trader.getName().getString()));
			trader.sendMessage(new TranslationTextComponent("chat.trade.success", player.getName().getString()));
			Trading._LOGGER.info("Trade done : " + player.getName().getString() + " <-> " + trader.getName().getString());
		}
		else
		{
			player.sendMessage(new TranslationTextComponent("chat.trade.cancelled", trader.getName().getString()));
			trader.sendMessage(new TranslationTextComponent("chat.trade.cancelled", player.getName().getString()));
			Trading._LOGGER.info("Trade cancelled : " + player.getName().getString() + " <-> " + trader.getName().getString());
		}
		
		registeredSessions.put(player, null);
		registeredSessions.put(trader, null);
	}

	@Override
	public void clear() 
	{
		
	}

	@Override
	public ItemStack decrStackSize(int slotID, int quantity) 
	{
		if(slotID < player.inventory.getSizeInventory())
			return player.inventory.decrStackSize(slotID, quantity);
		
		slotID -= player.inventory.getSizeInventory();
		if(slotID < playerTradingItems.length)
			return decrStackSize(slotID, quantity, playerTradingItems);
		
		slotID -= playerTradingItems.length;
		if(slotID < trader.inventory.getSizeInventory())
			return trader.inventory.decrStackSize(slotID, quantity);
		
		slotID -= trader.inventory.getSizeInventory();
		if(slotID < traderTradingItems.length)
			return decrStackSize(slotID, quantity, traderTradingItems);
		
		return ItemStack.EMPTY;
	}
	
	public static ItemStack getAndSplit(ItemStack[] stacks, int index, int amount) {
	      return index >= 0 && index < stacks.length && !stacks[index].isEmpty() && amount > 0 ? stacks[index].split(amount) : ItemStack.EMPTY;
	   }
	
	private ItemStack decrStackSize(int slotID, int quantity, ItemStack[] tradingItems)
	{
		return tradingItems != null && !tradingItems[slotID].isEmpty() ? getAndSplit(tradingItems, slotID, quantity) : ItemStack.EMPTY;
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
	public int getPlayerInventorySlotID(PlayerEntity p, int slotID)
	{
		return p == player ? slotID : player.inventory.getSizeInventory() + playerTradingItems.length + slotID;
	}
	
	/**
	 * 
	 * @param 
	 * 		Player to get his trading inventory slot id from
	 * @return
	 * 		Player trading first slot ID
	 * 
	 * @see
	 * 		ContainerTrading#putSlotInServer(PlayerEntity)
	 */
	public int getPlayerTradingSlotID(PlayerEntity p)
	{
		return p == player ? player.inventory.getSizeInventory() : player.inventory.getSizeInventory() + playerTradingItems.length + trader.inventory.getSizeInventory();
	}
	
	/**
	 * 
	 * @param 
	 * 		Trader to get his trading inventory slot id from
	 * @return
	 * 		Trader trading first slot ID
	 * 
	 * @see
	 * 		ContainerTrading#putSlotInServer(PlayerEntity)
	 */
	public int getTraderTradingSlotID(PlayerEntity p) {
		return p != player ? player.inventory.getSizeInventory() : player.inventory.getSizeInventory() + playerTradingItems.length + trader.inventory.getSizeInventory();
	}

	@Override
	public int getSizeInventory() 
	{
		return player.inventory.getSizeInventory() + trader.inventory.getSizeInventory() + playerTradingItems.length + traderTradingItems.length;
	}

	@Override
	public ItemStack getStackInSlot(int slotID) 
	{
		if(slotID < player.inventory.getSizeInventory())
			return player.inventory.getStackInSlot(slotID);
		
		slotID -= player.inventory.getSizeInventory();
		if(slotID < playerTradingItems.length)
			return playerTradingItems[slotID];
		
		slotID -= playerTradingItems.length;
		if(slotID < trader.inventory.getSizeInventory())
			return trader.inventory.getStackInSlot(slotID);
		
		slotID -= trader.inventory.getSizeInventory();
		if(slotID < traderTradingItems.length)
			return traderTradingItems[slotID];
		
		return ItemStack.EMPTY;
	}
	
	/**
	 * Fills an inventories (ItemStack array) with empty ItemStacks
	 * @param i
	 * 		Size of the inventory
	 * @return
	 * 		An ItemStack array filled with {@link ItemStack#EMPTY}
	 */
	public ItemStack[] getEmptyItemStackList(int i)
	{
		ItemStack[] items = new ItemStack[i];
		
		for(int a = 0 ; a < i ; a++)
			items[a] = ItemStack.EMPTY;
		
		return items;
	}

	@Override
	public boolean isEmpty() 
	{
		for(ItemStack stack : playerTradingItems)
			if(stack != null && stack != ItemStack.EMPTY)
				return false;
		
		for(ItemStack stack : traderTradingItems)
			if(stack != null && stack != ItemStack.EMPTY)
				return false;
		
		return true;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean isUsableByPlayer(PlayerEntity e) 
	{
		return player.isAlive() ? e.getDistanceSq(player) <= 64.0d : false && trader.isAlive() ? e.getDistanceSq(trader) <= 64.0d : false;
	}

	@Override
	public void markDirty() 
	{
		
	}

	@Override
	public ItemStack removeStackFromSlot(int slotID) 
	{
		if(slotID < player.inventory.getSizeInventory())
			return player.inventory.removeStackFromSlot(slotID);
		
		slotID -= player.inventory.getSizeInventory();
		if(slotID < playerTradingItems.length)
			return removeStackFromSlot(slotID, playerTradingItems);
		
		slotID -= 9;
		if(slotID < trader.inventory.getSizeInventory())
			return trader.inventory.removeStackFromSlot(slotID);
		
		slotID -= trader.inventory.getSizeInventory();
		if(slotID < traderTradingItems.length)
			return removeStackFromSlot(slotID, traderTradingItems);
		
		return ItemStack.EMPTY;
	}
	
	public ItemStack removeStackFromSlot(int slotID, ItemStack[] stacks) 
	{
		if(stacks != null && !stacks[slotID].isEmpty())
		{
			ItemStack stack = stacks[slotID];
			stacks[slotID] = ItemStack.EMPTY;
			return stack;
		}
		
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int slotID, ItemStack stack) 
	{
		if(slotID < player.inventory.getSizeInventory())
		{
			player.inventory.setInventorySlotContents(slotID, stack);
			return;
		}
		
		slotID -= player.inventory.getSizeInventory();
		if(slotID < playerTradingItems.length)
		{
			playerTradingItems[slotID] = stack;
			return;
		}
		
		slotID -= playerTradingItems.length;
		if(slotID < trader.inventory.getSizeInventory())
		{
			trader.inventory.setInventorySlotContents(slotID, stack);
			return;
		}
		
		slotID -= trader.inventory.getSizeInventory();
		if(slotID < traderTradingItems.length)
		{
			traderTradingItems[slotID] = stack;
			return;
		}
	}
	
	public ServerPlayerEntity getPlayerInstance(ServerPlayerEntity p)
	{
		return p == player ? player : trader;
	}
	
	public ServerPlayerEntity getTraderInstance(ServerPlayerEntity p)
	{
		return p != player ? player : trader;
	}
	
	public ItemStack[] getPlayerTradingInventory(ServerPlayerEntity p)
	{
		return p == player ? playerTradingItems : traderTradingItems;
	}

	/**
	 * Toggles the acceptation state for a given player.
	 * If both acceptance are {@code true}, then the trade is processed and the session is destroyed.
	 * @param sender
	 * 		The player who needs to get his acceptance toggled
	 */
	public static void toggleAcceptationFor(@Nonnull ServerPlayerEntity sender) 
	{
		Session session = registeredSessions.get(sender);
		
		if(sender == session.player)
		{
			session.playerAcceptation = !session.playerAcceptation;
			Trading._CHANNEL.send(PacketDistributor.PLAYER.with(() -> session.trader), new PacketUpdateTradingInventory(0));
		}
		else
		{
			session.traderAcceptation = !session.traderAcceptation;
			Trading._CHANNEL.send(PacketDistributor.PLAYER.with(() -> session.player), new PacketUpdateTradingInventory(0));
		}
		
		if(session.playerAcceptation && session.traderAcceptation)
		{
			session.processTrade();
			session.destroySession();
		}
	}
	
	/**
	 * Toggles both players acceptance to false
	 * @see ContainerTrading#slotClick(int, int, net.minecraft.inventory.container.ClickType, PlayerEntity)
	 * @param sender
	 * 		The player who clicked on a slot and needs to toggle off both acceptances
	 */
	public static void toggleBothAcceptations(ServerPlayerEntity sender)
	{
		Session session = registeredSessions.get(sender);
		
		session.playerAcceptation = false;
		session.traderAcceptation = false;
		
		Trading._CHANNEL.send(PacketDistributor.PLAYER.with(() -> session.player), new PacketUpdateTradingInventory(1));
		Trading._CHANNEL.send(PacketDistributor.PLAYER.with(() -> session.trader), new PacketUpdateTradingInventory(1));
	}

	/**
	 * Processes the trade by transferring trading inventories to trader inventories
	 */
	private void processTrade() 
	{
		for(ItemStack stack : traderTradingItems)
		{
			if(stack != null)
			{
				if(!player.inventory.addItemStackToInventory(stack))
				{
					player.dropItem(stack, false, true);
				}
			}
		}
		
		for(ItemStack stack : playerTradingItems)
		{
			if(stack != null)
			{
				if(!trader.inventory.addItemStackToInventory(stack))
				{
					trader.dropItem(stack, false, true);
				}
			}
		}
		
		for(int i = 0 ; i < 9 ; i++)
		{
			playerTradingItems = traderTradingItems = null;
		}
	}
}
