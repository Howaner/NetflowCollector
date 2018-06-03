package de.howaner.netflowcollector.exception;

import de.howaner.netflowcollector.types.TemplateField;
import io.netty.buffer.ByteBuf;

public class ConvertException extends RuntimeException {

	public ConvertException(ByteBuf buf, TemplateField field, String message) {
		super("Field convert exception | " + message + " | field type: " + field.getTypeId() + ", length: " + field.getLength() + ", data: " + buf);
	}

}
