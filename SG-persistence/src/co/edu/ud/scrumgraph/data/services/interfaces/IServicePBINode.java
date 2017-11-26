package co.edu.ud.scrumgraph.data.services.interfaces;

import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.IndicadoresTO;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ERelTypes;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public interface IServicePBINode  extends IServiceNode{

	final ERelTypes REL_TYPE_PROJECT = ERelTypes.BELONGS_TO;
	
	final ERelTypes REL_TYPE_SPRINT = ERelTypes.WORKS_ON;
	
	void validateCode(String code) throws ScrumGraphException;
	
	 /**
	  * Método que asigna un nodo PBI a un nodo proyecto.
	  * @param idPBI Identificador del PBI
	  * @param idProject Identificador del proyecto
	  * @throws ScrumGraphException Excepción al realizar la asignación.
	  */
	 void assignPBIToProject(Long idPBI, Long idProject) throws ScrumGraphException;
	 
	 /**
	  * Método que asigna un nodo PBI a un nodo sprint.
	  * @param idPBI Identificador del PBI
	  * @param idSprint Identificador del Sprint
	  * @return NodeTO Nodo con la información del Sprint
	  * @throws ScrumGraphException Excepción al realizar la asignación.
	  */
	 NodeTO assignPBIToSprint(Long idPBI, Long idSprint) throws ScrumGraphException;
	 
	 NodeTO getGraphByPbi(Long idPBI) throws ScrumGraphException;
	 
	 Map<String,Integer> getChartByPbi(Long idPBI) throws ScrumGraphException;
	 	 
	
}
