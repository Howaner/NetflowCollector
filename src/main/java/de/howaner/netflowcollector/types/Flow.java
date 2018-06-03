package de.howaner.netflowcollector.types;

import de.howaner.netflowcollector.fields.NetflowFields;
import io.netty.buffer.ByteBuf;
import java.util.Date;
import lombok.Data;
import org.bson.Document;

@Data
public class Flow {
	private final Template template;
	private final ByteBuf[] data;

	public Object getParsedValue(int fieldId) {
		TemplateField field = template.getFields().get(fieldId);
		NetflowFields fieldParser = NetflowFields.getFieldById(field.getTypeId());
		if (fieldParser == null)
			return null;

		return fieldParser.getConverter().run(this.data[fieldId], field);
	}

	public Document createDatabaseDocument() {
		Document doc = new Document("Date", new Date());
		for (int fieldId = 0; fieldId < this.template.getFields().size(); fieldId++) {
			TemplateField field = this.template.getFields().get(fieldId);
			NetflowFields fieldParser = NetflowFields.getFieldById(field.getTypeId());
			if (fieldParser == null || !fieldParser.isRelevant())
				continue;

			Object obj = fieldParser.getConverter().run(this.data[fieldId], field);
			doc.append(fieldParser.name(), obj);
		}
		return doc;
	}
}
