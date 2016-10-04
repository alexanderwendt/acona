package at.tuwien.ict.acona.framework.interfaces;

import at.tuwien.ict.acona.cell.config.CellConfig;

/**
 * Adding of cell config with one cellfunction and custom properties and one datapoint to be synchronized through subscription
 * //Controller
 * CellConfig controllerAgentConfig = CellConfig.newConfig(controllerAgentName)
 *		.addCellfunction(functionConfig);
 * 
 * 
 * functionConfig = CellFunctionConfig.newConfig([FUNCTIONNAME=Service name], SequenceController.class)
 *					.setProperty("agent1", agentName1)
 *					.setProperty("agent2", agentName2)
 *					.setProperty("agent3", agentName3)
 *					.setProperty("servicename", ServiceName)
 *				.addSyncDatapoint(datapointConfig)
 * 
 * Agent name is optionally (No name or "" means local agent)
 * datapointConfig = DatapointConfig.newConfig("rawdata", "jade.javadelveloper.blabla.haha.value", "agentName", "push")
 * 
 * 
 * @author wendt
 *
 */
public interface SystemConfigExternal {
	public void setTopController(String name);
	public String getTopController();
	public void addController(CellConfig controller);
	public void addService(CellConfig controller);
	public void addMemory(CellConfig controller);
	
}