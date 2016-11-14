package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

@Deprecated
public class TemporarySubscription implements CellFunction {
	protected static Logger log = LoggerFactory.getLogger(TemporarySubscription.class);

	private final static String METHODGETDATAPOINT = "getDatapoint";

	private final Cell cell;
	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<Datapoint>();
	private final String subscriptionAddress;
	// private static final String ID = "S";
	private int timeout = 10000;
	private final String functionName;
	private final Map<String, DatapointConfig> subscriptions = new HashMap<String, DatapointConfig>();
	private boolean isExitSet = false;

	public TemporarySubscription(Cell cell, String subscriptionAddress, String agentName, int timeout) {
		this.functionName = "TempSubscription" + this.hashCode() + "-" + subscriptionAddress;

		// this.queue = queue;
		// Get variables
		this.cell = cell;
		this.subscriptionAddress = subscriptionAddress;
		// this.agentName = agentName;
		this.timeout = timeout;
		// Register datapoint
		this.subscriptions.put(this.subscriptionAddress, DatapointConfig.newConfig(this.subscriptionAddress,
				this.subscriptionAddress, agentName, SyncMode.push));

		// Register in cell activator
		this.cell.getFunctionHandler().registerCellFunctionInstance(this);
	}

	@Override
	public CellFunction init(CellFunctionConfig config, Cell cell) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateSubscribedData(Map<String, Datapoint> data, String caller) throws Exception {
		log.debug("Received update message for temp subscription={}", data);
		Datapoint dp = data.get(subscriptionAddress);
		this.queue.put(dp);
		// After data was put in the queue, deregister subscription
		// Check if deregister has been executed before
		if (isExitSet == false) {
			this.shutDown();
			this.isExitSet = true;
		}

	}

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public Map<String, DatapointConfig> getSubscribedDatapoints() {
		return this.subscriptions;
	}

	@Override
	public void shutDown() {
		this.cell.getFunctionHandler().deregisterActivatorInstance(this);
	}

	@Override
	public CellFunctionConfig getFunctionConfig() {
		return CellFunctionConfig.newConfig(functionName, TemporarySubscription.class);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TempSubscription: cell=");
		builder.append(cell);
		builder.append(", subscriptionAddress=");
		builder.append(subscriptionAddress);
		return builder.toString();
	}

	public Datapoint getDatapoint() throws Exception {
		Datapoint result = null;

		try {
			log.trace("Poll temp queue");
			result = this.queue.poll(timeout, TimeUnit.MILLISECONDS);
			log.trace("Result recieved={}", result);
			if (result == null) {
				log.error("Timeouterror");
				throw new Exception("Timeout");
			}
		} catch (InterruptedException e) {
			log.error("Message received", result);
			throw new Exception(e.getMessage());
		} finally {
			// If deregister has not been executed yet, do it
			if (isExitSet == false) {
				this.shutDown();
				// this.cell.getFunctionHandler().deregisterActivatorInstance(this);
				this.isExitSet = true;
			}

		}

		return result;
	}

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

}
