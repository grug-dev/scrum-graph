package co.edu.ud.scrumgraph.logica.delegados;

import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.services.ServiceFactory;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceUserNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServicesFactory;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;
import co.edu.ud.scrumgraph.logica.serviciosrest.ISrvSGRest;

public class DelegateUser extends DelegateService  {

	private IServiceUserNode userService;

	protected IServiceUserNode getUserService() {
		if (userService == null) {
			IServicesFactory srvFactory = new ServiceFactory();
			userService = srvFactory.getUserNodeService();
		}

		return userService;
	}

	public DelegateUser(ISrvSGRest serviceRest) {
		super(serviceRest);
	}

	public DelegateUser(IDelegateService delegateService) {
		super(delegateService);
	}

	public void deleteUser(String authToken, Long idUser) throws ScrumGraphException {
		getUserService().deleteUser(authToken, idUser);

	}

	public NodeTO createUser(String authToken, Map<String, Object> userMap) throws ScrumGraphException {
		return getUserService().createUser(authToken, userMap);
	}

	public NodeTO validateAuthentication(String email, String password) throws ScrumGraphException {
		return getUserService().validateAuthentication(email, password);
	}

	public NodeTO updateUser(String authToken, Long userIdToUpdate, Map<String, Object> newPropUser) throws ScrumGraphException {
		return getUserService().updateUser(authToken, userIdToUpdate, newPropUser);
	}

	public NodeTO getUserById(String authToken, Long userId, boolean validateIsAdmin) throws ScrumGraphException {
		return getUserService().getUserById(authToken, userId, validateIsAdmin);
	}

	public List<NodeTO> getAllUsers(String authToken) throws ScrumGraphException {
		return getUserService().getAllUsers(authToken);
	}

	public NodeTO validateGetUserByAuthToken(String authToken, boolean validateIsAdmin) throws ScrumGraphException {
		NodeTO userTO = null;

		/*
		userTO = getUserService().validateGetUserByAuthToken(authToken, validateIsAdmin);
		getUserService().isUserAvailable(userTO);
		*/

		return userTO;
	}
	
	public NodeTO verifyToken(String authToken, boolean validateIsAdmin) throws ScrumGraphException {
		NodeTO userTO = null;

		userTO = getUserService().validateGetUserByAuthToken(authToken, validateIsAdmin);
		getUserService().isUserAvailable(userTO);
		return userTO;
	}

	public NodeTO getProjectsByIdUser(String authToken, Long idUser) throws ScrumGraphException {
		return getUserService().getProjectsByIdUser(authToken, idUser);
	}

	public void assignProjectToUser(String authToken, Long idUser, Long idProject) throws ScrumGraphException {
		getUserService().assignProjectToUser(authToken, idUser, idProject);
	}
	
	public void deleteUserFromProject(String authToken, Long idUser, Long idProject) throws ScrumGraphException {
		getUserService().deleteUserFromProject(authToken, idUser, idProject);
	}

	@Override
	protected IServiceNode getServiceNode() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
