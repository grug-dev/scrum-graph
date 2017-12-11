package com.kkpa.scrumgraph.dto;

import java.io.Serializable;
import java.util.Map;

import com.kkpa.scrumgraph.util.SGUtil;

public class ScrumHeaderDTO implements Serializable {

	private String authToken;
	
	/**
	 * Transform HTTP Request JSON Header into an ScrumHeaderDTO Object.
	 * @param sgHeader String HTTP Request Header
	 * @return ScrumHeaderDTO Object with the Header's info
	 */
	public static ScrumHeaderDTO valueOf(String sgHeader) {
		ScrumHeaderDTO shDTO = null; 
		
		if (sgHeader == null) {
			return null;
		}
		
		Map<String, Object> map = SGUtil.transformJSON(sgHeader);
		shDTO = new ScrumHeaderDTO();		
		shDTO.setAuthToken(map.getOrDefault("authToken", "emptyAuthToken").toString());
		
		return shDTO;
		
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	
}
