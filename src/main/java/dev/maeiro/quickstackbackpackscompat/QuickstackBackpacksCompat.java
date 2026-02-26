package dev.maeiro.quickstackbackpackscompat;

import dev.maeiro.quickstackbackpackscompat.networking.CompatPacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(QuickstackBackpacksCompat.MOD_ID)
public class QuickstackBackpacksCompat {
	public static final String MOD_ID = "quickstackbackpackscompat";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public QuickstackBackpacksCompat() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::onCommonSetup);

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CompatConfig.CLIENT_SPEC);
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(CompatPacketHandler::register);
	}
}
