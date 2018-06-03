package de.howaner.netflowcollector.config.collectionadapter;

import lombok.Data;
import org.bson.Document;

@Data
public class SingleCollectionAdapter implements CollectionAdapter {
	private final String collectionName;

	@Override
	public String getCollectionName(Document doc) {
		return this.collectionName;
	}

	@Override
	public String toString() {
		return String.format("SingleCollectionAdapter{collectionName=\"%s\"}", this.collectionName);
	}

	@Override
	public void load() {
	}

	@Override
	public void unload() {
	}

}
