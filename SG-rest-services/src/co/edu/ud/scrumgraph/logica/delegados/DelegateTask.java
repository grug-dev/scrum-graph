package co.edu.ud.scrumgraph.logica.delegados;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.EDefaultStateNodes;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceTaskNode;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.serviciosrest.ISrvSGRest;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;

public class DelegateTask extends DelegateService {
	
	
	/**
	 * Interfaz de servicio de los nodos PBI
	 */
	private IServiceTaskNode service;
	

	/**
	 * Constructor basado en un servicio rest
	 * @param serviceRest Servicio rest
	 */
	public DelegateTask(ISrvSGRest serviceRest) {
		super(serviceRest);
	}
	
	/**
	 * Constructor basado en un delegado
	 * @param delegateSrv Delegado
	 */
	public DelegateTask(IDelegateService delegateSrv) {
		super(delegateSrv);
	}

	@Override
	public IServiceTaskNode getServiceNode() {
		if (service == null) {
			service = srvFactory.getTaskNodeService();
		}
		return service;
	}
	
	/**
	 * Método para validar las propiedades del nodo.
	 * 
	 * @param properties
	 *            Propiedades a validar
	 * @throws SGException
	 *             Excepción si alguna propiedad no es válida.
	 */
	public void validateProperties(Map<String, Object> properties) throws SGException {
		Object propertyObj = null;
		String strProperty = null;

		// Validacion propiedades
		List<ETaskProperties> propToValid = new ArrayList<ETaskProperties>();
		propToValid.add(ETaskProperties.NAME);
		propToValid.add(ETaskProperties.CODE);
		propToValid.add(ETaskProperties.DESCRIPTION);
		propToValid.add(ETaskProperties.ESTIMATE_HOURS);
		propToValid.add(ETaskProperties.TYPE);

		// Validaciones Obligatorias
		for (ETaskProperties property : propToValid) {
			strProperty = validateProperty(property, properties);
			switch (property) {
			case NAME:
				HelperSG.validadLenght(1, 25, strProperty);
				break;
			case DESCRIPTION:
				HelperSG.validadLenght(1, 150, strProperty);
				break;
			default:
				break;
			}
		}
		
		// Validaciones opcionales
		if (!properties.containsKey(ETaskProperties.STATUS.getPropertyName())) {
			properties.put(ETaskProperties.STATUS.getPropertyName(), EDefaultStateNodes.TODO.getName());
		}
	}
	

	public String validateProperty(ETaskProperties taskProperty , Map<String, Object> properties) throws SGException {
		Object propertyObj = null;
		
		propertyObj = properties.get(taskProperty.getPropertyName());
		if (propertyObj == null) {
			String msg = null;
			msg = ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getMessage() + ": ";
			msg += taskProperty.getPropertyName();
			throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getCode(), msg);
		}
		
		return propertyObj.toString();
		
	}

	/**
	 * Métood que crea un nodo de tipo Task.
	 * @param authToken Token autenticación del usuario
	 * @param properties Propiedades del nodo a crear
	 * @return NodeTO Objeto con la información del nodo creado.
	 * @throws ScrumGraphException Excepción al crear un nodo Task.
	 */
	public NodeTO createTask(String authToken, Map<String, Object> properties) throws ScrumGraphException {
		NodeTO taskTO = null;
		
		// Validacion proyecto isAdmin
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		taskTO = getServiceNode().createNode(properties);

		return taskTO;
	}
	
	/**
	 * Método que consulta un nodo Task por su identificador pasado
	 * como parámetro
	 * @param authToken Token autenticación del usuario
	 * @param idNode Identificador del nodo a consultar
	 * @return NodeTO Objeto con la información del nodo consultado.
	 * @throws ScrumGraphException Excepción al consultar el nodo.
	 */
	public NodeTO getTaskById(String authToken, Long idNode) throws ScrumGraphException {

		// Validacion authTOken
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getNodeById(idNode);
	}
	
	/**
	 * Método para obtener todos los nodos de tipo Task.
	 * @param authToken  Token autenticación del usuario
	 * @return List<NodeTO> Lista de nodos Task almacenados en base de datos.
	 * @throws ScrumGraphException Excepción al consultar los nodos.
	 */
	public List<NodeTO> getAllNodes(String authToken) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getAllNodes();
	}
	
	/**
	 * Método que modifica un nodo Task en base de datos, basado
	 * en las propiedades pasadas como parámetros.
	 * @param authToken   Token autenticación del usuario. El usuario debe ser administrador.
	 * @param idTask Identificador del Task a actualizar.
	 * @param newProperties Propiedades a actualizar o adicionar al nodo
	 * @return NodeTO Objeto con la información actualizada del nodo.
	 * @throws ScrumGraphException Excepción al actualizar el nodo.
	 */
	public NodeTO updateTask(String authToken, Long idTask, Map<String, Object> newProperties) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().updateNode(idTask, newProperties);
	}
	

	/**
	 * Método que elimina un nodo de Task en base de datos.
	 * @param authToken Token autenticación del usuario. El usuario debe ser administrador.
	 * @param idTask Identificador del Task a eliminar.
	 * @throws ScrumGraphException Excepción ocurrida al realizar la eliminación.
	 */
	public void deleteTask(String authToken, Long idTask) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().deleteNode(idTask);

	}
	
	/**
	 * Valida el code pasado como parámetro
	 * @param authToken Token del usuario autenticado
	 * @param code Code a validar
	 * @throws ScrumGraphException Excepción de validación
	 */
	public void validateCode(String authToken, String code) throws ScrumGraphException {

		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().validateCode(code);

	}
	
	/**
	 * Asigna una tarea  a un PBI, basado en los atributos
	 * pasados como parámetros.
	 * @param authToken Token del usuario autenticado
	 * @param idTask Identificador de la tarea
	 * @param idPBI Identificador del PBI.
	 * @throws ScrumGraphException Excepción al realizar la asignación.
	 */
	public NodeTO assignTaskToPBI(String authToken, Long idTask, Long idPBI) throws ScrumGraphException {
		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().assignTaskToPBI(idTask, idPBI);
	}
	
	/**
	 * Asigna una tarea  a un usuario, basado en los atributos
	 * pasados como parámetros.
	 * @param authToken Token del usuario autenticado
	 * @param idTask Identificador de la tarea
	 * @param idUser Identificador del idUser.
	 * @throws ScrumGraphException Excepción al realizar la asignación.
	 */
	public void assignTaskToUser(String authToken, Long idTask, Long idUser) throws ScrumGraphException {
		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		List<Integer> lst = new ArrayList<Integer>();
		lst.add(idUser.intValue());
		getServiceNode().assignTaskToUser(idTask, lst);
	}
}
