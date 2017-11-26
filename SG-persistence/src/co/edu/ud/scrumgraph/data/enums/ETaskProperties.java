package co.edu.ud.scrumgraph.data.enums;

public enum ETaskProperties {

	NAME("name"),
	CODE("code"),
	DESCRIPTION("description"),
	ESTIMATE_HOURS("estimatedHours"),
	REMAINING_HOURS("remainingHours"),
	EXECUTE_HOURS("executedHours"),
	PERCENTAGE_DONE("percentageDone"),
	STATUS("status"),
	TYPE("type"),
	OWNER_ID("ownerId"),
	IS_CLOSED("isClosed"),
	STARTED_AT("startedAt"),
	COMPLETED_AT("completedAt"),
	USERS("users"),
	IS_BLOCKED("isBlocked")
	;
	
	private String propertyName;

	private ETaskProperties(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
