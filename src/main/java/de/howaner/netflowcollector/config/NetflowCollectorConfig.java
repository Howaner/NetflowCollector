package de.howaner.netflowcollector.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.howaner.netflowcollector.NetflowCollector;
import de.howaner.netflowcollector.config.collectionadapter.CollectionAdapter;
import de.howaner.netflowcollector.config.collectionadapter.DateCollectionAdapter;
import de.howaner.netflowcollector.config.collectionadapter.FlowFieldCollectionAdapter;
import de.howaner.netflowcollector.config.collectionadapter.SingleCollectionAdapter;
import de.howaner.netflowcollector.exception.ConfigException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

public class NetflowCollectorConfig {
	@Getter private CollectionAdapter collectionAdapter;
	@Getter private Set<String> collectionIndexes;
	@Getter private int cacheSize;
	@Getter private long cacheTimeout;

	@Getter private String databaseType;
	@Getter private String databaseIp;
	@Getter private int databasePort;
	@Getter private String databaseUser;
	@Getter private String databasePassword;
	@Getter private String databaseDB;

	private Map<String, Object> rawConfig;

	public boolean isExising() {
		File file = new File("config.json");
		return file.isFile();
	}

	public void loadConfig() throws IOException, ConfigException {
		String fileContent = new String(Files.readAllBytes(Paths.get("config.json")), StandardCharsets.UTF_8);

		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		this.rawConfig = gson.fromJson(fileContent, type);

		try {
			this.parseCollectionConfig();
		} catch (Exception ex) {
			throw new ConfigException("Error while reading Collection Config", ex);
		}

		try {
			this.parseCacheConfig();
		} catch (Exception ex) {
			throw new ConfigException("Error while reading Cache Config", ex);
		}

		try {
			this.parseDatabaseConfig();
		} catch (Exception ex) {
			throw new ConfigException("Error while reading Database Config", ex);
		}
	}

	private void parseCollectionConfig() throws ConfigException {
		Map<String, Object> collectionConfig = (Map<String, Object>) this.rawConfig.getOrDefault("Collection", Collections.EMPTY_MAP);
		switch ((String) collectionConfig.getOrDefault("Adapter", "")) {
			case "Date":
				String format = (String) collectionConfig.get("Format");
				if (format == null || format.isEmpty())
					throw new ConfigException("Missing Format");

				this.collectionAdapter = new DateCollectionAdapter(format);
				break;
			case "FlowField":
				String field = (String) collectionConfig.get("Field");
				if (field == null || field.isEmpty())
					throw new ConfigException("Missing Field");

				this.collectionAdapter = new FlowFieldCollectionAdapter(field);
				break;
			case "SingleCollection":
				String collectionName = (String) collectionConfig.get("CollectionName");
				if (collectionName == null || collectionName.isEmpty())
					throw new ConfigException("Missing CollectionName");

				this.collectionAdapter = new SingleCollectionAdapter(collectionName);
				break;
			default:
				throw new ConfigException("Invalid collection adapter: " + collectionConfig.get("adapter"));
		}
		this.collectionAdapter.load();

		this.collectionIndexes = new HashSet<>((List<String>) collectionConfig.getOrDefault("Indexes", Collections.EMPTY_LIST));
		if (!this.collectionIndexes.contains("Date"))
			this.collectionIndexes.add("Date");  // Date is always a index
	}

	private void parseCacheConfig() throws ConfigException {
		Map<String, Object> cacheConfig = (Map<String, Object>) this.rawConfig.getOrDefault("Cache", Collections.EMPTY_MAP);
		this.cacheSize = ((Double) cacheConfig.get("Size")).intValue();
		this.cacheTimeout = ((Double) cacheConfig.get("Timeout")).intValue() * 1000L;
	}

	private void parseDatabaseConfig() throws ConfigException {
		Map<String, Object> dbConfig = (Map<String, Object>) this.rawConfig.getOrDefault("Database", Collections.EMPTY_MAP);
		if (!dbConfig.getOrDefault("Type", "").equals("MongoDB"))
			throw new ConfigException("Currently only MongoDB is available as database type!");

		this.databaseType = (String) dbConfig.get("Type");
		this.databaseIp = (String) dbConfig.get("IP");
		this.databasePort = ((Double) dbConfig.get("Port")).intValue();
		this.databaseUser = (String) dbConfig.get("User");
		this.databasePassword = (String) dbConfig.get("Password");
		this.databaseDB = (String) dbConfig.get("Database");
	}

	public void writeDefaultConfig() throws IOException {
		NetflowCollector.getInstance().getLogger().info("Creating default config.");
		InputStream source = NetflowCollector.class.getResourceAsStream("/config.json");
		Files.copy(source, Paths.get("config.json"), StandardCopyOption.REPLACE_EXISTING);
	}

}
