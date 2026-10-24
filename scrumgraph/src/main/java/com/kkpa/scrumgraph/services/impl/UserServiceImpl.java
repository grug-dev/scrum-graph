package com.kkpa.scrumgraph.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kkpa.scrumgraph.aop.LogExecutionTime;
import com.kkpa.scrumgraph.converter.ScrumGraphConverter;
import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.entities.User;
import com.kkpa.scrumgraph.repository.UserRepository;
import com.kkpa.scrumgraph.services.UserService;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepo;

	private final ScrumGraphConverter<UserDTO, User> userConverter;

	public UserServiceImpl(UserRepository userRepo, ScrumGraphConverter<UserDTO, User> userConverter) {
		this.userRepo = userRepo;
		this.userConverter = userConverter;
	}

	@LogExecutionTime
	@Override
	public List<UserDTO> getAllUsers() {
		List<User> lstUsers = new ArrayList<>();
		for (User user : userRepo.findAll()) {
			lstUsers.add(user);
		}
		return userConverter.convertCollection(lstUsers);
	}

	@Override
	public boolean validateToken(String authToken) {
		return authToken != null && userRepo.findByAuthToken(authToken) != null;
	}

}
