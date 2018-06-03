package de.howaner.netflowcollector.database;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import de.howaner.netflowcollector.NetflowCollector;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.Document;

public class MongoConnection implements DatabaseConnection {
	private MongoClient mongoClient;
	private MongoDatabase mongoDatabase;

	private Set<String> collections = Collections.synchronizedSet(new HashSet<>());

	static {
		System.setProperty("DEBUG.MONGO", "false");
		System.setProperty("DB.TRACE", "false");
	}

	@Override
	public void insertFlows(String collectionName, List<Document> flows) {
		MongoCollection<Document> collection = this.mongoDatabase.getCollection(collectionName);
		if (!this.collections.contains(collectionName)) {
			this.collections.add(collectionName);
			NetflowCollector.getInstance().getLogger().info("[Database] Created collection {}", collectionName);
			for (String index : NetflowCollector.getInstance().getConfig().getCollectionIndexes()) {
				collection.createIndex(new Document(index, -1), new IndexOptions().name(index).background(true), (list, error) -> {});
			}
		}

		collection.insertMany(flows, (result, error) -> {
			if (error != null)
				NetflowCollector.getInstance().getLogger().error("[Database] Error while inserting " + flows.size() + " documents into database.", error);
		});
	}

	private void readCollectionNames() {
		this.mongoDatabase.listCollectionNames().into(new HashSet<>(), (list, error) -> {
			if (error != null)
				NetflowCollector.getInstance().getLogger().error("[Database] Error while reading collection names from database.", error);
			else
				this.collections = list;
		});
	}

	public void connect(String ip, int port, String username, String password, String database) throws Exception {
		this.connect(String.format("mongodb://%s:%s@%s:%d/%s", username, password, ip, port, database), database);
	}

	public void connect(String ip, int port, String database) throws Exception {
		this.connect(String.format("mongodb://%s:%d/%s", ip, port, database), database);
	}

	private void connect(String connectionString, String database) throws Exception {
		this.mongoClient = MongoClients.create(connectionString);
		this.mongoDatabase = this.mongoClient.getDatabase(database);
		this.readCollectionNames();
	}

	public void close() {
		if (this.mongoClient != null)
			this.mongoClient.close();
	}

}
