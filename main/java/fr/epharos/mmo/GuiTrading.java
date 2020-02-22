package fr.epharos.mmo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class GuiTrading extends ContainerScreen<ContainerTrading> implements IHasContainer<ContainerTrading>
{
	private boolean playerAcceptation = false, traderAcceptation = false;
	private static final ResourceLocation background = new ResourceLocation(Trading._MODID, "textures/gui/trade.png");
	private static final ResourceLocation playerHead = Minecraft.getInstance().player.getLocationSkin();
	public ResourceLocation traderHead = null;
	private static final ResourceLocation BEACON_GUI_TEXTURES = new ResourceLocation("textures/gui/container/beacon.png");
	
	public GuiTrading.ConfirmButton confirmButton;
	public GuiTrading.CancelButton cancelButton;
	
	public GuiTrading(ContainerTrading screenContainer, PlayerInventory inv, ITextComponent title) 
	{
		super(screenContainer, inv, title);
		Minecraft.getInstance().player.sendMessage(new StringTextComponent("Le gui est ouvert !"));
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
	      this.renderBackground();
	      super.render(p_render_1_, p_render_2_, p_render_3_);
	      this.renderHoveredToolTip(p_render_1_, p_render_2_);
	   }
	
	protected void init() 
	{
		super.init();
		xSize = 257;
		ySize = 216;
		Trading._CHANNEL.send(PacketDistributor.SERVER.noArg(), new PacketTraderName(-1));
		guiLeft = (this.width - this.xSize) / 2;
		guiTop = (this.height - this.ySize) / 2;
		
		this.addButton(confirmButton = new GuiTrading.ConfirmButton(guiLeft + 7, guiTop + 187));
		this.addButton(cancelButton = new GuiTrading.CancelButton(guiLeft + 33, guiTop + 187));
		this.addButton(new net.minecraft.client.gui.widget.button.Button(guiLeft + 59, guiTop + 188, 108, 20, I18n.format("gui.cancel"), (button) ->  {Trading._CHANNEL.send(PacketDistributor.SERVER.noArg(), new PacketUpdateTradingInventory(2));}));
		
		cancelButton.active = false;
	}
	
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) 
	{		
		for(Widget widget : this.buttons) 
		{
			if (widget.isHovered()) 
			{
				widget.renderToolTip(mouseX - this.guiLeft, mouseY - this.guiTop);
				break;
			}
		}
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) 
	{
		this.getMinecraft().getTextureManager().bindTexture(background);
		blit(guiLeft, guiTop, this.getBlitOffset(), 0.0f, 0.0f, xSize, ySize, 256, 512);
		
		if(playerAcceptation)
			blit(guiLeft + 3, guiTop + 3, this.getBlitOffset(), 257.f, 19.f, 22, 19, 256, 512);
		else
			blit(guiLeft + 3, guiTop + 3, this.getBlitOffset(), 257.f, 0.f, 22, 19, 256, 512);
		
		if(traderAcceptation)
			blit(guiLeft + 232, guiTop + 3, this.getBlitOffset(), 257.f, 19.f, 22, 19, 256, 512);
		else
			blit(guiLeft + 232, guiTop + 3, this.getBlitOffset(), 257.f, 0.f, 22, 19, 256, 512);
		
		this.getMinecraft().getTextureManager().bindTexture(playerHead);
		blit(guiLeft + 6, guiTop + 5, this.getBlitOffset(), 16.f, 16.f, 16, 16, 128, 128);
		
		if(traderHead != null)
		{
			this.getMinecraft().getTextureManager().bindTexture(traderHead);
			blit(guiLeft + 235, guiTop + 5, this.getBlitOffset(), 16.f, 16.f, 16, 16, 128, 128);
		}
		
		cancelButton.active = playerAcceptation;
		confirmButton.active = !playerAcceptation;
	}

	/**
	 * Toggles the trader acceptance to it's current opposite 
	 */
	public void toggleTraderAcceptation() 
	{
		traderAcceptation = !traderAcceptation;	
	}
	
	/**
	 * Toggles the trader acceptance to the given value
	 * @param b
	 */
	public void toggleTraderAcceptation(boolean b)
	{
		traderAcceptation = b;
	}
	
	/**
	 * Toggles the player acceptance to it's current opposite
	 */
	public void togglePlayerAcceptation()
	{
		playerAcceptation = !playerAcceptation;
	}

	/**
	 * Toggles the player acceptance to the given value
	 * @param b
	 */
	public void togglePlayerAcceptation(boolean b) 
	{
		playerAcceptation = b;
	}
	
	@OnlyIn(Dist.CLIENT)
	class CancelButton extends GuiTrading.SpriteButton 
	{
		public CancelButton(int p_i50829_2_, int p_i50829_3_) 
		{
			super(p_i50829_2_, p_i50829_3_, 112, 220);
		}

		public void onPress() 
		{
			Trading._CHANNEL.send(PacketDistributor.SERVER.noArg(), new PacketUpdateTradingInventory(0));
			playerAcceptation = !playerAcceptation;
		}

		public void renderToolTip(int p_renderToolTip_1_, int p_renderToolTip_2_) 
		{
			GuiTrading.this.renderTooltip(I18n.format("gui.cancel"), p_renderToolTip_1_, p_renderToolTip_2_);
		}
	}

	@OnlyIn(Dist.CLIENT)
	class ConfirmButton extends GuiTrading.SpriteButton {
		public ConfirmButton(int p_i50828_2_, int p_i50828_3_) 
		{
			super(p_i50828_2_, p_i50828_3_, 90, 220);
		}

		public void onPress() 
		{
			Trading._CHANNEL.send(PacketDistributor.SERVER.noArg(), new PacketUpdateTradingInventory(0));
			playerAcceptation = !playerAcceptation;
		}

		public void renderToolTip(int p_renderToolTip_1_, int p_renderToolTip_2_) {
			GuiTrading.this.renderTooltip(I18n.format("gui.done"), p_renderToolTip_1_, p_renderToolTip_2_);
		}
	}
   
	@OnlyIn(Dist.CLIENT)
	abstract static class SpriteButton extends GuiTrading.Button 
	{
		private final int field_212948_a;
		private final int field_212949_b;

		protected SpriteButton(int p_i50825_1_, int p_i50825_2_, int p_i50825_3_, int p_i50825_4_) 
		{
			super(p_i50825_1_, p_i50825_2_);
			this.field_212948_a = p_i50825_3_;
			this.field_212949_b = p_i50825_4_;
		}

		protected void renderSpritedButton() 
		{
			this.blit(this.x + 2, this.y + 2, this.field_212948_a, this.field_212949_b, 18, 18);
     	}
	}
	
	@OnlyIn(Dist.CLIENT)
	abstract static class Button extends AbstractButton 
	{
		private boolean selected;

		protected Button(int x, int y) {
			super(x, y, 22, 22, "");
		}

		public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) 
		{
			Minecraft.getInstance().getTextureManager().bindTexture(BEACON_GUI_TEXTURES);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			int i = 219;
			int j = 0;
			if (!this.active) {
				j += this.width * 2;
			} else if (this.selected) {
				j += this.width * 1;
			} else if (this.isHovered()) {
				j += this.width * 3;
			}

			this.blit(this.x, this.y, j, i, this.width, this.height);
			this.renderSpritedButton();
		}

		protected abstract void renderSpritedButton();

		public boolean isSelected() 
		{
			return this.selected;
		}

		public void setSelected(boolean selectedIn) 
		{
			this.selected = selectedIn;
		}
	}
}
