package co.edu.ud.scrumgraph.logica.serviciosrest;

import java.util.ArrayList;
import java.util.Arrays;
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

import co.edu.ud.scrumgraph.data.dto.GraphTO;
import co.edu.ud.scrumgraph.data.dto.IndicadoresTO;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.dto.RelationshipTO;
import co.edu.ud.scrumgraph.data.dto.StatsTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.enums.EProjectProperties;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.enums.EUserProperties;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.delegados.DelegateProject;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

@Path("projects")
public class SrvRestProject implements ISrvSGRest {

	private static final String NAME_TEAM_ON_PROJECT = "teamName";

	private DelegateProject projectDelegate;

	private DelegateProject getProjectDelegate() {
		if (projectDelegate == null) {
			projectDelegate = new DelegateProject(this);
		}
		return projectDelegate;
	}

	public SrvRestProject() {
	}
	
	/**
	 * Método que obtiene el response de los usuarios asociados a un proyecto
	 * pasado como parametro
	 * 
	 * @param projectTO
	 *            NodeTO usuario a extraer la informacion de los proyectos
	 * @return JSONObject Objeto JSON con la propiedad <i>users</i> con los
	 *         proyectos relacionados al usuario.
	 */
	private JSONObject getUsersByProjectJSON(NodeTO projectTO) {
		List<JSONObject> allUsersJSON = null;
		JSONObject projJSON = new JSONObject();

		if (projectTO.getIncomingNodes() != null) {
			allUsersJSON = new ArrayList<JSONObject>();
			JSONObject userJSON = null;
			List<String> lstPropKeys = new ArrayList<String>();

			for (EUserProperties userProp : EUserProperties.values()) {
				lstPropKeys.add(userProp.getPropertyName());
			}
			lstPropKeys.remove(EUserProperties.PASSWORD.getPropertyName());
			lstPropKeys.remove(EUserProperties.AUTHTOKEN.getPropertyName());

			for (NodeTO userNodeTO : projectTO.getIncomingNodes()) {
				userJSON = HelperSG.createJSONObject(lstPropKeys, userNodeTO, null);
				allUsersJSON.add(userJSON);
			}
		}

		projJSON.put("users", allUsersJSON);

		return projJSON;
	}
	
	/**
	 * Método que obtiene el response de los sprints asociados a un proyecto
	 * pasado como parametro
	 * 
	 * @param projectTO
	 *            NodeTO usuario a extraer la informacion de los proyectos
	 * @return JSONObject Objeto JSON con la propiedad <i>sprints</i> con los
	 *         proyectos relacionados al usuario.
	 */
	private JSONObject getSprintsByProjectJSON(NodeTO projectTO) {
		List<JSONObject> allOutGoingJSON = null;
		JSONObject projJSON = new JSONObject();
		
		if (projectTO == null) {
			return null;
		}
		
		projJSON = getProjectJSON(projectTO, true);

		if (projectTO.getOutgoingNodes() != null) {
			allOutGoingJSON = new ArrayList<JSONObject>();
			JSONObject sprintJSON = null;
			List<String> lstPropKeys = new ArrayList<String>();

			for (ESprintProperties sprintProp : ESprintProperties.values()) {
				lstPropKeys.add(sprintProp.getPropertyName());
			}
			lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

			for (NodeTO sprintNodeTO : projectTO.getOutgoingNodes()) {
				sprintJSON = HelperSG.createJSONObject(lstPropKeys, sprintNodeTO, null);
				sprintJSON.put("projectId", projectTO.getId());
				allOutGoingJSON.add(sprintJSON);
			}
		}

		projJSON.put("sprints", allOutGoingJSON);

		return projJSON;
	}
	
	/**
	 * Método que obtiene el response de los pbis asociados a un proyecto
	 * pasado como parametro
	 * 
	 * @param projectTO
	 *            NodeTO usuario a extraer la informacion de los proyectos
	 * @return JSONObject Objeto JSON con la propiedad <i>pbis</i> con los
	 *         proyectos relacionados al usuario.
	 */
	private JSONObject getPBISByProjectJSON(NodeTO projectTO) {
		List<JSONObject> allOutGoingJSON = null;
		JSONObject projJSON = new JSONObject();
		JSONObject responseJSON = new JSONObject(false);
		
		if (projectTO == null) {
			return null;
		}
		
		projJSON = getProjectJSON(projectTO, false, null);

		if (projectTO.getOutgoingNodes() != null) {
			allOutGoingJSON = new ArrayList<JSONObject>();
			JSONObject pbiJSON = new JSONObject(false);
			List<String> lstPropKeys = new ArrayList<String>();

			for (EPBIProperties pbiProp : EPBIProperties.values()) {
				lstPropKeys.add(pbiProp.getPropertyName());
			}

			for (NodeTO pbiNodeTO : projectTO.getOutgoingNodes()) {
				pbiJSON = HelperSG.createJSONObject(lstPropKeys, pbiNodeTO, pbiJSON);
				pbiJSON.put("projectId", projectTO.getId());
				allOutGoingJSON.add(pbiJSON);
			}
		}

		responseJSON.put("project", projJSON);
		responseJSON.put("pbis", allOutGoingJSON);

		return responseJSON;
	}

