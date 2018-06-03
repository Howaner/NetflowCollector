package de.howaner.netflowcollector.types;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class Template {
	private int id;
	private List<TemplateField> fields = new ArrayList<>();
	@Getter private int singleFlowSize;

	public void read(ByteBuf buf) throws IOException {
		this.id = buf.readUnsignedShort();
		int fieldCount = buf.readUnsignedShort();

		if (!buf.isReadable(fieldCount * 4))
			throw new IOException(String.format("Template is larger than flowset size (template id: %d, field count: %d, flowset offset: %d, flowset readable bytes: %d",
					this.id, fieldCount, buf.readerIndex(), buf.readableBytes()));

		for (int i = 0; i < fieldCount; i++) {
			short type = buf.readShort();
			int length = buf.readUnsignedShort();

			this.fields.add(new TemplateField(type, length));
			this.singleFlowSize += length;
		}
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Template && ((Template) obj).getId() == this.id);
	}

}
