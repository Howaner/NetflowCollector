package de.howaner.netflowcollector.handler;

import de.howaner.netflowcollector.types.FlowSet;
import de.howaner.netflowcollector.types.NetflowPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.io.IOException;
import java.util.List;

public class FlowSetDeserializer extends MessageToMessageDecoder<DatagramPacket> {

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket in, List<Object> out) throws Exception {
		ByteBuf buf = in.content();

		short version = buf.readShort();
		int count = buf.readUnsignedShort();
		long uptime = buf.readUnsignedInt();
		long timestamp = buf.readUnsignedInt();
		long flowSequence = buf.readUnsignedInt();
		long sourceId = buf.readUnsignedInt();

		NetflowPacket packet = NetflowPacket.builder()
				.version(version)
				.count(count)
				.uptime(uptime)
				.timestamp(timestamp)
				.flowSequence(flowSequence)
				.sourceId(sourceId)
				.sender(in.sender())
				.build();

		while (buf.isReadable()) {
			int flowsetId = buf.readUnsignedShort();
			int flowsetDataLength = buf.readUnsignedShort() - 4;

			if (!buf.isReadable(flowsetDataLength))
				throw new IOException(String.format("Flowset is larger than packet size (flowset id: %d, flowset length: %d, offset: %d, readable bytes: %d)", flowsetId, flowsetDataLength, buf.readerIndex(), buf.readableBytes()));

			ByteBuf flowsetData = buf.slice(buf.readerIndex(), flowsetDataLength);
			buf.skipBytes(flowsetDataLength);
			flowsetData.retain();

			packet.getFlowSets().add(new FlowSet(flowsetId, flowsetData));
		}

		out.add(packet);
	}

}
