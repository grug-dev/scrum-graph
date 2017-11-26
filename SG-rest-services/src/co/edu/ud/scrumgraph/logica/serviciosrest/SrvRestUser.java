package co.edu.ud.scrumgraph.logica.serviciosrest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.EProjectProperties;
import co.edu.ud.scrumgraph.data.enums.EUserProperties;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.delegados.DelegateUser;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;
import net.sf.json.JSONObject;

@Path("users")
public class SrvRestUser implements ISrvSGRest {

	
	private DelegateUser userDelegate;
	
	
	private DelegateUser getUserDelegate() {
		if (userDelegate == null) {
			userDelegate = new DelegateUser(this);
		}
		return userDelegate;
	}
	
	/**
	 * Método que obtiene el response de los proyectos asociados
	 * a un usuario pasado como parametro
	 * @param userTO NodeTO usuario a extraer la informacion de los proyectos 
	 * @return JSONObject Objeto JSON con la propiedad <i>projects</i> con los proyectos
	 * relacionados al usuario.
	 */
	private JSONObject getProjectsByUserJSON(NodeTO userTO) {
		List<JSONObject> allProjJO = null;
		JSONObject projJSON = new JSONObject();
		
		if (userTO.getOutgoingNodes() != null) {
			allProjJO = new ArrayList<JSONObject>();
			JSONObject projectJSON = null;
			List<String> lstPropKeys = new ArrayList<String>();
			for (EProjectProperties projProp : EProjectProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			// Adicionar Propiedad CreateAT
			lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
			for (NodeTO projNodeTO : userTO.getOutgoingNodes()) {
				projectJSON = HelperSG.createJSONObject(lstPropKeys, projNodeTO, null);
				allProjJO.add(projectJSON);
			}			
		}
		
		projJSON.put("projects", allProjJO);
		
		return projJSON;
	}
	
	private static void validatePropertiesUser(Map<String, Object> properties) throws SGException {
		Object propertyObj = null;
		String strProperty = null;


		// Validacion propiedades email, name, lastName, password, defaultRole.
		List<EUserProperties> propToValid = new ArrayList<EUserProperties>();
		propToValid.add(EUserProperties.NAME);
		propToValid.add(EUserProperties.LAST_NAME);
		propToValid.add(EUserProperties.EMAIL);
		propToValid.add(EUserProperties.ROLDEFAULT);

		for (EUserProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				String msg = null;
				msg = ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getMessage() +  ": "; 
				msg += property.getPropertyName();
				throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getCode(),msg);
			}
			strProperty = propertyObj.toString();
			switch (property) {
			case NAME:
			case LAST_NAME:
				HelperSG.validadLenght(3, 40, strProperty);
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
					throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getCode(), ECodesResponse.INCOMPLETE_REQUEST_FIELDS.getMessage());
				}
				break;
			default:
				break;
			}
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, String reqPayLoad) {
		Map<String, Object> userMap = null;
		JSONObject result = null;
		NodeTO userTO = null;
		JSONObject userJSON = null;
		String authToken = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			userMap = HelperSG.getReqPayLoadProperties(reqPayLoad);
			validatePropertiesUser(userMap);
			userTO = getUserDelegate().createUser(authToken , userMap);
			userJSON = getUserJSON(userTO , true);
			result = HelperSG.getOKResponse(userJSON);
		} catch (SGException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), e.getMessage());
		} 

		return HelperSG.makeResponse(result);
	}

	@POST
	@Path("login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validationAuth(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, String reqPayLoad) {
		NodeTO userTO = null;
		JSONObject result = null;
		Map<String, Object> userMap = null;

		try {
			userMap = HelperSG.getReqPayLoadProperties(reqPayLoad);
			// Validacion atributos
			Object emailObj = userMap.get(EUserProperties.EMAIL.getPropertyName());
			Object passObj = userMap.get(EUserProperties.PASSWORD.getPropertyName());
			
			if (emailObj == null || passObj == null) {
				throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS);
			}
			
			userTO = getUserDelegate().validateAuthentication(emailObj.toString(), passObj.toString());
			JSONObject userJSON = getUserJSON(userTO , true);
			result = HelperSG.getOKResponse(userJSON);
		} catch (SGException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}

	
	@PUT
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader , @PathParam("id") Long userIdToUpdate , String reqPayLoad) {
		JSONObject result = null;
		JSONObject userJSON = null;
		NodeTO userTO = null;
		String authToken = null;
		Map<String, Object> newPropUser = null;
		
		
		try {
			// Validacion y obtencion de authToken Requester
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			
			//Obtencion de par�metros a actualizar
			newPropUser = HelperSG.getReqPayLoadProperties(reqPayLoad);
			validatePropertiesUser(newPropUser);
			
			//Actualizar Usuario
			userTO = getUserDelegate().updateUser(authToken, userIdToUpdate, newPropUser);
			
			//Armar response
			userJSON = getUserJSON(userTO, true, EUserProperties.AUTHTOKEN, EUserProperties.AVAILABLE);
			result = HelperSG.getOKResponse(userJSON);
			
		}
		catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}
	
	@GET
	@Path("{id}")	
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader , @PathParam("id") Long userId) {
		JSONObject result = null;
		NodeTO userTO = null;
		String authToken = null;
		try {
			if (userId == null || userId < 0) {
				throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS);
			}

			authToken = HelperSG.validateHeader(scrumGraphHeader);
			userTO = getUserDelegate().getUserById(authToken,userId,false);
			JSONObject userJSON = getUserJSON(userTO, true , EUserProperties.AUTHTOKEN);
			result = HelperSG.getOKResponse(userJSON);
		}
		catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader) {
		List<NodeTO> allUsers = null;
		JSONObject result = null;
		JSONObject usersResponse = null;
		String authTOken = null;
		try {
			// validar header
			authTOken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener todos los usuarios
			allUsers = getUserDelegate().getAllUsers(authTOken);
			List<JSONObject> allUserJO = new ArrayList<JSONObject>();
			// Convertir lista usuarios formato Response
			for (NodeTO userTO : allUsers) {
				JSONObject user = getUserJSON(userTO, false , EUserProperties.AUTHTOKEN);
				allUserJO.add(user);
			}
			usersResponse = new JSONObject();
			usersResponse.put("users",allUserJO);
			result = HelperSG.getOKResponse(usersResponse);
			
		}
		catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}
		
		return HelperSG.makeResponse(result);
	}
	
	
	@GET
	@Path("verify/token")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyToken(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader) {
		JSONObject result = null;
		String authToken = null;
		try {
			
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			getUserDelegate().verifyToken(authToken, false);
			
			result = HelperSG.getOKResponse(null);
		}
		catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}
		
		
		return HelperSG.makeResponse(result);
	}

	/**
	 * Servicio usado para obtener listado de todos los proyectos a los que pertenece un usuario. 
	 * @param scrumGraphHeader
	 * @return
	 */
	@GET
	@Path("{id}/projects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectsByIdUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader ,@PathParam("id") Long idUser) {
		JSONObject result = null;
		String authToken = null;
		NodeTO userTO = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener Proyectos By User
			userTO = getUserDelegate().getProjectsByIdUser(authToken, idUser);
			JSONObject projectsResponse = getProjectsByUserJSON(userTO);
			result = HelperSG.getOKResponse(projectsResponse);
		}
		catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}
		
		
		return HelperSG.makeResponse(result);
	}
	
	
	/**
	 * Servicio usado para asignar un proyecto a un usuario. 
	 * @param scrumGraphHeader Custom Header de Scrum Graph
	 * @param idUser Long Identificador del usuario
	 * @return Response Respuesta del servicio REST.
	 */
	@POST
	@Path("{idUser}/project")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response assignProjectToUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader , @PathParam("idUser") Long idUser , String reqPayLoad) {
		JSONObject result = null;
		String authToken = null;
		Long idProject = null;
		Map<String,Object> payLoadProperties = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			
			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			if (payLoadProperties.containsKey("projectId") ) {
				idProject = Long.parseLong(payLoadProperties.get("projectId").toString());
			}
			
			// Asignar proyecto a un usuario
			getUserDelegate().assignProjectToUser(authToken, idUser , idProject);
			result = HelperSG.getOKResponse(null);
		}
		catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}
		
		
		return HelperSG.makeResponse(result);
	}
	
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader ,@PathParam("id") Long idUser) {
		JSONObject result = null;
		String authToken = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			getUserDelegate().deleteUser(authToken, idUser);
			result = HelperSG.getOKResponse(null);
		} catch (SGException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), e.getMessage());
		} 

		return HelperSG.makeResponse(result);
	
	}
	
	/**
	 * Servicio usado para desasignar un usuario de un proyecto 
	 * @param scrumGraphHeader Custom Header de Scrum Graph
	 * @param idUser Long Identificador del usuario
	 * @return Response Respuesta del servicio REST.
	 */
	@DELETE
	@Path("{idUser}/project")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response deleteUserFromProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader , @PathParam("idUser") Long idUser , String reqPayLoad) {
		JSONObject result = null;
		String authToken = null;
		Long idProject = null;
		Map<String,Object> payLoadProperties = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			
			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			if (payLoadProperties.containsKey("projectId") ) {
				idProject = Long.parseLong(payLoadProperties.get("projectId").toString());
			}
			
			// Asignar proyecto a un usuario
			getUserDelegate().deleteUserFromProject(authToken, idUser, idProject);
			result = HelperSG.getOKResponse(null);
		}
		catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		}catch (ScrumGraphException ex ) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodesResponse.ERROR_CODE.getCode(), ex.getMessage());
		}
		
		
		return HelperSG.makeResponse(result);
	}
	private JSONObject getUserJSON(NodeTO userTO, boolean includeUserResponse , EUserProperties... notIncludePropertie) {
		JSONObject userJSON = null;

		if (userTO == null) {
			return userJSON;
		}

		userJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();
		for (EUserProperties userProp : EUserProperties.values()) {
			if (!userProp.equals(EUserProperties.PASSWORD)) {
				lstPropKeys.add(userProp.getPropertyName());
			}
		}

		// Eliminar Propiedades
		if (notIncludePropertie != null) {
			for (EUserProperties property : notIncludePropertie) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}
		
		HelperSG.createJSONObject(lstPropKeys, userTO, userJSON);

		if (includeUserResponse) {
			JSONObject userResponseJSON = new JSONObject();
			userResponseJSON.put("user", userJSON);
			return userResponseJSON;
		}
		
		return userJSON;
	}
	
	
	

	

	/** OPTIONS */
	@OPTIONS 
	public Response rootCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/{id}")
	public Response userIdCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	

	@OPTIONS 
	@Path("/{id}/projects")
	public Response idProjectsCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	
	@OPTIONS 
	@Path("/{id}/project")
	public Response idProjectCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	
	@OPTIONS 
	@Path("/login")
	public Response loginCodeCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/verify/token")
	public Response verifyTokenCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	

}
