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
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.enums.EProjectProperties;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.delegados.DelegateSprint;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;
import net.sf.json.JSONObject;

@Path("sprints")
public class SrvRestSprint implements ISrvSGRest {

	/**
	 * Instancia del Delegado Sprint
	 */
	private DelegateSprint delegate;

	/**
	 * Obtener el delegado sprint
	 * 
	 * @return DelegateSprint Delegado.
	 */
	private DelegateSprint getDelegateSprint() {
		if (delegate == null) {
			delegate = new DelegateSprint(this);
		}
		return delegate;
	}


	

	/**
	 * Construye un objeto JSON basado en la información de las propiedades del
	 * nodo.
	 * 
	 * @param sprintTO
	 *            NodeTO Objeto con la información a convertir
	 * @return Response Objeto con la respuesta del servicio REST.
	 */
	private JSONObject getPbisSprintJSON(NodeTO sprintTO) {
		JSONObject sprintJSON = null;
		JSONObject projectJSON = null;
		List<JSONObject> lstPBIJSON = null;
		JSONObject responseJSON = null;

		if (sprintTO == null) {
			return sprintJSON;
		}

		responseJSON = new JSONObject(false);
		lstPBIJSON = new ArrayList<JSONObject>();
		List<String> lstPropKeys = new ArrayList<String>();

		// Create Project JSON Response
		for (EProjectProperties projProp : EProjectProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
		NodeTO projectTO = sprintTO.getIncomingNodes() != null && !sprintTO.getIncomingNodes().isEmpty() ? sprintTO.getIncomingNodes().get(0) : null;
		projectJSON = HelperSG.createJSONObject(lstPropKeys, projectTO, projectJSON);

		// Create Sprint JSON Response
		lstPropKeys.clear();
		for (ESprintProperties projProp : ESprintProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
		sprintJSON = HelperSG.createJSONObject(lstPropKeys, sprintTO, sprintJSON);
		if (sprintJSON != null && projectTO != null) {
			sprintJSON.put("projectId", projectTO.getId());
		}

		// Create PBIS JSON Response
		lstPropKeys.clear();
		for (EPBIProperties projProp : EPBIProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
		if (sprintTO.getOutgoingNodes() != null) {
			for (NodeTO pbiTO : sprintTO.getOutgoingNodes()) {
				JSONObject pbiJSON = HelperSG.createJSONObject(lstPropKeys, pbiTO, null);
				if (pbiJSON != null) {
					if (sprintTO != null && projectTO != null) {
						pbiJSON.put("projectId", projectTO.getId());
						pbiJSON.put("sprintId", sprintTO.getId());
					}
					lstPBIJSON.add(pbiJSON);
				}
			}
		}
		responseJSON.put("project", projectJSON);
		responseJSON.put("sprint", sprintJSON);
		responseJSON.put("pbis", lstPBIJSON);

		return responseJSON;
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

	/**
	 * Servicio REST para crear un Sprint.
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
	public Response createSprint(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, String reqPayLoad) {
		Map<String, Object> properties = null;
		JSONObject sprintJSON = null, responseJSON = null;
		String authToken = null;
		NodeTO sprintTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			properties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Validacion Properties
			getDelegateSprint().validateProperties(properties);

			sprintTO = getDelegateSprint().createSprint(authToken, properties);
			sprintJSON = HelperSG.getSprintJSON(sprintTO, "sprint");
			responseJSON = HelperSG.getOKResponse(sprintJSON);
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
	 * Servicio REST para consultar un Sprint basado en su identificador
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del Sprint a consultar
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
	public Response getSprintById(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject sprintJSON = null, responseJSON = null , resultJSON = null , projectJSON = null;
		String authToken = null;
		NodeTO sprintTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			sprintTO = getDelegateSprint().getSprintById(authToken, id);
			sprintJSON = HelperSG.getSprintJSON(sprintTO, null);
			if (sprintTO.getIncomingNodes() != null) {
				for (NodeTO nodeRelated : sprintTO.getIncomingNodes()) {
					switch (nodeRelated.getLabelNode()) {
					case PROJECT:
						projectJSON = HelperSG.getProjectJSON(nodeRelated, null);
						sprintJSON.put("projectId", nodeRelated.getId());
						break;
						default:
							break;
					}
				}
			}
			resultJSON = new JSONObject();
			resultJSON.put("sprint", sprintJSON);
			resultJSON.put("project", projectJSON);
			responseJSON = HelperSG.getOKResponse(resultJSON);
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
	 * Servicio REST para consultar todos los Sprints.
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
	public Response getAllSprints(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader) {
		List<NodeTO> allSprints = null;
		JSONObject result = null;
		JSONObject sprintResponse = null;
		String authTOken = null;
		try {
			// validar header
			authTOken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener todos los nodos
			allSprints = getDelegateSprint().getAllNodes(authTOken);
			List<JSONObject> allSprintsJSON = new ArrayList<JSONObject>();
			// Convertir lista nodos formato Response
			for (NodeTO sprintTO : allSprints) {
				JSONObject sprintJSON = HelperSG.getSprintJSON(sprintTO, null);
				allSprintsJSON.add(sprintJSON);
			}
			sprintResponse = new JSONObject();
			sprintResponse.put("sprints", allSprintsJSON);
			result = HelperSG.getOKResponse(sprintResponse);

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
	 * Servicio REST para actualizar un Sprint
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idSprint
	 *            Identificador del nodo del sprint a actualizar
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
	public Response updateSprint(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idSprint, String reqPayLoad) {
		JSONObject sprintJSON = null , resultJSON = null , projectJSON = null , responseJSON = null;
		NodeTO sprintTO = null;
		String authToken = null;
		Map<String, Object> newProperties = null;

		try {
			// Validacion y obtencion de authToken Requester
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtencion de parámetros a actualizar
			newProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Actualizar Sprint
			sprintTO = getDelegateSprint().updateSprint(authToken, idSprint, newProperties);

			// Armar response
			sprintJSON = HelperSG.getSprintJSON(sprintTO, null);
			if (sprintTO.getIncomingNodes() != null) {
				for (NodeTO nodeRelated : sprintTO.getIncomingNodes()) {
					switch (nodeRelated.getLabelNode()) {
					case PROJECT:
						projectJSON = HelperSG.getProjectJSON(nodeRelated, null);
						sprintJSON.put("projectId", nodeRelated.getId());
						break;
						default:
							break;
					}
				}
			}
			resultJSON = new JSONObject();
			resultJSON.put("sprint", sprintJSON);
			resultJSON.put("project", projectJSON);
			responseJSON = HelperSG.getOKResponse(resultJSON);

		} catch (SGException sge) {
			sge.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(sge.getCode(), sge.getMessage());
		} catch (ScrumGraphException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), ex.getMessage());
		}

		return HelperSG.makeResponse(responseJSON);
	}

	/**
	 * Servicio REST para eliminar un Sprint basado en el identificador del
	 * sprint.
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idSprint
	 *            Identificador del nodo del sprint a eliminar.
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
	public Response deleteSprint(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idSprint) {
		JSONObject result = null;
		String authToken = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			getDelegateSprint().deleteSprint(authToken, idSprint);
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
	 * Servicio REST para validar la exitencia de la propiedad code en los nodos
	 * tipo Sprint
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
			getDelegateSprint().validateCode(authToken, code);
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
	 * Servicio usado para asignar un sprint a un proyecto
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Long Identificador del sprint
	 * @return Response Respuesta del servicio REST.
	 */
	@POST
	@Path("{id}/project")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignSprintToProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject result = null;
		String authToken = null;
		Long idProject = null;
		Map<String, Object> payLoadProperties = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			if (payLoadProperties.containsKey("projectId")) {
				idProject = Long.parseLong(payLoadProperties.get("projectId").toString());
			}

			// Asignar proyecto a un usuario
			getDelegateSprint().assignSprintToProject(authToken, id, idProject);
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
	 * Servicio REST para consultar todos los pbis de un sprint
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del Sprint a consultar
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
	@Path("{id}/pbis")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPbisBySprint(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject SprintJSON = null, responseJSON = null;
		String authToken = null;
		NodeTO SprintTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			SprintTO = getDelegateSprint().getPbisBySprint(authToken, id);
			SprintJSON = getPbisSprintJSON(SprintTO);
			responseJSON = HelperSG.getOKResponse(SprintJSON);
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
	 * Servicio REST para asociar un PBI a un sprint
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del Sprint a asociar
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
	public Response assignPBIToSprint(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject responseJSON = null , assignPBIJSON;
		String authToken = null;
		NodeTO pbiTO = null;
		Map<String, Object> payLoadProperties = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			// Creacion Asociacion
			pbiTO = getDelegateSprint().assignPBIToSprint(authToken, payLoadProperties, id);
			assignPBIJSON = getAssignPBIJSON(pbiTO, "pbi", null);
			JSONObject pbiJ = (JSONObject) assignPBIJSON.get("pbi");
			pbiJ.put("sprintId", id);
			NodeTO projTO = pbiTO.getIncomingNodes() != null && !pbiTO.getIncomingNodes().isEmpty() ? pbiTO.getIncomingNodes().get(0) : null;
			if (projTO != null) {
				pbiJ.put("projectId", projTO.getId());
			}
			
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
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{id}/chart")
	public Response getChartById(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject chatJSON = null, responseJSON = null;
		Map<String, Integer> pbiChart = null;
		try {
			HelperSG.validateHeader(scrumGraphHeader);
			pbiChart = getDelegateSprint().getChartSprint(id);
			
			chatJSON = new JSONObject();
			if (pbiChart != null) {
				for (String status : pbiChart.keySet()) {
					chatJSON.put(status, pbiChart.get(status));
				}
			}
			
			responseJSON = HelperSG.getOKResponse(chatJSON);
		} catch (SGException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), e.getMessage());
		}

		return HelperSG.makeResponse(responseJSON);
	}

	/** OPTIONS */
	@OPTIONS
	public Response rootCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS
	@Path("/{id}")
	public Response sprintIdCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS
	@Path("/{id}/pbis")
	public Response sprintIdPbisCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
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
	@Path("/{id}/chart")
	public Response chartCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

}
