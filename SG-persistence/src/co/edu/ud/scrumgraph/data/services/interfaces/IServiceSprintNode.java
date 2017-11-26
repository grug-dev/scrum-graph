/**
 * 
 */
package co.edu.ud.scrumgraph.data.services.interfaces;

import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ERelTypes;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

/**
 * @author RaspuWIN7
 *
 */
public interface IServiceSprintNode extends IServiceNode {

	final ERelTypes REL_TYPE_PROJECT = ERelTypes.IS_COMPOSED;

	public void validateCode(String code) throws ScrumGraphException;

	/**
	 * Método que asigna un nodo Sprint a un nodo proyecto.
	 * 
	 * @param idSprint
	 *            Identificador del SPRINT
	 * @param idProject
	 *            Identificador del proyecto
	 * @throws ScrumGraphException
	 *             Excepción al realizar la asignación.
	 */
	void assignSprintToProject(Long idSprint, Long idProject) throws ScrumGraphException;

	/**
	 * Método que consulta todos los nodos PBIS asociados a un sprint
	 * @param idNode  Identificador del nodo
	 * @return NodeTO Objeto con la información del nodo.
	 * @throws ScrumGraphException
	 *             Excepción de consulta del nodo.
	 */
	NodeTO getPbisBySprint(Long idNode) throws ScrumGraphException;
	
	Map<String, Integer> getChartById(Long id) throws ScrumGraphException;

}
