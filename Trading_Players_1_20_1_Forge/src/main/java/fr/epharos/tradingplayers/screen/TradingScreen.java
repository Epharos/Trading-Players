package fr.epharos.tradingplayers.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.epharos.tradingplayers.core.TradingPlayers;
import fr.epharos.tradingplayers.network.PacketHandler;
import fr.epharos.tradingplayers.network.PacketUpdateTradingStateServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.components.Button;
import net.minecraftforge.network.PacketDistributor;

import javax.naming.spi.ResolveResult;

public class TradingScreen extends AbstractContainerScreen<TradingContainer> 
{

	static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
	private boolean playerAcceptance = false, traderAcceptance = false;
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TradingPlayers._MODID, "textures/gui/trade.png");
	private static final ResourceLocation playerHead = Minecraft.getInstance().player.getSkinTextureLocation();
	public ResourceLocation traderHead = null;
	
	public TradingScreen.ConfirmButton confirmButton;
	public TradingScreen.CancelButton cancelButton;
	
	public TradingScreen(TradingContainer container, Inventory inventory, Component c) 
	{
		super(container, inventory, Component.translatable("trade.name"));
	}
	
	protected void init() 
	{
		this.imageWidth = 257;
		this.imageHeight = 216;
		super.init();
		
		this.addRenderableWidget(confirmButton = new TradingScreen.ConfirmButton(leftPos + 7, topPos + 187));
		this.addRenderableWidget(cancelButton = new TradingScreen.CancelButton(leftPos + 33, topPos + 187));
		this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button ->  PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 2)))
				.bounds(leftPos + 59, topPos + 188, 108, 20)
				.build());
		
		cancelButton.active = false;
		
		PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 3));
	}

	protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {

	}

	@Override
	protected void renderBg(GuiGraphics pose, float p_97788_, int p_97789_, int p_97790_)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		pose.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, 512, 256);

		if(playerAcceptance)
			pose.blit(BACKGROUND_TEXTURE, leftPos + 3, topPos + 3, 0, 257.f, 19.f, 22, 19, 512, 256);
		else
			pose.blit(BACKGROUND_TEXTURE, leftPos + 3, topPos + 3, 0, 257.f, 0.f, 22, 19, 512, 256);

		if(traderAcceptance)
			pose.blit(BACKGROUND_TEXTURE, leftPos + 232, topPos + 3, 0, 257.f, 19.f, 22, 19, 512, 256);
		else
			pose.blit(BACKGROUND_TEXTURE, leftPos + 232, topPos + 3, 0, 257.f, 0.f, 22, 19, 512, 256);

		pose.blit(playerHead, leftPos + 6, topPos + 5, 0, 16.f, 16.f, 16, 16, 128, 128);

		if(traderHead != null)
		{
			pose.blit(traderHead, leftPos + 235, topPos + 5, 0, 16.f, 16.f, 16, 16, 128, 128);
		}

		cancelButton.active = playerAcceptance;
		confirmButton.active = !playerAcceptance;
	}
	
	/**
	 * Toggles the trader acceptance to its current opposite
	 */
	public void toggleTraderAcceptance() 
	{
		traderAcceptance = !traderAcceptance;	
	}
	
	/**
	 * Sets the trader acceptance to the given value
	 * @param b
	 */
	public void setTraderAcceptance(boolean b)
	{
		traderAcceptance = b;
	}
	
	/**
	 * Toggles the player acceptance to its current opposite
	 */
	public void togglePlayerAcceptance()
	{
		playerAcceptance = !playerAcceptance;
	}

	/**
	 * Sets the player acceptance to the given value
	 * @param b
	 */
	public void setPlayerAcceptance(boolean b) 
	{
		playerAcceptance = b;
	}
	
	@OnlyIn(Dist.CLIENT)
	class CancelButton extends TradingScreen.SpriteButton 
	{
		public CancelButton(int p_i50829_2_, int p_i50829_3_) 
		{
			super(p_i50829_2_, p_i50829_3_, 112, 220);
			this.setTooltip(Tooltip.create(Component.translatable("gui.cancel")));
		}

		public void onPress() 
		{
			PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 0));
			playerAcceptance = !playerAcceptance;
		}

		protected void updateWidgetNarration(NarrationElementOutput p_259858_) {}
	}

	@OnlyIn(Dist.CLIENT)
	class ConfirmButton extends TradingScreen.SpriteButton {

		public ConfirmButton(int p_i50828_2_, int p_i50828_3_) 
		{
			super(p_i50828_2_, p_i50828_3_, 90, 220);
			this.setTooltip(Tooltip.create(Component.translatable("gui.done")));
		}

		public void onPress() 
		{
			PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 0));
			playerAcceptance = !playerAcceptance;
		}

		protected void updateWidgetNarration(NarrationElementOutput p_259858_) {}
	}
   
	@OnlyIn(Dist.CLIENT)
	abstract static class SpriteButton extends TradingScreen.TradingButton
	{
		private final int iconX;
		private final int iconY;

		protected SpriteButton(int p_i50825_1_, int p_i50825_2_, int x, int y)
		{
			super(p_i50825_1_, p_i50825_2_);
			this.iconX = x;
			this.iconY = y;
		}

		protected void renderSpritedButton(GuiGraphics stack)
		{
			stack.blit(TradingScreen.BEACON_LOCATION, this.getX() + 2, this.getY() + 2, this.iconX, this.iconY, 18, 18);
     	}
	}
	
	@OnlyIn(Dist.CLIENT)
	abstract static class TradingButton extends AbstractButton
	{
		private boolean selected;

		protected TradingButton(int x, int y) {
			super(x, y, 22, 22, Component.empty());
		}

		public void renderWidget(GuiGraphics graphics, int p_93677_, int p_93678_, float p_93679_)
		{
			int i = 219;
			int j = 0;
			if (!this.active) {
				j += this.width * 2;
			} else if (this.selected) {
				j += this.width * 1;
			} else if (this.isHoveredOrFocused()) {
				j += this.width * 3;
			}

			graphics.blit(TradingScreen.BEACON_LOCATION, this.getX(), this.getY(), j, 219, this.width, this.height);
			this.renderSpritedButton(graphics);
		}

		protected abstract void renderSpritedButton(GuiGraphics stack);

		public boolean isSelected() 
		{
			return this.selected;
		}

		public void setSelected(boolean selectedIn) 
		{
			this.selected = selectedIn;
		}
	}
	
	protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) { }

}
