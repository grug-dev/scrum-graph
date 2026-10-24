package com.kkpa.scrumgraph.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkpa.scrumgraph.auth.ValidateAuthToken;
import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ResponseDTO;
import com.kkpa.scrumgraph.dto.UserCollectionDTO;
import com.kkpa.scrumgraph.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@ValidateAuthToken
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseDTO getUsers() {
		UserCollectionDTO userCollectionDTO = new UserCollectionDTO();
		userCollectionDTO.setLstUsers(userService.getAllUsers());

		ResponseDTO respDTO = new ResponseDTO();
		respDTO.setResponse(userCollectionDTO);
		respDTO.setCode(ECodeResponse.OK.getCode());

		return respDTO;
	}

}
