package com.kkpa.scrumgraph.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component("userCollection")
@Scope("prototype")
public class UserCollectionDTO implements Serializable {

	@JsonProperty("users")
	private List<UserDTO> lstUsers;

	public List<UserDTO> getLstUsers() {
		return lstUsers;
	}

	public void setLstUsers(List<UserDTO> lstUsers) {
		this.lstUsers = lstUsers;
	}
	
	
}
