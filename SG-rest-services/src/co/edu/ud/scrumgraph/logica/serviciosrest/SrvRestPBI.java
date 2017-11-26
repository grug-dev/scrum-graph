/**
 * 
 */
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
import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.enums.EProjectProperties;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.delegados.DelegatePBI;
import co.edu.ud.scrumgraph.logica.delegados.DelegateVisitor;
import co.edu.ud.scrumgraph.logica.delegados.IDelegateService;
import co.edu.ud.scrumgraph.logica.delegados.IDelegateVisitor;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;
import net.sf.json.JSONObject;

/**
 * @author RaspuWIN7
 *
 */
@Path("pbis")
public class SrvRestPBI implements ISrvSGRest {

	private DelegatePBI delegate;

	private DelegatePBI getDelegatePBI() {
		if (delegate == null) {
			visitorDelegate = new DelegateVisitor();
			delegate = (DelegatePBI) visitorDelegate.visit(this);
		}
		return delegate;
	}
	
	private IDelegateVisitor visitorDelegate;

	/**
	 * Servicio REST para crear un PBI.
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
	public Response createPBI(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, String reqPayLoad) {
		Map<String, Object> properties = null;
		JSONObject pbiJSON = null, responseJSON = null;
		String authToken = null;
		NodeTO pbiTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			properties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Validacion Properties
			getDelegatePBI().validateProperties(properties);

			pbiTO = getDelegatePBI().createPBI(authToken, properties);
			pbiJSON = HelperSG.getPBIJSON(pbiTO, "pbi");
			responseJSON = HelperSG.getOKResponse(pbiJSON);
		} catch (SGException ex) {
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
	 * Servicio REST para consultar un PBI basado en su identificador
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del PBI a consultar
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
	public Response getPBIById(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject pbiJSON = null, responseJSON = null, resultJSON = null, sprintJSON = null, projectJSON = null;
		String authToken = null;
		NodeTO pbiTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			pbiTO = getDelegatePBI().getPBIById(authToken, id);
			pbiJSON = HelperSG.getPBIJSON(pbiTO, "pbi");
			if (pbiTO.getIncomingNodes() != null) {
				for (NodeTO nodeRelated : pbiTO.getIncomingNodes()) {
					switch (nodeRelated.getLabelNode()) {
					case PROJECT:
						projectJSON = HelperSG.getProjectJSON(nodeRelated, null);
						break;
					case SPRINT:
						sprintJSON = HelperSG.getSprintJSON(nodeRelated, null);
						break;
					default:
						break;
					}
				}
			}

			resultJSON = new JSONObject();
			resultJSON.put("pbi", pbiJSON);
			resultJSON.put("sprint", sprintJSON);
			resultJSON.put("project", projectJSON);
			responseJSON = HelperSG.getOKResponse(resultJSON);
		} catch (SGException ex) {
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
	 * Servicio REST para consultar todos los PBIs.
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
	public Response getAllPbis(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader) {
		List<NodeTO> allPbis = null;
		JSONObject result = null;
		JSONObject pbiResponse = null;
		String authTOken = null;
		try {
			// validar header
			authTOken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener todos los nodos
			allPbis = getDelegatePBI().getAllNodes(authTOken);
			List<JSONObject> allPbisJSON = new ArrayList<JSONObject>();
			// Convertir lista nodos formato Response
			for (NodeTO pbiTO : allPbis) {
				JSONObject pbiJSON = HelperSG.getPBIJSON(pbiTO, null);
				allPbisJSON.add(pbiJSON);
			}
			pbiResponse = new JSONObject();
			pbiResponse.put("pbis", allPbisJSON);
			result = HelperSG.getOKResponse(pbiResponse);

		} catch (SGException sge) {
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
	 * Servicio REST para actualizar un PBI
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idPBI
	 *            Identificador del nodo del PBI a actualizar
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
	public Response updatePBI(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idPBI, String reqPayLoad) {
		JSONObject result = null;
		JSONObject pbiJSON = null;
		JSONObject sprintJSON = null;
		JSONObject projectJSON = null;
		JSONObject resultJSON = null;
		NodeTO pbiTO = null;
		String authToken = null;
		Map<String, Object> newProperties = null;

		try {
			// Validacion y obtencion de authToken Requester
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtencion de parámetros a actualizar
			newProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Actualizar PBI
			pbiTO = getDelegatePBI().updatePbi(authToken, idPBI, newProperties);

			// Armar response
			pbiJSON = HelperSG.getPBIJSON(pbiTO, null);
			if (pbiTO.getIncomingNodes() != null) {
				for (NodeTO nodeRelated : pbiTO.getIncomingNodes()) {
					switch (nodeRelated.getLabelNode()) {
					case PROJECT:
						projectJSON = HelperSG.getProjectJSON(nodeRelated, null);
						pbiJSON.put("projectId", nodeRelated.getId());
						break;
					case SPRINT:
						sprintJSON = HelperSG.getSprintJSON(nodeRelated, null);
						pbiJSON.put("sprintId", nodeRelated.getId());
						break;
					default:
						break;
					}
				}
			}

			resultJSON = new JSONObject();
			resultJSON.put("pbi", pbiJSON);
			resultJSON.put("sprint", sprintJSON);
			resultJSON.put("project", projectJSON);

			result = HelperSG.getOKResponse(resultJSON);

		} catch (SGException sge) {
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
	 * Servicio REST para eliminar un PBI basado en el identificador del PBI.
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idPBI
	 *            Identificador del nodo del PBI a eliminar.
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
	public Response deletePBI(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idPBI) {
		JSONObject result = null;
		String authToken = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			getDelegatePBI().deletePBI(authToken, idPBI);
			result = HelperSG.getOKResponse(null);
		} catch (SGException ex) {
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
	 * tipo PBI
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
			getDelegatePBI().validateCode(authToken, code);
			result = HelperSG.getOKResponse(null);
		} catch (SGException sge) {
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
	 * Servicio usado para asignar un pbi a un proyecto
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Long Identificador del pbi
	 * @return Response Respuesta del servicio REST.
	 */
	@POST
	@Path("{id}/project")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignPBIToProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
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
			getDelegatePBI().assignPBIToProject(authToken, id, idProject);
			result = HelperSG.getOKResponse(null);
		} catch (SGException sge) {
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
	 * Servicio usado para asignar un pbi a un sprint
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Long Identificador del pbi
	 * @return Response Respuesta del servicio REST.
	 */
	@POST
	@Path("{id}/sprint")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignPBIToSprint(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject result = null;
		String authToken = null;
		Long idSprint = null;
		Map<String, Object> payLoadProperties = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			if (payLoadProperties.containsKey("sprintId")) {
				idSprint = Long.parseLong(payLoadProperties.get("sprintId").toString());
			}

			// Asignar proyecto a un usuario
			getDelegatePBI().assignPBIToSprint(authToken, id, idSprint);
			result = HelperSG.getOKResponse(null);
		} catch (SGException sge) {
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
	 * Servicio REST para obtener todos los task asociados a un PBI.
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
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{id}/tasks")
	public Response getAllTaskByPBI(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject taskJSON = null, responseJSON = null;
		String authToken = null;
		NodeTO pbiTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			pbiTO = getDelegatePBI().getGraphPBI(id);
			taskJSON = getGraphPBIJSON(pbiTO);
			responseJSON = HelperSG.getOKResponse(taskJSON);
		} catch (SGException ex) {
			ex.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ex.getCode(), ex.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			responseJSON = HelperSG.getErrorResponse(ECodeExceptionSG.ERROR_CODE.getCode(), e.getMessage());
		}

		return HelperSG.makeResponse(responseJSON);
	}

	/**
	 * Construye un objeto JSON basado en la información de las propiedades del
	 * nodo.
	 * 
	 * @param pbiGraphTO
	 *            NodeTO Objeto con la información a convertir
	 * @return Response Objeto con la respuesta del servicio REST.
	 */
	private JSONObject getGraphPBIJSON(NodeTO pbiGraphTO) {
		JSONObject sprintJSON = null;
		JSONObject projectJSON = null;
		JSONObject pbiJSON = null;
		List<JSONObject> lstTaskJSON = null;
		JSONObject responseJSON = null;
		List<NodeTO> incomingNodes = null;

		if (pbiGraphTO == null) {
			return sprintJSON;
		}

		responseJSON = new JSONObject(false);
		lstTaskJSON = new ArrayList<JSONObject>();
		List<String> lstPropKeys = new ArrayList<String>();
		incomingNodes = pbiGraphTO.getIncomingNodes();
		NodeTO projectTO = null, sprintTO = null;
		if (incomingNodes != null) {
			for (NodeTO incNode : incomingNodes) {
				if (incNode.getLabelNode().equals(ELabels.PROJECT)) {
					projectTO = incNode;
				} else if (incNode.getLabelNode().equals(ELabels.SPRINT)) {
					sprintTO = incNode;
				}
			}
		}

		// Create Project JSON Response
		if (projectTO != null) {
			for (EProjectProperties projProp : EProjectProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
			projectJSON = HelperSG.createJSONObject(lstPropKeys, projectTO, projectJSON);
		}

		// Create Sprint JSON Response
		if (sprintTO != null) {
			lstPropKeys.clear();
			for (ESprintProperties projProp : ESprintProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
			sprintJSON = HelperSG.createJSONObject(lstPropKeys, sprintTO, sprintJSON);
			if (sprintJSON != null && projectTO != null) {
				sprintJSON.put("projectId", projectTO.getId());
			}
		}

		// Create PBI JSON Response
		pbiJSON = HelperSG.getPBIJSON(pbiGraphTO, null, null);
		if (sprintTO != null && projectTO != null) {
			pbiJSON.put("projectId", projectTO.getId());
			pbiJSON.put("sprintId", sprintTO.getId());
		}

		// Create Task JSON Response
		if (pbiGraphTO.getOutgoingNodes() != null) {
			lstPropKeys.clear();
			for (ETaskProperties projProp : ETaskProperties.values()) {
				lstPropKeys.add(projProp.getPropertyName());
			}
			lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());
			for (NodeTO taskTO : pbiGraphTO.getOutgoingNodes()) {
				JSONObject taskJSON = HelperSG.getTaskJSON(taskTO, null);
				if (taskJSON != null) {
					lstTaskJSON.add(taskJSON);
				}
			}
		}
		responseJSON.put("project", projectJSON);
		responseJSON.put("sprint", sprintJSON);
		responseJSON.put("pbi", pbiJSON);
		responseJSON.put("tasks", lstTaskJSON);

		return responseJSON;
	}

	/**
	 * Servicio REST para asociar y crear una tarea a un PBI
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del PBI a asociar
	 * @param reqPayLoad
	 *            String con la información del nodo task a crear.
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
	@Path("{id}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createTaskToPBI(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject taskJSON = null, responseJSON = null , resultJSON = null;
		String authToken = null;
		NodeTO taskTO = null;
		Map<String, Object> payLoadProperties = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener IdProject
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Verificacion si tiene usuarios a asociar la tarea
			if (payLoadProperties.containsKey("users")) {
				String strLstUsers = payLoadProperties.get("users").toString();
				List<Integer> lstIdUsers = HelperSG.getLstInteger(strLstUsers);
				payLoadProperties.put("users", lstIdUsers);
			}

			// Creacion Asociacion
			taskTO = getDelegatePBI().createTaskToPBI(authToken, payLoadProperties, id);
			// Armar response
			taskJSON = HelperSG.getTaskJSON(taskTO, null);
			if (taskTO.getIncomingNodes() != null) {
				for (NodeTO nodeRelated : taskTO.getIncomingNodes()) {
					switch (nodeRelated.getLabelNode()) {
					case PROJECT:
						taskJSON.put("projectId", nodeRelated.getId());
						break;
					case SPRINT:
						taskJSON.put("sprintId", nodeRelated.getId());
						break;
					case PBI:
						taskJSON.put("pbiId", nodeRelated.getId());
						break;
					default:
						break;
					}
				}
			}
			resultJSON = new JSONObject();
			resultJSON.put("task", taskJSON);
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
	
	private JSONObject getAssignTASKJSON(NodeTO taskTO, String nameObjJSON, EPBIProperties... notIncludeProperties) {
		JSONObject taskJSON = null;

		if (taskTO == null) {
			return taskJSON;
		}

		taskJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (ETaskProperties projProp : ETaskProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludeProperties != null) {
			for (EPBIProperties property : notIncludeProperties) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		HelperSG.createJSONObject(lstPropKeys, taskTO, taskJSON);

		if (nameObjJSON != null) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put(nameObjJSON, taskJSON);
			return projRespJSON;
		}

		return taskJSON;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{id}/chart")
	public Response getChartByPBI(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject taskJSON = null, responseJSON = null;
		Map<String, Integer> pbiChart = null;
		try {
			HelperSG.validateHeader(scrumGraphHeader);
			pbiChart = getDelegatePBI().getChartPBI(id);
			
			taskJSON = new JSONObject();
			if (pbiChart != null) {
				for (String status : pbiChart.keySet()) {
					taskJSON.put(status, pbiChart.get(status));
				}
			}
			
			responseJSON = HelperSG.getOKResponse(taskJSON);
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
	@Path("/")
	public Response rootCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS
	@Path("/{id}")
	public Response pbiIdCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS
	@Path("/verify/code/{code}")
	public Response validateCodeCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS
	@Path("/{id}/tasks")
	public Response taskCodeCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}
	
	@OPTIONS
	@Path("/{id}/chart")
	public Response taskChartCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}
	
}
