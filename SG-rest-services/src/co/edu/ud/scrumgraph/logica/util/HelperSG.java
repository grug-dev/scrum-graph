package co.edu.ud.scrumgraph.logica.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.ud.scrumgraph.data.dto.NodeTO;
import co.edu.ud.scrumgraph.data.enums.ENodeProperties;
import co.edu.ud.scrumgraph.data.enums.EPBIProperties;
import co.edu.ud.scrumgraph.data.enums.EProjectProperties;
import co.edu.ud.scrumgraph.data.enums.ESprintProperties;
import co.edu.ud.scrumgraph.data.enums.ETaskProperties;
import co.edu.ud.scrumgraph.data.enums.EUserProperties;
import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;
import co.edu.ud.scrumgraph.logica.enums.EResponseREST;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class HelperSG {

	private static SimpleDateFormat dateFormatSG;
	
	private static final String TRUE;
	

	static {
		TRUE = "true";
		dateFormatSG = new SimpleDateFormat("yyyy/MM/dd");
	}
	
	public static Map<String, Object> getReqPayLoadProperties(String reqPayLoad) throws SGException {
		Map<String, Object> result = null;

		try {
			result = new ObjectMapper().readValue(reqPayLoad, HashMap.class);
		} catch (JsonParseException | JsonMappingException ex) {
			String msg = "[";
			ex.printStackTrace();
			msg += ECodesResponse.INVALID_REQ_PAYLOAD.getMessage() + "] " + ex.getMessage();
			throw new SGException(ECodesResponse.INVALID_REQ_PAYLOAD.getCode(), msg);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SGException(e.getMessage());
		}

		return result;
	}
	
	
	
	public static String validateHeader(String scrumGraphHeader) throws SGException {
		JSONObject headerJSON= null;
		String authToken = null;
		
		if (scrumGraphHeader == null || scrumGraphHeader.isEmpty()) {
			throw new SGException(ECodesResponse.INVALID_REQUEST);
		}
		
		try {
			headerJSON = (JSONObject) JSONSerializer.toJSON(scrumGraphHeader);	
			if (!headerJSON.containsKey(EUserProperties.AUTHTOKEN.getPropertyName())) {
				throw new SGException(ECodesResponse.INVALID_REQUEST);
			}
			authToken = headerJSON.getString(EUserProperties.AUTHTOKEN.getPropertyName());
			
			if (authToken == null || authToken.isEmpty()) {
				throw new SGException(ECodesResponse.INVALID_REQUEST);
			}
			
		}
		catch (JSONException jsonEx) {
			throw new SGException(ECodesResponse.INVALID_REQUEST);
		}
		
		return authToken;
	}
	
	public static List<Integer> getLstInteger(String strLstUsers) throws JsonParseException, JsonMappingException, IOException {
		List<Integer> idUsers = new ArrayList<Integer>();
		
		String[] users = strLstUsers.split(",");
		for (String u : users) {
			idUsers.add(Integer.parseInt(u));
		}
		//ObjectMapper mapper = new ObjectMapper();
		//idUsers = mapper.readValue(strLstUsers, new TypeReference<List<Integer>>(){});
		
		return idUsers;  
	}

	/**
	 * M�todo que permite recibe un objeto JSON, para construir la respuesta de
	 * los servicios rest en un objeto Response.
	 * 
	 * @param objJSON
	 *            JSONObject Objeto JSON para construir en un objeto response.
	 * @return Response Objeto response para dar respuesta a un servicio rest.
	 */
	public static Response makeResponse(JSONObject objJSON) {

		ResponseBuilder r = null;

		if (objJSON.get(EResponseREST.STATUS.getName()).equals(EResponseREST.STATUS_OK.getName())) {
			r = Response.ok();
		} else {
			r = Response.serverError();
		}

		return r.entity(objJSON).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-ScrumGraph-Header")
				.allow("OPTIONS").build();
	}

	public static JSONObject getOKResponse(Object responseObject) {
		JSONObject result = new JSONObject();
		
		result.put(EResponseREST.STATUS.getName(), EResponseREST.STATUS_OK.getName());		
		result.put(EResponseREST.ERROR_MSG.getName(), "");
		result.put(EResponseREST.ERROR_CODE.getName(), ECodesResponse.OK_CODE.getCode());
		if (responseObject == null) {
			JSONObject successJSON = new JSONObject();
			successJSON.put(EResponseREST.STATUS_SUCCESS.getName(),TRUE);
			result.put(EResponseREST.RESPONSE.getName() , successJSON);
			return result;
		}
		result.put(EResponseREST.RESPONSE.getName() , responseObject);
		
		return result;
	}

	public static JSONObject getErrorResponse(int codeError, String msgError) {
		JSONObject result = new JSONObject();
		result.put(EResponseREST.STATUS.getName(), EResponseREST.STATUS_ERROR.getName());		
		result.put(EResponseREST.ERROR_MSG.getName(), msgError);
		result.put(EResponseREST.ERROR_CODE.getName(), codeError);
		
		return result;
	}

	public static JSONObject createJSONObject(List<String> lstPropKeys, NodeTO nodeTO, JSONObject objJSON) {
		Map<String, Object> mapProperties = null;

		if (lstPropKeys == null || lstPropKeys.isEmpty()) {
			return objJSON;
		}

		if (nodeTO == null) {
			return objJSON;
		}

		if (objJSON == null) {
			objJSON = new JSONObject();
		}

		objJSON.put(ENodeProperties.ID.getPropertyName(), nodeTO.getId());
		mapProperties = nodeTO.getProperties();
		for (String key : lstPropKeys) {
			if (mapProperties.containsKey(key) ) {
				String value = toString(mapProperties.get(key));
				if (value != null) {
					objJSON.put(key, value);
				}	
			}
		}

		return objJSON;
	}

	private static String toString(Object obj) {

		if (obj == null) {
			return null;
		}

		if (obj instanceof Date) {
			return toDateFormatSG(obj);
		}

		return obj.toString();
	}

	private static String toDateFormatSG(Object date) {
		String formatDate = null;

		formatDate = dateFormatSG.format(date);

		return formatDate;
	}
	
	public static void validadLenght(int minLength, int maxLength , String property) throws SGException {
    	int lenght = 0;
    	
    	if (property == null) {
    		throw new SGException(ECodesResponse.INVALID_FIELDS.getCode(), ECodesResponse.INVALID_FIELDS.getMessage());
    	}
    	
    	lenght = property.length();
		if (!(lenght >= minLength && lenght <= maxLength)) {
			throw new SGException(ECodesResponse.INVALID_FIELDS.getCode(), ECodesResponse.INVALID_FIELDS.getMessage());
		}
    	
    }
	
	/**
	 * Construye un objeto JSON basado en la información de las propiedades del
	 * nodo.
	 * 
	 * @param sprintTO
	 *            NodeTO Objeto con la información a convertir
	 * @param nameObjJSON
	 *            Indica si se desea que la información del objeto json
	 *            construido se encuentre como propiedad en otro objeto json. Si
	 *            es diferente de nulo, la propiedad donde se encapsula el
	 *            objeto JSON es la del nombre pasado como parámetro.
	 * @param notIncludeProperties
	 *            Array de objetos para indicar que alguna propiedad del nodo no
	 *            se debe incluir en el objeto JSON
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	public static JSONObject getSprintJSON(NodeTO sprintTO, String nameObjJSON, ESprintProperties... notIncludeProperties) {
		JSONObject sprintJSON = null;

		if (sprintTO == null) {
			return sprintJSON;
		}

		sprintJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (ESprintProperties projProp : ESprintProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludeProperties != null) {
			for (ESprintProperties property : notIncludeProperties) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		createJSONObject(lstPropKeys, sprintTO, sprintJSON);

		if (nameObjJSON != null) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put("sprint", sprintJSON);
			return projRespJSON;
		}

		return sprintJSON;
	}
	
	public static JSONObject getProjectJSON(NodeTO projectTO, String nameResponse, EProjectProperties... notIncludePropertie) {
		JSONObject projectJSON = null;

		if (projectTO == null) {
			return projectJSON;
		}

		projectJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (EProjectProperties projProp : EProjectProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add("teamName");
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludePropertie != null) {
			for (EProjectProperties property : notIncludePropertie) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		HelperSG.createJSONObject(lstPropKeys, projectTO, projectJSON);

		if (nameResponse != null) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put(nameResponse, projectJSON);
			return projRespJSON;
		}

		return projectJSON;
	}
	
	/**
	 * Construye un objeto JSON basado en la información de las propiedades
	 * del nodo. 
	 * @param pbiTO NodeTO Objeto con la información a convertir
	 * @param nameObjJSON Indica si se desea que la información del objeto json construido se encuentre como propiedad 
	 * en otro objeto json. Si es diferente de nulo, la propiedad donde se encapsula el objeto JSON es la del nombre pasado como parámetro.
	 * @param notIncludeProperties Array de objetos para indicar que alguna propiedad del nodo no se debe incluir en el objeto JSON
	 * @return
	 */
	public static JSONObject getPBIJSON(NodeTO pbiTO, String nameObjJSON, EPBIProperties... notIncludeProperties) {
		JSONObject pbiJSON = null;

		if (pbiTO == null) {
			return pbiJSON;
		}

		pbiJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (EPBIProperties projProp : EPBIProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludeProperties != null) {
			for (EPBIProperties property : notIncludeProperties) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		HelperSG.createJSONObject(lstPropKeys, pbiTO, pbiJSON);

		if (nameObjJSON != null ) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put(nameObjJSON, pbiJSON);
			return projRespJSON;
		}

		return pbiJSON;
	}
	
	/**
	 * Construye un objeto JSON basado en la información de las propiedades del
	 * nodo.
	 * 
	 * @param taskTO
	 *            NodeTO Objeto con la información a convertir
	 * @param nameObjJSON
	 *            Indica si se desea que la información del objeto json
	 *            construido se encuentre como propiedad en otro objeto json. Si
	 *            es diferente de nulo, la propiedad donde se encapsula el
	 *            objeto JSON es la del nombre pasado como parámetro.
	 * @param notIncludeProperties
	 *            Array de objetos para indicar que alguna propiedad del nodo no
	 *            se debe incluir en el objeto JSON
	 * @return Response Objeto con la respuesta del servicio REST. El formato de
	 *         respuesta está compuesto por :
	 *         <ul>
	 *         <li><b>status:</b> ok o error</li>
	 *         <li><b>errorMsg:</b> Mensaje de algún error de validación o de
	 *         ejecución al ejecutar el servicio</li>
	 *         <li><b>errorCode:</b>Código del error</li>
	 *         <li><b>response:</b>Objeto JSON con la respuesta del servicio.
	 *         </li>
	 *         </ul>
	 */
	public static JSONObject getTaskJSON(NodeTO taskTO, String nameObjJSON, ETaskProperties... notIncludeProperties) {
		JSONObject taskJSON = null;

		if (taskTO == null) {
			return taskJSON;
		}

		taskJSON = new JSONObject(false);
		List<String> lstPropKeys = new ArrayList<String>();

		// Asignar propiedades a retornar en el reponse
		for (ETaskProperties projProp : ETaskProperties.values()) {
			lstPropKeys.add(projProp.getPropertyName());
		}
		lstPropKeys.add(ENodeProperties.CREATE_AT.getPropertyName());

		// Eliminar Propiedades
		if (notIncludeProperties != null) {
			for (ETaskProperties property : notIncludeProperties) {
				lstPropKeys.remove(property.getPropertyName());
			}
		}

		createJSONObject(lstPropKeys, taskTO, taskJSON);
		
		if (nameObjJSON != null) {
			JSONObject projRespJSON = new JSONObject();
			projRespJSON.put(nameObjJSON, taskJSON);
			return projRespJSON;
		}

		return taskJSON;
	}

}
