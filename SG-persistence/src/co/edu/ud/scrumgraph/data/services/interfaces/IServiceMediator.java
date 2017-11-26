package co.edu.ud.scrumgraph.data.services.interfaces;

import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

public interface IServiceMediator {

	
	void notifyUpdate(ELabels lblSrv , Long idNode) throws ScrumGraphException;
}
