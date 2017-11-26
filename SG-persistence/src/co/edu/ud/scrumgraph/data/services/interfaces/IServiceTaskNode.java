package co.edu.ud.scrumgraph.data.services.interfaces;

import java.util.List;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ERelTypes;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public interface IServiceTaskNode extends IServiceNode  {
	
	final ERelTypes REL_TYPE_TO_PBI = ERelTypes.PERFORMS;
	
	final ERelTypes REL_TYPE_TO_USER = ERelTypes.IS_ASSIGNED_TO;

	/**
	 * Método que se encarga de realizar la validación del
	 * código Task. Este código debe ser único para los nodos Task.
	 * @param code String código a validar.
	 * @throws ScrumGraphException Excepción de validación.
	 */
	public void validateCode(String code) throws ScrumGraphException;
	
	 /**
	  * Método que asigna un nodo Task a un nodo PBI.
	  * @param idTask Identificador del Task
	  * @param idPBI Identificador del PBI
	  * @throws ScrumGraphException Excepción al realizar la asignación.
	  */
	 NodeTO assignTaskToPBI(Long idTask, Long idPBI) throws ScrumGraphException;
	 
	 /**
	  * Método que asigna un nodo Task a un nodo usuario.
	  * @param idTask Identificador del Task
	  * @param idUser Identificador del usuario
	  * @throws ScrumGraphException Excepción al realizar la asignación.
	  */
	 void assignTaskToUser(Long idTask, List<Integer> idUser) throws ScrumGraphException;
	 
}
