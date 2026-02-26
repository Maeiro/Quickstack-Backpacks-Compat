package dev.maeiro.quickstackbackpackscompat.networking;

import dev.maeiro.quickstackbackpackscompat.QuickstackBackpacksCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class CompatPacketHandler {
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(QuickstackBackpacksCompat.MOD_ID, "network"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	private CompatPacketHandler() {
	}

	public static void register() {
		int id = 0;
		INSTANCE.registerMessage(id++, C2SBackpackQuickstackRequest.class,
				C2SBackpackQuickstackRequest::encode,
				C2SBackpackQuickstackRequest::new,
				C2SBackpackQuickstackRequest::handle);

		INSTANCE.registerMessage(id, S2CBackpackQuickstackReport.class,
				S2CBackpackQuickstackReport::encode,
				S2CBackpackQuickstackReport::new,
				S2CBackpackQuickstackReport::handle);
	}
}
