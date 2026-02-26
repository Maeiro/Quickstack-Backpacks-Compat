package dev.maeiro.quickstackbackpackscompat.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.networking.PacketBufferExt;
import tfar.quickstack.util.ItemStackUtils;
import tfar.quickstack.util.SuccedableInventoryData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class C2SBackpackQuickstackRequest {
	private boolean dump;
	private boolean includePlayerInventory;
	private boolean ignoreHotbar;
	private List<BlockEntityType<?>> blacklistedBlockEntities = new ArrayList<>();
	private int minSlotCount;

	private int itemsCounter;

	public C2SBackpackQuickstackRequest() {
	}

	public C2SBackpackQuickstackRequest(FriendlyByteBuf buf) {
		PacketBufferExt ext = new PacketBufferExt(buf);
		dump = ext.readBoolean();
		includePlayerInventory = ext.readBoolean();
		ignoreHotbar = ext.readBoolean();
		blacklistedBlockEntities = ext.readRegistryIdArray();
		minSlotCount = ext.readInt();
	}

	public C2SBackpackQuickstackRequest(boolean dump, boolean includePlayerInventory, boolean ignoreHotbar,
										List<BlockEntityType<?>> blacklistedBlockEntities, int minSlotCount) {
		this.dump = dump;
		this.includePlayerInventory = includePlayerInventory;
		this.ignoreHotbar = ignoreHotbar;
		this.blacklistedBlockEntities = new ArrayList<>(blacklistedBlockEntities);
		this.minSlotCount = minSlotCount;
	}

	public void encode(FriendlyByteBuf buf) {
		PacketBufferExt ext = new PacketBufferExt(buf);
		ext.writeBoolean(dump);
		ext.writeBoolean(includePlayerInventory);
		ext.writeBoolean(ignoreHotbar);
		ext.writeRegistryIdArray(blacklistedBlockEntities);
		ext.writeInt(minSlotCount);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null || !(player.containerMenu instanceof BackpackContainer backpackContainer)) {
				return;
			}

			Set<SuccedableInventoryData> nearbyInventories = getNearbyInventories(player);
			IItemHandler backpackStacks = backpackContainer.getBackpackContext()
					.getBackpackWrapper(player)
					.getInventoryHandler();
			IItemHandler playerStacks = includePlayerInventory ? new InvWrapper(player.getInventory()) : null;

			nearbyInventories.forEach(inventoryData -> inventoryData.blockEntity
					.getCapability(ForgeCapabilities.ITEM_HANDLER)
					.ifPresent(target -> {
						if (dump) {
							dropOff(backpackStacks, 0, backpackStacks.getSlots(), target, inventoryData);
							if (playerStacks != null) {
								dropOff(playerStacks, ignoreHotbar ? 9 : 0, Math.min(36, playerStacks.getSlots()), target, inventoryData);
							}
						} else {
							dropOffExisting(backpackStacks, 0, backpackStacks.getSlots(), target, inventoryData);
							if (playerStacks != null) {
								dropOffExisting(playerStacks, ignoreHotbar ? 9 : 0, Math.min(36, playerStacks.getSlots()), target, inventoryData);
							}
						}
					}));

			player.containerMenu.broadcastChanges();

			List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
			int affectedContainers = 0;

			for (SuccedableInventoryData inventoryData : nearbyInventories) {
				int color;
				if (inventoryData.success) {
					affectedContainers++;
					color = 0x00FF00;
				} else {
					color = 0xFF0000;
				}
				rendererCubeTargets.add(new RendererCubeTarget(inventoryData.blockEntity.getBlockPos(), color));
			}

			CompatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new S2CBackpackQuickstackReport(itemsCounter, affectedContainers, nearbyInventories.size(), rendererCubeTargets));
		});
		ctx.get().setPacketHandled(true);
	}

	private void dropOff(IItemHandler source, int startSlotInclusive, int endSlotExclusive, IItemHandler target, SuccedableInventoryData data) {
		int end = Math.min(endSlotExclusive, source.getSlots());
		for (int i = startSlotInclusive; i < end; i++) {
			ItemStack sourceStack = source.getStackInSlot(i);
			if (sourceStack.isEmpty() || ItemStackUtils.isFavorited(sourceStack)) {
				continue;
			}

			data.setSuccessful();
			itemsCounter += sourceStack.getCount();

			ItemStack remainder = source.extractItem(i, Integer.MAX_VALUE, false);
			for (int j = 0; j < target.getSlots(); j++) {
				remainder = target.insertItem(j, remainder, false);
				if (remainder.isEmpty()) {
					break;
				}
			}
			if (!remainder.isEmpty()) {
				itemsCounter -= remainder.getCount();
				source.insertItem(i, remainder, false);
			}
		}
	}

	private void dropOffExisting(IItemHandler source, int startSlotInclusive, int endSlotExclusive, IItemHandler target,
								 SuccedableInventoryData data) {
		int end = Math.min(endSlotExclusive, source.getSlots());
		for (int i = startSlotInclusive; i < end; i++) {
			ItemStack sourceStack = source.getStackInSlot(i);
			if (sourceStack.isEmpty() || ItemStackUtils.isFavorited(sourceStack)) {
				continue;
			}

			boolean hasExistingStack = IntStream.range(0, target.getSlots())
					.mapToObj(target::getStackInSlot)
					.filter(existing -> !existing.isEmpty())
					.anyMatch(existing -> existing.getItem() == sourceStack.getItem());
			if (!hasExistingStack) {
				continue;
			}

			data.setSuccessful();
			itemsCounter += sourceStack.getCount();

			ItemStack remainder = source.extractItem(i, Integer.MAX_VALUE, false);
			int[] emptySlots = new int[target.getSlots()];
			int numEmptySlots = 0;

			for (int j = 0; j < target.getSlots(); j++) {
				if (target.getStackInSlot(j).isEmpty()) {
					emptySlots[numEmptySlots] = j;
					numEmptySlots++;
				}

				if (remainder.getItem() != target.getStackInSlot(j).getItem()) {
					continue;
				}

				remainder = target.insertItem(j, remainder, false);
				if (remainder.isEmpty()) {
					break;
				}
			}

			for (int j = 0; j < numEmptySlots; j++) {
				remainder = target.insertItem(emptySlots[j], remainder, false);
				if (remainder.isEmpty()) {
					break;
				}
			}

			if (!remainder.isEmpty()) {
				itemsCounter -= remainder.getCount();
				source.insertItem(i, remainder, false);
			}
		}
	}

	private Set<SuccedableInventoryData> getNearbyInventories(ServerPlayer player) {
		double playerX = player.position().x;
		double playerY = player.position().y;
		double playerZ = player.position().z;

		int scanRadius = tfar.quickstack.config.DropOffConfig.scanRadius.get();
		int minX = (int) (playerX - scanRadius);
		int maxX = (int) (playerX + scanRadius);
		int minY = (int) (playerY - scanRadius);
		int maxY = (int) (playerY + scanRadius);
		int minZ = (int) (playerZ - scanRadius);
		int maxZ = (int) (playerZ + scanRadius);

		Set<BlockEntityType<?>> blacklist = new HashSet<>(blacklistedBlockEntities);
		Level level = player.level();

		return BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ)
				.map(level::getBlockEntity)
				.filter(Objects::nonNull)
				.filter(tileEntity -> tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
						.filter(iItemHandler -> iItemHandler.getSlots() >= minSlotCount)
						.isPresent())
				.filter(tileEntity -> !blacklist.contains(tileEntity.getType()))
				.map(SuccedableInventoryData::new)
				.collect(Collectors.toSet());
	}
}
