package fr.epharos.tradingplayers.screen;

import java.util.Optional;

import fr.epharos.tradingplayers.core.Session;
import fr.epharos.tradingplayers.network.PacketHandler;
import fr.epharos.tradingplayers.network.PacketUpdateTradingStateClient;
import fr.epharos.tradingplayers.network.PacketUpdateTradingStateServer;
import fr.epharos.tradingplayers.registries.ContainerRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.PacketDistributor;

public class TradingContainer extends AbstractContainerMenu 
{
	public IItemHandler playerItems = new ItemStackHandler(36), traderItems = new ItemStackHandler(36);
	
	@SuppressWarnings("deprecation")
	public TradingContainer(int id, final Inventory playerInventory)
	{
		super(ContainerRegistry.TRADING_CONTAINER.get(), id);
		
		DistExecutor.runWhenOn(Dist.CLIENT, () -> this.putSlotInClient(playerInventory));
		DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> this.putSlotInServer(playerInventory));
	}
	
	private DistExecutor.SafeRunnable putSlotInClient(Inventory playerInventory)
	{		
		final TradingContainer tc = this;
		
		return new DistExecutor.SafeRunnable() 
		{
			private static final long serialVersionUID = 2L;

			public void run() 
			{
				for(int i = 0 ; i < 3 ; i++)
				{
					for(int j = 0 ; j < 9 ; j++)
					{
						tc.addSlot(new Slot(playerInventory, 9 + j + i * 9, j * 18 + 8, i * 18 + 108));
					}
				}
				
				for(int i = 0 ; i < 9 ; i++)
				{
					tc.addSlot(new Slot(playerInventory, i, i * 18 + 8, 166));
				}
				
				for(int i = 0 ; i < 4 ; i++)
				{
					for(int j = 0 ; j < 9 ; j++)
					{
						tc.addSlot(new SlotItemHandler(playerItems, j + i * 9, j * 18 + 8, i * 18 + 30));
					}
				}
				
				for(int i = 0 ; i < 4 ; i++)
				{
					for(int j = 0 ; j < 9 ; j++)
					{
						tc.addSlot(new SlotItemHandler(traderItems, j + i * 9, i * 18 + 179, j * 18 + 39)
							{
								public boolean mayPlace(ItemStack stack) 
								{
									return false;
								}
								
								public boolean mayPickup(Player playerIn) 
								{
									return false;
								}
							});
					}
				}
			}
		};
	}
	
	/**
	 * Puts server side slots
	 * @param player
	 * 		The player to get trading {@link Session} from
	 */
	public DistExecutor.SafeRunnable putSlotInServer(Inventory playerInventory)
	{
		final Optional<Session> session = Session.getPlayerSession((ServerPlayer) playerInventory.player);
		final TradingContainer tc = this;
		
		return new DistExecutor.SafeRunnable() 
		{
			private static final long serialVersionUID = 3L;

			public void run() 
			{
				session.ifPresent(s -> {
					for(int i = 0 ; i < 3 ; i++)
					{
						for(int j = 0 ; j < 9 ; j++)
						{
							tc.addSlot(new Slot(s, s.getPlayerInventorySlotID((ServerPlayer) playerInventory.player, 9 + j + i * 9), j * 18 + 8, i * 18 + 108));
						}
					}
					
					for(int i = 0 ; i < 9 ; i++)
					{
						tc.addSlot(new Slot(s, s.getPlayerInventorySlotID((ServerPlayer) playerInventory.player, i), i * 18 + 8, 166));
					}
					
					for(int i = 0 ; i < 4 ; i++)
					{
						for(int j = 0 ; j < 9 ; j++)
						{
							tc.addSlot(new Slot(s, s.getPlayerTradingSlotID((ServerPlayer) playerInventory.player) + j + i * 9, j * 18 + 8, i * 18 + 30));
						}
					}
					
					for(int i = 0 ; i < 4 ; i++)
					{
						for(int j = 0 ; j < 9 ; j++)
						{
							tc.addSlot(new Slot(s, s.getTraderTradingSlotID((ServerPlayer) playerInventory.player) + j + i * 9, i * 18 + 179, j * 18 + 39)
								{
									public boolean mayPlace(ItemStack stack) 
									{
										return false;
									}
									
									public boolean mayPickup(Player playerIn) 
									{
										return false;
									}
								});
						}
					}
				});
			}
		};
	}

	public boolean stillValid(Player player) 
	{
		return true;
	}
	
	public ItemStack quickMoveStack(Player player, int index) 
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		
		if (slot != null && slot.hasItem()) 
		{
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			
			if (index >= player.getInventory().getContainerSize() - 5) 
			{
				if (!this.moveItemStackTo(itemstack1, 0, player.getInventory().getContainerSize() - 5, false)) 
				{
					return ItemStack.EMPTY;
				}
			} 
			else if (!this.moveItemStackTo(itemstack1, player.getInventory().getContainerSize() - 5, player.getInventory().getContainerSize() - 5 + this.playerItems.getSlots(), true)) 
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) 
			{
				slot.set(ItemStack.EMPTY);
			} 
			else
			{
				slot.setChanged();
			}
		}

		return itemstack;
	}
	
	@SuppressWarnings("deprecation")
	public void clicked(int slotID, int dragType, ClickType type, Player player) 
	{		
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			if(player instanceof LocalPlayer cp)
			{
				PacketHandler.INSTANCE.send(new PacketUpdateTradingStateServer((byte) 1), PacketDistributor.SERVER.noArg());
			}
		});
		
		super.clicked(slotID, dragType, type, player);
	}
	
	public void removed(Player player) 
	{
		super.removed(player);
		
		if(player instanceof ServerPlayer sp)
		{
			Session.getPlayerSession(sp).ifPresent(s -> {
				s.putItemsBack(sp);
			});
			
			PacketHandler.INSTANCE.send(new PacketUpdateTradingStateClient((byte) 2), PacketDistributor.PLAYER.with(sp));
		}
	}

}
