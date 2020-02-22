package fr.epharos.mmo;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ObjectHolder;

public class ContainerTrading extends Container 
{
	@ObjectHolder(Trading._MODID + ":container_trading")
	public static ContainerType<ContainerTrading> _TYPE;
	
	public IInventory playerItems = new Inventory(9 * 4);
	public IInventory traderItems = new Inventory(9 * 4);
	
	public ContainerTrading(int windowID, PlayerEntity player) 
	{
		super(_TYPE, windowID);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> { this.putSlotInClient(player); });
		DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> { this.putSlotInServer(player); });
	}
	
	/**
	 * Puts slots on the client container
	 * @param player
	 * 		The player to put inventory slot from
	 */
	@OnlyIn(Dist.CLIENT)
	private void putSlotInClient(PlayerEntity player)
	{
		for(int i = 0 ; i < 3 ; i++)
		{
			for(int j = 0 ; j < 9 ; j++)
			{
				this.addSlot(new Slot(player.inventory, 9 + j + i * 9, j * 18 + 8, i * 18 + 108));
			}
		}
		
		for(int i = 0 ; i < 9 ; i++)
		{
			this.addSlot(new Slot(player.inventory, i, i * 18 + 8, 166));
		}
		
		for(int i = 0 ; i < 4 ; i++)
		{
			for(int j = 0 ; j < 9 ; j++)
			{
				this.addSlot(new Slot(playerItems, j + i * 9, j * 18 + 8, i * 18 + 30));
			}
		}
		
		for(int i = 0 ; i < 4 ; i++)
		{
			for(int j = 0 ; j < 9 ; j++)
			{
				this.addSlot(new Slot(traderItems, j + i * 9, i * 18 + 179, j * 18 + 39)
					{
						public boolean isItemValid(ItemStack stack) 
						{
							return false;
						}
						
						public boolean canTakeStack(PlayerEntity playerIn) 
						{
							return false;
						}
					});
				
				//ItemStack.EMPTY !!!!
			}
		}
	}
	
	/**
	 * Puts server side slots
	 * @param player
	 * 		The player to get trading {@link Session} from
	 */
	@OnlyIn(Dist.DEDICATED_SERVER)
	public void putSlotInServer(PlayerEntity player)
	{
		Session session = Session.registeredSessions.get(player);
		
		for(int i = 0 ; i < 3 ; i++)
		{
			for(int j = 0 ; j < 9 ; j++)
			{
				this.addSlot(new Slot(session, session.getPlayerInventorySlotID(player, 9 + j + i * 9), j * 18 + 8, i * 18 + 108));
			}
		}
		
		for(int i = 0 ; i < 9 ; i++)
		{
			this.addSlot(new Slot(session, session.getPlayerInventorySlotID(player, i), i * 18 + 8, 166));
		}
		
		for(int i = 0 ; i < 4 ; i++)
		{
			for(int j = 0 ; j < 9 ; j++)
			{
				this.addSlot(new Slot(session, session.getPlayerTradingSlotID(player) + j + i * 9, j * 18 + 8, i * 18 + 30));
			}
		}
		
		for(int i = 0 ; i < 4 ; i++)
		{
			for(int j = 0 ; j < 9 ; j++)
			{
				this.addSlot(new Slot(session, session.getTraderTradingSlotID(player) + j + i * 9, i * 18 + 179, j * 18 + 39)
					{
						public boolean isItemValid(ItemStack stack) 
						{
							return false;
						}
						
						public boolean canTakeStack(PlayerEntity playerIn) 
						{
							return false;
						}
					});
			}
		}
	}
	
	public void onContainerClosed(PlayerEntity playerIn) {
		PlayerInventory playerinventory = playerIn.inventory;
	      if (!playerinventory.getItemStack().isEmpty()) {
	         playerIn.inventory.placeItemBackInInventory(playerIn.world, playerinventory.getItemStack());
	         playerinventory.setItemStack(ItemStack.EMPTY);
	      }
	      
	      if(playerIn instanceof ServerPlayerEntity)
	      {
	    	  Session session = Session.registeredSessions.get(playerIn);
	    	  
	    	  if(!session.playerAcceptation || !session.traderAcceptation)
	    	  {
	    		  for(ItemStack stack : session.getPlayerTradingInventory((ServerPlayerEntity)playerIn))
	    			  session.getPlayerInstance((ServerPlayerEntity)playerIn).inventory.placeItemBackInInventory(session.getPlayerInstance((ServerPlayerEntity)playerIn).world, stack);
	    	  }
	    	  
	    	  Trading._CHANNEL.send(PacketDistributor.PLAYER.with(() -> session.getTraderInstance((ServerPlayerEntity)playerIn)), new PacketUpdateTradingInventory(2));
	      }
	   }
	
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) 
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack()) 
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			
			if (index >= playerIn.inventory.getSizeInventory() - 5) 
			{
				if (!this.mergeItemStack(itemstack1, 0, playerIn.inventory.getSizeInventory() - 5, false)) 
				{
					return ItemStack.EMPTY;
				}
			} 
			else if (!this.mergeItemStack(itemstack1, playerIn.inventory.getSizeInventory() - 5, playerIn.inventory.getSizeInventory() - 5 + this.playerItems.getSizeInventory(), true)) 
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) 
			{
				slot.putStack(ItemStack.EMPTY);
			} 
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
	
	public ContainerTrading(int windowID, PlayerInventory inventoryPlayer, PacketBuffer data)
	{
		this(windowID, inventoryPlayer.player);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) 
	{
		return true;
	}
	
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player)
	{
	     DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {if(player instanceof ClientPlayerEntity) Trading._CHANNEL.send(PacketDistributor.SERVER.noArg(), new PacketUpdateTradingInventory(1));});
	     return super.slotClick(slotId, dragType, clickTypeIn, player);
	}
}
