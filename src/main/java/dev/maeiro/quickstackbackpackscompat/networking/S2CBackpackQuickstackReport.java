package dev.maeiro.quickstackbackpackscompat.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.networking.PacketBufferExt;
import tfar.quickstack.task.ReportTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CBackpackQuickstackReport {
	private int itemsCounter;
	private int affectedContainers;
	private int totalContainers;
	private List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();

	public S2CBackpackQuickstackReport() {
	}

	public S2CBackpackQuickstackReport(FriendlyByteBuf buf) {
		itemsCounter = buf.readInt();
		affectedContainers = buf.readInt();
		totalContainers = buf.readInt();

		PacketBufferExt ext = new PacketBufferExt(buf);
		rendererCubeTargets = ext.readRendererCubeTargets();
	}

	public S2CBackpackQuickstackReport(int itemsCounter, int affectedContainers, int totalContainers,
									   List<RendererCubeTarget> rendererCubeTargets) {
		this.itemsCounter = itemsCounter;
		this.affectedContainers = affectedContainers;
		this.totalContainers = totalContainers;
		this.rendererCubeTargets = rendererCubeTargets;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(itemsCounter);
		buf.writeInt(affectedContainers);
		buf.writeInt(totalContainers);

		PacketBufferExt ext = new PacketBufferExt(buf);
		ext.writeRendererCubeTargets(rendererCubeTargets);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ReportTask reportTask = new ReportTask(itemsCounter, affectedContainers, totalContainers, rendererCubeTargets);
			reportTask.run();
		});
		ctx.get().setPacketHandled(true);
	}
}
