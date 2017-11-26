package co.edu.ud.scrumgraph.data.enums;

public enum ESprintProperties {

	NAME("name"),
	CODE("code"),
	DESCRIPTION("description"),
	STATUS("status"),
	INIT_DATE("initDate"),
	END_DATE("endDate"),
	OWNER_ID("ownerId"),
	VELOCITY("velocity"),
	IS_CLOSED("isClosed"),
	TOTAL_PBI("totalPBI"),
	NUM_PBI_DONE("numPBIDone"),
	VEL_POINTS_BY_HOUR("velocityCalculated"),
	VEL_ESTIMATED("velocityEstimated"),
	TOT_POINTS_DONE("totalPoints")
	;
	
	
	private String propertyName;

	private ESprintProperties(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}

}
