package co.edu.ud.scrumgraph.data.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.batch.CypherResult;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import co.edu.ud.scrumgraph.data.conexion.ConexionService;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.dto.RelationshipTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.enums.ERelTypes;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.util.SGUtil;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public abstract class NodeAPI {

	protected abstract void validateProperties(Map<String, Object> properties) throws ScrumGraphException;

	private static final String NODE_GRAL_QUERY = "n";

	private static final String NODE_FATHER = "f";

	private static final String NODE_X_RELATED = "x";

	private static final String LBL_SOURCE = "label_source";

	private static final String ID_SOURCE = "id_source";

	protected static NodeTO convertTO(Node nodeBD, ELabels lbl) {
		NodeTO nodeTO = null;

		if (nodeBD == null) {
			return nodeTO;
		}

		nodeTO = new NodeTO();
		nodeTO.setId(nodeBD.getId());
		nodeTO.setLabelNode(lbl);
		nodeTO.setProperties(getProperties(nodeBD));

		switch (lbl) {
		case SPRINT:
			// Actualizar Propiedades
			SGUtil.updateDateProperty(ESprintProperties.INIT_DATE.getPropertyName(), nodeTO.getProperties());
			SGUtil.updateDateProperty(ESprintProperties.END_DATE.getPropertyName(), nodeTO.getProperties());
			break;
		case PBI:
			SGUtil.updateDateProperty(EPBIProperties.STARTED_AT.getPropertyName(), nodeTO.getProperties());
			SGUtil.updateDateProperty(EPBIProperties.COMPLETED_AT.getPropertyName(), nodeTO.getProperties());
			break;
		case TASK:
			SGUtil.updateDateProperty(ETaskProperties.STARTED_AT.getPropertyName(), nodeTO.getProperties());
			SGUtil.updateDateProperty(ETaskProperties.COMPLETED_AT.getPropertyName(), nodeTO.getProperties());
			break;
		default:
			break;

		}

		return nodeTO;
	}

	protected static RelationshipTO convertTO(RestRelationship relationBD) {
		RelationshipTO relationTO = null;

		if (relationBD == null) {
			return relationTO;
		}

		relationTO = new RelationshipTO();
		relationTO.setId(relationBD.getId());
		relationTO.setType(relationBD.getType().name());
		relationTO.setSourceId(relationBD.getStartNode().getId());
		relationTO.setTargetId(relationBD.getEndNode().getId());

		return relationTO;
	}

	private static GraphDatabaseService getGraphDbService() throws ScrumGraphException {
		return ConexionService.getInstance().getGraphDbService();
	}

	private static QueryEngine getEngine() throws ScrumGraphException {
		return ConexionService.getInstance().getEngine();
	}

	private static RestAPI getRestAPI() throws ScrumGraphException {
		return ConexionService.getInstance().getRestAPI();
	}

	private static Map<String, Object> getProperties(Node restNode) {
		Map<String, Object> propertiesNode = new HashMap<String, Object>();

		Iterable<String> properties = restNode.getPropertyKeys();
		if (properties != null) {
			Iterator iteProperties = properties.iterator();
			while (iteProperties.hasNext()) {
				String key = iteProperties.next().toString();
				Object value = restNode.getProperty(key);
				// Asignar Fecha General Nodos
				if (key.equalsIgnoreCase(ENodeProperties.CREATE_AT.getPropertyName()) || key.equalsIgnoreCase(ENodeProperties.LAST_MODIFICATION.getPropertyName())) {
					value = new Date(Long.valueOf(value.toString()));
					value = SGUtil.toStringDateFormat((Date) value);
				}
				propertiesNode.put(key, value);
			}
		}
		return propertiesNode;

	}

	private static Node createNodeBDWithRelationship(NodeTO nodeTO, Node nodeRelated, RelationshipType typeRelation, Direction direction) throws ScrumGraphException {
		Node node = null;

		Label lblNode = DynamicLabel.label(nodeTO.getLabelNode().getName());

		try (Transaction tx = getGraphDbService().beginTx()) {
			node = getGraphDbService().createNode();
			addProperties(nodeTO, node);
			node.addLabel(lblNode);
			if (nodeRelated != null && typeRelation != null && direction != null) {
				if (direction.equals(Direction.OUTGOING)) {
					node.createRelationshipTo(nodeRelated, typeRelation);
				} else {
					nodeRelated.createRelationshipTo(node, typeRelation);
				}
			}
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof IllegalStateException) {
				returnConstraintViolation(nodeTO.getLabelNode());
			}
			throw new ScrumGraphException(e.getMessage());
		}

		return node;
	}

	private static Node createNodeBD(NodeTO nodeTO) throws ScrumGraphException {
		Node node = null;
		String query = null;
		Map<String, Object> properties = nodeTO.getProperties();

		try {
			query = "CREATE (n:";
			query += nodeTO.getLabelNode().getName() + "";
			query += " { props } )";
			query += " RETURN n";
			Iterator<Map<String, Object>> iterator = executeQuerySearch(query, MapUtil.map("props", properties));
			if (iterator.hasNext()) {
				Map<String, Object> row = iterator.next();
				node = (Node) row.get("n");
			}
		} catch (Exception e) {
			returnConstraintViolation(nodeTO.getLabelNode());
		}

		return node;
	}

	private static void addProperties(NodeTO objNodeTO, Node nodeBD) {

		if (objNodeTO == null) {
			return;
		}

		Map<String, Object> properties = objNodeTO.getProperties();
		if (properties != null) {
			for (String key : properties.keySet()) {
				nodeBD.setProperty(key, properties.get(key));
			}
		}
	}

	protected static int countLabelNodes(ELabels lbl) throws ScrumGraphException {
		String query = null;
		Integer count = -1;

		query = "MATCH (n:" + lbl.getName() + ") RETURN count(*) as total";
		Iterator<Map<String, Object>> iterator = executeQuerySearch(query, Collections.EMPTY_MAP);
		if (iterator.hasNext()) {
			Map<String, Object> row = iterator.next();
			count = (Integer) row.get("total");
		}

		return count;

	}

	protected static Node createNodeWithRelationship(NodeTO objNodeTO, Node nodeRelated, ERelTypes relType, Direction direction) throws ScrumGraphException {
		Date fechaActual = new Date();
		Map<String, Object> properties = null;

		// asignar global properties
		properties = objNodeTO.getProperties();
		if (properties == null) {
			properties = new HashMap<String, Object>();
		}
		properties.put(ENodeProperties.CREATE_AT.getPropertyName(), fechaActual);
		properties.put(ENodeProperties.LAST_MODIFICATION.getPropertyName(), fechaActual);

		Node nodeBD = createNodeBDWithRelationship(objNodeTO, nodeRelated, relType, direction);
		objNodeTO.setId(nodeBD.getId());

		return nodeBD;
	}

	protected static Node createNode(NodeTO objNodeTO) throws ScrumGraphException {
		Date fechaActual = new Date();
		Map<String, Object> properties = null;

		// asignar global properties
		properties = objNodeTO.getProperties();
		if (properties == null) {
			properties = new HashMap<String, Object>();
		}
		properties.put(ENodeProperties.CREATE_AT.getPropertyName(), fechaActual);
		properties.put(ENodeProperties.LAST_MODIFICATION.getPropertyName(), fechaActual);

		Node nodeBD = createNodeBD(objNodeTO);
		objNodeTO.setId(nodeBD.getId());
		objNodeTO.getProperties().put(ENodeProperties.CREATE_AT.getPropertyName(), SGUtil.toStringDateFormat(fechaActual));

		return nodeBD;
	}

	protected static NodeTO searchById(Long idNode, ELabels enumLabel) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();
		NodeTO nodeTO = null;
		Iterator<Map<String, Object>> iterator = null;
		String label = null;

		// Validacion parametros
		if (idNode == null || idNode < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		if (enumLabel == null) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		label = enumLabel.getName();

		query = "MATCH (n";
		if (label != null && label.length() > 0) {
			query += ":" + label + ")";
		}
		query += " WHERE id(n) = {id} ";
		query += "RETURN n";

		params.put(ENodeProperties.ID.getPropertyName(), idNode);

		iterator = executeQuerySearch(query, params);

		if (iterator.hasNext()) {
			Map<String, Object> result = iterator.next();
			Node nodeBD = (Node) result.get(NODE_GRAL_QUERY);
			nodeTO = convertTO(nodeBD, enumLabel);
		}

		return nodeTO;

	}

	protected static NodeTO searchById(Long idNode, ELabels enumLabel, Direction direction) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();
		NodeTO nodeTO = null;
		Iterator<Map<String, Object>> iterator = null;
		String label = null;

		// Validacion parametros
		if (idNode == null || idNode < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		if (enumLabel == null) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		label = enumLabel.getName();

		query = "MATCH (n";
		if (label != null && label.length() > 0) {
			query += ":" + label + ")";
		}
		query += " WHERE id(n) = {id} ";
		query += " OPTIONAL MATCH (n)";
		switch (direction) {
		case INCOMING:
			query += "<-[]-(x)";
			break;
		case OUTGOING:
			query += "-[]->(x)";
			break;
		}

		query += " RETURN n , x , labels(x) as lblX";

		params.put(ENodeProperties.ID.getPropertyName(), idNode);

		iterator = executeQuerySearch(query, params);
		List<NodeTO> lstNodeRelated = new ArrayList<NodeTO>();
		Node nodeBD = null;
		while (iterator.hasNext()) {
			Map<String, Object> result = iterator.next();
			if (nodeBD == null) {
				nodeBD = (Node) result.get(NODE_GRAL_QUERY);
				nodeTO = convertTO(nodeBD, enumLabel);
			}
			Node nodeX = (Node) result.get(NODE_X_RELATED);
			if (nodeX != null) {
				List<String> lbl = (List<String>) result.get("lblX");
				lstNodeRelated.add(convertTO(nodeX, ELabels.valueOf(lbl.get(0))));
			}

		}

		if (nodeTO != null) {
			switch (direction) {
			case OUTGOING:
				nodeTO.setOutgoingNodes(lstNodeRelated);
				break;
			case INCOMING:
				nodeTO.setIncomingNodes(lstNodeRelated);
				break;
			}
		}

		return nodeTO;

	}

	protected static NodeTO searchById(Long idNode, ELabels enumLabel, Direction direction, ELabels labelRelated) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();
		NodeTO nodeTO = null;
		Iterator<Map<String, Object>> iterator = null;
		String label = null;

		// Validacion parametros
		if (idNode == null || idNode < 0) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		if (enumLabel == null || labelRelated == null) {
			throw new ScrumGraphException(ECodeExceptionSG.INVALID_PROPERTIES);
		}

		label = enumLabel.getName();

		query = "MATCH (n";
		query += ":" + label;
		query += ")";
		query += " WHERE id(n) = {id} ";
		query += "OPTIONAL MATCH (n)--(x:" + labelRelated.getName() + ")";
		query += " RETURN n , x";
		params.put(ENodeProperties.ID.getPropertyName(), idNode);

		iterator = executeQuerySearch(query, params);
		List<NodeTO> lstNodeRelated = new ArrayList<NodeTO>();
		Node nodeBD = null;
		while (iterator.hasNext()) {
			Map<String, Object> result = iterator.next();
			if (nodeBD == null) {
				nodeBD = (Node) result.get(NODE_GRAL_QUERY);
				nodeTO = convertTO(nodeBD, enumLabel);
			}
			Node nodeX = (Node) result.get(NODE_X_RELATED);
			if (nodeX != null) {
				lstNodeRelated.add(convertTO(nodeX, labelRelated));
			}
		}

		if (nodeTO != null) {
			switch (direction) {
			case OUTGOING:
				nodeTO.setOutgoingNodes(lstNodeRelated);
				break;
			case INCOMING:
				nodeTO.setIncomingNodes(lstNodeRelated);
				break;
			}
		}

		return nodeTO;

	}

	protected static Iterator<Map<String, Object>> executeQuerySearch(String query, Map<String, Object> params) throws ScrumGraphException {
		QueryResult<Map<String, Object>> result = getEngine().query(query, params);
		Iterator<Map<String, Object>> iterator = result.iterator();
		return iterator;
	}

	protected static List<Object> executeStatement(String query, Map<String, Object> params) throws ScrumGraphException {
		CypherResult cypherResult = getRestAPI().query(query, params);
		if (cypherResult != null) {
			LinkedHashMap<String, Object> resultMap = (LinkedHashMap<String, Object>) cypherResult.asMap();
			if (resultMap.containsKey("errors")) {
				List<Object> lstErros = (List<Object>) resultMap.get("errors");
				if (lstErros != null) {
					for (Object error : lstErros) {
						LinkedHashMap<String, Object> linkedError = (LinkedHashMap<String, Object>) error;
						String codeError = (String) linkedError.get("code");
						if (codeError.equalsIgnoreCase("Neo.ClientError.Schema.ConstraintViolation")) {
							throw new ConstraintViolationException((String) linkedError.get("message"));
						}

					}
				}
			}
			return (List<Object>) resultMap.get("data");
		}

		return null;

	}

	protected static List<NodeTO> getAllByLabel(ELabels enumLabel) throws ScrumGraphException {
		List<NodeTO> lstNodesTO = new ArrayList<NodeTO>();

		Iterable<RestNode> allNodesBD = getRestAPI().getNodesByLabel(enumLabel.getName());

		if (allNodesBD != null) {
			Iterator iteratorNodes = allNodesBD.iterator();
			while (iteratorNodes.hasNext()) {
				RestNode nodeBD = (RestNode) iteratorNodes.next();
				if (nodeBD != null) {
					lstNodesTO.add(convertTO(nodeBD, enumLabel));
				}
			}
		}

		return lstNodesTO;

	}

	protected static List<Object> updateNodeById(ELabels label, Long idNode, Map<String, Object> newProperties) throws ScrumGraphException {
		Map<String, Object> params = null;
		String query = null;

		try {
			params = new HashMap<String, Object>();

			// validacion createAt
			if (newProperties.containsKey(ENodeProperties.CREATE_AT.getPropertyName())) {
				newProperties.remove(ENodeProperties.CREATE_AT.getPropertyName());
			}

			query = getMatchLabel(label);
			query += " WHERE id(n) = " + idNode;
			query += " SET ";
			for (String keyProp : newProperties.keySet()) {
				query += "n." + keyProp + " = {" + keyProp + "}";
				query += ", ";
				params.put(keyProp, newProperties.get(keyProp));
			}
			query += " n." + ENodeProperties.LAST_MODIFICATION.getPropertyName() + " = timestamp()";
			query += " WITH n OPTIONAL MATCH (n)<--(t) RETURN n ,labels(t) as " + LBL_SOURCE + ",id(t) as " + ID_SOURCE;

			// Ejecutar la actualizaci�n
			return executeStatement(query, params);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ConstraintViolationException) {
				returnConstraintViolation(label);
			}
			throw new ScrumGraphException(e.getMessage());
		}

	}

	private static void returnConstraintViolation(ELabels label) throws ScrumGraphException {
		switch (label) {
		case USER:
			throw new ScrumGraphException(ECodeExceptionSG.EMAIL_ALREADY_EXISTS);
		case PROJECT:
			throw new ScrumGraphException(ECodeExceptionSG.PROJECT_ALREADY_EXISTS);
		case PBI:
			throw new ScrumGraphException(ECodeExceptionSG.PBI_ALREADY_EXISTS);
		case SPRINT:
			throw new ScrumGraphException(ECodeExceptionSG.SPRINT_ALREADY_EXISTS);
		case TASK:
			throw new ScrumGraphException(ECodeExceptionSG.TASK_ALREADY_EXISTS);
		default:
			throw new ScrumGraphException(ECodeExceptionSG.CONSTRAINT_VIOLATION);
		}

	}

	protected static List<Object> deleteNodeById(ELabels label, Long idNode) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();

		query = getMatchLabel(label);
		query += " WHERE id(n) = {id} " + "OPTIONAL MATCH (n)-[r]-() ";
		query += " WITH n,r " + " OPTIONAL MATCH (n)<--(t) ";
		query += " DELETE n ,r";
		query += " RETURN DISTINCT 0,labels(t) as " + LBL_SOURCE + ",id(t) as " + ID_SOURCE;
		params.put("id", idNode);

		return executeStatement(query, params);

	}

	private static String getMatchLabel(ELabels label) {
		String query = null;

		query = "MATCH (n:";
		query += label.getName() + ") ";

		return query;
	}

	protected static Map<String, Object> updateProperties(Map<String, Object> initProperties, Map<String, Object> newProperties) {

		if (newProperties == null || newProperties.isEmpty()) {
			return initProperties;
		}

		if (initProperties == null) {
			initProperties = new HashMap<String, Object>();
		}

		// Actualizacion Propiedades
		for (String keyNewProp : newProperties.keySet()) {
			initProperties.put(keyNewProp, newProperties.get(keyNewProp));
		}

		return initProperties;
	}

}
