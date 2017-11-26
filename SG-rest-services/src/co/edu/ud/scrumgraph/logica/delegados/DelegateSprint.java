package co.edu.ud.scrumgraph.logica.delegados;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceSprintNode;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.serviciosrest.ISrvSGRest;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;

public class DelegateSprint extends DelegateService {

	/**
	 * Interfaz de servicio de los nodos Sprint
	 */
	private IServiceSprintNode service;

	/**
	 * Constructor basado en un servicio rest
	 * 
	 * @param serviceRest
	 *            Servicio rest
	 */
	public DelegateSprint(ISrvSGRest serviceRest) {
		super(serviceRest);
	}

	/**
	 * Constructor basado en un servicio rest
	 * 
	 * @param serviceRest
	 *            Servicio rest
	 */
	public DelegateSprint(IDelegateService delService) {
		super(delService);
	}

	@Override
	public IServiceSprintNode getServiceNode() {
		if (service == null) {
			service = srvFactory.getSprintNodeService();
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
		List<ESprintProperties> propToValid = new ArrayList<ESprintProperties>();
		propToValid.add(ESprintProperties.NAME);
		propToValid.add(ESprintProperties.DESCRIPTION);
		propToValid.add(ESprintProperties.CODE);
		propToValid.add(ESprintProperties.INIT_DATE);
		propToValid.add(ESprintProperties.END_DATE);
		propToValid.add(ESprintProperties.OWNER_ID);

		for (ESprintProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				String msg = null;
				msg = ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getMessage() + ": ";
				msg += property.getPropertyName();
				throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getCode(), msg);
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
	}

	/**
	 * Métood que crea un nodo de tipo Sprint.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario
	 * @param properties
	 *            Propiedades del nodo a crear
	 * @return NodeTO Objeto con la información del nodo creado.
	 * @throws ScrumGraphException
	 *             Excepción al crear un nodo Sprint.
	 */
	public NodeTO createSprint(String authToken, Map<String, Object> properties) throws ScrumGraphException {
		NodeTO sprintTO = null;

		// Validacion proyecto isAdmin
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		sprintTO = getServiceNode().createNode(properties);
		return sprintTO;
	}

	/**
	 * Método que consulta un nodo Sprint por su identificador pasado como
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
	public NodeTO getSprintById(String authToken, Long idNode) throws ScrumGraphException {

		// Validacion authTOken
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getNodeById(idNode);
	}

	/**
	 * Método para obtener todos los nodos de tipo Sprint.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario
	 * @return List<NodeTO> Lista de nodos Sprint almacenados en base de datos.
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
	 * Método que modifica un nodo Sprint en base de datos, basado en las
	 * propiedades pasadas como parámetros.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario. El usuario debe ser
	 *            administrador.
	 * @param idSprint
	 *            Identificador del Sprint a actualizar.
	 * @param newProperties
	 *            Propiedades a actualizar o adicionar al nodo
	 * @return NodeTO Objeto con la información actualizada del nodo.
	 * @throws ScrumGraphException
	 *             Excepción al actualizar el nodo.
	 */
	public NodeTO updateSprint(String authToken, Long idSprint, Map<String, Object> newProperties) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().updateNode(idSprint, newProperties);
	}

	/**
	 * Método que elimina un nodo de Sprint en base de datos.
	 * 
	 * @param authToken
	 *            Token autenticación del usuario. El usuario debe ser
	 *            administrador.
	 * @param idSprint
	 *            Identificador del Sprint a eliminar.
	 * @throws ScrumGraphException
	 *             Excepción ocurrida al realizar la eliminación.
	 */
	public void deleteSprint(String authToken, Long idSprint) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().deleteNode(idSprint);

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
	 * Asigna un nodo Sprint a un proyecto, basado en los atributos pasados como
	 * parámetros.
	 * 
	 * @param authToken
	 *            Token del usuario autenticado
	 * @param idSprint
	 *            Identificador del Sprint
	 * @param idProject
	 *            Identificador del proyecto.
	 * @throws ScrumGraphException
	 *             Excepción al realizar la asignación.
	 */
	public void assignSprintToProject(String authToken, Long idSprint, Long idProject) throws ScrumGraphException {
		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().assignSprintToProject(idSprint, idProject);
	}

	public NodeTO getPbisBySprint(String authToken, Long idSprint) throws ScrumGraphException {
		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().getPbisBySprint(idSprint);
	}

	/**
	 * Asigna un nodo pbi a un Sprint, basado en los atributos pasados como
	 * parámetros.
	 * 
	 * @param authToken
	 *            Token del usuario autenticado
	 * @param pbiProperties
	 *            Propiedades del PBI a crear
	 * @param idSprint
	 *            Identificador del Sprint.
	 * @throws ScrumGraphException
	 *             Excepción al realizar la asignación.
	 */
	public NodeTO assignPBIToSprint(String authToken, Map<String, Object> pbiProperties, Long idSprint) throws Exception {
		NodeTO pbiTO = null;
		NodeTO sprintTO = null;

		getPBIDelegate().validateProperties(pbiProperties);
		pbiTO = getPBIDelegate().createPBI(authToken, pbiProperties);
		try {
			sprintTO = getPBIDelegate().assignPBIToSprint(authToken, pbiTO.getId(), idSprint);
		} catch (ScrumGraphException ex) {
			getPBIDelegate().deletePBI(authToken, pbiTO.getId());
			throw ex;
		}
		if (sprintTO == null || sprintTO.getIncomingNodes() == null) {
			getPBIDelegate().deletePBI(authToken, pbiTO.getId());
			throw new ScrumGraphException(ECodeExceptionSG.SPRINT_DOESNT_EXISTS);
		}
		pbiTO.setIncomingNodes(sprintTO.getIncomingNodes());

		return pbiTO;

	}
	
	public  Map<String, Integer> getChartSprint(Long idSprint)  throws ScrumGraphException {
		return getServiceNode().getChartById(idSprint);
	}

}
