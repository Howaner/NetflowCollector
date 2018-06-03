package de.howaner.netflowcollector.config.collectionadapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.bson.Document;

public class DateCollectionAdapter implements CollectionAdapter {
	private final String format;
	private final DateInterval updateInterval;

	private String currentFormattedDate;
	private Timer timer;

	public DateCollectionAdapter(String format) {
		this.format = format;
		this.updateInterval = determinateInterval(format);
		this.reloadDate();
	}

	@Override
	public String toString() {
		return String.format("DateCollectionAdapter{format=\"%s\",updateInterval=\"%s\"}", this.format, String.valueOf(this.updateInterval));
	}

	@Override
	public String getCollectionName(Document doc) {
		return this.currentFormattedDate;
	}

	@Override
	public void load() {
		this.timer = new Timer();
		this.timer.start();
	}

	@Override
	public void unload() {
		this.timer.interrupt();
	}

	private void reloadDate() {
		SimpleDateFormat format = new SimpleDateFormat(this.format);
		this.currentFormattedDate = format.format(new Date());
	}

	/**
	 * Calculates the required milliseconds timestamp until the next update of the date-format will happen.
	 * @return 
	 */
	private long getMillisUntilNextUpdate() {
		Calendar nextUpdate = Calendar.getInstance();
		nextUpdate.set(Calendar.SECOND, 0);

		switch (this.updateInterval) {
			case MINUTE:
				nextUpdate.set(Calendar.MINUTE, nextUpdate.get(Calendar.MINUTE) + 1);
				break;
			case HOUR:
				nextUpdate.set(Calendar.MINUTE, 0);
				nextUpdate.set(Calendar.HOUR_OF_DAY, nextUpdate.get(Calendar.HOUR_OF_DAY) + 1);
				break;
			case DAY:
				nextUpdate.set(Calendar.MINUTE, 0);
				nextUpdate.set(Calendar.HOUR_OF_DAY, 0);
				nextUpdate.set(Calendar.DAY_OF_MONTH, nextUpdate.get(Calendar.DAY_OF_MONTH) + 1);
				break;
			case WEEK:
			case MONTH:
				throw new UnsupportedOperationException("Update interval " + this.updateInterval.name() + " is not possible to use.");
			default:
				throw new UnsupportedOperationException("Unsupported update interval: " + this.updateInterval.name());
		}

		Calendar now = Calendar.getInstance();
		return Math.max(0L, nextUpdate.getTimeInMillis() - now.getTimeInMillis());
	}

	private static DateInterval determinateInterval(String format) {
		int intervalNum = DateInterval.DAY.ordinal();
		boolean isEncapsulated = false;

		for (int i = 0; i < format.length(); i++) {
			char c = format.charAt(i);
			switch (c) {
				case '\'':
					// Encapsulation
					isEncapsulated = !isEncapsulated;
					break;
				case 'm':
					// Minute
					if (isEncapsulated)
						continue;

					intervalNum = Math.min(intervalNum, DateInterval.MINUTE.ordinal());
					break;
				case 'd':
					// Day
					if (isEncapsulated)
						continue;

					intervalNum = Math.min(intervalNum, DateInterval.DAY.ordinal());
					break;
				case 'M':
					// Month
					// Not needed because the maximum interval is a day
					break;
			}
		}

		return DateInterval.values()[intervalNum];
	}

	private class Timer extends Thread {

		public Timer() {
			super("DateCollectionAdapter Timer");
			this.setDaemon(true);
			this.setPriority(MIN_PRIORITY);
		}

		@Override
		public void run() {
			while (!this.isInterrupted()) {
				DateCollectionAdapter.this.reloadDate();

				// Wait until next update
				long nextUpdate = DateCollectionAdapter.this.getMillisUntilNextUpdate();
				try {
					Thread.sleep(nextUpdate + 1000L);
				} catch (InterruptedException ex) {
					break;
				}
			}
		}

	}

	private static enum DateInterval {
		MINUTE, HOUR, DAY, WEEK, MONTH;
	}

}
