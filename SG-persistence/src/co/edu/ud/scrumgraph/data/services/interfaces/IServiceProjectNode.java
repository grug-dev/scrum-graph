package co.edu.ud.scrumgraph.data.services.interfaces;

import co.edu.ud.scrumgraph.data.dto.GraphTO;
import co.edu.ud.scrumgraph.data.dto.IndicadoresTO;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ERelTypes;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public interface IServiceProjectNode  extends IServiceNode{
	
	final String CODE_PROJECT = "PRY_";
	final String CODE_TEAM = "TEAM_";
	final String NAME_TEAM_ON_PROJECT = "teamName";
	final String NEXT_PROJECT_CODE = "nextProjectCode";
	
	final ERelTypes REL_TYPE_TEAM_USER = ERelTypes.BELONGS_TO;
	final ERelTypes REL_TYPE_TEAM = ERelTypes.WORKING_ON;
	
	final ERelTypes REL_TYPE_SPRINT = ERelTypes.IS_COMPOSED;

	 NodeTO getUsersByProject( Long idProject) throws ScrumGraphException;
	
	 void validateCode( String code) throws ScrumGraphException;
	
	 GraphTO getGraphProject(Long idProject) throws ScrumGraphException;
	 
	 /**
	  * Servicio usado para consultar los sprints relacionados a un proyecto
	  * @param idProject Identificador del proyecto a consultar los sprints
	  * @return NodeTO Objeto TO con la información del proyecto
	  * @throws ScrumGraphException Excepción al realizar la consulta
	  */
	 NodeTO getSprintsByProject( Long idProject) throws ScrumGraphException;
	 
	 NodeTO getPBIUnAssignedByProject ( Long idProject) throws ScrumGraphException;
	 
	 IndicadoresTO getIndicadores(Long idProject) throws ScrumGraphException;
	 
}
