package at.tuwien.ict.acona.cell.cellfunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;

/**
 * @author wendt
 * 
 *         The blocking executor does not use any thread and is a combination of
 *         some conditions with activate on any change of a datapoint that is
 *         subscribed and the behaviour itself. The blocking executor can only
 *         be executed if a subscribed datapoint is received
 *
 */
public abstract class CellFunctionThreadImpl extends CellFunctionImpl implements Runnable {

	public final static String STATESUFFIX = "state";
	public final static String EXTENDEDSTATESUFFIX = "extendedstate";
	public final static String RESULTSUFFIX = "result";
	public final static String COMMANDSUFFIX = "command";
	public final static String DESCRIPTIONSUFFIX = "description";
	public final static String CONFIGSUFFIX = "config";

	private static Logger log = LoggerFactory.getLogger(CellFunctionThreadImpl.class);
	private static final int INITIALIZATIONPAUSE = 500;

	/**
	 * Deafult execute rate of the function
	 */
	private int executeRate = 1000;
	/**
	 * Execute the function only once
	 */
	private boolean executeOnce = true;

	private boolean finishedAfterSingleRun = true;

	/**
	 * Current control command
	 */
	protected ControlCommand currentCommand = ControlCommand.STOP;
	/**
	 * Current run state, if the system is allowed to run.
	 */
	protected boolean runAllowed = false;

	/**
	 * This command is reset as the start is triggered. It is used if a start
	 * command is triggered, while the thread is already running. In that case,
	 * the system shall run again to be up to date.
	 */
	protected boolean startCommandIsSet = false;

	/**
	 * In the value map all, subscribed values, as well as read values and all
	 * write values are put. Syntac: Key: Datapointid, value: Datapoint address
	 */
	private Map<String, Datapoint> valueMap = new ConcurrentHashMap<>();

	/**
	 * This thread
	 */
	private Thread t;

	/**
	 * If the run function of the thread is active
	 */
	private boolean isActive = true;

	public CellFunctionThreadImpl() {

	}

	@Override
	protected void cellFunctionInit() throws Exception {
		try {
			// Get execute once as optional
			if (this.getFunctionConfig().isExecuteOnce() != null) {
				this.setExecuteOnce(this.getFunctionConfig().isExecuteOnce().getAsBoolean());
			} else {
				this.getFunctionConfig().setExecuterate(executeRate);
			}

			// Get executerate as optional
			if (this.getFunctionConfig().getExecuteRate() != null) {
				this.setExecuteRate(this.getFunctionConfig().getExecuteRate().getAsInt());
			} else {
				this.getFunctionConfig().setExecuteOnce(executeOnce);
			}

			// Get customized finished after single run
			//			if (this.getFunctionConfig().isFinishStateAfterSingleRun() != null) {
			//				this.setFinishedAfterSingleRun(this.getFunctionConfig().isFinishStateAfterSingleRun().getAsBoolean());
			//			} else {
			//				this.getFunctionConfig().setExecuteOnce(this.finishedAfterSingleRun);
			//			}

			// Set state register
			//			if (this.getFunctionConfig().getRegisterState() == null) {
			//				this.getFunctionConfig().setRegisterState(true);
			//			}

			cellFunctionThreadInit();
			// Create a thread from this class
			t = new Thread(this, this.getCell().getLocalName() + "#" + this.getFunctionName());
			//Start the thread as well as the internal initialization
			t.start();

			//Init all service datapoints, which present the system
			this.initServiceDatapoints();
			//log.info("CellFunction as thread implementation {} initilized", this.getFunctionName());

		} catch (Exception e) {
			log.error("CellFunction {} could not be initialized", this.getFunctionName(), e);
			throw new Exception(e.getMessage());
		}
	}

	protected abstract void cellFunctionThreadInit() throws Exception;

