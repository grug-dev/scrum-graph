package co.edu.ud.scrumgraph.data.dto;

import java.util.ArrayList;
import java.util.List;

public class IndicadoresTO {

	private NodeTO nodeTO;
	
	private List<StatsTO> lstStats = new ArrayList<StatsTO>();

	public NodeTO getNodeTO() {
		return nodeTO;
	}

	public void setNodeTO(NodeTO nodeTO) {
		this.nodeTO = nodeTO;
	}

	public List<StatsTO> getLstStats() {
		return lstStats;
	}

	public void setLstStats(List<StatsTO> lstStats) {
		this.lstStats = lstStats;
	}
	
	
	
	
}
