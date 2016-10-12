package at.tuwien.ict.acona.framework.modules;

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

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
public abstract class AconaFunctionService extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(AconaFunctionService.class);

	private String COMMANDDATAPOINTNAME = "command";
	private String STATEDATAPOINTNAME = "state";
	private String DESCRIPTIONDATAPOINTNAME = "description";
	private String PARAMETERDATAPOINTNAME = "parameter";
	private String CONFIGDATAPOINTNAME = "config";

	@Override
	protected void cellFunctionInternalInit() throws Exception {
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

	protected abstract void serviceInit();

	private void initServiceDatapoints(String serviceName) throws Exception {
		COMMANDDATAPOINTNAME = serviceName + "." + "command";
		STATEDATAPOINTNAME = serviceName + "." + "state";
		DESCRIPTIONDATAPOINTNAME = serviceName + "." + "description";
		PARAMETERDATAPOINTNAME = serviceName + "." + "parameter";
		CONFIGDATAPOINTNAME = serviceName + "." + "config";

		Datapoint command = Datapoint.newDatapoint(COMMANDDATAPOINTNAME).setValue(ControlCommand.STOP.toString());
		Datapoint state = Datapoint.newDatapoint(STATEDATAPOINTNAME).setValue(ServiceState.STOPPED.toString());
		Datapoint description = Datapoint.newDatapoint(DESCRIPTIONDATAPOINTNAME).setValue("Service " + this.getFunctionName());
		Datapoint parameter = Datapoint.newDatapoint(PARAMETERDATAPOINTNAME).setValue("");
		Datapoint config = Datapoint.newDatapoint(CONFIGDATAPOINTNAME).setValue("");

		log.trace("Subscribe the following datapoints:\ncommand: {}\nstate: {}\ndescription: {}\nparameter: {}\nconfig: {}", command.getAddress(), state.getAddress(), description.getAddress(),
				parameter.getAddress(), config.getAddress());

		this.getSubscribedDatapoints().put(command.getAddress(), DatapointConfig.newConfig(command.getAddress(), command.getAddress(), SyncMode.push));
		this.getSubscribedDatapoints().put(state.getAddress(), DatapointConfig.newConfig(state.getAddress(), state.getAddress(), SyncMode.push));
		this.getSubscribedDatapoints().put(description.getAddress(), DatapointConfig.newConfig(description.getAddress(), description.getAddress(), SyncMode.push));
		this.getSubscribedDatapoints().put(parameter.getAddress(), DatapointConfig.newConfig(parameter.getAddress(), parameter.getAddress(), SyncMode.push));
		this.getSubscribedDatapoints().put(config.getAddress(), DatapointConfig.newConfig(config.getAddress(), config.getAddress(), SyncMode.push));

		this.getCommunicator().write(Arrays.asList(command, state, description, parameter, config));

	}

	@Override
	protected void executePreProcessing() throws Exception {
		// Read all values from the store or other agent
		log.info("{}>Start preprocessing by reading function variables={}", this.getFunctionName(), this.getReadDatapoints());
		this.getReadDatapoints().forEach((k, v) -> {
			try {
				// Read the remote datapoint
				if (v.getAgentid().equals(this.getCell().getLocalName()) == false) {
					Datapoint temp = this.getCommunicator().read(v.getAddress(), v.getAgentid());
					// Write local value to synchronize the datapoints
					this.writeLocal(temp);
					log.trace("{}> Preprocessing phase: Read datapoint and write local={}", temp);
				}
			} catch (Exception e) {
				log.error("{}>Cannot read datapoint={}", this.getFunctionName(), v, e);
			}
		});

	}

	@Override
	protected void executePostProcessing() throws Exception {
		log.debug("{}>Execute post processing for the datapoints={}", this.getFunctionName(), this.getReadDatapoints());
		// 6. At end, write subscribed datapoints to remote datapoints from
		// local datapoints
		this.getReadDatapoints().values().forEach(config -> {
			try {
				Datapoint dp = this.readLocal(config.getAddress());
				String agentName = config.getAgentid();
				this.getCommunicator().write(dp, agentName);
				log.trace("{}>Written datapoint={} to agent={}", this.getFunctionName(), dp, agentName);
			} catch (Exception e) {
				log.error("{}>Cannot write datapoint {} to remote memory module", this.getFunctionName(), config, e);
			}
		});

		this.writeLocal(Datapoint.newDatapoint(this.getSubscribedDatapoints().get(COMMANDDATAPOINTNAME).getAddress())
				.setValue(ControlCommand.PAUSE.toString()));
		this.writeLocal(Datapoint.newDatapoint(this.getSubscribedDatapoints().get(STATEDATAPOINTNAME).getAddress())
				.setValue(ServiceState.STOPPED.toString()));

		log.info("{}>Service execution finished", this.getFunctionName());
	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		log.trace("{}>Update datapoints={}. Command name={}", this.getFunctionName(), data, COMMANDDATAPOINTNAME);
		// 4. If update datapoints is executed, do start command or other update
		if (data.containsKey(COMMANDDATAPOINTNAME) && data.get(COMMANDDATAPOINTNAME).getValue().toString().equals("{}") == false) {
			try {
				this.setCommand(data.get(COMMANDDATAPOINTNAME).getValueAsString());
			} catch (Exception e) {
				log.error("{}>Cannot execute command={}", this.getFunctionName(), data.get(COMMANDDATAPOINTNAME).getValueAsString(), e);
			}
		} else {
			log.info("{}>Datapoint {} received. Expected datapoints={}", this.getFunctionName(), data.values(), this.getSubscribedDatapoints().values());
		}

	}

	@Override
	protected abstract void executeFunction() throws Exception;

}
