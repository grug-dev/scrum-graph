package co.edu.ud.scrumgraph.data.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.MapUtil;

import co.edu.ud.scrumgraph.data.api.NodeAPI;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.EDefaultStateNodes;
import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IColleagueNodeSrvMediator;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceMediator;
import co.edu.ud.scrumgraph.data.services.interfaces.IServicePBINode;
import co.edu.ud.scrumgraph.data.util.SGUtil;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public class ServicePBINode extends NodeAPI implements IServicePBINode, IColleagueNodeSrvMediator {

	private IServiceMediator srvMediator = null;

	ServicePBINode() {
		srvMediator = new ServiceMediator();
	}

	@Override
	protected void validateProperties(Map<String, Object> properties) throws ScrumGraphException {
		Object propertyObj = null;
		String propValue = null;

		// Validación Nulidad
		SGUtil.validateProperties(properties);

		properties.remove(ENodeProperties.ID.getPropertyName());

		// Validacion propiedades
		List<EPBIProperties> propToValid = new ArrayList<EPBIProperties>();
		propToValid.add(EPBIProperties.CODE);
		propToValid.add(EPBIProperties.STARTED_AT);
		propToValid.add(EPBIProperties.COMPLETED_AT);

		for (EPBIProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				continue;
			}
			propValue = propertyObj.toString();
			switch (property) {
			case CODE:
				validateCode(propValue, false);
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

	@Override
	public NodeTO createNode(Map<String, Object> properties) throws ScrumGraphException {
		NodeTO pbiNode = null;

		// Validacion de propiedades
		validateProperties(properties);

		// Asignacion de propiedades por default
		properties.put(EPBIProperties.AVAILABLE.getPropertyName(), true);
		properties.put(EPBIProperties.IS_CLOSED.getPropertyName(), false);
		properties.put(EPBIProperties.VEL_CALCULATED.getPropertyName(), 0);
		properties.put(EPBIProperties.TOTAL_TASK.getPropertyName(), 0);
		properties.put(EPBIProperties.NUM_TASK_DONE.getPropertyName(), 0);

		// Creacion de PBI
		pbiNode = new NodeTO();
		pbiNode.setProperties(properties);
		pbiNode.setLabelNode(ELabels.PBI);

		createNode(pbiNode);

		return pbiNode;
	}

	@Override
	public NodeTO updateNode(Long idNode, Map<String, Object> newProperties) throws ScrumGraphException {
		NodeTO pbiNode = null;
		Map<String, Object> pbiProperties = null;
		List<Object> lstResultUpdate = null;
		String oldStatus = null;
		Long sprintId = null;
		boolean changePBI = false;

		// Verificacion de existencia de nodo
		pbiNode = getNodeById(idNode);
		pbiProperties = pbiNode.getProperties();
		oldStatus = pbiNode.getProperties().get(EPBIProperties.STATUS.getPropertyName()).toString();

		// Validacion Nuevas Propiedades
		validateProperties(newProperties);

		// Asignación SPRINT
		if (newProperties.containsKey("sprintId")) {
			sprintId = Long.parseLong(newProperties.get("sprintId").toString());
			newProperties.remove("sprintId");
		}
		
		// Verificacion STATUS
		String statusProp = newProperties.get(ETaskProperties.STATUS.getPropertyName()).toString();
		if (!oldStatus.equalsIgnoreCase(statusProp)) {
			if (statusProp.equalsIgnoreCase(EDefaultStateNodes.DONE.getName())) {
				newProperties.put(EPBIProperties.COMPLETED_AT.getPropertyName(), new Date());
			}	
			else if (statusProp.equalsIgnoreCase(EDefaultStateNodes.WIP.getName())) {
				newProperties.put(EPBIProperties.STARTED_AT.getPropertyName(), new Date());
			}
		}

		// Actualizacion Nodo
		lstResultUpdate = updateNodeById(ELabels.PBI, idNode, newProperties);

		// Asignación SPRINT
		if (sprintId != null) {
			NodeTO currentSprint = null;
			if (pbiNode.getIncomingNodes() != null) {
				for (NodeTO nodeTO : pbiNode.getIncomingNodes()) {
					switch (nodeTO.getLabelNode()) {
					case SPRINT:
						currentSprint = nodeTO;
						break;
					default:
						break;
					}
				}
				if (currentSprint == null || (sprintId != currentSprint.getId())) {
					assignPBIToSprint(idNode, sprintId);
					if (currentSprint != null) {
						notifyUpdate(ELabels.SPRINT.getName(), currentSprint.getId());
					}
					changePBI = true;
				}
			}
		}

		// Notificacion de actualización
		if (statusProp.equalsIgnoreCase(EDefaultStateNodes.DONE.name()) || oldStatus.equalsIgnoreCase(EDefaultStateNodes.DONE.name())) {
			notifyChange(lstResultUpdate);
			changePBI = true;
		}
		
		if (changePBI) {
			pbiNode = getNodeById(idNode);
		}
		else {
			pbiProperties = updateProperties(pbiProperties, newProperties);
			pbiNode.setProperties(pbiProperties);	
		}

		return pbiNode;
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

	@Override
	public void deleteNode(Long idNode) throws ScrumGraphException {

		// Verificacion de existencia de nodo
		getNodeById(idNode);

		// Eliminación del nodo.
		List<Object> lstResultUpdate = deleteNodeById(ELabels.PBI, idNode);

		notifyChange(lstResultUpdate);

	}

	@Override
	public NodeTO getNodeById(Long idNode) throws ScrumGraphException {
		NodeTO pbiNode = null;

		pbiNode = searchById(idNode, ELabels.PBI, Direction.INCOMING);

		if (pbiNode == null) {
			throw new ScrumGraphException(ECodeExceptionSG.PBI_DOESNT_EXISTS);
		}

		// Actualizar Propiedades
		SGUtil.updateDateProperty(EPBIProperties.STARTED_AT.getPropertyName(), pbiNode.getProperties());
		SGUtil.updateDateProperty(EPBIProperties.COMPLETED_AT.getPropertyName(), pbiNode.getProperties());

		return pbiNode;
	}

	@Override
	public List<NodeTO> getAllNodes() throws ScrumGraphException {
		List<NodeTO> allNodes = null;

		allNodes = getAllByLabel(ELabels.PBI);

		if (allNodes != null) {
			for (NodeTO pbiNode : allNodes) {
				SGUtil.updateDateProperty(EPBIProperties.STARTED_AT.getPropertyName(), pbiNode.getProperties());
				SGUtil.updateDateProperty(EPBIProperties.COMPLETED_AT.getPropertyName(), pbiNode.getProperties());
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
	@Override
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
			throw new ScrumGraphException(ECodeExceptionSG.PBI_CODE_INVALID.getCode(), ECodeExceptionSG.PBI_CODE_INVALID.getMessage());
		}

		lenght = code.length();
		if (!(lenght >= 1 && lenght <= 8)) {
			throw new ScrumGraphException(ECodeExceptionSG.PBI_CODE_INVALID.getCode(), ECodeExceptionSG.PBI_CODE_INVALID.getMessage());
		}

		if (validateExists) {
			query = "MATCH (p:PBI) WHERE lower(p." + EPBIProperties.CODE.getPropertyName() + ")";
			query += "={code} ";
			query += " RETURN 1";
			params.put("code", code.toLowerCase());

			resultQuery = executeQuerySearch(query, params);

			if (resultQuery.hasNext()) {
				throw new ScrumGraphException(ECodeExceptionSG.PBI_CODE_INVALID);
			}
		}
	}

	@Override
	public void assignPBIToProject(Long idPBI, Long idProject) throws ScrumGraphException {

		String query = null;
		Map<String, Object> params = null;
		boolean relationCreated = false;

		if (idProject == null || idProject < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		// Verificar si existe relacion previa.
		// Si no existe, crea la relaci�n, si existe no la crea.
		params = new HashMap<>();
		params.put("idPBI", idPBI);
		params.put("idProj", idProject);
		query = "MATCH (pbi:PBI) WHERE id(pbi) = {idPBI} ";
		query += "MATCH (p:PROJECT) WHERE id(p) = {idProj} WITH pbi,p";
		query += " MERGE (p)-[r:";
		query += REL_TYPE_PROJECT + "]->";
		query += "(pbi) ON CREATE SET r.created=true ON MATCH SET r.created=false RETURN r.created ";

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);
		if (resultQuery != null) {
			if (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("r.created")) {
					Object rta = mapProjects.get("r.created");
					if (rta != null && (Boolean) rta) {
						relationCreated = true;
						notifyUpdate(ELabels.PROJECT.getName(), idProject);
					}
				}
			}
		}

		if (!relationCreated) {
			throw new ScrumGraphException(ECodeExceptionSG.OPERATION_FAILED);
		}

	}

	@Override
	public NodeTO assignPBIToSprint(Long idPBI, Long idSprint) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		NodeTO sprintTO = null;

		if (idSprint == null || idSprint < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		// Verificar si existe relacion previa.
		// Si no existe, crea la relación, si existe no la crea.
		params = new HashMap<>();
		params.put("idSprint", idSprint);
		params.put("idPBI", idPBI);

		query = "MATCH (s:SPRINT) WHERE id(s)={idSprint} ";
		query += " MATCH (p:PBI) WHERE id(p)={idPBI} ";
		query += "WITH p,s ";
		query += "OPTIONAL MATCH (p)-[r]-(x:SPRINT) ";
		query += "DELETE r ";
		query += "MERGE (s)-[nr:WORKS_ON]->(p) ";
		query += "WITH s,p ";
		query += "MATCH (s)-[]-(proj:PROJECT) WITH p,proj , s ";
		query += "MERGE (proj)-[pr:BELONGS_TO]->(p) ";
		query += "RETURN distinct(proj) as nodeProj , s";

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);
		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("nodeProj")) {
					Node sprintBD = (Node) mapProjects.get("s");
					Node proj = (Node) mapProjects.get("nodeProj");
					if (proj != null) {
						notifyUpdate(ELabels.SPRINT.getName(), idSprint);
						notifyUpdate(ELabels.PROJECT.getName(), proj.getId());
						sprintTO = convertTO(sprintBD, ELabels.SPRINT);
						NodeTO projTO = convertTO(proj, ELabels.PBI);
						sprintTO.setIncomingNodes(new ArrayList<NodeTO>());
						sprintTO.getIncomingNodes().add(projTO);
					}
				}
			}
		}

		return sprintTO;
	}

	@Override
	public void refreshNode(Long idNode) throws ScrumGraphException {
		NodeTO pbiTO = null;
		int contPBIDone = 0;
		int contPBITODO = 0;
		Object objVelocity = null;
		double velPBIHours = 0d;
		String statusPBI = EDefaultStateNodes.WIP.getName();

		// Consultar Nodo a refrescar
		pbiTO = searchById(idNode, ELabels.PBI, Direction.OUTGOING, ELabels.TASK);

		// Propiedades iniciales
		objVelocity = pbiTO.getProperties().get(EPBIProperties.VEL_CALCULATED.getPropertyName());
		if (objVelocity != null) {
			velPBIHours = Double.parseDouble(objVelocity.toString());
		}

		// Refrescar el nodo pbi
		if (pbiTO != null) {
			Map<String, Object> newProperties = new HashMap<String, Object>();
			List<NodeTO> lstTask = pbiTO.getOutgoingNodes();

			if (lstTask != null && !lstTask.isEmpty()) {
				velPBIHours = 0d;
				// Recorrer los TASK asociados al PBI.
				for (NodeTO taskTO : lstTask) {
					Map<String, Object> taskProperties = taskTO.getProperties();

					// Actualizar Numero PBIS en estado DONE y Velocidad SPRINT
					Object objStatus = taskProperties.get(ETaskProperties.STATUS.getPropertyName());
					if (objStatus != null) {
						if (EDefaultStateNodes.DONE.getName().toString().equals(objStatus.toString())) {
							contPBIDone++;
							objVelocity = taskProperties.get(ETaskProperties.EXECUTE_HOURS.getPropertyName());
							if (objVelocity != null) {
								velPBIHours += Double.valueOf(objVelocity.toString()).doubleValue();
							}
						} else {
							if (EDefaultStateNodes.TODO.getName().toString().equals(objStatus.toString())) {
								contPBITODO++;
							}
						}
					}
				}
				// Actualizar Estado SPRINT
				if (contPBIDone == lstTask.size()) {
					statusPBI = EDefaultStateNodes.DONE.getName();
				} else if (contPBITODO == lstTask.size()) {
					statusPBI = EDefaultStateNodes.TODO.getName();
				}

				newProperties.put(EPBIProperties.TOTAL_TASK.getPropertyName(), lstTask.size());
				newProperties.put(EPBIProperties.NUM_TASK_DONE.getPropertyName(), contPBIDone);
				newProperties.put(EPBIProperties.VEL_CALCULATED.getPropertyName(), velPBIHours);
				newProperties.put(EPBIProperties.STATUS.getPropertyName(), statusPBI);
			} else {
				newProperties.put(EPBIProperties.TOTAL_TASK.getPropertyName(), 0);
				newProperties.put(EPBIProperties.NUM_TASK_DONE.getPropertyName(), contPBIDone);
				newProperties.put(EPBIProperties.VEL_CALCULATED.getPropertyName(), velPBIHours);
				newProperties.put(EPBIProperties.STATUS.getPropertyName(), EDefaultStateNodes.TODO.getName());
			}

			// Actualizar Nodo
			// Si el nodo cambio de estado a DONE, notificar
			List<Object> lstResultUpdate = null;
			lstResultUpdate = updateNodeById(ELabels.PBI, idNode, newProperties);
			verifyUpdateToNotify(lstResultUpdate, pbiTO.getProperties(), newProperties);

		}
	}

	private void verifyUpdateToNotify(List<Object> lstResultUpdate, Map<String, Object> properties, Map<String, Object> newProperties) throws ScrumGraphException {
		Object statusProp = newProperties.get(EPBIProperties.STATUS.getPropertyName());
		Object oldStatus = properties.get(EPBIProperties.STATUS.getPropertyName());
		if (statusProp.toString().equalsIgnoreCase(EDefaultStateNodes.DONE.name()) || oldStatus.toString().equalsIgnoreCase(EDefaultStateNodes.DONE.name())) {
			notifyChange(lstResultUpdate);
		}
	}

	@Override
	public void notifyUpdate(String label, Long idNode) throws ScrumGraphException {
		ELabels lblEnum = null;

		lblEnum = ELabels.valueOf(label);

		if (lblEnum != null) {
			srvMediator.notifyUpdate(lblEnum, idNode);
		}

	}

	@Override
	public NodeTO getGraphByPbi(Long idPBI) throws ScrumGraphException {
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		NodeTO pbiTO = null;

		query = "OPTIONAL MATCH (pbi:PBI) WHERE id(pbi) = {idPbi} WITH pbi OPTIONAL MATCH (pbi)--(x) RETURN pbi,x , labels(x) as lblX";

		resultQuery = executeQuerySearch(query, MapUtil.map("idPbi", idPBI));

		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> result = resultQuery.next();
				Node nodePBIBD = (Node) result.get("pbi");
				if (nodePBIBD != null && pbiTO == null) {
					pbiTO = convertTO(nodePBIBD, ELabels.PBI);
					pbiTO.setIncomingNodes(new ArrayList<NodeTO>());
					pbiTO.setOutgoingNodes(new ArrayList<NodeTO>());
				}
				Node nodeX = (Node) result.get("x");
				if (nodeX != null) {
					List<String> lbl = (List<String>) result.get("lblX");
					NodeTO nodeRelated = convertTO(nodeX, ELabels.valueOf(lbl.get(0)));
					if (nodeRelated != null) {
						switch (nodeRelated.getLabelNode()) {
						case PROJECT:
						case SPRINT:
							pbiTO.getIncomingNodes().add(nodeRelated);
							break;
						case TASK:
							pbiTO.getOutgoingNodes().add(nodeRelated);
							break;
						}
					}
				}

			}
		}

		if (pbiTO == null) {
			throw new ScrumGraphException(ECodeExceptionSG.PBI_DOESNT_EXISTS);
		}

		return pbiTO;
	}

	@Override
	public Map<String, Integer> getChartByPbi(Long idPBI) throws ScrumGraphException {
		Map<String, Integer> chartPBI = null;
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		
		//Agrupar Task
		query = "MATCH (p:PBI)--(t:TASK)  WHERE id(p)={idPbi} RETURN distinct(t.status) as status , count(t.status) as cant";
		resultQuery = executeQuerySearch(query,MapUtil.map("idPbi",idPBI));
		if (resultQuery != null) {
			chartPBI = new HashMap<String,Integer>();
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("status")) {
					String s = mapProjects.get("status").toString();
					String cant = mapProjects.get("cant").toString(); 
						 chartPBI.put(s, Integer.parseInt(cant));
					}
				}
			}
		
		return chartPBI;
	}
}
