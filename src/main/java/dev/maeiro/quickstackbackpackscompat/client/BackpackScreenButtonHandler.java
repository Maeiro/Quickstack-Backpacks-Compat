package dev.maeiro.quickstackbackpackscompat.client;

import dev.maeiro.quickstackbackpackscompat.CompatConfig;
import dev.maeiro.quickstackbackpackscompat.QuickstackBackpacksCompat;
import dev.maeiro.quickstackbackpackscompat.client.position.ButtonPositionManager;
import dev.maeiro.quickstackbackpackscompat.client.position.model.ButtonPositionConfig;
import dev.maeiro.quickstackbackpackscompat.networking.C2SBackpackQuickstackRequest;
import dev.maeiro.quickstackbackpackscompat.networking.CompatPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import tfar.quickstack.client.ClientUtils;
import tfar.quickstack.config.DropOffConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = QuickstackBackpacksCompat.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class BackpackScreenButtonHandler {
	private BackpackScreenButtonHandler() {
	}

	@SubscribeEvent
	public static void onScreenInit(ScreenEvent.Init.Post event) {
		if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen) || !canDisplay(containerScreen)) {
			return;
		}
		if (!(containerScreen.getMenu() instanceof BackpackContainer backpackContainer)) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return;
		}

		String itemProfileKey = resolveItemProfileKey(backpackContainer);
		ButtonPositionManager.ResolvedProfile resolvedProfile = ButtonPositionManager.INSTANCE.resolveProfile(itemProfileKey);
		ButtonCoordinates coordinates = resolveButtonCoordinates(containerScreen, backpackContainer, resolvedProfile);

		if (DropOffConfig.Client.enableDump.get()) {
			Button dump = Button.builder(Component.literal("^"), button -> actionPerformed(true))
					.pos(coordinates.dumpX(), coordinates.dumpY())
					.size(10, 14)
					.tooltip(Tooltip.create(Component.translatable("dropoff.dump_nearby")))
					.build();
			event.addListener(dump);
		}

		Button deposit = Button.builder(Component.literal("^"), button -> actionPerformed(false))
				.pos(coordinates.depositX(), coordinates.depositY())
				.size(10, 14)
				.tooltip(Tooltip.create(Component.translatable("dropoff.quick_stack")))
				.build();
		event.addListener(deposit);
	}

	private static boolean canDisplay(AbstractContainerScreen<?> screen) {
		if (!DropOffConfig.Client.showInventoryButton.get()) {
			return false;
		}
		// Avoid querying menu type on screens that don't provide it (e.g. vanilla inventory).
		return screen instanceof BackpackScreen || screen.getMenu() instanceof BackpackContainer;
	}

	private static void actionPerformed(boolean dump) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return;
		}
		if (minecraft.player.isSpectator()) {
			ClientUtils.printToChat("Action not allowed in spectator mode.");
			return;
		}

		List<BlockEntityType<?>> blockEntityBlacklist = DropOffConfig.blockEntityBlacklist == null
				? Collections.emptyList()
				: new ArrayList<>(DropOffConfig.blockEntityBlacklist);

		C2SBackpackQuickstackRequest packet = new C2SBackpackQuickstackRequest(
				dump,
				CompatConfig.CLIENT.includePlayerInventory.get(),
				DropOffConfig.Client.ignoreHotBar.get(),
				blockEntityBlacklist,
				DropOffConfig.Client.minSlotCount.get());
		CompatPacketHandler.INSTANCE.sendToServer(packet);
	}

	private static String resolveItemProfileKey(BackpackContainer backpackContainer) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return ButtonPositionConfig.WILDCARD_PROFILE_KEY;
		}

		try {
			ItemStack backpackStack = backpackContainer.getBackpackContext().getBackpackWrapper(minecraft.player).getBackpack();
			if (backpackStack.isEmpty()) {
				return ButtonPositionConfig.WILDCARD_PROFILE_KEY;
			}
			ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(backpackStack.getItem());
			if (itemId == null) {
				return ButtonPositionConfig.WILDCARD_PROFILE_KEY;
			}
			return itemId.toString();
		} catch (Exception e) {
			QuickstackBackpacksCompat.LOGGER.warn("Failed to resolve backpack item id for button profile.", e);
			return ButtonPositionConfig.WILDCARD_PROFILE_KEY;
		}
	}

	private static ButtonCoordinates resolveButtonCoordinates(AbstractContainerScreen<?> screen,
															  BackpackContainer menu,
															  ButtonPositionManager.ResolvedProfile profile) {
		AnchorCoordinates anchorCoordinates = resolveAnchorCoordinates(screen, menu, profile.anchor());
		int dumpX = anchorCoordinates.x() + profile.offsetX();
		int dumpY = anchorCoordinates.y() + profile.offsetY();
		int depositX = dumpX + profile.buttonSpacingX();
		return new ButtonCoordinates(dumpX, dumpY, depositX, dumpY);
	}

	private static AnchorCoordinates resolveAnchorCoordinates(AbstractContainerScreen<?> screen,
															  BackpackContainer menu,
															  ButtonPositionManager.Anchor anchor) {
		if (anchor != ButtonPositionManager.Anchor.STORAGE_TOP_RIGHT) {
			return fallbackAnchor(screen);
		}

		int storageSlots = menu.getNumberOfStorageInventorySlots();
		if (storageSlots <= 0 || menu.slots.size() < storageSlots) {
			return fallbackAnchor(screen);
		}

		int maxStorageX = Integer.MIN_VALUE;
		int minStorageY = Integer.MAX_VALUE;

		for (int slotIndex = 0; slotIndex < storageSlots; slotIndex++) {
			Slot slot = menu.slots.get(slotIndex);
			maxStorageX = Math.max(maxStorageX, slot.x);
			minStorageY = Math.min(minStorageY, slot.y);
		}

		if (maxStorageX == Integer.MIN_VALUE || minStorageY == Integer.MAX_VALUE) {
			return fallbackAnchor(screen);
		}

		return new AnchorCoordinates(
				screen.getGuiLeft() + maxStorageX + 18,
				screen.getGuiTop() + minStorageY
		);
	}

	private static AnchorCoordinates fallbackAnchor(AbstractContainerScreen<?> screen) {
		return new AnchorCoordinates(screen.getGuiLeft() + 80, screen.getGuiTop() + 80);
	}

	private record AnchorCoordinates(int x, int y) {
	}

	private record ButtonCoordinates(int dumpX, int dumpY, int depositX, int depositY) {
	}
}
