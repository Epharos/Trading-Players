package fr.epharos.tradingplayers.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.epharos.tradingplayers.core.TradingPlayers;
import fr.epharos.tradingplayers.network.PacketHandler;
import fr.epharos.tradingplayers.network.PacketUpdateTradingStateServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		super(container, inventory, new TranslatableComponent("trade.name"));
	}
	
	protected void init() 
	{
		this.imageWidth = 257;
		this.imageHeight = 216;
		super.init();
		
		this.addRenderableWidget(confirmButton = new TradingScreen.ConfirmButton(leftPos + 7, topPos + 187));
		this.addRenderableWidget(cancelButton = new TradingScreen.CancelButton(leftPos + 33, topPos + 187));
		this.addRenderableWidget(new net.minecraft.client.gui.components.Button(leftPos + 59, topPos + 188, 108, 20, new TranslatableComponent("gui.cancel"), (button) ->  {PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 2));}));
		
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
			blit(pose, leftPos + 3, topPos + 3, this.getBlitOffset(), 257.f, 19.f, 22, 19, 512, 256);
		else
			blit(pose, leftPos + 3, topPos + 3, this.getBlitOffset(), 257.f, 0.f, 22, 19, 512, 256);
		
		if(traderAcceptance)
			blit(pose, leftPos + 232, topPos + 3, this.getBlitOffset(), 257.f, 19.f, 22, 19, 512, 256);
		else
			blit(pose, leftPos + 232, topPos + 3, this.getBlitOffset(), 257.f, 0.f, 22, 19, 512, 256);
		
		RenderSystem.setShaderTexture(0, playerHead);
		blit(pose, leftPos + 6, topPos + 5, this.getBlitOffset(), 16.f, 16.f, 16, 16, 128, 128);
		
		if(traderHead != null)
		{
			RenderSystem.setShaderTexture(0, traderHead);
			blit(pose, leftPos + 235, topPos + 5, this.getBlitOffset(), 16.f, 16.f, 16, 16, 128, 128);
		}
		
		cancelButton.active = playerAcceptance;
		confirmButton.active = !playerAcceptance;
	}
	
	/**
	 * Toggles the trader acceptance to it's current opposite 
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
	 * Toggles the player acceptance to it's current opposite
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
		}

		public void onPress() 
		{
			PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 0));
			playerAcceptance = !playerAcceptance;
		}
		
		public void renderToolTip(PoseStack stack, int mouseX, int mouseY) {
			List<Component> l = new ArrayList<>();
			l.add(new TranslatableComponent("gui.cancel"));
			TradingScreen.this.renderComponentTooltip(stack, l, mouseX, mouseY, font);
			super.renderToolTip(stack, mouseX, mouseY);
		}

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			// TODO Auto-generated method stub
			
		}
	}

	@OnlyIn(Dist.CLIENT)
	class ConfirmButton extends TradingScreen.SpriteButton {
		public ConfirmButton(int p_i50828_2_, int p_i50828_3_) 
		{
			super(p_i50828_2_, p_i50828_3_, 90, 220);
		}

		public void onPress() 
		{
			PacketHandler.INSTANCE.sendToServer(new PacketUpdateTradingStateServer((byte) 0));
			playerAcceptance = !playerAcceptance;
		}

		public void renderToolTip(PoseStack stack, int mouseX, int mouseY) {
			List<Component> l = new ArrayList<>();
			l.add(new TranslatableComponent("gui.done"));
			TradingScreen.this.renderComponentTooltip(stack, l, mouseX, mouseY, font);
			super.renderToolTip(stack, mouseX, mouseY);
		}

		public void updateNarration(NarrationElementOutput p_169152_) { }
	}
   
	@OnlyIn(Dist.CLIENT)
	abstract static class SpriteButton extends TradingScreen.Button 
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
			this.blit(stack, this.x + 2, this.y + 2, this.field_212948_a, this.field_212949_b, 18, 18);
     	}
	}
	
	@OnlyIn(Dist.CLIENT)
	abstract static class Button extends AbstractButton 
	{
		private boolean selected;

		protected Button(int x, int y) {
			super(x, y, 22, 22, null);
		}

		public void renderButton(PoseStack stack, int p_93677_, int p_93678_, float p_93679_)
		{
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, BEACON_GUI_TEXTURES);
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			int i = 219;
			int j = 0;
			if (!this.active) {
				j += this.width * 2;
			} else if (this.selected) {
				j += this.width * 1;
			} else if (this.isHovered) {
				j += this.width * 3;
			}

			this.blit(stack, this.x, this.y, j, i, this.width, this.height);
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
