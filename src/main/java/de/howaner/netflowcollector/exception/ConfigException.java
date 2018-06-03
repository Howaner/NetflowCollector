package de.howaner.netflowcollector.exception;

public class ConfigException extends Exception {

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(String message, Throwable parent) {
		super(message, parent);
	}

}
