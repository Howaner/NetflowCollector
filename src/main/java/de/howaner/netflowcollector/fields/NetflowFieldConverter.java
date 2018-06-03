package de.howaner.netflowcollector.fields;

import de.howaner.netflowcollector.types.TemplateField;
import io.netty.buffer.ByteBuf;

public interface NetflowFieldConverter {

	public Object run(ByteBuf buf, TemplateField field);

}
