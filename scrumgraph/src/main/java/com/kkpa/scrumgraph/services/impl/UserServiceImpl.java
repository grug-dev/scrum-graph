package com.kkpa.scrumgraph.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kkpa.scrumgraph.aop.LogExecutionTime;
import com.kkpa.scrumgraph.aop.ValidateAuthToken;
import com.kkpa.scrumgraph.converter.ScrumGraphConverter;
import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.entities.User;
import com.kkpa.scrumgraph.repository.UserRepository;
import com.kkpa.scrumgraph.services.UserService;

@Service("userSrv")
public class UserServiceImpl implements UserService  {

	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	@Qualifier("usrConverter")
	private ScrumGraphConverter<UserDTO, User> userConverter;
	

	@LogExecutionTime
	@ValidateAuthToken
	@Override
	public List<UserDTO> getAllUsers(String authToken) {
			List<User> lstUsers = (List<User>) userRepo.findAll(); 
			return userConverter.convertCollection(lstUsers);	
	}


	@Override
	public boolean validateToken(String authToken) {
		
		if (authToken == null) {
			return false;
		}
		
		return userRepo.findByAuthToken(authToken) != null ? true : false;
	}

}
