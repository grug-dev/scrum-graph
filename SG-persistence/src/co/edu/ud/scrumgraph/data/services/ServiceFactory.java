package co.edu.ud.scrumgraph.data.services;

import co.edu.ud.scrumgraph.data.services.interfaces.IServicePBINode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceProjectNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceSprintNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceTaskNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceUserNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServicesFactory;

public class ServiceFactory implements IServicesFactory {

	@Override
	public IServiceUserNode getUserNodeService() {
		return new ServiceUserNode();
	}

	@Override
	public IServiceProjectNode getProjectNodeService() {
		return new ServiceProjectNode();
	}

	@Override
	public IServicePBINode getPBINodeService() {
		return new ServicePBINode();
	}

	@Override
	public IServiceSprintNode getSprintNodeService() {
		return new ServiceSprintNode();
	}

	@Override
	public IServiceTaskNode getTaskNodeService() {
		return new ServiceTaskNode();
	}

}
