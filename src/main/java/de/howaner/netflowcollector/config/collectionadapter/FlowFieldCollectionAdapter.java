package de.howaner.netflowcollector.config.collectionadapter;

import lombok.RequiredArgsConstructor;
import org.bson.Document;

@RequiredArgsConstructor
public class FlowFieldCollectionAdapter implements CollectionAdapter {
	private final String field;

	@Override
	public String getCollectionName(Document doc) {
		Object value = doc.get(this.field);
		return (value == null ? "Unknown" : value.toString());
	}

	@Override
	public String toString() {
		return String.format("FlowFieldCollectionAdapter{field=\"%s\"}", this.field);
	}

	@Override
	public void load() {
	}

	@Override
	public void unload() {
	}

}
