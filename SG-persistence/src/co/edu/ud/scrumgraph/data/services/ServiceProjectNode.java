package co.edu.ud.scrumgraph.data.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.rest.graphdb.entity.RestRelationship;

import co.edu.ud.scrumgraph.data.api.NodeAPI;
import co.edu.ud.scrumgraph.data.dto.GraphTO;
import co.edu.ud.scrumgraph.data.dto.IndicadoresTO;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.dto.RelationshipTO;
import co.edu.ud.scrumgraph.data.dto.StatsTO;
import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;
import co.edu.ud.scrumgraph.data.enums.EDefaultStateNodes;
import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.enums.EProjectProperties;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.enums.ETeamProperties;
import co.edu.ud.scrumgraph.data.services.interfaces.IColleagueNodeSrvMediator;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceProjectNode;
import co.edu.ud.scrumgraph.data.util.SGUtil;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

class ServiceProjectNode extends NodeAPI implements IServiceProjectNode , IColleagueNodeSrvMediator {

	protected void validateProperties(Map<String, Object> properties) throws ScrumGraphException {
		Object propertyObj = null;
		String strProperty = null;

		// Validación Nulidad
		SGUtil.validateProperties(properties);

		// Validacion propiedades
		List<EProjectProperties> propToValid = new ArrayList<EProjectProperties>();
		propToValid.add(EProjectProperties.CODE);

		for (EProjectProperties property : propToValid) {
			propertyObj = properties.get(property.getPropertyName());
			if (propertyObj == null) {
				continue;
			}
			strProperty = propertyObj.toString();
			switch (property) {
			case CODE:
				validateCode(strProperty, false);
				break;
			}
		}
	}

	/**
	 * Método para validar la propiedad code.
	 * 
	 * @param code  String code a verificar su existencia
	 * @param validateExists  boolean para indicar si se valida la existencia
	 * del code en base de datos.
	 * @throws ScrumGraphException
	 */
	private void validateCode(String code, boolean validateExists) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = new HashMap<String, Object>();
		Iterator<Map<String, Object>> resultQuery = null;
		int lenght = -1;

		if (code == null) {
			throw new ScrumGraphException(ECodeExceptionSG.PROJECT_CODE_INVALID.getCode(), ECodeExceptionSG.PROJECT_CODE_INVALID.getMessage());
		}

		lenght = code.length();
		if (!(lenght >= 1 && lenght <= 8)) {
			throw new ScrumGraphException(ECodeExceptionSG.PROJECT_CODE_INVALID.getCode(), ECodeExceptionSG.PROJECT_CODE_INVALID.getMessage());
		}

