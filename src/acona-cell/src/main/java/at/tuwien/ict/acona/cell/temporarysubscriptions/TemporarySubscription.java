package at.tuwien.ict.acona.cell.temporarysubscriptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.communicator.CommunicatorImpl;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class TemporarySubscription implements CellFunction {
	protected static Logger log = LoggerFactory.getLogger(TemporarySubscription.class);
	
	private final Cell cell;
	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<Datapoint>();
	private final String subscriptionAddress;
	//private static final String ID = "S";
	//private final String agentName;
	private int timeout = 10000;
	private final String functionName;
	private final Map<String, DatapointConfig> subscriptions = new HashMap<String, DatapointConfig>();
	
	public TemporarySubscription(Cell cell, String subscriptionAddress, String agentName, int timeout) {
		this.functionName = "TempSubscription-" + subscriptionAddress; 
		
		//this.queue = queue;
		//Get variables
		this.cell = cell;
		this.subscriptionAddress =  subscriptionAddress;
		//this.agentName = agentName;
		this.timeout = timeout;
		//Register datapoint
		this.subscriptions.put(this.subscriptionAddress, DatapointConfig.newConfig(this.subscriptionAddress, this.subscriptionAddress, agentName));
		
		//Register in cell activator
		this.cell.getFunctionHandler().registerCellFunctionInstance(this);
		
	}
	
	public Datapoint getDatapoint() throws Exception {
		Datapoint result = null;
		
		try {
			log.trace("Poll temp queue");
			result = this.queue.poll(timeout, TimeUnit.MILLISECONDS);
			log.trace("Result recieved={}", result);
			if (result==null) {
				log.error("Timeouterror");
				throw new Exception("Timeout");
			}
		} catch (InterruptedException e) {
			log.error("Message received", result);
			throw new Exception(e.getMessage());
		} finally {
			this.cell.getFunctionHandler().deregisterActivatorInstance(this);
		}
		
		return result;
	}

	@Override
	public CellFunction init(CellFunctionConfig config, Cell cell) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateData(Map<String, Datapoint> data) throws Exception {
		log.debug("Received update message for temp subscription={}", data);
		Datapoint dp = data.get(subscriptionAddress);
		this.queue.put(dp);
		//After data was put in the queue, deregister subscription
		this.setExit();
		
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
	public void setStart() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setStop() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setPause() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void setExit() {
		this.cell.getFunctionHandler().deregisterActivatorInstance(this);
	}

	@Override
	public CellFunctionConfig getFunctionConfig() {
		throw new UnsupportedOperationException();
		//return null;
	}	
	
	
}
