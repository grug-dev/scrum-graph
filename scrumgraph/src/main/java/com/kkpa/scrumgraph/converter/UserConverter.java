package com.kkpa.scrumgraph.converter;

import java.lang.reflect.Type;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Component;

import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.entities.User;

@Component
public class UserConverter implements ScrumGraphConverter<UserDTO, User> {

	private static final Type TARGET_LIST_TYPE = new TypeToken<List<UserDTO>>() {}.getType();

	private final ModelMapper mapper = new ModelMapper();

	@Override
	public UserDTO convertToDTO(User entity) {
		return mapper.map(entity, UserDTO.class);
	}

	@Override
	public User convertToEntity(UserDTO dto) {
		return mapper.map(dto, User.class);
	}

	@Override
	public List<UserDTO> convertCollection(List<User> lstEntities) {
		return mapper.map(lstEntities, TARGET_LIST_TYPE);
	}

}