		if (validateExists) {
			query = "MATCH (p:PROJECT) WHERE lower(p." + EProjectProperties.CODE.getPropertyName() + ")";
			query += "={code} ";
			query += " RETURN 1";
			params.put("code", code.toLowerCase());

			resultQuery = executeQuerySearch(query, params);

			if (resultQuery.hasNext()) {
				throw new ScrumGraphException(ECodeExceptionSG.PROJECT_ALREADY_EXISTS);
			}
		}

	}

	/**
	 * M�todo que retorna el <i>code</i> del siguiente proyecto a crear.
	 * 
	 * @param count
	 *            Total de nodos de proyectos.
	 * @return String Nombre del pr�ximo <i>code</i> del proyecto.
	 */
	private String getNextProjectCode(int count) {
		return CODE_PROJECT + count;
	}

	/**
	 * M�todo para obtener el nombre del nodo <i>Team</i> basado en el code del
	 * proyecto pasado como par�metro o de un nombre sugerido en la creci�n del
	 * proyecto.
	 * 
	 * @param name
	 *            Nombre del team asignado por el proyecto.
	 * @param codeProject
	 *            codigo del proyecto.
	 * @return String nombre del nodo <i>Team</i>.
	 */
	private String getNameTeam(Object name, String codeProject) {

		if (name == null) {
			return CODE_TEAM + codeProject;
		}

		return name.toString();

	}

	/**
	 * Método para crear un nodo de tipo proyectos. La creaci�n del nodo se basa
	 * en las propiedades pasadas c�mo parametros.
	 * 
	 * @param properties
	 *            Map<String, Object> Mapa de propiedades a crear al nodo.
	 * @return NodeTO Objeto con la informaci�n del nodo creado.
	 * @throws ScrumGraphException
	 */
	public NodeTO createNode(Map<String, Object> properties) throws ScrumGraphException {
		NodeTO projectTO = null;
		int count = 0;
		Node nodeProjBD = null;
		String teamName = null;

		// Validacion Properties
		validateProperties(properties);

		// Obtener Nombre Team
		String codeProject = properties.get(EProjectProperties.CODE.getPropertyName()).toString();
		teamName = getNameTeam(properties.get(NAME_TEAM_ON_PROJECT), codeProject);
		properties.remove(NAME_TEAM_ON_PROJECT);

		// Crear Nodo Proyectos
		properties.put(EProjectProperties.IS_CLOSED.getPropertyName(), false);
		properties.put(EProjectProperties.NUM_PBI_DONE.getPropertyName(), 0);
		properties.put(EProjectProperties.TOTAL_PBI.getPropertyName(), 0);
		projectTO = new NodeTO();
		projectTO.setLabelNode(ELabels.PROJECT);
		projectTO.setProperties(properties);
		nodeProjBD = createNode(projectTO);

		// Crear Nodo TEAM
		NodeTO nodeTeamTO = new NodeTO();
		Map<String, Object> mapTeamProp = new HashMap<>();
		mapTeamProp.put(ETeamProperties.NAME.getPropertyName(), teamName);
		nodeTeamTO.setProperties(mapTeamProp);
		nodeTeamTO.setLabelNode(ELabels.TEAM);
		createNodeWithRelationship(nodeTeamTO, nodeProjBD, REL_TYPE_TEAM, Direction.OUTGOING);

		// Buscar siguiente code project
		count = countLabelNodes(ELabels.PROJECT);
		// Asignar propiedades no almacenadas en el nodo proyecto
		projectTO.getProperties().put(NAME_TEAM_ON_PROJECT, teamName);
		projectTO.getProperties().put(NEXT_PROJECT_CODE, getNextProjectCode(++count));

		return projectTO;

	}

	/**
	 * M�todo para obtener un proyecto por medio de su id. Adem�s, obtiene los
	 * nodos con los cuales est�n relacionados dependiendo del valor de los
	 * booleanos pasados como parametros.
	 * 
	 * @param projectId
	 *            Identificador del proyecto a buscar.
	 * @return NodeTO Nodo con la informaci�n del proyecto.
	 */
	public NodeTO getNodeById(Long projectId) throws ScrumGraphException {
		NodeTO projectTO = null;
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		String teamName = null;

		// Se busca el proyecto
		query = "MATCH (p:PROJECT)--(t:TEAM) WHERE id(p) = {id} RETURN p,t.name as " + NAME_TEAM_ON_PROJECT;
		resultQuery = executeQuerySearch(query, MapUtil.map("id", projectId));

		if (resultQuery != null) {
			if (resultQuery.hasNext()) {
				Map<String, Object> result = resultQuery.next();
				Node nodeBD = (Node) result.get("p");
				projectTO = convertTO(nodeBD , ELabels.PROJECT);
				teamName = result.get(NAME_TEAM_ON_PROJECT).toString();
				projectTO.getProperties().put(NAME_TEAM_ON_PROJECT, teamName);
			}
		}

		if (projectTO == null) {
			throw new ScrumGraphException(ECodeExceptionSG.PROJECT_DOESNT_EXISTS);
		}

		return projectTO;
	}

	/**
	 * Método que retorna todos los proyectos existentes.
	 * 
	 * @return List<NodeTO> Lista con los nodos de los proyectos.
	 */
	public List<NodeTO> getAllNodes() throws ScrumGraphException {
		List<NodeTO> allProjects = null;
		NodeTO projectTO = null;
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		String teamName = null;

		// Consultar todos los proyectos.
		query = "MATCH (p:PROJECT)--(t:TEAM) RETURN p,t.name as " + NAME_TEAM_ON_PROJECT;
		resultQuery = executeQuerySearch(query, Collections.EMPTY_MAP);

		if (resultQuery != null) {
			allProjects = new ArrayList<NodeTO>();
			while (resultQuery.hasNext()) {
				Map<String, Object> result = resultQuery.next();
				Node nodeBD = (Node) result.get("p");
				projectTO = convertTO(nodeBD , ELabels.PROJECT);
				teamName = result.get(NAME_TEAM_ON_PROJECT).toString();
				projectTO.getProperties().put(NAME_TEAM_ON_PROJECT, teamName);
				allProjects.add(projectTO);
			}
		}

		return allProjects;
	}

	/**
	 * M�todo para actualizar un proyecto en BDOG
	 * 
	 * @param idProjectToUpdate
	 *            Identificador del proyecto a actualizar
	 * @param newProperties
	 *            Map<String, Object> Mapa con las propiedades a actualizar o
	 *            agregar si no existen
	 * @return NodeTO Objeto TO con la informaci�n del proyecto actualizado.
	 * @throws ScrumGraphException
	 *             Excecpi�n de validaci�n
	 */
	public NodeTO updateNode(Long idProjectToUpdate, Map<String, Object> newProperties) throws ScrumGraphException {
		NodeTO projectTO = null;
		String teamName = null;

		// Validacion existencia proyecto
		projectTO = getNodeById(idProjectToUpdate);

		// Validacion Nuevas Propiedades
		validateProperties(newProperties);

		// Actualizar Team Name
		if (newProperties.containsKey(NAME_TEAM_ON_PROJECT)) {
			teamName = getNameTeam(newProperties.get(NAME_TEAM_ON_PROJECT), projectTO.getProperties().get(EProjectProperties.CODE.getPropertyName()).toString());
			newProperties.put(NAME_TEAM_ON_PROJECT, null);
			String query = "MATCH (p:PROJECT)--(t:TEAM) WHERE id(p) = {id} SET t.name = {teamName}";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", projectTO.getId());
			params.put("teamName", teamName);
			executeStatement(query, params);
		}

		// Actualizar Proyecto
		updateNodeById(ELabels.PROJECT, idProjectToUpdate, newProperties);

		projectTO = getNodeById(idProjectToUpdate);

		return projectTO;

	}

	/**
	 * Método para eliminar un proyecto en BDOG.
	 * 
	 * @param idProject
	 *            Identificador del proyecto a eliminar
	 * @throws ScrumGraphException
	 *             Excecpi�n de validaci�n
	 */
	public void deleteNode(Long idProject) throws ScrumGraphException {

		// Validar y obtener proyecto a actualizar
		getNodeById(idProject);

		// Eliminaci�n proyecto
		deleteNodeById(ELabels.PROJECT, idProject);

	}

	/**
	 * Servicio usado para obtener listado de todos los usuarios que pertenencen
	 * a un proyecto espec�fico.
	 * 
	 * @param idProject
	 *            Identificador del proyecto a consultar los usuarios.
	 * @return List<NodeTO> listado de usuarios
	 * @throws ScrumGraphException
	 */
	public NodeTO getUsersByProject(Long idProject) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		NodeTO proyectoTO = null;
		List<NodeTO> lstUsers = new ArrayList<NodeTO>();

		// Validacion Usuario Autenticado.
		// Validacion Existencia Usuario
		proyectoTO = getNodeById(idProject);

		// Obtener proyectos por usuario
		query = "MATCH (u:USER)-[]->(t:TEAM)-[]-(p:PROJECT) WHERE id(p) = {id} RETURN u";
		params = new HashMap<>();
		params.put("id", idProject);

		// Ejecutar consulta
		// Obtener Proyectos
		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);

		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				Node userBD = (Node) mapProjects.get("u");
				if (userBD != null) {
					NodeTO userTO = convertTO(userBD, ELabels.PROJECT);
					lstUsers.add(userTO);
				}
			}
		}

		// Asignando los proyectos a la relacion outgoing del nodo usuario
		proyectoTO.setIncomingNodes(lstUsers);

		return proyectoTO;
	}

	/**
	 * Método para validar el code pasado como parámetro.
	 * 
	 * @param code String code a validar
	 * @throws ScrumGraphException  Excepción lanzada por alguna validación no cumplida.
	 */
	public void validateCode(String code) throws ScrumGraphException {
		// Validar Codigo
		validateCode(code, true);
	}

	public GraphTO getGraphProject(Long idProject) throws ScrumGraphException {
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		GraphTO graphTO = null;
		NodeTO projectTO = null;

		// Validacion Proyecto
		projectTO = getNodeById(idProject);

		Set<NodeTO> nodesGraph = new HashSet<NodeTO>();
		Set<RelationshipTO> links = new HashSet<RelationshipTO>();

		// Consulta
		query = "MATCH (p:PROJECT)-[rps]-(s:SPRINT) ";
		query += "WHERE id(p) = {id}";
		query += " OPTIONAL MATCH (s)-[rspbi]-(pbi:PBI)";
		query += " WHERE pbi." + EPBIProperties.AVAILABLE.getPropertyName() + "=true";
		query += " OPTIONAL MATCH (pbi)-[rpbit]-(t:TASK)";
		query += " RETURN s,pbi,t , rps , rspbi , rpbit";

		resultQuery = executeQuerySearch(query, MapUtil.map("id", idProject));

		if (resultQuery != null) {
			graphTO = new GraphTO();
			while (resultQuery.hasNext()) {
				Map<String, Object> result = resultQuery.next();
				Node nodeSprintBD = (Node) result.get("s");
				Node nodePBIBD = (Node) result.get("pbi");
				Node nodeTaskBD = (Node) result.get("t");
				RestRelationship relationPS = (RestRelationship) result.get("rps");
				RestRelationship relationSPBI = (RestRelationship) result.get("rspbi");
				RestRelationship relationPBIT = (RestRelationship) result.get("rpbit");

				nodesGraph.add(convertTO(nodeSprintBD, ELabels.SPRINT));
				nodesGraph.add(convertTO(nodePBIBD , ELabels.PBI));
				nodesGraph.add(convertTO(nodeTaskBD , ELabels.TASK));

				links.add(convertTO(relationPS));
				links.add(convertTO(relationSPBI));
				links.add(convertTO(relationPBIT));

			}

			graphTO.getNodes().add(projectTO);
			graphTO.getNodes().addAll(nodesGraph);
			graphTO.getLinks().addAll(links);

		}

		return graphTO;

	}

	@Override
	public void notifyUpdate(String label , Long idNode) {
		
	}

	@Override
	public void refreshNode(Long idNode)  throws ScrumGraphException {
		NodeTO projectTO = null;
		int contPBIDone = 0 , contSprintDone = 0;
		double velProj = 0.0d;
		double velPtosHourProj = 0.0d;
		
		// Consultar Nodo a refrescar
		projectTO = searchById(idNode, ELabels.PROJECT,Direction.OUTGOING);
		
		//Refrescar el nodo sprint
		if (projectTO != null) {
			Map<String,Object> properties = new HashMap<>();
			List<NodeTO> lstOutNodes = projectTO.getOutgoingNodes();
			
			if (lstOutNodes != null && !lstOutNodes.isEmpty()) {
				List<NodeTO> lstPBIS = new ArrayList<NodeTO>();
				List<NodeTO> lstSprints = new ArrayList<NodeTO>();
				// Recorrer los PBI asociados al sprint.
				for (NodeTO nodeOutGoing : lstOutNodes) {
					Map<String,Object> nodeOutProp = nodeOutGoing.getProperties();
					Object  objStatus = nodeOutProp.get(EPBIProperties.STATUS.getPropertyName());
					switch(nodeOutGoing.getLabelNode()) {
					case PBI:
						// Actualizar Numero PBIS en estado DONE
						if (objStatus != null) {
							if (EDefaultStateNodes.DONE.getName().toString().equals(objStatus.toString())) {
								contPBIDone++;
							}
						}
						lstPBIS.add(nodeOutGoing);
						break;
					case SPRINT:
						if (objStatus != null) {
							if (EDefaultStateNodes.DONE.getName().toString().equals(objStatus.toString())) {
								contSprintDone++;
								Object vel = nodeOutProp.get(ESprintProperties.VELOCITY.getPropertyName());
								if (vel != null) {
									velProj += Double.parseDouble(vel.toString());
								}
								Object velPtosHora = nodeOutProp.get(ESprintProperties.VEL_POINTS_BY_HOUR.getPropertyName());
								if (velPtosHora != null) {
									velPtosHourProj += Double.parseDouble(velPtosHora.toString());
								}
							}
						}
						lstSprints.add(nodeOutGoing);
						break;
					}
				}
				// Calcular Velocidad Actual Proyecto.
				if (contSprintDone != 0) {
					velProj = new BigDecimal(velProj).divide(new BigDecimal(contSprintDone),2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
					velPtosHourProj = new BigDecimal(velPtosHourProj).divide(new BigDecimal(contSprintDone),2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
				}
				
				
				properties.put(EProjectProperties.TOTAL_PBI.getPropertyName(), lstPBIS.size());
				properties.put(EProjectProperties.NUM_PBI_DONE.getPropertyName(), contPBIDone);
				properties.put(EProjectProperties.VELOCITY.getPropertyName(), velProj);
				properties.put(EProjectProperties.VEL_POINTS_BY_HOUR.getPropertyName(), velPtosHourProj);
			}
			else {
				properties.put(EProjectProperties.TOTAL_PBI.getPropertyName(), 0);
				properties.put(EProjectProperties.VELOCITY.getPropertyName(), 0);
				properties.put(EProjectProperties.VEL_POINTS_BY_HOUR.getPropertyName(), 0);
				properties.put(EProjectProperties.NUM_PBI_DONE.getPropertyName(), contPBIDone);
			}
			// Actualizar Nodo
			updateNodeById(ELabels.PROJECT, idNode, properties);
		}
	}
	

	@Override
	public NodeTO getSprintsByProject(Long idProject) throws ScrumGraphException {
		String query = null;
		Map<String, Object> params = null;
		NodeTO proyectoTO = null;
		List<NodeTO> lstSprints = new ArrayList<NodeTO>();

		// Validacion Usuario Autenticado.
		// Validacion Existencia Usuario
		proyectoTO = getNodeById(idProject);

		// Obtener proyectos por usuario
		query = "MATCH (s:SPRINT)-[]-(p:PROJECT) WHERE id(p) = {id} RETURN s";
		params = new HashMap<>();
		params.put("id", idProject);

		// Ejecutar consulta
		// Obtener Proyectos
		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);

		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				Node sprintBD = (Node) mapProjects.get("s");
				if (sprintBD != null) {
					NodeTO sprintTO = convertTO(sprintBD, ELabels.SPRINT);
					lstSprints.add(sprintTO);
				}
			}
		}

		// Asignando los proyectos a la relacion outgoing del nodo usuario
		proyectoTO.setOutgoingNodes(lstSprints);

		return proyectoTO;
	
	}

	@Override
	public NodeTO getPBIUnAssignedByProject(Long idProject) throws ScrumGraphException {

		String query = null;
		Map<String, Object> params = null;
		NodeTO proyectoTO = null;
		List<NodeTO> lstPBIS = new ArrayList<NodeTO>();

		// Validacion Usuario Autenticado.
		// Validacion Existencia Usuario
		proyectoTO = getNodeById(idProject);

		// Obtener proyectos por usuario
		query = "MATCH (pbi:PBI)<--(p:PROJECT) WHERE NOT (pbi)-[:WORKS_ON]-() AND id(p)={id} RETURN pbi";
		params = new HashMap<>();
		params.put("id", idProject);

		// Ejecutar consulta
		// Obtener Proyectos
		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);

		if (resultQuery != null) {
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				Node pbiBD = (Node) mapProjects.get("pbi");
				if (pbiBD != null) {
					NodeTO pbiTO = convertTO(pbiBD , ELabels.PBI);
					lstPBIS.add(pbiTO);
				}
			}
		}

		// Asignando los proyectos a la relacion outgoing del nodo usuario
		proyectoTO.setOutgoingNodes(lstPBIS);

		return proyectoTO;
	
	
	}

	private Object getCountPBIUnassigned(Long idProject) throws ScrumGraphException {
		Object totalPBIUn = null;
		String query = null;
		Map<String, Object> params = null;

		// Obtener proyectos por usuario
		query = "MATCH (pbi:PBI)<--(p:PROJECT) WHERE NOT (pbi)-[:WORKS_ON]-() AND id(p)={id} RETURN count(pbi) as total";
		params = new HashMap<>();
		params.put("id", idProject);

		// Ejecutar consulta
		// Obtener Proyectos
		Iterator<Map<String, Object>> resultQuery = executeQuerySearch(query, params);

		if (resultQuery != null) {
			if (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				 totalPBIUn = (Object) mapProjects.get("total");
			}
		}
		return totalPBIUn;
	}
	
	public Map<String, Integer> getStatusGroupBy(Long idProject) throws ScrumGraphException {
		Map<String, Integer> statusMap = null;
		String query = null;
		Iterator<Map<String, Object>> resultQuery = null;
		
		//Agrupar Task
		query = "MATCH (pbi:PBI)--(p:PROJECT)  WHERE id(p)={id} RETURN distinct(pbi.status) as status , sum(pbi.velocity) as cant";
		resultQuery = executeQuerySearch(query,MapUtil.map("id",idProject));
		if (resultQuery != null) {
			statusMap = new HashMap<String,Integer>();
			while (resultQuery.hasNext()) {
				Map<String, Object> mapProjects = resultQuery.next();
				if (mapProjects.containsKey("status")) {
					String s = mapProjects.get("status").toString();
					String cant = mapProjects.get("cant").toString(); 
						 statusMap.put(s, Integer.parseInt(cant));
					}
				}
			}
		
		return statusMap;
	}
	
	@Override
	public IndicadoresTO getIndicadores(Long idProject) throws ScrumGraphException {
		NodeTO projectTO = null;
		IndicadoresTO indicadoresTO = new IndicadoresTO();
		Map<String, Integer> statusMap = null;
		
		projectTO = getSprintsByProject(idProject);
		indicadoresTO.setNodeTO(projectTO);
		
		int countPtosDone = 0;
		int countPtosTotal = 0;
		statusMap = getStatusGroupBy(idProject);
		if (statusMap != null) {
			Integer pts = statusMap.get(EDefaultStateNodes.DONE.getName());
			if (pts != null) {
				countPtosDone = pts.intValue();
				countPtosTotal += pts.intValue();
			}
			
			pts = statusMap.get(EDefaultStateNodes.TODO.getName());
			if (pts != null) {
				countPtosTotal += pts.intValue();
			}
			
			pts = statusMap.get(EDefaultStateNodes.WIP.getName());
			if (pts != null) {
				countPtosTotal += pts.intValue();
			}
		}
		
		// Stats
		StatsTO stat = new StatsTO();
		stat.setLabel("NO PBIS");
		stat.setValue(projectTO.getProperties().get(EProjectProperties.TOTAL_PBI.getPropertyName()));
		indicadoresTO.getLstStats().add(stat);
		
		stat = stat.clone();
		stat.setLabel("NO SPRINTS");
		stat.setValue(projectTO.getOutgoingNodes().size());
		indicadoresTO.getLstStats().add(stat);
		
		stat = stat.clone();
		stat.setLabel("NO PBI FINALIZADOS");
		stat.setValue(projectTO.getProperties().get(EProjectProperties.NUM_PBI_DONE.getPropertyName()));
		indicadoresTO.getLstStats().add(stat);
		
		stat = stat.clone();
		stat.setLabel("VELOCIDAD MEDIA");
		stat.setValue(projectTO.getProperties().get(EProjectProperties.VELOCITY.getPropertyName()));
		indicadoresTO.getLstStats().add(stat);
		
		stat = stat.clone();
		stat.setLabel("VEL HORAS/PTOS");
		stat.setValue(projectTO.getProperties().get(EProjectProperties.VEL_POINTS_BY_HOUR.getPropertyName()));
		indicadoresTO.getLstStats().add(stat);
		
		
		stat = stat.clone();
		stat.setLabel("NO PBI EN BACKLOG");
		stat.setValue(getCountPBIUnassigned(idProject));
		indicadoresTO.getLstStats().add(stat);
		
		stat = stat.clone();
		stat.setLabel("PUNTOS TOTAL");
		stat.setValue(countPtosTotal);
		indicadoresTO.getLstStats().add(stat);
		
		stat = stat.clone();
		stat.setLabel("PUNTOS COMPLETADOS");
		stat.setValue(countPtosDone);
		indicadoresTO.getLstStats().add(stat);
		
		
		return indicadoresTO;
		
	}

}
