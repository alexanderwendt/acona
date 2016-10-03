package at.tuwien.ict.acona.framework.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

/**
 * Service function
 * 1. Register DF service from function name
 * 2. Initialize=subscribe datapoints to offer with start values
 * 3. Subscribe datapoints for this function
 * 4. If update datapoints is executed, do start command or other update
 * 5. At command, start function
 * 6. At end, write subscribed datapoints to remote datapoints from local datapoints
 * 
 * 
 * @author wendt
 *
 */
public abstract class AconaService extends CellFunctionThreadImpl {

	private final static String COMMANDDATAPOINTNAME = "command";
	private final static String STATEDATAPOINTNAME = "state";
	private final static String DESCRIPTIONDATAPOINTNAME = "description";
	private final static String PARAMETERDATAPOINTNAME = "parameter";
	private final static String CONFIGDATAPOINTNAME = "config";
	

	@Override
	protected void cellFunctionInternalInit() throws Exception {
		//1. Register DF service from function name
		//this.getCell().registerService(this.getFunctionName());
		//2. Initialize=subscribe datapoints to offer with start values
		initServiceDatapoints(this.getFunctionName());
		//3. Subscribe custom datapoints for this function
		//This should be done in the config file
		serviceInit();
		
	}
	
	protected abstract void serviceInit();
	
	private void initServiceDatapoints(String serviceName) throws Exception {
		//List<Datapoint> result = new ArrayList<Datapoint>();
		Datapoint command = Datapoint.newDatapoint(serviceName + "." + COMMANDDATAPOINTNAME).setValue(ControlCommand.STOP.toString());
		Datapoint state = Datapoint.newDatapoint(serviceName + "." + STATEDATAPOINTNAME).setValue(ServiceState.STOPPED.toString());
		Datapoint description = Datapoint.newDatapoint(serviceName + "." + DESCRIPTIONDATAPOINTNAME).setValue("Service " + this.getFunctionName());
		Datapoint parameter = Datapoint.newDatapoint(serviceName + "." + PARAMETERDATAPOINTNAME).setValue("");
		Datapoint config = Datapoint.newDatapoint(serviceName + "." + CONFIGDATAPOINTNAME).setValue("");
		
		this.getSubscribedDatapoints().put(COMMANDDATAPOINTNAME, DatapointConfig.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME));
		this.getSubscribedDatapoints().put(STATEDATAPOINTNAME, DatapointConfig.newConfig(STATEDATAPOINTNAME, STATEDATAPOINTNAME));
		this.getSubscribedDatapoints().put(DESCRIPTIONDATAPOINTNAME, DatapointConfig.newConfig(DESCRIPTIONDATAPOINTNAME, DESCRIPTIONDATAPOINTNAME));
		this.getSubscribedDatapoints().put(PARAMETERDATAPOINTNAME, DatapointConfig.newConfig(PARAMETERDATAPOINTNAME, PARAMETERDATAPOINTNAME));
		this.getSubscribedDatapoints().put(CONFIGDATAPOINTNAME, DatapointConfig.newConfig(CONFIGDATAPOINTNAME, CONFIGDATAPOINTNAME));
		
		this.getCommunicator().write(Arrays.asList(command, state, description, parameter, config));
		
	}

	@Override
	protected void executePostProcessing() throws Exception {
		 // 6. At end, write subscribed datapoints to remote datapoints from local datapoints
		this.getSubscribedDatapoints().values().forEach(config->{
			try {
				this.getCommunicator().write(this.readLocal(config.getAddress()), config.getAgentid());
			} catch (Exception e) {
				log.error("Cannot write datapoint {} to remote memory module", config, e);
			}
		});
		
		this.writeLocal(Datapoint.newDatapoint(this.getSubscribedDatapoints().get(COMMANDDATAPOINTNAME).getAddress()).setValue(ControlCommand.PAUSE.toString()));
		this.writeLocal(Datapoint.newDatapoint(this.getSubscribedDatapoints().get(STATEDATAPOINTNAME).getAddress()).setValue(ServiceState.STOPPED.toString()));
	}

	@Override
	protected void executePreProcessing() throws Exception {
		log.debug("Preprocessing");
		
	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		//4. If update datapoints is executed, do start command or other update
		//5. At command, start function
		log.info("test {}", data);
		
		
		
	}

	@Override
	protected void executeFunction() throws Exception {
		log.info("Test");
		
	}

}
