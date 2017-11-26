/**
 * 
 */
package co.edu.ud.scrumgraph.data.services;

import java.math.BigDecimal;
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
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IColleagueNodeSrvMediator;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceSprintNode;
import co.edu.ud.scrumgraph.data.util.SGUtil;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

/**
 * @author RaspuWIN7
 *
 */
public class ServiceSprintNode extends NodeAPI implements IServiceSprintNode, IColleagueNodeSrvMediator {

	private ServiceMediator srvMediator = null;

	public ServiceSprintNode() {
		srvMediator = new ServiceMediator();
	}

	@Override
	protected void validateProperties(Map<String, Object> properties) throws ScrumGraphException {
		Object propertyObj = null;
		String propValue = null;

		// Validación Nulidad
		SGUtil.validateProperties(properties);

		// Validacion propiedades
		List<ESprintProperties> propToValid = new ArrayList<ESprintProperties>();
		propToValid.add(ESprintProperties.CODE);
		propToValid.add(ESprintProperties.INIT_DATE);
		propToValid.add(ESprintProperties.END_DATE);

		for (ESprintProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				continue;
			}
			propValue = propertyObj.toString();
			switch (property) {
			case CODE:
				validateCode(propValue, false);
				break;
			case INIT_DATE:
			case END_DATE:
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
		NodeTO sprintNode = null;

		// Validacion de propiedades
		validateProperties(properties);

		// Asignacion de propiedades por default
		properties.put(ESprintProperties.IS_CLOSED.getPropertyName(), false);
		properties.put(ESprintProperties.VELOCITY.getPropertyName(), 0);
		properties.put(ESprintProperties.VEL_POINTS_BY_HOUR.getPropertyName(), 0);
		properties.put(ESprintProperties.TOTAL_PBI.getPropertyName(), 0);
		properties.put(ESprintProperties.NUM_PBI_DONE.getPropertyName(), 0);
		properties.put(ESprintProperties.STATUS.getPropertyName(), EDefaultStateNodes.TODO.getName());

		// Creacion de SPRINT
		sprintNode = new NodeTO();
		sprintNode.setProperties(properties);
		sprintNode.setLabelNode(ELabels.SPRINT);

		createNode(sprintNode);

		return sprintNode;
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
		NodeTO sprintNode = null;
		Map<String, Object> sprintProperties = null;

		// Verificacion de existencia de nodo
		sprintNode = getNodeById(idNode);

		// Validacion Nuevas Propiedades
		validateProperties(newProperties);
		
		// Actualizacion Nodo
		List<Object> lstResultUpdate = updateNodeById(ELabels.SPRINT, idNode, newProperties);

		// Notificacion de actualización
		verifyUpdateToNotify(lstResultUpdate, sprintNode.getProperties(), newProperties);

		// Actualizar propiedades para devolver nodo
		sprintProperties = sprintNode.getProperties();
		sprintProperties = updateProperties(sprintProperties, newProperties);
		sprintNode.setProperties(sprintProperties);

		return sprintNode;
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
		List<Object> lstResultUpdate = deleteNodeById(ELabels.SPRINT, idNode);

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
		NodeTO sprintNode = null;

		sprintNode = searchById(idNode, ELabels.SPRINT , Direction.INCOMING);

		if (sprintNode == null) {
			throw new ScrumGraphException(ECodeExceptionSG.SPRINT_DOESNT_EXISTS);
		}

		// Actualizar Propiedades
		SGUtil.updateDateProperty(ESprintProperties.INIT_DATE.getPropertyName(), sprintNode.getProperties());
		SGUtil.updateDateProperty(ESprintProperties.END_DATE.getPropertyName(), sprintNode.getProperties());

		return sprintNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see co.edu.ud.scrumgraph.data.services.impl.IServiceNode#getAllNodes()
	 */
	@Override
	public List<NodeTO> getAllNodes() throws ScrumGraphException {
		List<NodeTO> allNodes = null;

		allNodes = getAllByLabel(ELabels.SPRINT);

		if (allNodes != null) {
			for (NodeTO sprintNode : allNodes) {
				SGUtil.updateDateProperty(ESprintProperties.INIT_DATE.getPropertyName(), sprintNode.getProperties());
				SGUtil.updateDateProperty(ESprintProperties.END_DATE.getPropertyName(), sprintNode.getProperties());
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
			throw new ScrumGraphException(ECodeExceptionSG.SPRINT_CODE_INVALID.getCode(), ECodeExceptionSG.SPRINT_CODE_INVALID.getMessage());
		}

		lenght = code.length();
		if (!(lenght >= 1 && lenght <= 8)) {
			throw new ScrumGraphException(ECodeExceptionSG.SPRINT_CODE_INVALID.getCode(), ECodeExceptionSG.SPRINT_CODE_INVALID.getMessage());
		}

		if (validateExists) {
			query = "MATCH (p:SPRINT) WHERE lower(p." + ESprintProperties.CODE.getPropertyName() + ")";
			query += "={code} ";
			query += " RETURN 1";
			params.put("code", code.toLowerCase());

			resultQuery = executeQuerySearch(query, params);

			if (resultQuery.hasNext()) {
				throw new ScrumGraphException(ECodeExceptionSG.SPRINT_ALREADY_EXISTS);
			}
		}
	}

	@Override
	public void assignSprintToProject(Long idSprint, Long idProject) throws ScrumGraphException {

		String query = null;
		Map<String, Object> params = null;
		boolean relationCreated = false;

		if (idProject == null || idProject < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		// Verificar si existe relacion previa.
		// Si no existe, crea la relaci�n, si existe no la crea.
		params = new HashMap<>();
		params.put("idSprint", idSprint);
		params.put("idProj", idProject);
		query = "MATCH (p:PROJECT) WHERE id(p) = {idProj} WITH p MATCH (s:SPRINT) WHERE id(s) = {idSprint} ";
		query += " MERGE (p)-[r:";
		query += REL_TYPE_PROJECT + "]->";
		query += "(s) ON CREATE SET r.created=true ON MATCH SET r.created=false RETURN r.created ";

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

	@Override
	public void notifyUpdate(String lbl, Long idNode) throws ScrumGraphException {
		ELabels lblEnum = null;

		lblEnum = ELabels.valueOf(lbl);

		if (lblEnum != null) {
			srvMediator.notifyUpdate(lblEnum, idNode);
		}
	}

	@Override
	public void refreshNode(Long idNode) throws ScrumGraphException {
		NodeTO sprintTO = null;
		int contPBIDone = 0;
		int contPBITODO = 0;
		Object objVelocity = null;
		double velocitySprint = 0d;
		double velPointByHour = 0d;
		double totalPoints = 0d;
		String statusSprint = EDefaultStateNodes.WIP.getName();

		// Consultar Nodo a refrescar
		sprintTO = searchById(idNode, ELabels.SPRINT, Direction.OUTGOING);

		// Propiedades iniciales
		objVelocity = sprintTO.getProperties().get(ESprintProperties.VELOCITY.getPropertyName());
		if (objVelocity != null) {
			velocitySprint = Double.parseDouble(objVelocity.toString());
		}

		// Refrescar el nodo sprint
		if (sprintTO != null) {
			Map<String, Object> newProperties = new HashMap<String, Object>();
			List<NodeTO> lstPBIS = sprintTO.getOutgoingNodes();

			if (lstPBIS != null && !lstPBIS.isEmpty()) {
				velocitySprint = 0d;
				// Recorrer los PBI asociados al sprint.
				for (NodeTO pbiTO : lstPBIS) {
					Map<String, Object> pbiProperties = pbiTO.getProperties();
					objVelocity = pbiProperties.get(EPBIProperties.HISTORY_POINTS.getPropertyName());
					totalPoints += Double.valueOf(objVelocity.toString()).doubleValue();
					
					// Actualizar Numero PBIS en estado DONE y Velocidad SPRINT
					Object objStatus = pbiProperties.get(EPBIProperties.STATUS.getPropertyName());
					if (objStatus != null) {
						if (EDefaultStateNodes.DONE.getName().toString().equals(objStatus.toString())) {
							contPBIDone++;
							if (objVelocity != null) {
								velocitySprint += Double.valueOf(objVelocity.toString()).doubleValue();
							}
							// Sumatoria velocidad por hora.
							Object velPointHour = pbiProperties.get(EPBIProperties.VEL_CALCULATED.getPropertyName());
							if (velPointHour != null)
							velPointByHour +=  Double.valueOf(velPointHour.toString());
						} else {
							if (EDefaultStateNodes.TODO.getName().toString().equals(objStatus.toString())) {
								contPBITODO++;
							}
						}
					}
				}
				// Actualizar Estado SPRINT
				if (contPBIDone == lstPBIS.size()) {
					statusSprint = EDefaultStateNodes.DONE.getName();
				} else if (contPBITODO == lstPBIS.size()) {
					statusSprint = EDefaultStateNodes.TODO.getName();
				}

				if (velPointByHour != 0.0d)
				velPointByHour = new BigDecimal(velocitySprint/velPointByHour).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
				
				newProperties.put(ESprintProperties.TOTAL_PBI.getPropertyName(), lstPBIS.size());
				newProperties.put(ESprintProperties.NUM_PBI_DONE.getPropertyName(), contPBIDone);
				newProperties.put(ESprintProperties.VELOCITY.getPropertyName(), velocitySprint);
				newProperties.put(ESprintProperties.STATUS.getPropertyName(), statusSprint);
				newProperties.put(ESprintProperties.VEL_POINTS_BY_HOUR.getPropertyName(), velPointByHour);
			} else {
				newProperties.put(ESprintProperties.TOTAL_PBI.getPropertyName(), 0);
				newProperties.put(ESprintProperties.NUM_PBI_DONE.getPropertyName(), contPBIDone);
				newProperties.put(ESprintProperties.VELOCITY.getPropertyName(), velocitySprint);
				newProperties.put(ESprintProperties.STATUS.getPropertyName(), EDefaultStateNodes.TODO.getName());
				newProperties.put(ESprintProperties.VEL_POINTS_BY_HOUR.getPropertyName(), velocitySprint);
			}
			// Actualizacion Total puntos historia
			newProperties.put(ESprintProperties.TOT_POINTS_DONE.getPropertyName(), totalPoints);
			// Actualizar Nodo
			List<Object> lstResultUpdate = updateNodeById(ELabels.SPRINT, idNode, newProperties);
			verifyUpdateToNotify(lstResultUpdate, sprintTO.getProperties(), newProperties);
		}
	}
	
	private void verifyUpdateToNotify(List<Object> lstResultUpdate , Map<String, Object> properties, Map<String, Object> newProperties) throws ScrumGraphException {
		Object statusProp = newProperties.get(EPBIProperties.STATUS.getPropertyName());
		Object oldStatus = properties.get(EPBIProperties.STATUS.getPropertyName());
		if (statusProp.toString().equalsIgnoreCase(EDefaultStateNodes.DONE.getName()) 
				|| oldStatus.toString().equalsIgnoreCase(EDefaultStateNodes.DONE.getName()) ) {
			notifyChange(lstResultUpdate);	
		}
	}

	@Override
	public NodeTO getPbisBySprint(Long idSprint) throws ScrumGraphException {
		NodeTO sprintTO = null;
		String query = null;
		Map<String, Object> params = null;

		if (idSprint == null || idSprint < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		
		// Verificar si existe relacion previa.
		// Si no existe, crea la relaci�n, si existe no la crea.
		params = new HashMap<>();
		params.put("idSprint", idSprint);
		query = "MATCH (s:SPRINT)--(x) WHERE id(s) = {idSprint} RETURN s,x , labels(x) as lblX" ;

		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);
		Node sprintBD = null;
		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> result = resultQuery.next();
				if (sprintBD == null) {
					sprintBD = (Node) result.get("s");
					sprintTO = convertTO(sprintBD, ELabels.SPRINT);
					sprintTO.setIncomingNodes(new ArrayList<NodeTO>());
					sprintTO.setOutgoingNodes(new ArrayList<NodeTO>());
				}
				Node nodeX = (Node) result.get("x");
				if (nodeX != null) {
					List<String> lbl = (List<String>) result.get("lblX");
					NodeTO nodeRelated = convertTO(nodeX, ELabels.valueOf(lbl.get(0)));
					if (nodeRelated != null) {
						switch (nodeRelated.getLabelNode()) {
						case PROJECT:
							sprintTO.getIncomingNodes().add(nodeRelated);
							break;
						case PBI:
							sprintTO.getOutgoingNodes().add(nodeRelated);
							break;
						}
					}
				}
			}
		}
		
		if (sprintTO == null) {
			throw new ScrumGraphException(ECodeExceptionSG.SPRINT_DOESNT_EXISTS);
		}
		
		return sprintTO;
	}

	@Override
	public Map<String, Integer> getChartById(Long idSprint) throws ScrumGraphException {
		Map<String, Integer> chatSprint = null;
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		
		//Agrupar Task
		query = "MATCH (s:SPRINT)--(p:PBI {status:'done'}) WHERE id(s) = {idSprint}";
		query += " RETURN DISTINCT(p.completedAt) as dateCompleted, count(p.completedAt) as cant" ;
		resultQuery = executeQuerySearch(query,MapUtil.map("idSprint",idSprint));
		if (resultQuery != null) {
			chatSprint = new HashMap<String,Integer>();
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("dateCompleted")) {
					Date dateComp = SGUtil.toDate(mapProjects.get("dateCompleted"));
					String s = SGUtil.toDateFormatSG(dateComp);
					String cant = mapProjects.get("cant").toString(); 
						 chatSprint.put(s, Integer.parseInt(cant));
					}
				}
			}
		
		return chatSprint;
	
	}

}
