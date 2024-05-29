package fr.epharos.tradingplayers.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.epharos.tradingplayers.core.TradingPlayers;
import fr.epharos.tradingplayers.network.PacketHandler;
import fr.epharos.tradingplayers.network.PacketUpdateTradingStateServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.components.Button;

public class TradingScreen extends AbstractContainerScreen<TradingContainer> 
{

	private boolean playerAcceptance = false, traderAcceptance = false;
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(TradingPlayers._MODID, "textures/gui/trade.png");
	private static final ResourceLocation playerHead = Minecraft.getInstance().player.getSkinTextureLocation();
	public ResourceLocation traderHead = null;
	private static final ResourceLocation BEACON_GUI_TEXTURES = new ResourceLocation("textures/gui/container/beacon.png");
	
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

	protected void renderBg(PoseStack pose, float mouseX, int mouseY, int partialTick) 
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
		
		blit(pose, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, 512, 256);
		
		if(playerAcceptance)
			blit(pose, leftPos + 3, topPos + 3, 0, 257.f, 19.f, 22, 19, 512, 256);
		else
			blit(pose, leftPos + 3, topPos + 3, 0, 257.f, 0.f, 22, 19, 512, 256);
		
		if(traderAcceptance)
			blit(pose, leftPos + 232, topPos + 3, 0, 257.f, 19.f, 22, 19, 512, 256);
		else
			blit(pose, leftPos + 232, topPos + 3, 0, 257.f, 0.f, 22, 19, 512, 256);
		
		RenderSystem.setShaderTexture(0, playerHead);
		blit(pose, leftPos + 6, topPos + 5, 0, 16.f, 16.f, 16, 16, 128, 128);
		
		if(traderHead != null)
		{
			RenderSystem.setShaderTexture(0, traderHead);
			blit(pose, leftPos + 235, topPos + 5, 0, 16.f, 16.f, 16, 16, 128, 128);
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
		private final int field_212948_a;
		private final int field_212949_b;

		protected SpriteButton(int p_i50825_1_, int p_i50825_2_, int p_i50825_3_, int p_i50825_4_) 
		{
			super(p_i50825_1_, p_i50825_2_);
			this.field_212948_a = p_i50825_3_;
			this.field_212949_b = p_i50825_4_;
		}

		protected void renderSpritedButton(PoseStack stack) 
		{
			Screen.blit(stack, this.getX() + 2, this.getY() + 2, this.field_212948_a, this.field_212949_b, 18, 18);
     	}
	}
	
	@OnlyIn(Dist.CLIENT)
	abstract static class TradingButton extends AbstractButton
	{
		private boolean selected;

		protected TradingButton(int x, int y) {
			super(x, y, 22, 22, null);
		}

		public void renderWidget(PoseStack stack, int p_93677_, int p_93678_, float p_93679_)
		{
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, BEACON_GUI_TEXTURES);
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			int i = 219;
			int j = 0;
			if (!this.active) {
				j += this.width * 2;
			} else if (this.selected) {
				j += this.width;
			} else if (this.isHovered) {
				j += this.width * 3;
			}

			Screen.blit(stack, this.getX(), this.getY(), j, i, this.width, this.height);
			this.renderSpritedButton(stack);
		}

		protected abstract void renderSpritedButton(PoseStack stack);

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
