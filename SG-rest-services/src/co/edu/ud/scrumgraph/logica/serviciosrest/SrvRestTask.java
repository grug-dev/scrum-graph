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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.delegados.DelegateTask;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.util.HelperSG;
import co.edu.ud.scrumgraph.logica.util.SGException;
import net.sf.json.JSONObject;

/**
 * @author RaspuWIN7
 *
 */
@Path("tasks")
public class SrvRestTask implements ISrvSGRest {

	/**
	 * Instancia del Delegado Task
	 */
	private DelegateTask delegate;

	/**
	 * Obtener el delegado task
	 * 
	 * @return DelegateTask Delegado.
	 */
	private DelegateTask getDelegateTask() {
		if (delegate == null) {
			delegate = new DelegateTask(this);
		}
		return delegate;
	}

	/**
	 * Servicio REST para consultar un Task basado en su identificador
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Identificador del Task a consultar
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
	public Response getTaskById(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id) {
		JSONObject taskJSON = null, responseJSON = null, pbiJSON = null, sprintJSON = null, projectJSON = null, resultJSON = null;
		String authToken = null;
		NodeTO taskTO = null;
		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			taskTO = getDelegateTask().getTaskById(authToken, id);
			taskJSON = HelperSG.getTaskJSON(taskTO, null);
			if (taskTO.getIncomingNodes() != null) {
				for (NodeTO nodeRelated : taskTO.getIncomingNodes()) {
					switch (nodeRelated.getLabelNode()) {
					case PROJECT:
						projectJSON = HelperSG.getProjectJSON(nodeRelated, null);
						taskJSON.put("projectId", nodeRelated.getId());
						break;
					case SPRINT:
						sprintJSON = HelperSG.getSprintJSON(nodeRelated, null);
						taskJSON.put("sprintId", nodeRelated.getId());
						break;
					case PBI:
						pbiJSON = HelperSG.getPBIJSON(nodeRelated, null);
						taskJSON.put("pbiId", nodeRelated.getId());
						break;
					default:
						break;
					}
				}
				sprintJSON.put("projectId", taskJSON.get("projectId"));
				pbiJSON.put("sprintId", taskJSON.get("sprintId"));
				pbiJSON.put("projectId", taskJSON.get("projectId"));
			}
			resultJSON = new JSONObject();
			resultJSON.put("task", taskJSON);
			resultJSON.put("pbi", pbiJSON);
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
	 * Servicio REST para consultar todos los Tasks.
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
	public Response getAllTasks(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader) {
		List<NodeTO> allTasks = null;
		JSONObject result = null;
		JSONObject taskResponse = null;
		String authTOken = null;
		try {
			// validar header
			authTOken = HelperSG.validateHeader(scrumGraphHeader);
			// Obtener todos los nodos
			allTasks = getDelegateTask().getAllNodes(authTOken);
			List<JSONObject> allTasksJSON = new ArrayList<JSONObject>();
			// Convertir lista nodos formato Response
			for (NodeTO taskTO : allTasks) {
				JSONObject taskJSON = HelperSG.getTaskJSON(taskTO, null);
				allTasksJSON.add(taskJSON);
			}
			taskResponse = new JSONObject();
			taskResponse.put("tasks", allTasksJSON);
			result = HelperSG.getOKResponse(taskResponse);

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
	 * Servicio REST para actualizar un Task
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idTask
	 *            Identificador del nodo del task a actualizar
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
	public Response updateTask(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idTask, String reqPayLoad) {
		JSONObject taskJSON = null, responseJSON = null, pbiJSON = null, sprintJSON = null, projectJSON = null, resultJSON = null;
		NodeTO taskTO = null;
		String authToken = null;
		Map<String, Object> newProperties = null;

		try {
			// Validacion y obtencion de authToken Requester
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtencion de parámetros a actualizar
			newProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);

			// Validacion Propiedades
			getDelegateTask().validateProperty(ETaskProperties.EXECUTE_HOURS, newProperties);

			// Verificacion si tiene usuarios a asociar la tarea
			if (newProperties.containsKey("users")) {
				String strLstUsers = newProperties.get("users").toString();
				List<Integer> lstIdUsers = HelperSG.getLstInteger(strLstUsers);
				newProperties.put("users", lstIdUsers);
			}

			// Actualizar Task
			taskTO = getDelegateTask().updateTask(authToken, idTask, newProperties);

			// Armar response
			taskJSON = HelperSG.getTaskJSON(taskTO, null);
			if (taskTO.getIncomingNodes() != null) {
				for (NodeTO nodeRelated : taskTO.getIncomingNodes()) {
					switch (nodeRelated.getLabelNode()) {
					case PROJECT:
						projectJSON = HelperSG.getProjectJSON(nodeRelated, null);
						taskJSON.put("projectId", nodeRelated.getId());
						break;
					case SPRINT:
						sprintJSON = HelperSG.getSprintJSON(nodeRelated, null);
						taskJSON.put("sprintId", nodeRelated.getId());
						break;
					case PBI:
						pbiJSON = HelperSG.getPBIJSON(nodeRelated, null);
						taskJSON.put("pbiId", nodeRelated.getId());
						break;
					default:
						break;
					}
				}
				sprintJSON.put("projectId", taskJSON.get("projectId"));
				pbiJSON.put("sprintId", taskJSON.get("sprintId"));
				pbiJSON.put("projectId", taskJSON.get("projectId"));
			}
			resultJSON = new JSONObject();
			resultJSON.put("task", taskJSON);
			resultJSON.put("pbi", pbiJSON);
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
	 * Servicio REST para eliminar un Task basado en el identificador del task.
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param idTask
	 *            Identificador del nodo del task a eliminar.
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
	public Response deleteTask(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long idTask) {
		JSONObject result = null;
		String authToken = null;

		try {
			authToken = HelperSG.validateHeader(scrumGraphHeader);
			getDelegateTask().deleteTask(authToken, idTask);
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
	 * tipo Task
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
			getDelegateTask().validateCode(authToken, code);
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
	 * Servicio usado para asignar una tarea a un PBI
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Long Identificador de la tarea
	 * @return Response Respuesta del servicio REST.
	 */
	@POST
	@Path("{id}/pbi")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignPBIToProject(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
		JSONObject result = null;
		String authToken = null;
		Long idPBI = null;
		Map<String, Object> payLoadProperties = null;
		try {
			// Validacion AuthToken
			authToken = HelperSG.validateHeader(scrumGraphHeader);

			// Obtener identificador PBI
			payLoadProperties = HelperSG.getReqPayLoadProperties(reqPayLoad);
			if (payLoadProperties.containsKey("pbiId")) {
				idPBI = Long.parseLong(payLoadProperties.get("pbiId").toString());
			}

			// Asignar tarea a un pbi
			getDelegateTask().assignTaskToPBI(authToken, id, idPBI);
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
	 * Servicio rest usado para asignar una tarea a un usuario
	 * 
	 * @param scrumGraphHeader
	 *            Custom Header de Scrum Graph
	 * @param id
	 *            Long Identificador de la tarea
	 * @return Response Respuesta del servicio REST.
	 */
	@POST
	@Path("{id}/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignPBIToUser(@HeaderParam(SCRUM_GRAPH_HEADER) String scrumGraphHeader, @PathParam("id") Long id, String reqPayLoad) {
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

			// Asignar tarea a un usuario
			getDelegateTask().assignTaskToUser(authToken, id, idUser);
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

	/** OPTIONS */
	@OPTIONS
	public Response rootCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS
	@Path("/{id}")
	public Response taskIdCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS
	@Path("/{id}/tasks")
	public Response getTasksCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

	@OPTIONS 
	@Path("/verify/code/{code}")
	public Response validateCodeCORS() {
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header").allow("OPTIONS").build();
	}

}
