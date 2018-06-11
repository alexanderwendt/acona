package at.tuwien.ict.acona.mq.cell.cellfunction;

import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public interface MqttCellFunction {

	/**
	 * Initialize the cellfunction with the cell and a jsonobject with settings
	 * 
	 * @param settings:
	 *            Settings shall contain: functionname; subscriptions as a list of ID, Agent, datapointaddress and optional conditions; custom properties as json objects
	 * @param cell
	 * @return itself, in order to instantiate the cell and init at the same time
	 * @throws Exception
	 */
	public void init(CellFunctionConfig config, MqttCell cell) throws Exception;

	/**
	 * Return the name of the function, which has been specified in the config file
	 * 
	 * @return Name of the function
	 */
	public String getFunctionName();

//	/**
//	 * Get the defined type of function, e.g. base functions shall not be shown in the monitoring of an application, only codelets and threads.
//	 * 
//	 * @return
//	 */
//	public CellFunctionType getFunctionType();

	/**
	 * Perform an operation of this service. The actual method that is executed, is defined in the parameter data.
	 * 
	 * @param param
	 * @param caller
	 * @return
	 */
	public JsonRpcResponse performOperation(JsonRpcRequest param, String caller);

	/**
	 * Shut down function
	 */
	public void shutDownFunction();

	/**
	 * Get the cell function configuration
	 * 
	 * @return
	 */
	public CellFunctionConfig getFunctionConfig();

	/**
	 * Get the current state of the function
	 *
	 * @return
	 */
	public ServiceState getCurrentState();

	/**
	 * Get the name of the agent, which is holding the function
	 * 
	 * @return
	 */
	public String getAgentName();
}
