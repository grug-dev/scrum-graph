package co.edu.ud.scrumgraph.data.dto;

public class StatsTO {

	private String label;
	
	private double value;
	
	private int id;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getValue() {
		return value;
	}

	public void setValue(Object value) {
		if (value == null) {
			this.value = 0.0d;
		}
		else {
			this.value = Double.parseDouble(value.toString());	
		}		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public StatsTO clone() {
		StatsTO x = new StatsTO();
		return x;
	}
	
	
}
