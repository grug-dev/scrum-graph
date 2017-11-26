package co.edu.ud.scrumgraph.data.services.interfaces;

import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public interface IServiceNode  {

	/**
	 * Método encargado de crear un nodo en la base de datos. 
	 * El nodo se crea con un label dependiendo del servicio
	 * qué se esté utilizando para su creación.
	 * @param properties Propiedades de creación del nodo, si son vacías no se creará el nodo y lanzará una excepción.
	 * @return NodeTO Objeto con la información del nodo creado, con su respectivo ID de la base de datos.
	 * @throws ScrumGraphException Excepción al crear el nodo.
	 */
	NodeTO createNode(Map<String, Object> properties) throws ScrumGraphException;

	/**
	 * Método encargado de actualizar un nodo existente en base de datos. Si alguna
	 * propiedad no existe en el nodo, la crea y la asigna al nodo.
	 * Si el nodo a actualizar no existe, se lanzará una excepción. 
	 * @param idNode Identificador del nodo a actualizar.
	 * @param newProperties Nuevas propiedades del nodo.
	 * @return NodeTO Objeto con la información del nodo actualizado.
	 * @throws ScrumGraphException Excepción de actualización del nodo.
	 */
	NodeTO updateNode(Long idNode, Map<String, Object> newProperties) throws ScrumGraphException;

	/**
	 * Método encargado de eliminar un nodo en base de datos, con sus respectivas
	 * relaciones. Se verifica qué el nodo exista y qué se encuentre relacionado
	 * al label del servicio utilizado.
	 * @param idNode Identificador del nodo a eliminar.
	 * @throws ScrumGraphException Excepción de eliminación del nodo.
	 */
	void deleteNode(Long idNode) throws ScrumGraphException;

	/**
	 * Método que consulta un nodo en la base de datos 
	 * @param idNode Identificador del nodo
	 * @return NodeTO Objeto con la información del nodo.
	 * @throws ScrumGraphException Excepción de consulta del nodo.
	 */
	NodeTO getNodeById(Long idNode) throws ScrumGraphException;

	/**
	 * Método que consulta todos los nodos del label asociado
	 * al servicio.
	 * @return List<NodeTO> Lista de nodos tipado por el label del servicio.
	 * @throws ScrumGraphException Excepción de consulta del nodo.
	 */
	List<NodeTO> getAllNodes() throws ScrumGraphException;
}
