package fr.epharos.tradingplayers.util;

import net.minecraftforge.common.ForgeConfigSpec;

public class TradingPlayersConfig 
{
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	
	public static final ForgeConfigSpec.ConfigValue<Double> MAX_TRADING_RANGE;
	public static final ForgeConfigSpec.ConfigValue<Integer> PENDING_TIME;
	
	static
	{
		BUILDER.push("Config file for Trading Players");
		
		MAX_TRADING_RANGE = BUILDER.comment("Maximum range (squared) for two players to be able to trade")
				.define("Max range (squared)", 64.0d);
		
		PENDING_TIME = BUILDER.comment("Number of seconds before a pending trade offer expires")
				.define("Number of seconds", 30);
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
