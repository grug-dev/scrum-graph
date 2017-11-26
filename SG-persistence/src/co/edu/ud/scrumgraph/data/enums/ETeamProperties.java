package co.edu.ud.scrumgraph.data.enums;

public enum ETeamProperties {

	NAME ("name")
	
	;
	private String propertyName;
	
	private ETeamProperties(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
}
