package co.edu.ud.scrumgraph.logica.delegados;

import co.edu.ud.scrumgraph.data.services.ServiceFactory;
import co.edu.ud.scrumgraph.data.services.interfaces.IServiceNode;
import co.edu.ud.scrumgraph.data.services.interfaces.IServicesFactory;
import co.edu.ud.scrumgraph.logica.serviciosrest.ISrvSGRest;

public abstract class DelegateService implements IDelegateService  {
	
	/**
	 * Delegado de PBI
	 */
	private DelegatePBI pbiDelegate;
	
	/**
	 * Delegado de usuarios
	 */
	private DelegateUser userDelegate;
	
	/**
	 * Delegado de Tareas
	 */
	private DelegateTask taskDelegate;
	
	/**
	 * Delegado de Sprint
	 */
	private DelegateSprint sprintDelegate;

	/**
	 * Instancia interfaz servicio factoria de servicios de nodos
	 */
	protected IServicesFactory srvFactory = new ServiceFactory();
	
	/**
	 * Constructor para permitir delegados desde la interfaz
	 * de un servicio rest
	 * @param serviceRest Interfaz del servicio rest
	 */
	public DelegateService(ISrvSGRest serviceRest) {
		super();
	}

	/**
	 * Constructor para permitir delegado desde otro delegado
	 * @param delegateService Interfaz del delegado
	 */
	public DelegateService(IDelegateService delegateService) {
		super();
	}
	
	/**
	 * Método abstracto para obligar a los delegados a construir
	 * un método que retorne la interfaz de los servicios
	 * de nodos que van a consumir
	 * @return IServiceNode Interfaz del servicio de nodo a consumir.
	 */
	protected abstract IServiceNode getServiceNode();
	
	

	/**
	 * Método que obtiene el delegado de PBI
	 * 
	 * @return DelegatePBI Delegado de PBI
	 */
	protected DelegatePBI getPBIDelegate() {
		if (pbiDelegate == null) {
			pbiDelegate = new DelegatePBI(this);
		}
		return pbiDelegate;
	}
	
	/**
	 * Método que obtiene el delegado de usuarios
	 * 
	 * @return DelegateUser Delegado de usuarios
	 */
	protected DelegateUser getUserDelegate() {
		if (userDelegate == null) {
			userDelegate = new DelegateUser(this);
		}

		return userDelegate;
	}
	
	/**
	 * Método que obtiene el delegado de tareas
	 * 
	 * @return DelegateUser Delegado de tareas
	 */
	protected DelegateTask getTaskDelegate() {
		if (taskDelegate == null) {
			taskDelegate = new DelegateTask(this);
		}

		return taskDelegate;
	}
	
	/**
	 * Método que obtiene el delegado de Sprint
	 * 
	 * @return DelegateSprint Delegado de tareas
	 */
	protected DelegateSprint getSprintDelegate() {
		if (sprintDelegate == null) {
			sprintDelegate = new DelegateSprint(this);
		}

		return sprintDelegate;
	}

}
