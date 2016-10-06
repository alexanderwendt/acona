package _OLD_at.tuwien.ict.acona.cell.cellfunction.special;

import java.util.List;

public class DatapointConditionHandler {
	private String datapoint;
	private String agentName;
	private List<Condition> conditions;
	
	public DatapointConditionHandler(String agent, String datapoint) {
		
	}
	
	public DatapointConditionHandler(String datapoint) {
		
	}
	
	public DatapointConditionHandler(String agent, String datapoint, List<Condition> conditions) {
		
	}
	
	public String getDatapoint() {
		return datapoint;
	}
	public void setDatapoint(String datapoint) {
		this.datapoint = datapoint;
	}
	public List<Condition> getConditions() {
		return conditions;
	}
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	
	
}
