package com.kkpa.scrumgraph.dto;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kkpa.scrumgraph.constants.ECodeResponse;

@Component("responseDTO")
@Scope("prototype")
public class ResponseDTO implements Serializable {
	
	private String status = ECodeResponse.OK.getStatus();
	
	private String errorMsg;
	
	private String errorCode;
	
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

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

}
