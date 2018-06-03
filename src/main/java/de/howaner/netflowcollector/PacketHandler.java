package de.howaner.netflowcollector;

import de.howaner.netflowcollector.types.Flow;
import de.howaner.netflowcollector.types.FlowSet;
import de.howaner.netflowcollector.types.NetflowPacket;
import de.howaner.netflowcollector.types.Template;
import de.howaner.netflowcollector.types.TemplateField;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.HashMap;
import java.util.Map;

public class PacketHandler extends SimpleChannelInboundHandler<NetflowPacket> {
	private Map<Integer, Template> templates = new HashMap<>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, NetflowPacket packet) throws Exception {
		NetflowCollector.getInstance().getLogger().info("Received {} flowsets from {}", packet.getFlowSets().size(), packet.getSender().getHostString());

		for (FlowSet flowset : packet.getFlowSets()) {
			ByteBuf buf = flowset.getData();

			switch (flowset.getType()) {
				case TEMPLATE:
					while (buf.isReadable(4)) {
						Template template = new Template();
						template.read(buf);

						templates.put(template.getId(), template);
						NetflowCollector.getInstance().getLogger().info("Received template {} with {} fields", template.getId(), template.getFields().size());
					}
					break;
				case TEMPLATE_OPTIONS:
					// TODO
					break;
				case DATA:
					Template template = this.templates.get(flowset.getId());
					if (template == null) {
						NetflowCollector.getInstance().getLogger().info("Unknown template {}", flowset.getId());
						flowset.getData().release();
						continue;
					}

					int flowSize = template.getSingleFlowSize();
					while (buf.isReadable(flowSize)) {
						ByteBuf[] fields = new ByteBuf[template.getFields().size()];
						for (int i = 0; i < template.getFields().size(); i++) {
							TemplateField field = template.getFields().get(i);

							ByteBuf fieldBuf = buf.slice(buf.readerIndex(), field.getLength());
							buf.skipBytes(field.getLength());
							fields[i] = fieldBuf;
						}

						Flow flow = new Flow(template, fields);
						NetflowCollector.getInstance().getDatabaseCache().addFlow(flow);

						/*NetflowCollector.getInstance().getLogger().info("Read flow with template {} and following data:", flow.getTemplate().getId());

						for (int i = 0; i < template.getFields().size(); i++) {
							NetflowCollector.getInstance().getLogger().info("  {}: {}", template.getFields().get(i).getName(), flow.getParsedValue(i));
						}
						NetflowCollector.getInstance().getLogger().info("");*/
					}
					break;
				case UNKNOWN:
					NetflowCollector.getInstance().getLogger().warn("Received flowset with unknown type {}", flowset.getId());
					break;
			}

			flowset.getData().release();
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("ACTIVE from " + ctx.channel().remoteAddress() + "!");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		System.out.println("INACTIVE!");
	}

}
