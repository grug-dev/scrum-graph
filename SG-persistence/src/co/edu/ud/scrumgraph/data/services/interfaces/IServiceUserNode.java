package co.edu.ud.scrumgraph.data.services.interfaces;

import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public interface IServiceUserNode {

	NodeTO createUser(String authToken, Map<String, Object> properties) throws ScrumGraphException;

	NodeTO validateAuthentication(String email, String password) throws ScrumGraphException;

	NodeTO updateUser(String authToken, Long idUserToUpdate, Map<String, Object> newProperties) throws ScrumGraphException;

	NodeTO getUserById(String authToken, Long userId, boolean validateIsAdmin) throws ScrumGraphException;

	List<NodeTO> getAllUsers(String authToken) throws ScrumGraphException;

	NodeTO validateGetUserByAuthToken(String authToken, boolean validateIsAdmin) throws ScrumGraphException;

	void isUserAvailable(NodeTO userTO) throws ScrumGraphException;

	NodeTO getProjectsByIdUser(String authToken, Long idUser) throws ScrumGraphException;

	void assignProjectToUser(String authToken, Long idUser, Long idProject) throws ScrumGraphException;

	void deleteUser(String authToken, Long idUserToDelete) throws ScrumGraphException;
	
	/**
	 * Desasignar un usuario de un proyecto. 
	 * @param authToken Token de autenticación del usuario
	 * @param idUser Identificador del usuario
	 * @param idProject Identificador del proyecto
	 * @throws ScrumGraphException Excepción de validación o de deasignación del usuario.
	 */
	void deleteUserFromProject(String authToken , Long idUser , Long idProject) throws ScrumGraphException;

}
