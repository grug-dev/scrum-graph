package co.edu.ud.scrumgraph.data.dto;

import java.util.List;
import java.util.Map;

import co.edu.ud.scrumgraph.data.enums.ELabels;


public class NodeTO implements Comparable<NodeTO> {

	private Long id;

	private ELabels labelNode;

	private Map<String, Object> properties;

	private List<NodeTO> outgoingNodes;

	private List<NodeTO> incomingNodes;

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public ELabels getLabelNode() {
		return labelNode;
	}

	public void setLabelNode(ELabels labelNode) {
		this.labelNode = labelNode;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<NodeTO> getOutgoingNodes() {
		return outgoingNodes;
	}

	public void setOutgoingNodes(List<NodeTO> outgoingNodes) {
		this.outgoingNodes = outgoingNodes;
	}

	public List<NodeTO> getIncomingNodes() {
		return incomingNodes;
	}

	public void setIncomingNodes(List<NodeTO> incomingNodes) {
		this.incomingNodes = incomingNodes;
	}

	public int compareTo(NodeTO o) {
		return o.getId().compareTo(id);
	}

	@Override
	public boolean equals(Object o) {
		return ((NodeTO) o).getId().equals(id);
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}

}
