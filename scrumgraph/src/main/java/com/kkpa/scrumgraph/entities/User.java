package com.kkpa.scrumgraph.entities;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label="USER")
public class User {

	@GraphId private Long id;
	
	private String name;
	
	private String lastName;
	
	private String email;
	
	private boolean available;
	
	private String authToken;
	
	private String password;
	
	private String roleDefault;
	
	private boolean isAdmin;
	
	private Long createAt;
	
	private Long lastModification;
	
	/*GETTER && SETTER */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoleDefault() {
		return roleDefault;
	}

	public void setRoleDefault(String roleDefault) {
		this.roleDefault = roleDefault;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public Long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

	public Long getLastModification() {
		return lastModification;
	}

	public void setLastModification(Long lastModification) {
		this.lastModification = lastModification;
	}
	
}
