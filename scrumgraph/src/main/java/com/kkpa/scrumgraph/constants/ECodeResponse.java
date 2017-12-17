package com.kkpa.scrumgraph.constants;

public enum ECodeResponse {

	OK(200,"OK"),
	ERROR_CODE(500 , "error") , 
	INVALID_REQUEST (101 , "Invalid Request") , 
	INCOMPLETE_REQUEST_FIELDS ( 202 , "Incomplete request fields"),
	INVALID_FIELDS( 203 ,"Invalid Fields"),
	INVALID_REQ_PAYLOAD( 204 ,"Invalid Request PayLoad"),
	
	;
	
	private int code;
	
	private String status;
	
	private ECodeResponse(int pCode, String pStatus) {
		this.code = pCode;
		this.status = pStatus;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
