package co.edu.ud.scrumgraph.data.enums;

public enum EProjectProperties {
	NAME("name"),
	DESCRIPTION("description"),
	CODE("code"),
	OWNER_ID("ownerId"),
	IS_CLOSED("isClosed"),
	TOTAL_PBI("totalPBI"),
	NUM_PBI_DONE("totalPBIDone"),
	VELOCITY("velocity"),
	VEL_POINTS_BY_HOUR("velocityPtsByHour"),
	;
	private String propertyName;

	private EProjectProperties(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
