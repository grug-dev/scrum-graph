package co.edu.ud.scrumgraph.logica.util;

import co.edu.ud.scrumgraph.logica.enums.ECodesResponse;

public class SGException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4157994927050600608L;
	private int code = ECodesResponse.ERROR_CODE.getCode();
	
	public SGException(ECodesResponse response) {
		super(response.getMessage());
		this.code = response.getCode();
	}
	
	public SGException(int code , String mensaje) {
		super(mensaje);
		this.code = code;
	}

	public SGException(String mensaje) {
		super(mensaje);
	}
	
	public int getCode() {
		return code;
	}

}
