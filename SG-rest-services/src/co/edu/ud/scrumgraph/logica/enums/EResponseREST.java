package co.edu.ud.scrumgraph.logica.enums;

public enum EResponseREST {

	STATUS("status"), ERROR_CODE("errorCode") ,ERROR_MSG("errorMsg"),RESPONSE("response") , STATUS_OK ("ok") , STATUS_ERROR ("error") , STATUS_SUCCESS("success");
	
	private String name;
	
	private EResponseREST(String name) {
		this.name  = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
}
