package at.tuwien.ict.acona.mq.cell.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.storage.DataStorageSubscriberNotificator;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

/**
 * The cell notifier notifies subscribers at every cell update. It runs as a thread, in order not to block the database.
 * 
 * @author wendt
 *
 */
public class CellNotificator implements DataStorageSubscriberNotificator {

	private final MqttCommunicator comm;
	protected static Logger log = LoggerFactory.getLogger(CellNotificator.class);

	// private final int corePoolSize = 5;
	// private final int maxPoolSize = 10;
	// private final long keepAliveTime = 10000;

	// private ExecutorService threadPoolExecutor;

	public CellNotificator(MqttCommunicator comm) {
		this.comm = comm;
		// threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	@Override
	public void notifySubscribers(Datapoint subscribedData) {
		// threadPoolExecutor.execute(new WorkerThread(cell.getFunctionHandler(), cell.getCommunicator(), subscribers, caller, cell.getLocalName(), subscribedData));
		try {
			this.comm.publishDatapoint(subscribedData);
		} catch (Exception e) {
			log.error("Cannot notify subscribers for datapoint", subscribedData, e);
		}
	}

	public void shutDown() {
		log.debug("Shut down notificator");
	}


}
