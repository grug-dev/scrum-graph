package co.edu.ud.scrumgraph.data.enums;

public enum EPBIProperties {

	NAME("name"),
	CODE("code"),
	DESCRIPTION("description"),
	PRIORITY("priority"),
	HISTORY_POINTS("historyPoints"),
	STATUS("status"),
	OWNER_ID("ownerId"),
	AVAILABLE("available"),
	TYPE("type"),
	IS_CLOSED("isClosed"),
	VEL_CALCULATED("velocityCalculated"),
	VELOCITY("velocity"),
	TOTAL_TASK("totalTask"),
	NUM_TASK_DONE("numTaskDone"),
	STARTED_AT("startedAt"),
	COMPLETED_AT("completedAt");

	private String propertyName;

	private EPBIProperties(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
