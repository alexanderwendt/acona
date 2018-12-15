package at.tuwien.ict.acona.evolutiondemo.brokeragent;

public class AgentValue {

	private String name;
	private double value;
	
	public AgentValue(String name, double value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "AgentValue [name=" + name + ", value=" + value + "]";
	}

}
