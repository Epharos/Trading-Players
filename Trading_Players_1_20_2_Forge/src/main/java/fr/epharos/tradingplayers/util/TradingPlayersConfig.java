package fr.epharos.tradingplayers.util;

import com.example.examplemod.ExampleMod;
import fr.epharos.tradingplayers.core.TradingPlayers;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = TradingPlayers._MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TradingPlayersConfig 
{
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	public static final ForgeConfigSpec.ConfigValue<Double> MAX_TRADING_RANGE = BUILDER
			.comment("Maximum range (squared) for two players to be able to trade (64 = 8 blocks)")
			.define("Max range (squared)", 64.0d);
	public static final ForgeConfigSpec.ConfigValue<Integer> PENDING_TIME = BUILDER
			.comment("Number of seconds before a pending trade offer expires")
			.define("Number of seconds", 30);

	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public static double maxTradingRange;
	public static int pendingTime;

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event)
	{
		maxTradingRange = MAX_TRADING_RANGE.get();
		pendingTime = PENDING_TIME.get();
	}
}
