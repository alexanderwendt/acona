package at.tuwien.ict.acona.demowebservice.cellfunctions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.demowebservice.helpers.WeatherServiceClientMock;

/**
 * The user interface collector will collect values from various datapoints in the system and represent them on a user interface.
 * 
 * @author wendt
 *
 */
public class UserInterfaceCollector extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(UserInterfaceCollector.class);

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// TODO Auto-generated method stub
		
		//If you poll, use this function in the init method and set
		//this.setExecuteOnce(false);
		//this.setExecuteRate(1000); //once 1s
		
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		//Add your own service here and test it with
		//parameterdata.getMethod()
		
		
		
		return null;
	}
	
	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
		//@Lampros: If you poll a datapoints, put the datapoints to read in 
		//this.addManagedDatapoint(DatapointConfig.newConfig("testid", "test", "agenttest", SyncMode.READONLY));
		//and read like this
		//this.getValueMap().get("testid").getValue()
		
	}

	@Override
	protected void executeFunction() throws Exception {
		// TODO Auto-generated method stub
		
		//Here is the execution function, if you run the thread

		
	}
	
	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		//Cleaning after the run
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
		log.info("Got data={}", data.get("ui1").getValue());
		//@Lampros: Here, in data, you get all datapoints that you need if you are a subscriber. This information shall be presented in a user interface
		
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
