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
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * @author wendt
 * 
 *         Set a certain value in a datapoint and wait for a subscribed value to
 *         be returned.
 * 
 *
 *
 */
public class CFQuery extends CellFunctionImpl {

	protected static Logger log = LoggerFactory.getLogger(CFQuery.class);

	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<>();

	private String resultAddress = "";
	private JsonElement resultContent = null;
	//private boolean isExitSet = false;

	public Datapoint newQuery(String destinationAgentName, String destinationAddress, JsonElement sendcontent, String resultAgentName, String resultAddress, JsonElement resultcontent, int timeout, Cell cell) throws Exception {
		Datapoint result = null;

		//CFQuery instance = new CFQuery();
		try {
			//create and register instance
			String name = "CFQuery_" + destinationAddress + "_" + resultAddress;
			log.trace("Service {}>Initialize with dest={}:{} content={}, result={}:{}, content={}", name, destinationAgentName, destinationAddress, sendcontent, resultAgentName, resultAddress, resultcontent);
			this.init(CellFunctionConfig.newConfig(name, CFQuery.class).addManagedDatapoint(resultAddress, resultAddress, resultAgentName, SyncMode.SUBSCRIBEONLY), cell);

			//Execute the function method
			result = this.query(destinationAgentName, destinationAddress, sendcontent, resultAgentName, resultAddress, resultcontent, timeout);

			//Deregister
			//instance.shutDown();

		} catch (Exception e) {
			log.error("Query error");
			throw new Exception(e);
		} finally {
			//Deregister
			this.shutDown();
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

			if ((resultContent != null) && (resultContent.isJsonNull() == false)) {
				if (dp.getValue().equals(resultContent)) {
					log.debug("Received trigger value={}", dp.getValue(), resultContent);
					this.queue.put(dp);
				} else {
					log.debug("recieved a non-triggering value={}. Expected={}", dp.getValue(), resultContent);
				}
			} else {
				this.queue.put(dp);
			}

		} catch (InterruptedException e) {
			log.error("Service {}>Cannot receive data through subscription", this.getFunctionConfig().getName());
		}
	}

	private Datapoint query(String destinationAgentName, String destinationAddress, JsonElement sendContent, String resultAgentName, String resultAddress, JsonElement resultContent, int timeout) throws Exception {
		Datapoint result = null;
		this.resultAddress = resultAddress;
		this.resultContent = resultContent;
		//this.resultAgent = resultAgentName;

		try {
			//subscribe
			//this.getCommunicator().subscribe(resultAddress, resultAgentName);

			//write to destination
			List<Datapoint> sendlist = Arrays.asList(DatapointBuilder.newDatapoint(destinationAddress).setValue(sendContent));
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
				log.error("Service {}>Timeouterror after {}ms. Expected datapoint={}, value={}", this.getFunctionConfig().getName(), timeout, resultAgentName + ":" + resultAddress, resultContent);
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
