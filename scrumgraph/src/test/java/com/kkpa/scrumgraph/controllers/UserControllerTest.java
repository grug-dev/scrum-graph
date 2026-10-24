package com.kkpa.scrumgraph.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kkpa.scrumgraph.auth.AuthTokenInterceptor;
import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;
import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGRestControllerAdvice;
import com.kkpa.scrumgraph.services.UserService;

public class UserControllerTest {

	private UserService userService;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		when(userService.validateToken("good-token")).thenReturn(true);

		mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
				.addInterceptors(new AuthTokenInterceptor(userService))
				.setControllerAdvice(new SGRestControllerAdvice())
				.build();
	}

	@Test
	public void getUsersReturnsTheUsers() throws Exception {
		UserDTO user = new UserDTO();
		user.setEmail("alice.smith@example.com");
		when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));

		mockMvc.perform(get("/users").header(ScrumHeaderDTO.HEADER_NAME, "{\"authToken\": \"good-token\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(ECodeResponse.OK.getCode()))
				.andExpect(jsonPath("$.response.users[0].email").value("alice.smith@example.com"));
	}

	@Test
	public void invalidTokenGives401() throws Exception {
		mockMvc.perform(get("/users").header(ScrumHeaderDTO.HEADER_NAME, "{\"authToken\": \"bad-token\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value(ECodeResponse.UNAUTHENTICATED_USER.getCode()));

		verify(userService, never()).getAllUsers();
	}

	@Test
	public void missingHeaderGives400() throws Exception {
		mockMvc.perform(get("/users"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value(ECodeResponse.INVALID_REQUEST.getCode()))
				.andExpect(jsonPath("$.errorMsg").value(ScrumHeaderDTO.HEADER_NAME + " is missing"));
	}

	@Test
	public void badHeaderJsonGives400() throws Exception {
		mockMvc.perform(get("/users").header(ScrumHeaderDTO.HEADER_NAME, "{not json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value(ECodeResponse.INVALID_REQUEST.getCode()));
	}

}
