package com.dot.me.exceptions;

public class LostUserAccessException extends Exception{

	/**
	 * 
	 */
	
	private String message;
	
	public LostUserAccessException(String message){
		this.message=message;
	}
	
	private static final long serialVersionUID = -2741346037092886700L;

	@Override
	public String getMessage() {
		
		return message;
	}

	
	
}
