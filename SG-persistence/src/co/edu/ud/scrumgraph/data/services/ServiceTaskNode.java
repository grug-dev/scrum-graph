/**
 * 
 */
package co.edu.ud.scrumgraph.data.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.MapUtil;

import co.edu.ud.scrumgraph.data.api.NodeAPI;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.EDefaultStateNodes;
import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IColleagueNodeSrvMediator;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceTaskNode;
import co.edu.ud.scrumgraph.data.util.SGUtil;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

/**
 * @author RaspuWIN7
 *
 */
class ServiceTaskNode extends NodeAPI implements IServiceTaskNode, IColleagueNodeSrvMediator {

	private ServiceMediator srvMediator = null;

	ServiceTaskNode() {
		srvMediator = new ServiceMediator();
	}

	@Override
	protected void validateProperties(Map<String, Object> properties) throws ScrumGraphException {

		Object propertyObj = null;
		String propValue = null;

		// Validaci�n Nulidad
		SGUtil.validateProperties(properties);

		// Validacion propiedades
		List<ETaskProperties> propToValid = new ArrayList<ETaskProperties>();
		propToValid.add(ETaskProperties.STARTED_AT);
		propToValid.add(ETaskProperties.COMPLETED_AT);
		propToValid.add(ETaskProperties.CODE);
		propToValid.add(ETaskProperties.STATUS);

		for (ETaskProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				continue;
			}
			propValue = propertyObj.toString();
			switch (property) {
			case CODE:
				validateCode(propValue, false);
				break;
			case STATUS:
				if (propValue.toString().equalsIgnoreCase(EDefaultStateNodes.DONE.getName())) {
					properties.put(ETaskProperties.PERCENTAGE_DONE.getPropertyName(), 100);
					properties.put(ETaskProperties.REMAINING_HOURS.getPropertyName(), 0);
				} else if (propValue.toString().equalsIgnoreCase(EDefaultStateNodes.TODO.getName())) {
					properties.put(ETaskProperties.PERCENTAGE_DONE.getPropertyName(), 0);
				}
				break;
			case STARTED_AT:
			case COMPLETED_AT:
				if (propertyObj instanceof String) {
					Date someDate = SGUtil.toDate(propValue);
					properties.put(property.getPropertyName(), someDate);
				}
				break;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * co.edu.ud.scrumgraph.data.services.impl.IServiceNode#createNode(java.util
	 * .Map)
	 */
	@Override
	public NodeTO createNode(Map<String, Object> properties) throws ScrumGraphException {
		NodeTO taskNode = null;
		List<Integer> users = null;

		// Validacion de propiedades
		properties.put(ETaskProperties.STATUS.getPropertyName(), EDefaultStateNodes.TODO.getName());
		validateProperties(properties);

		// Asignacion de propiedades por default
		properties.put(ETaskProperties.EXECUTE_HOURS.getPropertyName(), 0);
		properties.put(ETaskProperties.IS_CLOSED.getPropertyName(), false);
		properties.put(ETaskProperties.PERCENTAGE_DONE.getPropertyName(), 0);

		// Verificacion si tiene usuarios a asociar la tarea
		if (properties.containsKey(ETaskProperties.USERS.getPropertyName())) {
			users = (List<Integer>) properties.get(ETaskProperties.USERS.getPropertyName());
			String strUsers = SGUtil.toStringList(users);
			properties.put(ETaskProperties.USERS.getPropertyName() , strUsers);
		}

		// Creacion de TASK
		taskNode = new NodeTO();
		taskNode.setProperties(properties);
		taskNode.setLabelNode(ELabels.TASK);

		createNode(taskNode);

		assignTaskToUser(taskNode.getId(), users);
		
		return taskNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * co.edu.ud.scrumgraph.data.services.impl.IServiceNode#updateNode(java.lang
	 * .Long, java.util.Map)
	 */
	@Override
	public NodeTO updateNode(Long idNode, Map<String, Object> newProperties) throws ScrumGraphException {
		NodeTO taskNode = null;
		Map<String, Object> taskProperties = null;
		List<Object> lstResultUpdate = null;
		String oldStatus = null;
		List<Integer> users = null;
		Long idPBI = null;
		boolean assigned = false;

		// Verificacion de existencia de nodo
		taskNode = getNodeById(idNode);
		taskProperties = taskNode.getProperties();
		oldStatus = taskProperties.get(ETaskProperties.STATUS.getPropertyName()).toString();

		// Validacion Nuevas Propiedades
		validateProperties(newProperties);
		
		//Acumulacion executedHours
		Integer currentExecHours = (Integer) taskProperties.get(ETaskProperties.EXECUTE_HOURS.getPropertyName());
		if (newProperties.containsKey(ETaskProperties.EXECUTE_HOURS.getPropertyName())) {
			Integer newExecHours = (Integer) newProperties.get(ETaskProperties.EXECUTE_HOURS.getPropertyName());
			newExecHours += currentExecHours;
			newProperties.put(ETaskProperties.EXECUTE_HOURS.getPropertyName(), newExecHours);
		}

		// Verificacion si tiene usuarios a asociar la tarea
		if (newProperties.containsKey(ETaskProperties.USERS.getPropertyName())) {
			users = (List<Integer>) newProperties.get(ETaskProperties.USERS.getPropertyName());
			String strUsers = SGUtil.toStringList(users);
			newProperties.put(ETaskProperties.USERS.getPropertyName(), strUsers);
		}
		
		// Verificacion si tiene PBI a asociar
		if (newProperties.containsKey("pbiId")) {
			idPBI = Long.parseLong(newProperties.get("pbiId").toString());
			newProperties.remove("pbiId");
		}
		
		// Verificacion STATUS
		String statusProp = newProperties.get(ETaskProperties.STATUS.getPropertyName()).toString();
		if (!oldStatus.equalsIgnoreCase(statusProp)) {
			if (statusProp.equalsIgnoreCase(EDefaultStateNodes.DONE.getName())) {
				newProperties.put(ETaskProperties.COMPLETED_AT.getPropertyName(), new Date());
			}	
			else if (statusProp.equalsIgnoreCase(EDefaultStateNodes.WIP.getName())) {
				newProperties.put(ETaskProperties.STARTED_AT.getPropertyName(), new Date());
			}
		}
		

		// Actualizacion Nodo
		lstResultUpdate = updateNodeById(ELabels.TASK, idNode, newProperties);

		// Asignación PBI
		if (idPBI != null) {
			NodeTO currentPBI = null;
			if (taskNode.getIncomingNodes() != null) {
				for (NodeTO nodeTO : taskNode.getIncomingNodes()) {
					switch (nodeTO.getLabelNode()) {
					case PBI:
						currentPBI = nodeTO;
						break;
					default:
						break;
					}
				}
				if (idPBI != currentPBI.getId()) {
					assignTaskToPBI(idNode, idPBI);
					notifyUpdate(ELabels.PBI.getName(), currentPBI.getId());
					assigned = true;
				}
			}
		}
		
		// AsignacionUsuarios
		if (users != null) {
			assignTaskToUser(idNode, users);
			assigned = true;
		}

		if (assigned) {
			taskNode = getNodeById(idNode);
		}
		else {
			taskProperties = updateProperties(taskProperties, newProperties);
			taskNode.setProperties(taskProperties);	
		}
		
		// Enviar notificacion de cambio
		// Notificacion de actualización
		if (statusProp.equalsIgnoreCase(EDefaultStateNodes.DONE.getName()) || oldStatus.toString().equalsIgnoreCase(EDefaultStateNodes.DONE.getName())) {
			notifyChange(lstResultUpdate);
		}

		return taskNode;
	}

	private void notifyChange(List<Object> lstResultUpdate) throws ScrumGraphException {
		if (lstResultUpdate != null) {
			for (Object result : lstResultUpdate) {
				List<Object> lstObjResult = (List<Object>) result;
				List<String> lstLabels = (List<String>) lstObjResult.get(1);
				Integer idSource = (Integer) lstObjResult.get(2);
				if (lstLabels != null && idSource != null) {
					notifyUpdate(lstLabels.get(0), idSource.longValue());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * co.edu.ud.scrumgraph.data.services.impl.IServiceNode#deleteNode(java.lang
	 * .Long)
	 */
	@Override
	public void deleteNode(Long idNode) throws ScrumGraphException {

		// Verificacion de existencia de nodo
		getNodeById(idNode);

		// Eliminación del nodo.
		List<Object> lstResultUpdate = deleteNodeById(ELabels.TASK, idNode);

		// Notificacion de actualización
		notifyChange(lstResultUpdate);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * co.edu.ud.scrumgraph.data.services.impl.IServiceNode#getNodeById(java.
	 * lang.Long)
	 */
	@Override
	public NodeTO getNodeById(Long idNode) throws ScrumGraphException {
		NodeTO taskNode = null;
		String query = null;
		List<Integer> idUsers = new ArrayList<Integer>();

		query = " MATCH (t:TASK) WHERE id(t) = {idTask} ";
		query += "OPTIONAL MATCH (t)--(u:USER) ";
		query += " OPTIONAL MATCH (t)--(pbi:PBI) ";
		query += "OPTIONAL MATCH (pbi)--(s:SPRINT) ";
		query += "OPTIONAL MATCH (s)--(p:PROJECT) ";
		query += "RETURN t,id(u) as idUser,pbi,s,p ";

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, MapUtil.map("idTask", idNode));
		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> result = resultQuery.next();
				if (taskNode == null) {
					Node nodeTaskBD = (Node) result.get("t");
					Node nodePBIBD = (Node) result.get("pbi");
					Node nodeSprintBD = (Node) result.get("s");
					Node nodeProjectBD = (Node) result.get("p");

					if (nodeTaskBD != null) {
						taskNode = convertTO(nodeTaskBD, ELabels.TASK);
						if (nodePBIBD != null) {
							NodeTO pbiTO = convertTO(nodePBIBD, ELabels.PBI);
							taskNode.setIncomingNodes(new ArrayList<NodeTO>());
							taskNode.getIncomingNodes().add(pbiTO);
						}
						if (nodeSprintBD != null) {
							NodeTO nodeTO = convertTO(nodeSprintBD, ELabels.SPRINT);
							taskNode.getIncomingNodes().add(nodeTO);
						}
						if (nodePBIBD != null) {
							NodeTO nodeTO = convertTO(nodeProjectBD, ELabels.PROJECT);
							taskNode.getIncomingNodes().add(nodeTO);
						}
					}
				}
				Object objIdUsers = result.get("idUser");
				if (objIdUsers != null) {
					idUsers.add((Integer) objIdUsers);
				}
			}
		}

		if (taskNode == null) {
			throw new ScrumGraphException(ECodeExceptionSG.TASK_DOESNT_EXISTS);
		}

		// Actualizar Propiedades
		SGUtil.updateDateProperty(ETaskProperties.COMPLETED_AT.getPropertyName(), taskNode.getProperties());
		SGUtil.updateDateProperty(ETaskProperties.STARTED_AT.getPropertyName(), taskNode.getProperties());

		return taskNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see co.edu.ud.scrumgraph.data.services.impl.IServiceNode#getAllNodes()
	 */
	@Override
	public List<NodeTO> getAllNodes() throws ScrumGraphException {
		List<NodeTO> allNodes = null;

		allNodes = getAllByLabel(ELabels.TASK);

		if (allNodes != null) {
			for (NodeTO taskTO : allNodes) {
				SGUtil.updateDateProperty(ETaskProperties.STARTED_AT.getPropertyName(), taskTO.getProperties());
				SGUtil.updateDateProperty(ETaskProperties.COMPLETED_AT.getPropertyName(), taskTO.getProperties());
			}
		}

		return allNodes;
	}

	/**
	 * Método para validar el code pasado como parámetro.
	 * 
	 * @param code
	 *            String code a validar
	 * @throws ScrumGraphException
	 *             Excepción lanzada por alguna validación no cumplida.
	 */
	public void validateCode(String code) throws ScrumGraphException {
		// Validar Codigo
		validateCode(code, true);
	}

	/**
	 * Método para validar la propiedad code.
	 * 
	 * @param code
	 *            String code a verificar su existencia
	 * @param validateExists
	 *            boolean para indicar si se valida la existencia del code en
	 *            base de datos.
	 * @throws ScrumGraphException
	 */
	private void validateCode(String code, boolean validateExists) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();
		Iterator<Map<String, Object>> resultQuery = null;
		int lenght = -1;

		if (code == null) {
			throw new ScrumGraphException(ECodeExceptionSG.TASK_CODE_INVALID.getCode(), ECodeExceptionSG.TASK_CODE_INVALID.getMessage());
		}

		lenght = code.length();
		if (!(lenght >= 1 && lenght <= 8)) {
			throw new ScrumGraphException(ECodeExceptionSG.TASK_CODE_INVALID.getCode(), ECodeExceptionSG.TASK_CODE_INVALID.getMessage());
		}

		if (validateExists) {
			query = "MATCH (p:TASK) WHERE lower(p." + ESprintProperties.CODE.getPropertyName() + ")";
			query += "={code} ";
			query += " RETURN 1";
			params.put("code", code.toLowerCase());

			resultQuery = executeQuerySearch(query, params);

			if (resultQuery.hasNext()) {
				throw new ScrumGraphException(ECodeExceptionSG.TASK_ALREADY_EXISTS);
			}
		}
	}

	@Override
	public NodeTO assignTaskToPBI(Long idTask, Long idPBI) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		NodeTO taskto = null;

		if (idPBI == null || idPBI < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		// Verificar si existe relacion previa.
		// Si no existe, crea la relación, si existe no la crea.
		params = new HashMap<>();
		params.put("idPBI", idPBI);
		params.put("idTask", idTask);

		query = "MATCH (t:TASK) WHERE id(t)={idTask} ";
		query += "MATCH (p:PBI) WHERE id(p)={idPBI} ";
		query += "WITH p, t ";
		query += "OPTIONAL MATCH (t)-[r]-(x:PBI) ";
		query += "DELETE r ";
		query += "MERGE (p)-[nr:WORKS_ON]->(t) ";
		query += "RETURN (t)-[:WORKS_ON]-() as result";

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);
		if (resultQuery != null && resultQuery.hasNext()) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("result")) {
					List<Map<String, Object>> rta = (List<Map<String, Object>>) mapProjects.get("result");
					if (rta != null) {
						notifyUpdate(ELabels.PBI.getName(), idPBI);
					}
				}
			}
		}
		else {
			throw new ScrumGraphException(ECodeExceptionSG.PBI_DOESNT_EXISTS);
		}
		
		taskto = getNodeById(idTask);
		
		return taskto;
	}

	@Override
	public void notifyUpdate(String lbl, Long idNode) throws ScrumGraphException {
		ELabels lblEnum = null;

		lblEnum = ELabels.valueOf(lbl);

		if (lblEnum != null) {
			srvMediator.notifyUpdate(lblEnum, idNode);
		}

	}

	@Override
	public void refreshNode(Long idNode) {
	}

	/**
	 * Método que asigna un nodo Task a un listado de usuarios
	 * 
	 * @param idTask
	 *            Identificador del Task
	 * @param idUsers
	 *            Listado de Identificadores de usuarios
	 * @throws ScrumGraphException
	 *             Excepción al realizar la asignación.
	 */
	public void assignTaskToUser(Long idTask, List<Integer> idUsers) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		boolean relationCreated = false;

		if (idUsers == null || idUsers.isEmpty()) {
			return;
		}

		// Verificar si existe relacion previa.
		// Si no existe, crea la relaci�n, si existe no la crea.
		params = new HashMap<>();
		params.put("idTask", idTask);
		params.put("idUsers", idUsers);
		query = "MATCH (u:USER) WHERE id(u) IN {idUsers} WITH u MATCH (t:TASK) WHERE id(t) = {idTask} ";
		query += " MERGE (t)-[r:";
		query += REL_TYPE_TO_USER + "]->";
		query += "(u) ON CREATE SET r.created=true ON MATCH SET r.created=false RETURN r.created ";

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);
		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("r.created")) {
					Object rta = mapProjects.get("r.created");
					if (rta != null && (Boolean) rta) {
						relationCreated = true;
					}
				}
			}
		}
	}

}
