package co.edu.ud.scrumgraph.data.enums;

public enum ELabels {
	
	USER("USER"),
	TEAM("TEAM"),
	PROJECT("PROJECT") ,
	PBI("PBI"),
	SPRINT("SPRINT"),
	TASK("TASK")
	;
	
	private String name;;
	
	private ELabels(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
