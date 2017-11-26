package co.edu.ud.scrumgraph.logica.enums;

/**
 * Enumerado para administrar los c�digos de respuesta
 * del response de los servicios REST.
 * @author RaspuWIN7
 */
public enum ECodesResponse {

	// GLOBAL CODES
	OK_CODE(0 , "ok") , 
	ERROR_CODE(500 , "error") , 
	INVALID_REQUEST (101 , "Invalid Request") , 
	INCOMPLETE_REQUEST_FIELDS ( 202 , "Incomplete request fields"),
	INVALID_FIELDS( 203 ,"Invalid Fields"),
	INVALID_REQ_PAYLOAD( 204 ,"Invalid Request PayLoad"),

	;
	
	
	
	private int code;
	
	private String message;
	
	private ECodesResponse( int code , String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
