package co.edu.ud.scrumgraph.data.services.interfaces;

public interface IServicesFactory {

	
	IServiceUserNode getUserNodeService();
	
	IServiceProjectNode getProjectNodeService();
	
	IServicePBINode getPBINodeService();
	
	IServiceSprintNode getSprintNodeService();
	
	IServiceTaskNode getTaskNodeService();
	
}
