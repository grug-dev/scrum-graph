package co.edu.ud.scrumgraph.data.enums;

public enum ENodeProperties {

	ID("id") , CREATE_AT("createAt") , LAST_MODIFICATION("lastModification");
	
	private String propertyName;
	
	private ENodeProperties(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
}