	private void initServiceDatapoints() throws Exception {
		String functionDescription = setFunctionDescription();

		Datapoint command = DatapointBuilder.newDatapoint(this.addServiceName(COMMANDSUFFIX)).setValue(ControlCommand.STOP.toString());
		Datapoint state = DatapointBuilder.newDatapoint(this.addServiceName(STATESUFFIX)).setValue(ServiceState.FINISHED.toString());
		Datapoint description = DatapointBuilder.newDatapoint(this.addServiceName(DESCRIPTIONSUFFIX)).setValue(functionDescription);
		Datapoint config = DatapointBuilder.newDatapoint(this.addServiceName(CONFIGSUFFIX)).setValue("");
		Datapoint result = DatapointBuilder.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue("");
		Datapoint extendedState = DatapointBuilder.newDatapoint(this.addServiceName(EXTENDEDSTATESUFFIX)).setValue(ChunkBuilder.newChunk(this.getFunctionName() + "_EXTSTATE", "EXTENDEDSTATE"));

		log.debug("Subscribe the following datapoints:\ncommand: {}\nstate: {}\ndescription: {}\nparameter: {}\nconfig: {}",
				command.getAddress(), state.getAddress(), description.getAddress(),
				config.getAddress(), result.getAddress());

		//Add subscriptions
		this.addManagedDatapoint(DatapointConfig.newConfig(command.getAddress(), command.getAddress(), SyncMode.SUBSCRIBEONLY));
		//this.addManagedDatapoint(DatapointConfig.newConfig(state.getAddress(), state.getAddress(), SyncMode.SUBSCRIBEONLY));
		//this.addManagedDatapoint(DatapointConfig.newConfig(description.getAddress(), description.getAddress(), SyncMode.SUBSCRIBEONLY));
		this.addManagedDatapoint(DatapointConfig.newConfig(config.getAddress(), config.getAddress(), SyncMode.SUBSCRIBEONLY));
		//Result will only be written

		this.getCommunicator().write(Arrays.asList(command, state, description, config, result, extendedState));
	}

	protected String setFunctionDescription() {
		return "Service " + this.getFunctionName() + ". Thread; external responder=" + this.getFunctionConfig().getGenerateReponder().getAsBoolean();
	}

	protected abstract void executeFunction() throws Exception;

	@Override
	public void run() {
		log.debug("Start cell function {}", this.getFunctionName());

		//		log.debug("Start internal initialization");
		//		boolean initFinished = false;
		//		do {
		//			try {
		//				cellFunctionThreadInit();
		//				initFinished = true;
		//			} catch (Exception e2) {
		//				log.error("Cannot initialize cell function={}. Try again in {}ms", this.getFunctionName(), INITIALIZATIONPAUSE, e2);
		//				synchronized (this) {
		//					try {
		//						this.wait(INITIALIZATIONPAUSE);
		//					} catch (InterruptedException e3) {
		//
		//					}
		//				}
		//			}
		//		} while (initFinished == false);
		//		log.info("CellFunction as thread implementation {} initilized", this.getFunctionName());

		while (isActive == true) {
			// Stop the system at the end of the turn, if STOP command has been
			// given
			executeWait();

			try {
				if (this.isAllowedToRun() == true) {
					//Clear the blocker queue
					//blocker.clear();
					executePreProcessing();

					executeFunction();

					executePostProcessing();

					//Add true to release the queue
					//blocker.(true);
				}
			} catch (Exception e1) {
				log.error("Error in program execution", e1);
			}

			if (this.isExecuteOnce() == false) {
				try {
					Thread.sleep(this.getExecuteRate());
				} catch (InterruptedException e) {
					log.warn("Sleep was interrupted", e);
				}
			} else {
				// Set datapoint as pause and set pause command here
				try {
					if (this.getCurrentCommand().equals(ControlCommand.EXIT) == false) {
						this.setCommand(ControlCommand.PAUSE.toString());
					}
				} catch (Exception e) {
					log.error("Error setting pause", e);
				}
			}
		}

		log.debug("Stop executor {}", this.getFunctionName());
		this.getCell().takeDownCell();
	}

