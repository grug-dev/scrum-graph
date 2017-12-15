package com.kkpa.scrumgraph.converter;

import java.lang.reflect.Type;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Component;

import com.kkpa.scrumgraph.dto.UserDTO;
import com.kkpa.scrumgraph.entities.User;

@Component("usrConverter")
public class UserConverter implements ScrumGraphConverter<UserDTO,User> {

	private ModelMapper mapper = new ModelMapper();
	
	private Type targetListType = new TypeToken<List<UserDTO>>(){}.getType();

	@Override
	public UserDTO convertToDTO(User entity) {
		UserDTO dto = null;
		
		dto = mapper.map(entity, UserDTO.class);
		
		return dto;
	
	}

	@Override
	public User convertToEntity(UserDTO dto) {
		User u = null;
		
		u = mapper.map(dto, User.class);
		
		return u;
	}

	@Override
	public List<UserDTO> convertCollection(List<User> lstEntities) {
		return mapper.map(lstEntities, targetListType);
	}
	

}
