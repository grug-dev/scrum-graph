package co.edu.ud.scrumgraph.data.enums;

public enum EDefaultStateNodes {

	TODO("to-do"),
	WIP("in-progress"),
	DONE("done")
	;
	
	private String name;;
	
	private EDefaultStateNodes(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