	private void validateProperties(Map<String, Object> properties) throws SGException {
		Object propertyObj = null;
		String strProperty = null;

		// Validacion propiedades
		List<EProjectProperties> propToValid = new ArrayList<EProjectProperties>();
		propToValid.add(EProjectProperties.NAME);
		propToValid.add(EProjectProperties.DESCRIPTION);
		propToValid.add(EProjectProperties.CODE);
		propToValid.add(EProjectProperties.OWNER_ID);

		for (EProjectProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				throw new SGException(ECodesResponse.INCOMPLETE_REQUEST_FIELDS);
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

	private JSONObject getProjectJSON(NodeTO projectTO, boolean includeProjectResponse, EProjectProperties... notIncludePropertie) {
		JSONObject projectJSON = null;

		if (projectTO == null) {
			return projectJSON;
		}

		projectJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (EProjectProperties projProp : EProjectProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(NAME_TEAM_ON_PROJECT);
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludePropertie != null) {
			for (EProjectProperties property : notIncludePropertie) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		HelperSG.createJSONObject(lstPropKeys, projectTO, projectJSON);

		if (includeProjectResponse) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put("project", projectJSON);
			return projRespJSON;
		}

		return projectJSON;
	}

	private JSONObject getGraphNodesJSON(NodeTO nodeTO) {
		JSONObject graphJSON = null;
		if (nodeTO == null) {
			return graphJSON;
		}

		graphJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();
		switch (nodeTO.getLabelNode()) {
		case PROJECT:
			// Asignar propiedades a retornar en el reponse
			for (EProjectProperties projProp : EProjectProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			lstPropKeys.add(NAME_TEAM_ON_PROJECT);
			break;
		case SPRINT:
			// Asignar propiedades a retornar en el reponse
			for (ESprintProperties projProp : ESprintProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			break;
		case PBI:
			// Asignar propiedades a retornar en el reponse
			for (EPBIProperties projProp : EPBIProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			break;
		case TASK:
			// Asignar propiedades a retornar en el reponse
			for (ETaskProperties projProp : ETaskProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			break;
			default:
				break;
		}

		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
		HelperSG.createJSONObject(lstPropKeys, nodeTO, graphJSON);
		if (graphJSON != null) {
			graphJSON.put("nodeType", nodeTO.getLabelNode().getName());
			graphJSON.put("nodeLabels", Arrays.asList(nodeTO.getLabelNode().getName()));
		}

		return graphJSON;
	}

	private JSONObject getGraphLinksJSON(RelationshipTO relationTO) {
		JSONObject graphJSON = null;

		if (relationTO == null) {
			return graphJSON;
		}

		graphJSON = new JSONObject(false);
		graphJSON.put(ENodeProperties.ID.getPropertyName(), relationTO.getId());
		graphJSON.put("type", relationTO.getType());
		graphJSON.put("sourceId", relationTO.getSourceId());
		graphJSON.put("targetId", relationTO.getTargetId());

		return graphJSON;
	}

	/**
	 * Servicio REST para crear un Project.
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param reqPayLoad
	 *            Formato JSON compuesto por un conjunto de propiedades
	 *            referentes al nodo a crear.
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, String reqPayLoad) {
		Map<String, Object> projProperties = null;
		JSONObject projectJSON = null, responseJSON = null;
		String authToken = null;
		NodeTO projectTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			projProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Validacion Properties
			validateProperties(projProperties);

			projectTO = getProjectDelegate().createProject(authToken, projProperties);
			projectJSON = getProjectJSON(projectTO, true);
			responseJSON = HelperSG.getOKResponse(projectJSON);
		} catch (SGException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), e.getMessage());
		}

		return HelperSG.makeResponse(responseJSON);
	}

	/**
	 * Servicio REST para consultar un Project basado en su identificador
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del Project a consultar
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectById(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject projectJSON = null, responseJSON = null;
		String authToken = null;
		NodeTO projectTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			projectTO = getProjectDelegate().getProjectById(authToken, id);
			projectJSON = getProjectJSON(projectTO, true);
			responseJSON = HelperSG.getOKResponse(projectJSON);
		} catch (SGException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), e.getMessage());
		}

		return HelperSG.makeResponse(responseJSON);

	}

	/**
	 * Servicio REST para consultar todos los Projects.
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllProjects(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader) {
		List<NodeTO> allProjects = null;
		JSONObject result = null;
		JSONObject projectResponse = null;
		String authTOken = null;
		try {
			// validar header
			authTOken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener todos los proyectos
			allProjects = getProjectDelegate().getAllProjects(authTOken);
			List<JSONObject> allProjectsJSON = new ArrayList<JSONObject>();
			// Convertir lista usuarios formato Response
			for (NodeTO projectTO : allProjects) {
				JSONObject projectJSON = getProjectJSON(projectTO, false);
				allProjectsJSON.add(projectJSON);
			}
			projectResponse = new JSONObject();
			projectResponse.put("projects", allProjectsJSON);
			result = HelperSG.getOKResponse(projectResponse);

		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}

	/**
	 * Servicio REST para actualizar un Project
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idProject
	 *            Identificador del nodo del project a actualizar
	 * @param reqPayLoad
	 *            Formato JSON compuesto por un conjunto de propiedades
	 *            referentes al nodo a actualizar.
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@PUT
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject, String reqPayLoad) {
		JSONObject result = null;
		JSONObject projectJSON = null;
		NodeTO projectTO = null;
		String authToken = null;
		Map<String, Object> newProperties = null;

		try {
			// Validacion y obtencion de authToken Requester
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtencion de parámetros a actualizar
			newProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Actualizar Proyecto
			projectTO = getProjectDelegate().updateProject(authToken, idProject, newProperties);

			// Armar response
			projectJSON = getProjectJSON(projectTO, true);
			result = HelperSG.getOKResponse(projectJSON);

		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}

	/**
	 * Servicio REST para eliminar un Project basado en el identificador del
	 * project.
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idProject
	 *            Identificador del nodo del project a eliminar.
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject) {
		JSONObject result = null;
		String authToken = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			getProjectDelegate().deleteProject(authToken, idProject);
			result = HelperSG.getOKResponse(null);
		} catch (SGException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), e.getMessage());
		}

		return HelperSG.makeResponse(result);

	}

	/**
	 * Servicio usado para asignar un usuario a un proyecto
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Long Identificador del proyecto.
	 * @return Response Respuesta del servicio REST.
	 */
	@POST
	@Path("{id}/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignProjectToUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject result = null;
		String authToken = null;
		Long idUser = null;
		Map<String, Object> payLoadProperties = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			if (payLoadProperties.containsKey("userId")) {
				idUser = Long.parseLong(payLoadProperties.get("userId").toString());
			}

			// Asignar proyecto a un usuario
			getProjectDelegate().assignUserToProject(authToken, idUser, id);
			result = HelperSG.getOKResponse(null);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}

	/**
	 * Servicio REST  usado para obtener todos los nodos sprints de un proyecto. 
	 * @param scrumGraphHeader Custom header de scrum graph
	 * @param idProject
	 * @return
	 */
	@GET
	@Path("{id}/sprints")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSprintsByIdProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject) {
		JSONObject result = null;
		String authToken = null;
		NodeTO projectTO = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener Usuarios Por Proyecto
			projectTO = getProjectDelegate().getSprintsByProject(authToken, idProject);
			JSONObject projectsResponse = getSprintsByProjectJSON(projectTO);
			result = HelperSG.getOKResponse(projectsResponse);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}
	
	@GET
	@Path("{id}/chart")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChartByProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject) {
		JSONObject result ,  sprintJSON = null;
		List<JSONObject> allOutGoingJSON = new ArrayList<JSONObject>();
		String authToken = null;
		NodeTO projectTO = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener Usuarios Por Proyecto
			projectTO = getProjectDelegate().getSprintsByProject(authToken, idProject);
			
			if (projectTO.getOutgoingNodes() != null) {
				List<String> lstPropKeys = new ArrayList<String>();
				lstPropKeys.add(ESprintProperties.NAME.getPropertyName());
				lstPropKeys.add(ESprintProperties.VELOCITY.getPropertyName());
				for (NodeTO sprintNodeTO : projectTO.getOutgoingNodes()) {
					sprintJSON = HelperSG.createJSONObject(lstPropKeys, sprintNodeTO, null);
					allOutGoingJSON.add(sprintJSON);
				}
			}
			
			JSONObject projectsResponse = new JSONObject();
			projectsResponse.put("sprints", allOutGoingJSON);
			result = HelperSG.getOKResponse(projectsResponse);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}
	
	@GET
	@Path("{id}/indicators")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIndicators(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject) {
		JSONObject result ,  sprintJSON = null;
		List<JSONObject> allOutGoingJSON = new ArrayList<JSONObject>();
		List<JSONObject> allStatsJSON = new ArrayList<JSONObject>();
		String authToken = null;
		IndicadoresTO indicadorTO = null;
		NodeTO projectTO = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener Usuarios Por Proyecto
			indicadorTO = getProjectDelegate().getIndicators(idProject);
			
			projectTO = indicadorTO.getNodeTO();
			
			if (projectTO.getOutgoingNodes() != null) {
				List<String> lstPropKeys = new ArrayList<String>();
				lstPropKeys.add(ESprintProperties.NAME.getPropertyName());
				lstPropKeys.add(ESprintProperties.VELOCITY.getPropertyName());
				for (NodeTO sprintNodeTO : projectTO.getOutgoingNodes()) {
					sprintJSON = HelperSG.createJSONObject(lstPropKeys, sprintNodeTO, null);
					allOutGoingJSON.add(sprintJSON);
				}
			}
			JSONObject chartResponse = new JSONObject();
			chartResponse.put("sprints", allOutGoingJSON);
			
			int count = 1;
			for (StatsTO stat : indicadorTO.getLstStats()) {
				JSONObject statJSON = new JSONObject();
				statJSON.put("label", stat.getLabel());
				statJSON.put("value", stat.getValue());
				statJSON.put("id", count++);
				allStatsJSON.add(statJSON);
			}
			
			JSONObject projectsResponse = new JSONObject();
			projectsResponse.put("chart", chartResponse);
						
			projectsResponse.put("stats", allStatsJSON);
			
			result = HelperSG.getOKResponse(projectsResponse);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}
	
	/**
	 * Servicio REST  usado para obtener todos los nodos pbis de un proyecto
	 * que no se encuentran asignados a ningún sprint
	 * @param scrumGraphHeader Custom header de scrum graph
	 * @param idProject Identificador del proyecto
	 * @return
	 */
	@GET
	@Path("{id}/pbis/unassigned")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPBIUnAssignedByProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject) {
		JSONObject result = null;
		String authToken = null;
		NodeTO projectTO = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener Usuarios Por Proyecto
			projectTO = getProjectDelegate().getPBIUnAssignedByProject(authToken, idProject);
			JSONObject projectsResponse = getPBISByProjectJSON(projectTO);
			result = HelperSG.getOKResponse(projectsResponse);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}

	/**
	 * Servicio REST para validar la exitencia de la propiedad code en los nodos
	 * tipo Project
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param code
	 *            Code a validar
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@GET
	@Path("verify/code/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateCode(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("code") String code) {
		JSONObject result = null;
		String authToken = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			getProjectDelegate().validateCode(authToken, code);
			result = HelperSG.getOKResponse(null);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}

	@GET
	@Path("{id}/graph/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGraphByProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject) {
		JSONObject result = null;
		String authToken = null;
		GraphTO graphTO = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			graphTO = getProjectDelegate().getGraphProject(authToken, idProject);
			// Armar Respuesta
			JSONObject graphResponse = new JSONObject(false);
			if (graphTO != null) {
				List<JSONObject> allNodesJSON = new ArrayList<>();
				List<JSONObject> allRelationJSON = new ArrayList<>();
				for (NodeTO nodeTO : graphTO.getNodes()) {
					JSONObject nodeJSON = getGraphNodesJSON(nodeTO);
					if (nodeJSON != null)
						allNodesJSON.add(nodeJSON);
				}
				for (RelationshipTO relationTO : graphTO.getLinks()) {
					JSONObject linkJSON = getGraphLinksJSON(relationTO);
					if (linkJSON != null)
						allRelationJSON.add(linkJSON);
				}
				graphResponse.put("nodes", allNodesJSON);
				graphResponse.put("links", allRelationJSON);
			}
			result = HelperSG.getOKResponse(graphResponse);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}
	
	@GET
	@Path("{id}/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsersByIdProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idProject) {
		JSONObject result = null;
		String authToken = null;
		NodeTO projectTO = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener Usuarios Por Proyecto
			projectTO = getProjectDelegate().getUsersByProject(authToken, idProject);
			JSONObject projectsResponse = getUsersByProjectJSON(projectTO);
			result = HelperSG.getOKResponse(projectsResponse);
		} catch (SGException sge) {
			sge.printStackTrace();
			result = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(result);
	}
	
	/**
	 * Servicio REST para asociar un PBI a un proyecto
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del proyecto a asociar
	 * @param reqPayLoad
	 *            String con la información del PBI a asociar.
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@POST
	@Path("{id}/pbis")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAssignPBIToProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject responseJSON = null , assignPBIJSON;
		String authToken = null;
		NodeTO pbiTO = null;
		Map<String, Object> payLoadProperties = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			// Creacion Asociacion
			pbiTO = getProjectDelegate().assignPBIToProject(authToken, payLoadProperties, id);
			assignPBIJSON = getAssignPBIJSON(pbiTO, "pbi", null);
			JSONObject pbiJ = (JSONObject) assignPBIJSON.get("pbi");
			pbiJ.put("projectId", id);
			
			responseJSON = HelperSG.getOKResponse(assignPBIJSON);
		} catch (SGException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), e.getMessage());
		}

		return HelperSG.makeResponse(responseJSON);
	}
	
	/**
	 * Servicio REST para crear y asociar un Sprint a un proyecto
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del proyecto a asociar
	 * @param reqPayLoad
	 *            String con la información del PBI a asociar.
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	@POST
	@Path("{id}/sprints")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAssignSprintToProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject responseJSON = null , assignSprintJSON;
		String authToken = null;
		NodeTO sprintTO = null;
		Map<String, Object> payLoadProperties = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			// Creacion Asociacion
			sprintTO = getProjectDelegate().assignSprintToProject(authToken, payLoadProperties, id);
			assignSprintJSON = getAssignSprintJSON(sprintTO, "sprint", null);
			JSONObject pbiJ = (JSONObject) assignSprintJSON.get("sprint");
			pbiJ.put("projectId", id);
			
			responseJSON = HelperSG.getOKResponse(assignSprintJSON);
		} catch (SGException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), e.getMessage());
		}

		return HelperSG.makeResponse(responseJSON);
	}

	private JSONObject getAssignPBIJSON(NodeTO pbiTO, String nameObjJSON, EPBIProperties... notIncludeProperties) {
		JSONObject pbiJSON = null;

		if (pbiTO == null) {
			return pbiJSON;
		}

		pbiJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (EPBIProperties projProp : EPBIProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludeProperties != null) {
			for (EPBIProperties property : notIncludeProperties) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		HelperSG.createJSONObject(lstPropKeys, pbiTO, pbiJSON);

		if (nameObjJSON != null) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put(nameObjJSON, pbiJSON);
			return projRespJSON;
		}

		return pbiJSON;
	}
	
	
	private JSONObject getAssignSprintJSON(NodeTO pbiTO, String nameObjJSON, ESprintProperties... notIncludeProperties) {
		JSONObject sprintJSON = null;

		if (pbiTO == null) {
			return sprintJSON;
		}

		sprintJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (ESprintProperties projProp : ESprintProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludeProperties != null) {
			for (ESprintProperties property : notIncludeProperties) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		HelperSG.createJSONObject(lstPropKeys, pbiTO, sprintJSON);

		if (nameObjJSON != null) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put(nameObjJSON, sprintJSON);
			return projRespJSON;
		}

		return sprintJSON;
	}
	
	/* OPTIONS METHODS */
	
	@OPTIONS 
	public Response rootCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/{id}")
	public Response projectIdCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/{id}/users")
	public Response projectUsersCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/verify/code/{code}")
	public Response validateCodeCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/{id}/sprints")
	public Response projectSprintsCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/{id}/pbis")
	public Response projectPBISCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS 
	@Path("/{id}/pbis/unassigned")
	public Response projectPBISUnassignedCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	@OPTIONS 
	@Path("{id}/graph/")
	public Response projectGraphCORS() {		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}
	
	@OPTIONS
	@Path("/{id}/chart")
	public Response chartCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}
	
	@OPTIONS
	@Path("/{id}/indicators")
	public Response indicatorsCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

}
