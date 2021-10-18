package com.zdawn.commons.mutex;

public class LockException extends Exception {
	private static final long serialVersionUID = -2775536847954706417L;
	
	public LockException(String errorMessage) {
		super(errorMessage);
	}
}
