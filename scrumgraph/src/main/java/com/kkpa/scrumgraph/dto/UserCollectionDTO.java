package com.kkpa.scrumgraph.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserCollectionDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("users")
	private List<UserDTO> lstUsers;

	public List<UserDTO> getLstUsers() {
		return lstUsers;
	}

	public void setLstUsers(List<UserDTO> lstUsers) {
		this.lstUsers = lstUsers;
	}
	
	
}
