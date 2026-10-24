package com.kkpa.scrumgraph.services;

import java.util.List;

import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGException;

public interface UserService {

	/**
	 * @throws SGException when the auth token is not valid
	 */
	List<UserDTO> getAllUsers(String authToken) throws SGException;
	
	boolean validateToken(String authToken);
	
}
