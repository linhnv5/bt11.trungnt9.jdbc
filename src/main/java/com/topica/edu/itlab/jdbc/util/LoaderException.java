package com.topica.edu.itlab.jdbc.util;

/**
 * Exception when load entity
 * @author ljnk975
 */
public class LoaderException extends Exception {

	private static final long serialVersionUID = 489760931936816550L;

	/**
	 * Create a loader exception
	 * @param message the message
	 */
	public LoaderException(String message) {
		super(message);
	}

}
