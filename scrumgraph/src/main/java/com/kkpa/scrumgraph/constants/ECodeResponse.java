package com.kkpa.scrumgraph.constants;

public enum ECodeResponse {

	OK(200,"OK")
	
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
