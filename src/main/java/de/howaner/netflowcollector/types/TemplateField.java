package de.howaner.netflowcollector.types;

import de.howaner.netflowcollector.fields.NetflowFields;
import lombok.Data;

@Data
public class TemplateField {
	private final short typeId;
	private final int length;

	public String getName() {
		NetflowFields fieldConverter = NetflowFields.getFieldById(this.typeId);
		if (fieldConverter != null)
			return fieldConverter.name();
		else
			return "Unknown-" + this.typeId;
	}

}
