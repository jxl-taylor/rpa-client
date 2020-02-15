package com.mr.rpa.assistant.exceptions;

import org.apache.log4j.Logger;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * @author afsal
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler {

	private final static Logger LOGGER = Logger.getLogger(DefaultExceptionHandler.class);

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		LOGGER.error("Exception occurred {}", ex);
	}
}
