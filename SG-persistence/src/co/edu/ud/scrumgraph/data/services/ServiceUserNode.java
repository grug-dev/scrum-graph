package co.edu.ud.scrumgraph.data.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;

import co.edu.ud.scrumgraph.data.api.NodeAPI;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.ERelTypes;
import co.edu.ud.scrumgraph.data.enums.EUserProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceUserNode;
import co.edu.ud.scrumgraph.data.util.SGUtil;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

class ServiceUserNode extends NodeAPI implements  IServiceUserNode {

	private  void validatePropertiesUser(Map<String, Object> properties, Long idUser) throws ScrumGraphException {
		Object propertyObj = null;
		String strProperty = null;

		// Validaci�n Nulidad
		SGUtil.validateProperties(properties);

		// Validacion propiedades email, name, lastName, password, defaultRole.
		List<EUserProperties> propToValid = new ArrayList<EUserProperties>();
		propToValid.add(EUserProperties.NAME);
		propToValid.add(EUserProperties.LAST_NAME);
		propToValid.add(EUserProperties.EMAIL);
		propToValid.add(EUserProperties.PASSWORD);
		propToValid.add(EUserProperties.ROLDEFAULT);

		for (EUserProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				continue;
			}
			strProperty = propertyObj.toString();
			switch (property) {
			case EMAIL:
				// Asignar MD5 al authToken via email.
				validateEmail(strProperty, idUser);
				// Si es para crear un usuario, se obtiene su authtoken.
				properties.put(EUserProperties.AUTHTOKEN.getPropertyName(), SGUtil.toMD5(strProperty));
				break;
			case PASSWORD:
				validatePassword(strProperty);
				break;
			case ROLDEFAULT:
				switch (strProperty.toLowerCase()) {
				case "scrum":
				case "team-member":
				case "team-leader":
				case "scrum-master":
				case "product-owner":
					break;
				default:
					throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES.getCode(), ECodeExceptionSG.INVALID_PROPERTIES.getMessage());
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * M�todo que basado en Iterator del mapa pasado como par�metro, �l cu�l es
	 * el resultante de las consultas hechas de usuario, retorna el resultado
	 * del nodo <i>u</i> del query.
	 * 
	 * @param resultQuery
	 *            Iterator<Map<String, Object>> Resultado del query
	 * @return Node Objeto con la informaci�n del usuario encontrado en el
	 *         query.
	 */
	private  Node getUserMatchU(Iterator<Map<String, Object>> resultQuery) {
		Node userBD = null;

		if (resultQuery != null) {
			if (resultQuery.hasNext()) {
				Map<String, Object> mapUser = resultQuery.next();
				userBD = (Node) mapUser.get("u");
			}
		}

		return userBD;
	}

	/**
	 * M�todo encargado de validar el par�metro email.
	 * 
	 * @param email
	 *            Email a Validar
	 * @throws ScrumGraphException
	 *             Excepci�n si no cumple alguna validaci�n del email.
	 */
	private  void validateEmail(String email, Long idUser) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();
		Iterator<Map<String, Object>> resultQuery = null;

		if (email == null) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES.getCode(), ECodeExceptionSG.INVALID_PROPERTIES.getMessage());
		}

