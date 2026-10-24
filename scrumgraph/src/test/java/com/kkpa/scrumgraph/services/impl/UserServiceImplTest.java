package com.kkpa.scrumgraph.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.kkpa.scrumgraph.converter.ScrumGraphConverter;
import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.entities.User;
import com.kkpa.scrumgraph.repository.UserRepository;

public class UserServiceImplTest {

	private UserRepository userRepo;

	private ScrumGraphConverter<UserDTO, User> userConverter;

	private UserServiceImpl userService;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		userRepo = mock(UserRepository.class);
		userConverter = mock(ScrumGraphConverter.class);
		userService = new UserServiceImpl(userRepo, userConverter);
	}

	@Test
	public void validateTokenIsFalseForNullToken() {
		assertFalse(userService.validateToken(null));
		verify(userRepo, never()).findByAuthToken(anyString());
	}

	@Test
	public void validateTokenIsFalseForUnknownToken() {
		assertFalse(userService.validateToken("unknown"));
	}

	@Test
	public void validateTokenIsTrueForKnownToken() {
		when(userRepo.findByAuthToken("known")).thenReturn(new User());

		assertTrue(userService.validateToken("known"));
	}

	@Test
	public void getAllUsersConvertsAllEntities() {
		User user = new User();
		List<User> users = Arrays.asList(user);
		List<UserDTO> dtos = Collections.singletonList(new UserDTO());
		when(userRepo.findAll()).thenReturn(users);
		when(userConverter.convertCollection(users)).thenReturn(dtos);

		assertSame(dtos, userService.getAllUsers());
	}

	@Test
	public void getAllUsersReturnsEmptyListWhenNoUsers() {
		List<User> noUsers = Collections.emptyList();
		List<UserDTO> noDtos = Collections.emptyList();
		when(userRepo.findAll()).thenReturn(noUsers);
		when(userConverter.convertCollection(noUsers)).thenReturn(noDtos);

		assertEquals(0, userService.getAllUsers().size());
	}

}
