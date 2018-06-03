package de.howaner.netflowcollector.config.collectionadapter;

import org.bson.Document;

public interface CollectionAdapter {

	public String getCollectionName(Document doc);

	public void load();

	public void unload();

}
