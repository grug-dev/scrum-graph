package co.edu.ud.scrumgraph.data.enums;

public enum EUserProperties {

	NAME("name"),
	LAST_NAME("lastName"),
	EMAIL("email"),
	ROLDEFAULT("roleDefault"),
	AVAILABLE("available"),
	ISADMIN("isAdmin"),
	AUTHTOKEN("authToken"),
	PASSWORD("password");

	private String propertyName;

	private EUserProperties(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
