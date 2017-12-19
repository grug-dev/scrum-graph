package com.kkpa.scrumgraph.exceptionhandler;

import com.kkpa.scrumgraph.constants.ECodeResponse;

public class SGException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4157994927050600608L;
	
	private int code = ECodeResponse.ERROR_CODE.getCode();
	
	private String message ;
	
	
	public SGException(String mensaje) {
		super(mensaje);
	}
	
	public SGException(ECodeResponse response) {
		super(response.getStatus());
		this.code = response.getCode();
		this.message = response.getStatus();
	}
	
	public SGException(int code , String mensaje) {
		super(mensaje);
		this.code = code;
	}

	
	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setCode(int code) {
		this.code = code;
	}


}