package co.edu.ud.scrumgraph.data.services;

import co.edu.ud.scrumgraph.data.enums.ELabels;
import co.edu.ud.scrumgraph.data.services.interfaces.IColleagueNodeSrvMediator;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceMediator;
import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

class ServiceMediator implements IServiceMediator {

	@Override
	public void notifyUpdate(ELabels lblSrv, Long idNode) throws ScrumGraphException {
		IColleagueNodeSrvMediator srvNode = null;

		// Validacion Objeto actualizado.
		if (idNode == null) {
			return;
		}

		switch (lblSrv) {
		case PROJECT:
			// Obtener interfaz de PBI
			srvNode = new ServiceProjectNode();
			break;
		case SPRINT:
			// Obtener interfaz de PBI
			srvNode = new ServiceSprintNode();
			break;
		case PBI:
			// Obtener interfaz de PBI
			srvNode = new ServicePBINode();
			break;
		}
		srvNode.refreshNode(idNode);
	}
}