	protected void executePreProcessing() throws Exception {
		// Read all values from the store or other agent
		this.setServiceState(ServiceState.RUNNING);
		if (this.getReadDatapointConfigs().isEmpty() == false) {
			log.info("{}>Start preprocessing by reading function variables={}", this.getFunctionName(), this.getReadDatapointConfigs());
		}

		this.getReadDatapointConfigs().forEach((k, v) -> {
			try {

				//FIXME: Make that also local datapoints are put in the table

				// Read the remote datapoint
				//if (v.getAgentid(this.getCell().getLocalName()).equals(this.getCell().getLocalName()) == false) {
				Datapoint temp = this.getCommunicator().read(v.getAgentid(this.getCell().getLocalName()), v.getAddress());
				// Write local value to synchronize the datapoints
				this.valueMap.put(k, temp);
				log.trace("{}> Preprocessing phase: Read datapoint and write into value table={}", temp);
				//}
			} catch (Exception e) {
				log.error("{}>Cannot read datapoint={}", this.getFunctionName(), v, e);
			}
		});

		this.getWriteDatapointConfigs().forEach((k, v) -> {
			try {

				// Write local value to synchronize the datapoints
				this.valueMap.putIfAbsent(k, v.toDatapoint(this.getAgentName()));
				log.trace("{}> Preprocessing phase: Init write datapoint and write into value table={}", v);
				//}
			} catch (Exception e) {
				log.error("{}>Cannot write init datapoint={}", this.getFunctionName(), v, e);
			}
		});

		this.executeCustomPreProcessing();

	}

	protected void executePostProcessing() throws Exception {

		//Put the custom post processing before the values of the value map are written. In that way, values can be added in advance.
		this.executeCustomPostProcessing();

		// FIXME: The update here is not working well
		if (this.getWriteDatapointConfigs().isEmpty() == false) {
			log.debug("{}>Execute post processing action write for the datapoints={}", this.getFunctionName(), this.getWriteDatapointConfigs());
		}

		// 6. At end, write subscribed datapoints to remote datapoints from
		// local datapoints
		this.getWriteDatapointConfigs().values().forEach(config -> {
			try {
				Datapoint dp = this.valueMap.get(config.getId());
				if (dp != null) {
					String agentName = config.getAgentid(this.getCell().getLocalName());
					this.getCommunicator().write(agentName, dp);
					log.trace("{}>Written datapoint={} to agent={}", this.getFunctionName(), dp, agentName);
				} else {
					log.warn("A datapoint in the write config is not available in the value map with values that should be written");
				}

			} catch (Exception e) {
				log.error("{}>Cannot write datapoint {} to remote memory module", this.getFunctionName(), config, e);
			}
		});

		if (this.isExecuteOnce() == true) {
			this.writeLocal(DatapointBuilder.newDatapoint(this.addServiceName(COMMANDSUFFIX)).setValue(ControlCommand.PAUSE.toString()));
		}

		if (this.isFinishedAfterSingleRun() == true) {
			this.setServiceState(ServiceState.FINISHED);
		}

		log.debug("{}>Service execution run finished", this.getFunctionName());
	}

	protected abstract void executeCustomPostProcessing() throws Exception;

	protected abstract void executeCustomPreProcessing() throws Exception;

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		log.trace("{}>Update datapoints={}. Command name={}", this.getFunctionName(), data, this.addServiceName(COMMANDSUFFIX));
		// Update command
		if (data.containsKey(this.addServiceName(COMMANDSUFFIX)) && data.get(this.addServiceName(COMMANDSUFFIX)).getValue().toString().equals("{}") == false) {
			try {
				this.setCommand(data.get(this.addServiceName(COMMANDSUFFIX)).getValueAsString());
			} catch (Exception e) {
				log.error("{}>Cannot execute command={}", this.getFunctionName(), data.get(this.addServiceName(COMMANDSUFFIX)).getValueAsString(), e);
			}
		}

		// Update config
		if (data.containsKey(this.addServiceName(CONFIGSUFFIX))) {
			log.info("New config set={}", data.get(CONFIGSUFFIX).getValue());

			data.keySet().forEach(key -> {
				this.getFunctionConfig().setProperty(key, data.get(key).getValue());
			});
		}

