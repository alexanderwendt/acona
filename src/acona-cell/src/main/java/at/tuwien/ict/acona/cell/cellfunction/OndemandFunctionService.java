package at.tuwien.ict.acona.cell.cellfunction;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;

/**
 * Service function 1. Register DF service from function name 2.
 * Initialize=subscribe datapoints to offer with start values 3. Subscribe
 * datapoints for this function 4. If update datapoints is executed, do start
 * command or other update 5. At command, start function 6. At end, write
 * subscribed datapoints to remote datapoints from local datapoints
 * 
 * Methods for custom settings access: Get custom property:
 * this.getConfig().getProperty(key, classtype) Get custom property: String
 * configvalue = this.getCustomSetting("test", String.class); Set custom
 * propery: this.getConfig().setProperty(key, value);
 * 
 * Methods for data access: JsonElement value =
 * this.readLocalSyncDatapointById([STRING ID FROM CONFIG], [ClassType]);
 * this.writeLocalSyncDatapointById([STRING ID FROM CONFIG, [value of
 * Gson-convertable type]);
 * 
 * Implementing custom services implement abstract class AconaService
 * 
 * @author wendt
 *
 */
@Deprecated
public abstract class OndemandFunctionService extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(OndemandFunctionService.class);

	protected String COMMANDDATAPOINTNAME = "command"; //method=controllcommand, command=start
	protected String STATEDATAPOINTNAME = "state";
	protected String DESCRIPTIONDATAPOINTNAME = "description";
	//protected String PARAMETERDATAPOINTNAME = "parameter";
	protected String CONFIGDATAPOINTNAME = "config";
	protected String RESULTDATAPOINTNAME = "result";

	protected Datapoint command, state, description, config, result;

	/**
	 * In the value map all, subscribed values as well as read values are put.
	 * Syntac: Key: Datapointid, value: Datapoint address
	 */
	protected Map<String, Datapoint> valueMap = new ConcurrentHashMap<>();

	protected SynchronousQueue<Boolean> blocker;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		log.debug("{}> Init service", this.getFunctionName());
		// 1. Register DF service from function name
		// this.getCell().registerService(this.getFunctionName());
		// 2. Initialize=subscribe datapoints to offer with start values
		initServiceDatapoints(this.getFunctionName());
		// 3. Subscribe custom datapoints for this function
		// This should be done in the config file
		// Init the table for read datapoints
		// this.getConfig().getSyncDatapoints().forEach(dpconfig->{
		// this.getSubscribedDatapoints().put(dpconfig.getId(), dpconfig);
		// });

		serviceInit();

		log.debug("{}> Service initialized", this.getFunctionName());

	}

	protected abstract void serviceInit() throws Exception;

	private void initServiceDatapoints(String serviceName) throws Exception {
		COMMANDDATAPOINTNAME = serviceName + "." + "command";
		STATEDATAPOINTNAME = serviceName + "." + "state";
		DESCRIPTIONDATAPOINTNAME = serviceName + "." + "description";
		//PARAMETERDATAPOINTNAME = serviceName + "." + "parameter";
		CONFIGDATAPOINTNAME = serviceName + "." + "config";
		RESULTDATAPOINTNAME = serviceName + "." + "result";

		command = Datapoints.newDatapoint(COMMANDDATAPOINTNAME).setValue(ControlCommand.STOP.toString());
		state = Datapoints.newDatapoint(STATEDATAPOINTNAME).setValue(ServiceState.IDLE.toString());
		description = Datapoints.newDatapoint(DESCRIPTIONDATAPOINTNAME).setValue("Service " + this.getFunctionName());
		//parameter = Datapoint.newDatapoint(PARAMETERDATAPOINTNAME).setValue("");
		config = Datapoints.newDatapoint(CONFIGDATAPOINTNAME).setValue("");
		result = Datapoints.newDatapoint(RESULTDATAPOINTNAME).setValue("");

		log.debug("Subscribe the following datapoints:\ncommand: {}\nstate: {}\ndescription: {}\nparameter: {}\nconfig: {}",
				command.getAddress(), state.getAddress(), description.getAddress(),
				config.getAddress(), result.getAddress());

		//Add subscriptions
		this.addManagedDatapoint(DatapointConfig.newConfig(command.getAddress(), command.getAddress(), SyncMode.SUBSCRIBEONLY));
		this.addManagedDatapoint(DatapointConfig.newConfig(state.getAddress(), state.getAddress(), SyncMode.SUBSCRIBEONLY));
		this.addManagedDatapoint(DatapointConfig.newConfig(description.getAddress(), description.getAddress(), SyncMode.SUBSCRIBEONLY));
		//this.addManagedDatapoint(DatapointConfig.newConfig(parameter.getAddress(), parameter.getAddress(), SyncMode.SUBSCRIBEONLY));
		this.addManagedDatapoint(DatapointConfig.newConfig(config.getAddress(), config.getAddress(), SyncMode.SUBSCRIBEONLY));
		//Result will only be written

		this.getCommunicator().write(Arrays.asList(command, state, description, config, result));

	}

	@Override
	protected void executePreProcessing() throws Exception {
		// Read all values from the store or other agent
		log.info("{}>Start preprocessing by reading function variables={}", this.getFunctionName(), this.getReadDatapoints());

		this.getReadDatapoints().forEach((k, v) -> {
			try {
				// Read the remote datapoint
				if (v.getAgentid(this.getCell().getLocalName()).equals(this.getCell().getLocalName()) == false) {
					Datapoint temp = this.getCommunicator().read(v.getAddress(), v.getAgentid(this.getCell().getLocalName()));
					// Write local value to synchronize the datapoints
					this.valueMap.put(k, temp);
					log.trace("{}> Preprocessing phase: Read datapoint and write local={}", temp);
				}
			} catch (Exception e) {
				log.error("{}>Cannot read datapoint={}", this.getFunctionName(), v, e);
			}
		});

	}

	@Override
	protected void executePostProcessing() throws Exception {
		// FIXME: The update here is not working well
		log.debug("{}>Execute post processing for the datapoints={}", this.getFunctionName(), this.getWriteDatapoints());
		// 6. At end, write subscribed datapoints to remote datapoints from
		// local datapoints
		this.getWriteDatapoints().values().forEach(config -> {
			try {
				Datapoint dp = this.valueMap.get(config.getAddress());
				String agentName = config.getAgentid(this.getCell().getLocalName());
				this.getCommunicator().write(agentName, dp);
				log.trace("{}>Written datapoint={} to agent={}", this.getFunctionName(), dp, agentName);
			} catch (Exception e) {
				log.error("{}>Cannot write datapoint {} to remote memory module", this.getFunctionName(), config, e);
			}
		});

		this.writeLocal(this.command.setValue(ControlCommand.PAUSE.toString()));
		this.writeLocal(this.state.setValue(ServiceState.IDLE.toString()));

		log.info("{}>Service execution finished", this.getFunctionName());
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		log.trace("{}>Update datapoints={}. Command name={}", this.getFunctionName(), data, command.getAddress());
		// 4. If update datapoints is executed, do start command or other update

		//		// Update parameters
		//		if (data.containsKey(this.parameter.getAddress())) {
		//			log.info("New parameter set={}", data.get(parameter).getValue());
		//
		//			this.parameter.setValue(data.get(this.parameter.getAddress()).getValue());
		//		}

		// Update command
		if (data.containsKey(command.getAddress())
				&& data.get(command.getAddress()).getValue().toString().equals("{}") == false) {
			try {
				this.setCommand(data.get(command.getAddress()).getValueAsString());
			} catch (Exception e) {
				log.error("{}>Cannot execute command={}", this.getFunctionName(), data.get(command.getAddress()).getValueAsString(), e);
			}
		}

		// Update config
		if (data.containsKey(this.config.getAddress())) {
			log.info("New config set={}", data.get(config).getValue());

			data.keySet().forEach(key -> {
				this.getFunctionConfig().setProperty(key, data.get(key).getValue());
			});
		}

		// Else
		if (data.containsKey(command.getAddress()) == false) {
			log.info("{}>Datapoint {} received. Expected datapoints={}", this.getFunctionName(), data.values(), this.getSubscribedDatapoints().values());
		}

		valueMap.putAll(data);

	}

	@Override
	protected abstract void executeFunction() throws Exception;

}
