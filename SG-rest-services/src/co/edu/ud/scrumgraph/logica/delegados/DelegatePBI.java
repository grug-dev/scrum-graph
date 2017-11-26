package co.edu.ud.scrumgraph.logica.delegados;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.EDefaultStateNodes;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IServicePBINode;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.serviciosrest.ISrvSGRest;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;

public class DelegatePBI extends DelegateService {

	/**
	 * Interfaz de servicio de los nodos PBI
	 */
	private IServicePBINode service;

	

	/**
	 * Constructor basado en un servicio rest
	 * 
	 * @param serviceRest
	 *            Servicio rest
	 */
	public DelegatePBI(ISrvSGRest serviceRest) {
		super(serviceRest);
	}
	
	/**
	 * Constructor basado en un servicio rest
	 * 
	 * @param serviceRest
	 *            Servicio rest
	 */
	public DelegatePBI(IDelegateService delegateSrv) {
		super(delegateSrv);
	}


	@Override
	protected IServicePBINode getServiceNode() {
		if (service == null) {
			service = srvFactory.getPBINodeService();
		}
		return service;
	}
	
	/**
	 * Método para validar las propiedades del nodo.
	 * @param properties Propiedades a validar
	 * @throws SGException Excepción si alguna propiedad no es válida.
	 */
	public void validateProperties(Map<String, Object> properties) throws SGException {
		Object propertyObj = null;
		String strProperty = null;

		// Validacion propiedades
		List<EPBIProperties> propToValid = new ArrayList<EPBIProperties>();
		propToValid.add(EPBIProperties.NAME);
		propToValid.add(EPBIProperties.DESCRIPTION);
		propToValid.add(EPBIProperties.CODE);
		propToValid.add(EPBIProperties.PRIORITY);
		propToValid.add(EPBIProperties.HISTORY_POINTS);
		propToValid.add(EPBIProperties.TYPE);
		propToValid.add(EPBIProperties.OWNER_ID);

		for (EPBIProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				String msg = null;
				msg = ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getMessage() +  ": "; 
				msg += property.getPropertyName();
				throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getCode() , msg);
			}
			strProperty = propertyObj.toString();
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
		
		if (!properties.containsKey(EPBIProperties.STATUS.getPropertyName()) ) {
			properties.put(EPBIProperties.STATUS.getPropertyName(), EDefaultStateNodes.TODO.getName());
		}
	}

	/**
	 * Métood que crea un nodo de tipo PBI.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario
	 * @param properties
	 *            Propiedades del nodo a crear
	 * @return NodeTO Objeto con la información del nodo creado.
	 * @throws ScrumGraphException
	 *             Excepción al crear un nodo PBI.
	 */
	public NodeTO createPBI(String authToken, Map<String, Object> properties) throws ScrumGraphException {
		NodeTO pbiTO = null;

		// Validacion proyecto isAdmin
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		pbiTO = getServiceNode().createNode(properties);

		return pbiTO;
	}

	/**
	 * Método que consulta un nodo PBI por su identificador pasado como
	 * parámetro
	 * 
	 * @param authToken
	 *            Token autenticación del usuario
	 * @param idNode
	 *            Identificador del nodo a consultar
	 * @return NodeTO Objeto con la información del nodo consultado.
	 * @throws ScrumGraphException
	 *             Excepción al consultar el nodo.
	 */
	public NodeTO getPBIById(String authToken, Long idNode) throws ScrumGraphException {

		// Validacion authTOken
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getNodeById(idNode);
	}

	/**
	 * Método para obtener todos los nodos de tipo PBI.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario
	 * @return List< NodeTO > Lista de nodos PBI almacenados en base de datos.
	 * @throws ScrumGraphException
	 *             Excepción al consultar los nodos.
	 */
	public List<NodeTO> getAllNodes(String authToken) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getAllNodes();
	}

	/**
	 * Método que modifica un nodo PBI en base de datos, basado en las
	 * propiedades pasadas como parámetros.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario. El usuario debe ser
	 *            administrador.
	 * @param idPbi
	 *            Identificador del PBI a actualizar.
	 * @param newProperties
	 *            Propiedades a actualizar o adicionar al nodo
	 * @return NodeTO Objeto con la información actualizada del nodo.
	 * @throws ScrumGraphException
	 *             Excepción al actualizar el nodo.
	 */
	public NodeTO updatePbi(String authToken, Long idPbi, Map<String, Object> newProperties) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().updateNode(idPbi, newProperties);
	}

	/**
	 * Método que elimina un nodo de PBI en base de datos.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario. El usuario debe ser
	 *            administrador.
	 * @param idPBI
	 *            Identificador del PBI a eliminar.
	 * @throws ScrumGraphException
	 *             Excepción ocurrida al realizar la eliminación.
	 */
	public void deletePBI(String authToken, Long idPBI) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().deleteNode(idPBI);

	}

	/**
	 * Valida el code pasado como parámetro
	 * 
	 * @param authToken
	 *            Token del usuario autenticado
	 * @param code
	 *            Code a validar
	 * @throws ScrumGraphException
	 *             Excepción de validación
	 */
	public void validateCode(String authToken, String code) throws ScrumGraphException {

		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().validateCode(code);

	}

	/**
	 * Asigna un nodo PBI a un proyecto, basado en los atributos
	 * pasados como parámetros.
	 * @param authToken Token del usuario autenticado
	 * @param idPBI Identificador del PBI
	 * @param idProject Identificador del proyecto.
	 * @throws ScrumGraphException Excepción al realizar la asignación.
	 */
	public void assignPBIToProject(String authToken, Long idPBI, Long idProject) throws ScrumGraphException {
		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().assignPBIToProject(idPBI, idProject);
	}
	
	/**
	 * Asigna un nodo pbi a un Sprint, basado en los atributos
	 * pasados como parámetros.
	 * @param authToken Token del usuario autenticado
	 * @param idPBI Identificador del PBI
	 * @param idSprint Identificador del Sprint.
	 * @throws ScrumGraphException Excepción al realizar la asignación.
	 */
	public NodeTO assignPBIToSprint(String authToken, Long idPBI, Long idSprint) throws ScrumGraphException {
		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().assignPBIToSprint(idPBI, idSprint);
	}
	
	public NodeTO getGraphPBI(Long idPBI)  throws ScrumGraphException {
		return getServiceNode().getGraphByPbi(idPBI);
	}
	
	public  Map<String, Integer> getChartPBI(Long idPBI)  throws ScrumGraphException {
		return getServiceNode().getChartByPbi(idPBI);
	}
	
	/**
	 * Asigna un nodo task a un pbi, basado en los atributos
	 * pasados como parámetros.
	 * @param authToken Token del usuario autenticado
	 * @param taskProperties Propiedades del PBI a crear
	 * @param idPBI Identificador del PBI.
	 * @throws ScrumGraphException Excepción al realizar la asignación.
	 */
	public NodeTO createTaskToPBI(String authToken, Map<String,Object> taskProperties, Long idPBI) throws Exception {
		NodeTO taskTO = null;
		
		getTaskDelegate().validateProperties(taskProperties);
		
		taskTO = getTaskDelegate().createTask(authToken, taskProperties);
		
		taskTO = getTaskDelegate().assignTaskToPBI(authToken, taskTO.getId(), idPBI);
		
		return taskTO;
		
	}

}