		valueMap.putAll(data);

		this.updateDatapointsByIdOnThread(data);
	}

	protected abstract void updateDatapointsByIdOnThread(Map<String, Datapoint> data);

	// === Internal functions for the control of the tread ===//

	/**
	 * Check, which command is valid and block until finished
	 */
	private synchronized void executeWait() {
		if (this.isStartCommandIsSet() == false) {
			while (this.getCurrentCommand().equals(ControlCommand.STOP) == true || this.getCurrentCommand().equals(ControlCommand.PAUSE) == true) {
				try {
					// Block profile controller
					this.setAllowedToRun(false);
					this.wait();
				} catch (InterruptedException e) {
					log.trace("Wait interrupted client");
				}
			}
		}

		this.setStartCommandIsSet(false);
	}

	protected synchronized void setCommand(String commandString) throws Exception {
		if (ControlCommand.isCommand(commandString)) {
			this.setCurrentCommand(ControlCommand.valueOf(commandString));
			setCommand(this.getCurrentCommand());
			log.debug("Codelet {}: command {} set", this.getFunctionName(), this.getCurrentCommand());
		} else {
			log.warn("Command string is no command: {}", commandString);
		}
	}

	public synchronized void setCommand(ControlCommand command) {
		this.setCurrentCommand(command);
		if (this.getCurrentCommand().equals(ControlCommand.START) == true) {
			this.setAllowedToRun(true);
			this.setStartCommandIsSet(true);
			this.notify();
		} else if (this.getCurrentCommand().equals(ControlCommand.STOP) == true) {
			this.setAllowedToRun(false);
			this.notify();
		} else if (this.getCurrentCommand().equals(ControlCommand.EXIT) == true) {
			this.setAllowedToRun(false);
			this.setActive(false);
			this.notify();
		}
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setStart() {
		this.setCommand(ControlCommand.START);
	}

	public void setStop() {
		this.setCommand(ControlCommand.STOP);
	}

	public void setPause() {
		this.setCommand(ControlCommand.PAUSE);

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

	protected boolean isStartCommandIsSet() {
		return startCommandIsSet;
	}

	protected void setStartCommandIsSet(boolean startCommandIsSet) {
		this.startCommandIsSet = startCommandIsSet;
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		this.setCommand(ControlCommand.EXIT);
		this.shutDownExecutor();
	}

	protected abstract void shutDownExecutor() throws Exception;

	/**
	 * For a certain datapoint suffix, add the service name and a . to the
	 * suffix.
	 * 
	 * @param suffix
	 * @return
	 */
	protected String addServiceName(String suffix) {
		return this.getFunctionName() + "." + suffix;
	}

	/**
	 * @param testList
	 * @return
	 */
	protected boolean isSystemDatapoint(Map<String, ?> map) {
		boolean result = false;

		ArrayList<String> testList = new ArrayList<>(map.keySet());

		if (testList.contains(this.addServiceName(COMMANDSUFFIX))
				|| testList.contains(this.addServiceName(CONFIGSUFFIX))) {
			result = true;
		}

		return result;
	}

	@Override
	protected void processServiceState() throws Exception {
		try {
			this.getCommunicator().write(DatapointBuilder.newDatapoint(this.addServiceName(STATESUFFIX)).setValue(this.getCurrentState().toString()));
		} catch (Exception e) {
			log.error("Cannot write the state = {} to datapoint = {}", this.getCurrentState(), this.addServiceName(STATESUFFIX));
			throw new Exception(e.getMessage());
		}
	}

	protected Map<String, Datapoint> getValueMap() {
		return valueMap;
	}

	@Override
	public CellFunctionType getFunctionType() {
		return CellFunctionType.THREADFUNCTION;
	}

	public boolean isFinishedAfterSingleRun() {
		return finishedAfterSingleRun;
	}

	public void setFinishedAfterSingleRun(boolean finishedAfterSingleRun) {
		this.finishedAfterSingleRun = finishedAfterSingleRun;
	}

}
