package co.edu.ud.scrumgraph.data.conexion;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;

import co.edu.ud.scrumgraph.data.util.ScrumGraphException;

/**
 * Clase que administra la base de datos
 * 
 * @author RaspuWIN7
 *
 */
public class ConexionService {

	/**
	 * Objeto que controla la conexi�n a BD
	 */
	private static ConexionService objConexion;
	/**
	 * Objeto del servicio de base de datos orientados a grafos.
	 */
	private GraphDatabaseService graphDBService;
	
	private QueryEngine engine;
	
	/**
	 * Ruta de almacenamiento de la base de datos
	 */
	private String DB_PATH = "http://localhost:7474/db/data/";
	
	/**
	 * Usuario de BD
	 */
	private final String USER_DB = "neo4j";
	
	/**
	 * Password de BD
	 */
	private final String PASS_DB = "adminneo4j";
	
	/**
	 * Objeto RestAPI
	 */
	private RestAPI restAPIObj;
	
	
	private static final String CONNECTION_FAILED_MSG = "Ha ocurrido un error al intentar conectarse a la Base de Datos";
	
	/**
	 * Constructor por defecto
	 */
	private ConexionService() {
		super();
	}

	/**
	 * Obtiene la �nica instancia del objeto <i>ConexionBDAPI</i>
	 * @return ConexionBDAPI Instancia creada.
	 */
	public static ConexionService getInstance() {
		if (objConexion == null) {
			objConexion = new ConexionService();
		}
		return objConexion;
	}
	
	/**
	 * M�todo para obtener el servicio de la base de datos orientada a grafos.
	 * @return GraphDatabaseService Objeto con la informaci�n del servicio.
	 * @throws ScrumGraphException
	 */
	public GraphDatabaseService getGraphDbService() throws ScrumGraphException {
		if (graphDBService == null) {
			try {
				graphDBService =  new RestGraphDatabase(DB_PATH,USER_DB,PASS_DB);
				registerShutdownHook(graphDBService);
			}
			catch (Exception ioException) {
				throw new ScrumGraphException(CONNECTION_FAILED_MSG);
			}
		}
		return graphDBService;
	}
	
	public QueryEngine getEngine() throws ScrumGraphException {
		if (engine == null) {
			 engine = new RestCypherQueryEngine(getRestAPI());
		}
		return engine;
	}
	
	/**
	 * M�todo que obtiene la conexi�n RESTAPI de la base de datos
	 * orientada a grafos
	 * @return RestAPI Objeto
	 */
	public RestAPI getRestAPI () throws ScrumGraphException {
		try {
			if (restAPIObj == null) {
				restAPIObj = new RestAPIFacade(DB_PATH , USER_DB , PASS_DB);	
			}	
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new ScrumGraphException(CONNECTION_FAILED_MSG);
		}
		
		return restAPIObj;
		 
	}
	
	
	/**
	 * M�todo para detener los servicios de la base de datos pasa como parametro
	 * cuando se registra una ca�da inesperada de la m�quina virtual de Java.
	 * @param graphDBService GraphDatabaseService Servicio de base de datos orientada
	 * a grafos que se desea detener.
	 */
    private void registerShutdownHook(final GraphDatabaseService graphDBService)
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
            	graphDBService.shutdown();
            }
        } );
    }
	
}
