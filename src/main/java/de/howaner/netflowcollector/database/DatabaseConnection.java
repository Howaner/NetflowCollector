package de.howaner.netflowcollector.database;

import java.util.List;
import org.bson.Document;

public interface DatabaseConnection {

	public void insertFlows(String collectionName, List<Document> flows);

}
