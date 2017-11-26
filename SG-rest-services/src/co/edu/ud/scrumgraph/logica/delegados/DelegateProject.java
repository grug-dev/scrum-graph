package co.edu.ud.scrumgraph.logica.delegados;

import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.GraphTO;
import co.edu.ud.scrumgraph.data.dto.IndicadoresTO;
import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceProjectNode;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.serviciosrest.ISrvSGRest;

public class DelegateProject extends DelegateService {

	private IServiceProjectNode projectService;

	public DelegateProject(ISrvSGRest serviceRest) {
		super(serviceRest);
	}

	@Override
	protected IServiceProjectNode getServiceNode() {
		if (projectService == null) {
			projectService = srvFactory.getProjectNodeService();
		}

		return projectService;
	}

	public NodeTO createProject(String authToken, Map<String, Object> projProperties) throws ScrumGraphException {

		// Validacion proyecto isAdmin
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().createNode(projProperties);
	}

	public NodeTO getProjectById(String authToken,Long projectId) throws ScrumGraphException {

		// Validacion authTOken
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getNodeById(projectId);
	}

	public List<NodeTO> getAllProjects(String authToken) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getAllNodes();
	}

	public NodeTO updateProject(String authToken, Long idProject, Map<String, Object> newProperties) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		return getServiceNode().updateNode(idProject, newProperties);
	}

	public void deleteProject(String authToken, Long idProject) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().deleteNode(idProject);

	}

	public void assignUserToProject(String authToken, Long idUser, Long idProject) throws ScrumGraphException {
		getUserDelegate().assignProjectToUser(authToken, idUser, idProject);
	}

	public NodeTO getUsersByProject(String authToken, Long idProject) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getUsersByProject(idProject);
	}
	
	public NodeTO getSprintsByProject(String authToken, Long idProject) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getSprintsByProject(idProject);
	}
	
	public NodeTO getPBIUnAssignedByProject(String authToken, Long idProject) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getPBIUnAssignedByProject(idProject);
	}

	/**
	 * Valida el code pasado como parámetro
	 * @param authToken Token del usuario autenticado
	 * @param code Code a validar
	 * @throws ScrumGraphException Excepción de validación
	 */
	public void validateCode(String authToken, String code) throws ScrumGraphException {

		// Validar el token usuario autenticado
		getUserDelegate().validateGetUserByAuthToken(authToken, true);

		getServiceNode().validateCode(code);

	}

	public GraphTO getGraphProject(String authToken, Long idProject) throws ScrumGraphException {

		// Validacion authToken
		// Validacion si es admin o no el usuario.
		getUserDelegate().validateGetUserByAuthToken(authToken, false);

		return getServiceNode().getGraphProject(idProject);
	}

	/**
	 * Asigna un nodo pbi a un proyecto, basado en los atributos
	 * pasados como parámetros.
	 * @param authToken Token del usuario autenticado
	 * @param pbiProperties Propiedades del PBI a crear
	 * @param idProject Identificador del Proyecto.
	 * @throws ScrumGraphException Excepción al realizar la asignación.
	 */
	public NodeTO assignPBIToProject(String authToken, Map<String,Object> pbiProperties, Long idProject) throws Exception {
		NodeTO pbiTO = null;
		
		getPBIDelegate().validateProperties(pbiProperties);
		
		pbiTO = getPBIDelegate().createPBI(authToken, pbiProperties);
		
		getPBIDelegate().assignPBIToProject(authToken, pbiTO.getId(), idProject);
		
		return pbiTO;
		
	}
	
	/**
	 * Asigna un nodo pbi a un proyecto, basado en los atributos
	 * pasados como parámetros.
	 * @param authToken Token del usuario autenticado
	 * @param sprintProperties Propiedades del Sprint a crear
	 * @param idProject Identificador del Proyecto.
	 * @throws ScrumGraphException Excepción al realizar la asignación.
	 */
	public NodeTO assignSprintToProject(String authToken, Map<String,Object> sprintProperties, Long idProject) throws Exception {
		NodeTO sprintTO = null;
		
		getSprintDelegate().validateProperties(sprintProperties);
		
		sprintTO = getSprintDelegate().createSprint(authToken, sprintProperties);
		
		getSprintDelegate().assignSprintToProject(authToken, sprintTO.getId(), idProject);
		
		return sprintTO;
		
	}
	
	public IndicadoresTO getIndicators(Long idProject) throws Exception {
		return getServiceNode().getIndicadores(idProject);
	}
	

}
