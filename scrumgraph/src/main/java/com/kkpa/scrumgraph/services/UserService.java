package com.kkpa.scrumgraph.services;

import java.util.List;

import com.kkpa.scrumgraph.dto.UserDTO;

public interface UserService {

	List<UserDTO> getAllUsers(String authToken) ;
	
	boolean validateToken(String authToken);
	
}
