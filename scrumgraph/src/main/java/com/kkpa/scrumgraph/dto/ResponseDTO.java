package com.kkpa.scrumgraph.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kkpa.scrumgraph.constants.ECodeResponse;

public class ResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String status = ECodeResponse.OK.getStatus();
	
	private String errorMsg = "";
	
	private int code;
	
	@JsonInclude(Include.NON_NULL)
	private Object response;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int errorCode) {
		this.code = errorCode;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	
}
