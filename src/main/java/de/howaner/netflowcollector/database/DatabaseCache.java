package de.howaner.netflowcollector.database;

import de.howaner.netflowcollector.NetflowCollector;
import de.howaner.netflowcollector.types.Flow;
import de.howaner.netflowcollector.util.CacheList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

public class DatabaseCache {
	private final Map<String, CacheList<Document>> cachedCollections = new HashMap<>();
	private final ReadWriteLock cachedCollectionsLock = new ReentrantReadWriteLock();
	private CacheTimeoutChecker timeoutCheckerThread;

	public void startTimeoutChecker() {
		this.timeoutCheckerThread = new CacheTimeoutChecker();
		this.timeoutCheckerThread.start();
	}

	public CacheList<Document> getOrCreateCacheList(String collection) {
		this.cachedCollectionsLock.readLock().lock();
		try {
			CacheList<Document> list = this.cachedCollections.get(collection);
			if (list == null) {
				list = new CacheList<>(Document.class, NetflowCollector.getInstance().getConfig().getCacheSize());
				list.setFullHandler(new MongoCacheFullHandler(collection));
				this.cachedCollections.put(collection, list);
			}

			return list;
		} finally {
			this.cachedCollectionsLock.readLock().unlock();
		}
	}

	public void timeoutCache() {
		long timeout = NetflowCollector.getInstance().getConfig().getCacheTimeout();
		Map<String, CacheList<Document>> timeoutedCaches = new HashMap<>();

		this.cachedCollectionsLock.writeLock().lock();
		try {
			Iterator<Map.Entry<String, CacheList<Document>>> itr = this.cachedCollections.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, CacheList<Document>> entry = itr.next();
				if ((System.currentTimeMillis() - entry.getValue().getLastChange()) >= timeout) {
					itr.remove();
					timeoutedCaches.put(entry.getKey(), entry.getValue());
					NetflowCollector.getInstance().getLogger().info("Time-out for collection cache {} -> It will be saved to database and the cache get removed.", entry.getKey());
				}
			}
		} finally {
			this.cachedCollectionsLock.writeLock().unlock();
		}

		timeoutedCaches.values().forEach(CacheList::forceFull);
	}

	public void addFlow(Flow flow) {
		Document doc = flow.createDatabaseDocument();
		String collection = NetflowCollector.getInstance().getConfig().getCollectionAdapter().getCollectionName(doc);
		CacheList<Document> cache = this.getOrCreateCacheList(collection);
		cache.add(doc);
	}

	/**
	 * Forces all cache data to save and clear the cache. Used at application shutdown.
	 */
	public void forceSave() {
		this.cachedCollectionsLock.writeLock().lock();
		try {
			this.cachedCollections.values().forEach(CacheList::forceFull);
			this.cachedCollections.clear();
		} finally {
			this.cachedCollectionsLock.writeLock().unlock();
		}
	}

	@RequiredArgsConstructor
	private class MongoCacheFullHandler implements CacheList.FullHandler<Document> {
		private final String collection;

		@Override
		public void onFull(Document[] elements) {
			NetflowCollector.getInstance().getLogger().info("[Database] Write {} flows of collection {} into database", elements.length, this.collection);

			List<Document> list = Arrays.asList(elements);
			NetflowCollector.getInstance().getDatabaseConnection().insertFlows(this.collection, list);
		}
	}

	private class CacheTimeoutChecker extends Thread {

		public CacheTimeoutChecker() {
			super("DatabaseCache Timeout Checker");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			while (!this.isInterrupted()) {
				try {
					Thread.sleep(60000L);
				} catch (InterruptedException ex) {
					break;
				}

				DatabaseCache.this.timeoutCache();
			}
		}
	}

}
