package co.edu.ud.scrumgraph.data.util;

import co.edu.ud.scrumgraph.data.enums.ECodeExceptionSG;

public class ScrumGraphException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int code = ECodeExceptionSG.ERROR_CODE.getCode();
	
	public ScrumGraphException(ECodeExceptionSG response) {
		super(response.getMessage());
		this.code = response.getCode();
	}
	
	public ScrumGraphException(int code , String mensaje) {
		super(mensaje);
		this.code = code;
	}

	public ScrumGraphException(String mensaje) {
		super(mensaje);
	}
	
	public int getCode() {
		return code;
	}

}
