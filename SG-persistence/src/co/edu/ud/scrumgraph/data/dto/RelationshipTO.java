package co.edu.ud.scrumgraph.data.dto;

public class RelationshipTO {

	private Long id;
	
	private String type;
	
	private Long sourceId;
	
	private Long targetId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return ((RelationshipTO) obj).getId().equals(id);
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
}