		if (!SGUtil.validateFormatEmail(email)) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_EMAIL.getCode(), ECodeExceptionSG.INVALID_EMAIL.getMessage());
		}

		query = "MATCH (u:USER) WHERE u." + EUserProperties.EMAIL.getPropertyName();
		query += "={email} ";
		if (idUser != null) {
			query += " AND id(u) <> {idUser}";
			params.put("idUser", idUser);
		}
		query += " RETURN 1";
		params.put("email", email);

		resultQuery = executeQuerySearch(query, params);

		if (resultQuery.hasNext()) {
			throw new ScrumGraphException(ECodeExceptionSG.EMAIL_ALREADY_EXISTS.getCode(), ECodeExceptionSG.EMAIL_ALREADY_EXISTS.getMessage());
		}
	}

	public  void isUserAvailable(NodeTO userTO) throws ScrumGraphException {
		Object available = null;

		available = userTO.getProperties().get(EUserProperties.AVAILABLE.getPropertyName());

		if (available == null) {
			throw new ScrumGraphException(ECodeExceptionSG.USER_UNAVAILABLE.getCode(), ECodeExceptionSG.USER_UNAVAILABLE.getMessage());
		}

		if (!(available instanceof Boolean)) {
			throw new ScrumGraphException(ECodeExceptionSG.USER_UNAVAILABLE.getCode(), ECodeExceptionSG.USER_UNAVAILABLE.getMessage());
		}

		if (!(Boolean) available) {
			throw new ScrumGraphException(ECodeExceptionSG.USER_UNAVAILABLE.getCode(), ECodeExceptionSG.USER_UNAVAILABLE.getMessage());
		}

	}

	/**
	 * M�todo encargado de buscar un usuario por su propiedad authToken.
	 * 
	 * @param authToken
	 *            AuthToken a buscar en la base de datos
	 * @return NodeTO Objeto con la informaci�n del usuario encontrado, nulo en
	 *         caso contrario.
	 */
	public  NodeTO validateGetUserByAuthToken(String authToken, boolean validateIsAdmin) throws ScrumGraphException {
		NodeTO userTO = null;
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		Map<String, Object> params = null;
		Node nodeBD = null;

		if (authToken == null || authToken.isEmpty()) {
			throw new ScrumGraphException(ECodeExceptionSG.UNAUTHENTICATED_USER);
		}

		query = "MATCH (u:USER) USING INDEX u:USER(authToken) WHERE u.authToken = {paramAuthToken} RETURN u";
		params = new HashMap<String, Object>();
		params.put("paramAuthToken", authToken);
		resultQuery = executeQuerySearch(query, params);
		nodeBD = getUserMatchU(resultQuery);

		if (nodeBD != null) {
			userTO = convertTO(nodeBD);
		}

		if (userTO == null) {
			throw new ScrumGraphException(ECodeExceptionSG.UNAUTHENTICATED_USER);
		}

		if (validateIsAdmin) {
			Object isAdmin = userTO.getProperties().get(EUserProperties.ISADMIN.getPropertyName());
			if (isAdmin == null || !Boolean.valueOf(isAdmin.toString())) {
				throw new ScrumGraphException(ECodeExceptionSG.UNAUTHORIZED_USER);
			}
		}
		
		isUserAvailable(userTO);

		return userTO;
	}

	/**
	 * M�todo encargado de validar la autenticaci�n de un usuario basado en el
	 * email y password pasados como par�metros.
	 * 
	 * @param email
	 *            Email
	 * @param password
	 *            Password
	 * @return NodeTO con la informaci�n del usuario si la contrase�a y correo
	 *         electronico fueron correctos. <i>null</i> si fue invalido el
	 *         ingreso.
	 */
	public  NodeTO validateAuthentication(String email, String password) throws ScrumGraphException {
		NodeTO userTO = null;
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();
		Iterator<Map<String, Object>> resultQuery = null;
		Node userBD = null;

		if (!SGUtil.validateFormatEmail(email)) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_EMAIL.getCode(), ECodeExceptionSG.INVALID_EMAIL.getMessage());
		}

		validatePassword(password);

		query = "MATCH ( u:USER ) WHERE u.password =  {password} AND u.email = {email} RETURN u";
		params.put("email", email);
		params.put("password", password);

		resultQuery = executeQuerySearch(query, params);
		userBD = getUserMatchU(resultQuery);

		if (userBD != null) {
			userTO = convertTO(userBD);
			isUserAvailable(userTO);
		}

		if (userTO == null) {
			throw new ScrumGraphException(ECodeExceptionSG.USER_DOESNT_EXISTS.getCode(), ECodeExceptionSG.USER_DOESNT_EXISTS.getMessage());
		}

		return userTO;
	}

	/**
	 * M�todo encargado de crear un usuario basado en las propiedades pasadas
	 * c�mo parametros.
	 * 
	 * @param properties
	 *            Propiedades del usuario a crear.
	 * @return NodeTO Objeto con la informaci�n del usuario creado.
	 * @throws ScrumGraphException
	 */
	public  NodeTO createUser(String authToken, Map<String, Object> properties) throws ScrumGraphException {
		NodeTO userNode = null;

		// Validar Usuario que desea crear el nodo
		validateGetUserByAuthToken(authToken, true);

		// Validacion Propiedades
		validatePropertiesUser(properties, null);

		// Asignar Available
		properties.put(EUserProperties.AVAILABLE.getPropertyName(), true);

		// Creaci�n de usuario
		userNode = new NodeTO();
		userNode.setProperties(properties);
		userNode.setLabelNode(ELabels.USER);
		createNode(userNode);

		return userNode;
	}

	/**
	 * M�todo que realiza las validaciones a la propiedad password pasada como
	 * par�metro.
	 * 
	 * @param password
	 *            String Password a validar
	 * @throws ScrumGraphException
	 *             Excepci�n indicando que la validaci�n no fue satisfactoria.
	 */
	private  void validatePassword(String password) throws ScrumGraphException {

		if (password == null) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES.getCode(), ECodeExceptionSG.INVALID_PROPERTIES.getMessage());
		}

		int lenght = password.length();
		if (!(lenght >= 6 && lenght <= 20)) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES.getCode(), ECodeExceptionSG.INVALID_PROPERTIES.getMessage());
		}

	}

	/**
	 * M�todo que retorna todos los usuarios existentes.
	 * 
	 * @return List<NodeTO> Lista con los nodos de los usuarios.
	 */
	public  List<NodeTO> getAllUsers(String authToken) throws ScrumGraphException {
		List<NodeTO> allUsers = null;

		validateGetUserByAuthToken(authToken, false);
		allUsers = getAllByLabel(ELabels.USER);

		return allUsers;
	}

	public  NodeTO getUserById(String authToken, Long userId, boolean validateIsAdmin) throws ScrumGraphException {
		NodeTO userTO = null;

		validateGetUserByAuthToken(authToken, validateIsAdmin);

		userTO = searchById(userId, ELabels.USER);
		if (userTO == null) {
			throw new ScrumGraphException(ECodeExceptionSG.USER_DOESNT_EXISTS);
		}
		
		

		return userTO;
	}

	/**
	 * Servicio usado para obtener listado de todos los proyectos a los que
	 * pertenece un usuario
	 * 
	 * @param authToken String token del proyecto autenticado y ejecutor del API.
	 * @param idUser Identificador del usuario
	 * @return List<NodeTO> listado de proyectos
	 * @throws ScrumGraphException
	 */
	public  NodeTO getProjectsByIdUser(String authToken, Long idUser) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		NodeTO userTO = null;
		List<NodeTO> lstProjects = new ArrayList<NodeTO>();

		// Validacion Usuario Autenticado.
		// Validacion Existencia Usuario
		userTO = getUserById(authToken, idUser, false);

		// Obtener proyectos por usuario
		query = "MATCH (u:USER)-[]->(t:TEAM)-[]-(p:PROJECT) WHERE id(u) = {id} RETURN p";
		params = new HashMap<>();
		params.put("id", idUser);

		// Ejecutar consulta
		// Obtener Proyectos
		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);

		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				Node proyectoBD = (Node) mapProjects.get("p");
				if (proyectoBD != null) {
					NodeTO proyectoTO = convertTO(proyectoBD);
					lstProjects.add(proyectoTO);
				}
			}
		}

		// Asignando los proyectos a la relacion outgoing del nodo usuario
		userTO.setOutgoingNodes(lstProjects);

		return userTO;
	}

	public  void assignProjectToUser(String authToken, Long idUser, Long idProject) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		Boolean relationCreated = null;

		if (idProject == null || idProject < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		// Validacion usuario requester
		getUserById(authToken, idUser, true);

		// Verificar si existe relacion previa.
		// Si no existe, crea la relaci�n, si existe no la crea.
		params = new HashMap<>();
		params.put("idUser", idUser);
		params.put("idProj", idProject);
		query = "MATCH (p:PROJECT)-[]-(t:TEAM) WHERE id(p) = {idProj} WITH t,p MATCH (u:USER) WHERE id(u) = {idUser} ";
		query += " MERGE (u)-[r:";
		query += ERelTypes.BELONGS_TO + "]->(t)";
		query += " ON CREATE SET r.created=true ON MATCH SET r.created=false  RETURN r.created , p ";

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);
		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("r.created")) {
					Object rta = mapProjects.get("r.created");
					if (rta != null && (Boolean) rta) {
						relationCreated = true;
					}
					else {
						relationCreated = false;
					}
				}
			}
		}
		
		if (relationCreated == null) {
			throw new ScrumGraphException(ECodeExceptionSG.PROJECT_DOESNT_EXISTS);
		}

		if (!relationCreated) {
			throw new ScrumGraphException(ECodeExceptionSG.OPERATION_FAILED);
		}
	}

	/**
	 * M�todo para actualizar un usuario en BDOG
	 * 
	 * @param authToken String token del usuario autenticado y ejecutor del API.
	 * @param idUserToUpdate Identificador del usuario a actualizar
	 * @param newProperties Map<String, Object> Mapa con las propiedades a actualizar o agregar si no existen
	 * @return NodeTO Objeto TO con la informaci�n del usuario actualizado. 
	 * @throws ScrumGraphException Excecpi�n de validaci�n
	 */
	public  NodeTO updateUser(String authToken, Long idUserToUpdate, Map<String, Object> newProperties) throws ScrumGraphException {
		NodeTO userTO = null;

		// Validacion de si usuario es IsAdmin
		// Validar y obtener usuario a actualizar
		userTO = getUserById(authToken, idUserToUpdate, true);

		// Validacion Nuevas Propiedades
		validatePropertiesUser(newProperties, idUserToUpdate);

		
		newProperties.put(ENodeProperties.ID.getPropertyName(), null);

		// Actualizar Usuario
		updateNodeById(ELabels.USER, idUserToUpdate, newProperties);
		userTO.setProperties(newProperties);

		return userTO;

	}

	/**
	 * M�todo para eliminar un usuario en BDOG
	 * 
	 * @param authToken
	 *            String token del usuario autenticado y ejecutor del API.
	 * @param idUserToDelete
	 *            Identificador del usuario a eliminar
	 * @return NodeTO Objeto TO con la informaci�n del usuario
	 * @throws ScrumGraphException
	 *             Excecpi�n de validaci�n
	 */
	public  void deleteUser(String authToken, Long idUserToDelete) throws ScrumGraphException {

		// Validacion de si usuario es IsAdmin
		// Validar y obtener usuario a actualizar
		getUserById(authToken, idUserToDelete, true);
		// Eliminaci�n Usuario
		deleteNodeById(ELabels.USER, idUserToDelete);

	}

	@Override
	protected void validateProperties(Map<String, Object> validateProperties) throws ScrumGraphException {
		// TODO Auto-generated method stub
		
	}
	
	private NodeTO convertTO(Node nodeBD) {
		return convertTO(nodeBD, ELabels.USER);
	}

	@Override
	public void deleteUserFromProject(String authToken, Long idUser, Long idProject) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		boolean relationCreated = false;

		if (idProject == null || idProject < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		// Validacion usuario requester
		getUserById(authToken, idUser, true);

		// Verificar si existe relacion previa.
		// Si no existe, crea la relaci�n, si existe no la crea.
		params = new HashMap<>();
		params.put("idUser", idUser);
		params.put("idProj", idProject);
		
		query = "OPTIONAL MATCH (u:USER) WHERE id(u) = {idUser} WITH u ";
		query += "OPTIONAL MATCH (p:PROJECT)-[]-(t:TEAM) WHERE id(p) = {idProj} WITH p,t,u ";
		query += "OPTIONAL MATCH (u)-[r]->(t) ";
		query += "WITH p,u,r, type(r) as result ";
		query += "DELETE r RETURN result , p.name as project , u.name as user";

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);
		if (resultQuery != null) {
			if (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.get("project") != null) {
					if (mapProjects.get("user") != null) {
						if (mapProjects.get("result") != null) {
							relationCreated = true;
						}
					}
					else {
						throw new ScrumGraphException(ECodeExceptionSG.USER_DOESNT_EXISTS);
					}
				}
				else {
					throw new ScrumGraphException(ECodeExceptionSG.PROJECT_DOESNT_EXISTS);
				}
			}
		}

		if (!relationCreated) {
			throw new ScrumGraphException(ECodeExceptionSG.OPERATION_FAILED);
		}
	
		
	}

}
