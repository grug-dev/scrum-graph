package co.edu.ud.scrumgraph.data.dto;

import java.util.ArrayList;
import java.util.List;

public class GraphTO {

	
	private List<NodeTO> nodes;
	
	private List<RelationshipTO> links;
	
	public GraphTO() {
		nodes = new ArrayList<NodeTO>();
		links = new ArrayList<RelationshipTO>();
	}

	public List<NodeTO> getNodes() {
		return nodes;
	}

	public void setNodes(List<NodeTO> nodes) {
		this.nodes = nodes;
	}

	public List<RelationshipTO> getLinks() {
		return links;
	}

	public void setLinks(List<RelationshipTO> links) {
		this.links = links;
	}
	
}
