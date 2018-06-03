package de.howaner.netflowcollector.types;

import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class FlowSet {
	private final int id;
	private final ByteBuf data;

	public FlowSetType getType() {
		if (this.id == 0)
			return FlowSetType.TEMPLATE;
		else if (this.id == 1)
			return FlowSetType.TEMPLATE_OPTIONS;
		else if (this.id > 255)
			return FlowSetType.DATA;
		else
			return FlowSetType.UNKNOWN;
	}

	public static enum FlowSetType {
		TEMPLATE, TEMPLATE_OPTIONS, DATA, UNKNOWN
	}

}
