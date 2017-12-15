package com.kkpa.scrumgraph.converter;

import java.util.List;

public interface ScrumGraphConverter<DTO,ENT>  {
	
	DTO convertToDTO(ENT entity);
	
	ENT convertToEntity(DTO dto);
	
	List<DTO> convertCollection(List<ENT> lstEntities);
}
