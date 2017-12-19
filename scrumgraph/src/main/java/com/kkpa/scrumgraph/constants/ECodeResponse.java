package com.kkpa.scrumgraph.constants;

public enum ECodeResponse {

	OK(200,"OK"),
	ERROR_CODE(500 , "error") , 
	INVALID_REQUEST (101 , "Invalid Request") , 
	INCOMPLETE_REQUEST_FIELDS ( 202 , "Incomplete request fields"),
	INVALID_FIELDS( 203 ,"Invalid Fields"),
	INVALID_REQ_PAYLOAD( 204 ,"Invalid Request PayLoad"),
	
	// USER CODES
	INVALID_EMAIL(310, "Invalid Email"),
	UNAUTHENTICATED_USER(311, "Unauthenticated user"),
	UNAUTHORIZED_USER(312, "Unauthorized user"),
	EMAIL_ALREADY_EXISTS(313, "Email already exists"),
	USER_DOESNT_EXISTS(314, "User doesn't exists"),
	USER_UNAVAILABLE(315, "User unavailable "),
	
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
