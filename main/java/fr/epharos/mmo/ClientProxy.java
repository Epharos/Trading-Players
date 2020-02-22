package fr.epharos.mmo;

import net.minecraft.client.gui.ScreenManager;

public class ClientProxy extends Proxy 
{
	public ClientProxy()
	{
		ScreenManager.<ContainerTrading, GuiTrading>registerFactory(ContainerTrading._TYPE, (container, inv, title) -> { return new GuiTrading(container, inv, title); });
		Trading._LOGGER.info("Registered trading GUI");
	}
}
