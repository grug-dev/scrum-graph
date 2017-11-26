package co.edu.ud.scrumgraph.data.services.interfaces;

import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public interface IColleagueNodeSrvMediator {
	
	
	void notifyUpdate(String label , Long idNode) throws ScrumGraphException;
	
	void refreshNode(Long idNode) throws ScrumGraphException;
	
}
