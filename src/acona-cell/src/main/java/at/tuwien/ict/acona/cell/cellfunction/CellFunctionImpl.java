package at.tuwien.ict.acona.cell.cellfunction;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public abstract class CellFunctionImpl implements CellFunction {
	
	protected static Logger log = LoggerFactory.getLogger(CellFunctionImpl.class);
	protected static final String SYNCMODEPUSH = "push";
	protected static final String SYNCMODEPULL = "pull";
	
	/**
	 * Cell, which executes this function
	 */
	private Cell cell;
	private CellFunctionConfig config;
	
	private int executeRate = 1000;
	private boolean executeOnce = true;
	
	/**
	 * Name of the activator
	 */
	private String name;
	
	/**
	 * List of datapoints that shall be subscribed
	 */
	private final Map<String, DatapointConfig> subscriptions = new HashMap<String, DatapointConfig>();	//Variable, datapoint
	private final Map<String, DatapointConfig> readDatapoints = new HashMap<String, DatapointConfig>();	//Variable, datapoint
	private final Map<String, DatapointConfig> syncDatapoints = new HashMap<String, DatapointConfig>();
	
	protected ControlCommand currentCommand = ControlCommand.STOP;
	protected boolean runAllowed = false;

	@Override
	public CellFunction init(CellFunctionConfig config, Cell caller) throws Exception {
		try {
			//Extract settings
			this.config = config;
			this.cell = caller;
			
			//Get name
			this.name = config.getName();
			//Get execute once as optional
			if (config.isExecuteOnce()!=null) {
				this.setExecuteOnce(config.isExecuteOnce().getAsBoolean());
			}
			
			//Get executerate as optional
			if (config.getExecuteRate()!=null) {
				this.setExecuteRate(config.getExecuteRate().getAsInt());
			}
			
			//Possibility to add more subscriptions
			cellFunctionInit();
			
			//Get subscriptions from config and add to subscription list
			this.config.getSyncDatapoints().forEach(s->{
				if (s.getSyncMode().equals(SYNCMODEPUSH)) {
					this.subscriptions.put(s.getId(), s);
				} else if (s.getSyncMode().equals(SYNCMODEPULL)) {
					this.readDatapoints.put(s.getId(), s);
				} else {
					try {
						throw new Exception("No syncmode=" + s.getSyncMode() + ". only pull and push available");
					} catch (Exception e) {
						log.error("Cannot set sync mode", e);
					}
				}
				
				syncDatapoints.put(s.getId(), s);
				
			});
			
			//Register in cell
			this.cell.getFunctionHandler().registerCellFunctionInstance(this);
		} catch (Exception e) {
			log.error("Cannot init function with config={}", config);
			throw new Exception(e.getMessage());
		}
		
		return this;
	}
	
	protected abstract void cellFunctionInit() throws Exception;
	
	//protected abstract void updateDatapoint(Datapoint subscribedData) throws Exception;
	@Override
	public void updateData(Map<String, Datapoint> data) {
		//Create datapointmapping ID to datapoint with new value
		Map<String, Datapoint> subscriptions = new HashMap<String, Datapoint>();
		this.getSubscribedDatapoints().forEach((k, v)->{
			if (data.containsKey(v.getAddress())) {
				subscriptions.put(k, data.get(v.getAddress()));
			}
		});
		
		this.updateDatapointsById(subscriptions);
	}
	
	protected abstract void updateDatapointsById(Map<String, Datapoint> data);
	
	protected abstract void executeFunction() throws Exception;
	
	protected abstract void executePostProcessing() throws Exception;
	
	protected abstract void executePreProcessing() throws Exception;

	public abstract void setCommand(ControlCommand command);

	@Override
	public String getFunctionName() {
		return this.name;
	}
	
	@Override
	public void setStart() {
		this.setCommand(ControlCommand.START);
	}

	@Override
	public void setStop() {
		this.setCommand(ControlCommand.STOP);
	}

	@Override
	public void setPause() {
		this.setCommand(ControlCommand.PAUSE);
		
	}

	@Override
	public void setExit() {
		//Unsubscribe all datapoints
		//this.getCell().getFunctionHandler().deregisterActivatorInstance(this);
		
		//Execute specific functions
		this.cell.getFunctionHandler().deregisterActivatorInstance(this);
		this.setCommand(ControlCommand.EXIT);
	}
	
	@Override
	public Map<String, DatapointConfig> getSubscribedDatapoints() {	//ID config
		return subscriptions;
	}
	
	@Override
	public CellFunctionConfig getFunctionConfig() {
		return this.config;
	}
	
	public int getExecuteRate() {
		return executeRate;
	}

	public void setExecuteRate(int blockingTime) {
		this.executeRate = blockingTime;
	}
	
	protected boolean isExecuteOnce() {
		return executeOnce;
	}

	protected void setExecuteOnce(boolean executeOnce) {
		this.executeOnce = executeOnce;
	}
	
	
	
	//=== read and write shortcuts ===//
	
	protected Communicator getCommunicator() {
		return this.getCell().getCommunicator();
	}
	
	protected void writeLocal(Datapoint datapoint) throws Exception {
		this.getCommunicator().write(datapoint);
	}
	
	protected <DATATYPE> void writeLocal(String address, DATATYPE datapoint) throws Exception {
		Gson gson = new Gson();
		String value = gson.toJson(datapoint);
		this.getCommunicator().write(Datapoint.newDatapoint(address).setValue(value));
	}
	
	protected Datapoint readLocal(String address) throws Exception {
		return this.getCommunicator().read(address);
	}
	
	protected JsonElement readLocalAsJson(String address) throws Exception {
		return this.getCommunicator().read(address).getValue();
	}
	
	protected <T> T readLocalSyncDatapointById(String id, Class<T> type) throws Exception {
		Gson gson = new Gson();
	    JsonElement value = this.readLocal(this.getConfig().getSyncDatapointsAsMap().get(id).getAddress()).getValue();
	    T convertedValue = gson.fromJson(value, type);
	    
	    return convertedValue;
	}
	
	protected <T> void writeLocalSyncDatapointById(String id, T value) throws Exception {
		//Gson gson = new Gson();
		
		JsonElement writeValue = new Gson().toJsonTree(value);
		this.writeLocal(Datapoint.newDatapoint(this.getConfig().getSyncDatapointsAsMap().get(id).getAddress()).setValue(writeValue));
	}
	
	protected <T> T getCustomSetting(String key, Class<T> type) {
		return this.getConfig().getProperty(name, type);
	}

	protected Cell getCell() {
		return cell;
	}

	protected ControlCommand getCurrentCommand() {
		return currentCommand;
	}

	protected void setCurrentCommand(ControlCommand currentCommand) {
		this.currentCommand = currentCommand;
	}

	protected boolean isAllowedToRun() {
		return runAllowed;
	}

	protected void setAllowedToRun(boolean isAllowedToRun) {
		this.runAllowed = isAllowedToRun;
	}

	protected CellFunctionConfig getConfig() {
		return config;
	}
	
	/**
	 * Return the subscribed datapoint based on its ID in the function
	 * 
	 * @param data: inputmap from subscribed data
	 * @param id: datapoint id defined in the config or in the code 
	 * @return
	 */
	protected Datapoint getDatapointFromId(Map<String, Datapoint> data, String id) {
		return data.get(this.getSubscribedDatapoints().get(id).getAddress());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CellFunctionImpl [name=");
		builder.append(name);
		builder.append(", subscriptions=");
		builder.append(subscriptions);
		builder.append("]");
		return builder.toString();
	}

	protected Map<String, DatapointConfig> getSyncDatapoints() {
		return syncDatapoints;
	}

	protected Map<String, DatapointConfig> getReadDatapoints() {
		return readDatapoints;
	}


}
