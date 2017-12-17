package com.kkpa.scrumgraph.services;

import java.util.List;

import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGException;

public interface UserService {

	List<UserDTO> getAllUsers(String authToken) ;
	
}
