package dev.maeiro.quickstackbackpackscompat;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CompatConfig {
	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT = specPair.getLeft();
		CLIENT_SPEC = specPair.getRight();
	}

	private CompatConfig() {
	}

	public static class Client {
		public final ForgeConfigSpec.BooleanValue includePlayerInventory;

		Client(ForgeConfigSpec.Builder builder) {
			builder.push("general");

			includePlayerInventory = builder
					.comment("When true, backpack quickstack also moves items from player inventory.")
					.translation("quickstackbackpackscompat.config.include_player_inventory")
					.define("Include Player Inventory", false);

			builder.pop();
		}
	}
}
