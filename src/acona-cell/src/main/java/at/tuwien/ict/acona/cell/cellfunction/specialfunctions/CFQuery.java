package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CFQuery extends CellFunctionImpl {

	protected static Logger log = LoggerFactory.getLogger(CFQuery.class);

	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<>();

	private String resultAddress = "";
	private boolean isExitSet = false;

	public static Datapoint newQuery(String destinationAgentName, String destinationAddress, JsonElement content, String resultAgentName, String resultAddress, int timeout, Cell cell) throws Exception {
		Datapoint result = null;

		CFQuery instance = new CFQuery();
		try {
			//create and register instance
			String name = "CFQuery_" + destinationAddress + "_" + resultAddress;
			log.trace("Service {}>Initialize with dest={}:{}, result={}:{}", name, destinationAgentName, destinationAddress, resultAgentName, resultAddress);
			instance.init(CellFunctionConfig.newConfig(name, CFQuery.class).addManagedDatapoint(resultAddress, resultAddress, resultAgentName, SyncMode.SUBSCRIBEONLY), cell);

			//Execute the function method
			result = instance.query(destinationAgentName, destinationAddress, content, resultAgentName, resultAddress, timeout);

			//Deregister
			//instance.shutDown();

		} catch (Exception e) {
			log.error("Query error");
			throw new Exception(e);
		} finally {
			//Deregister
			instance.shutDown();
		}

		return result;
	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void cellFunctionInit() throws Exception {

	}

	@Override
	protected void shutDownImplementation() {
		log.trace("Service {}>Shut down.", this.getFunctionConfig().getName());

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		try {
			log.debug("Service {}>Received update message for temp subscription={}", this.getFunctionConfig().getName(), data);
			Datapoint dp = data.get(resultAddress);

			this.queue.put(dp);

		} catch (InterruptedException e) {
			log.error("Service {}>Cannot receive data through subscription", this.getFunctionConfig().getName());
		}
	}

	private Datapoint query(String destinationAgentName, String destinationAddress, JsonElement content, String resultAgentName, String resultAddress, int timeout) throws Exception {
		Datapoint result = null;
		this.resultAddress = resultAddress;
		//this.resultAgent = resultAgentName;

		try {
			//subscribe
			//this.getCommunicator().subscribe(resultAddress, resultAgentName);

			//write to destination
			List<Datapoint> sendlist = Arrays.asList(Datapoints.newDatapoint(destinationAddress).setValue(content));
			this.getCommunicator().write(destinationAgentName, sendlist, timeout, false);

			try {
				log.trace("Service {}>Poll temp queue", this.getFunctionConfig().getName());
				result = this.queue.poll(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("Message received", result);
				throw new Exception(e.getMessage());
			}

			log.trace("Service {}>Result recieved={}", this.getFunctionConfig().getName(), result);
			if (result == null) {
				log.error("Service {}>Timeouterror", this.getFunctionConfig().getName());
				throw new Exception("Timeout on service " + this.getFunctionConfig().getName());
			}
		} catch (Exception e) {
			log.error("Service {}>Error on receiving data", this.getFunctionConfig().getName());
			throw new Exception(e);
		} finally {
			// If deregister has not been executed yet, do it
			//			if (isExitSet == false) {
			//				this.shutDown();
			//				this.isExitSet = true;
			//			}
		}

		return result;
	}

}
