package com.kkpa.scrumgraph.exceptionhandler;

import com.kkpa.scrumgraph.constants.ECodeResponse;

/**
 * Business error of the application. It carries one {@link ECodeResponse}.
 */
public class SGException extends Exception {

	private static final long serialVersionUID = 4157994927050600608L;

	private final ECodeResponse codeResponse;

	public SGException(ECodeResponse codeResponse) {
		super(codeResponse.getStatus());
		this.codeResponse = codeResponse;
	}

	/** Use it when the client needs more detail than the code's default text. */
	public SGException(ECodeResponse codeResponse, String message) {
		super(message);
		this.codeResponse = codeResponse;
	}

	public ECodeResponse getCodeResponse() {
		return codeResponse;
	}

	public int getCode() {
		return codeResponse.getCode();
	}

}
