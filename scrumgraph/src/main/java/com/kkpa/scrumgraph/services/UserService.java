package com.kkpa.scrumgraph.services;

import java.util.List;

import co.edu.ud.scrumgraph.data.dto.NodeTO;

public interface UserService {

	List<NodeTO> getAllUsers(String authToken);
	
}
