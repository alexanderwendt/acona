package at.tuwien.ict.acona.cell.core;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionHandler;
import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.storage.DataStorageSubscriberNotificator;

/**
 * The cell notifier notifies subscribers at every cell update. It runs as a
 * thread, in order not to block the database.
 * 
 * @author wendt
 *
 */
public class CellNotificator implements DataStorageSubscriberNotificator {

	private final Cell cell;
	protected static Logger log = LoggerFactory.getLogger(CellNotificator.class);

	int corePoolSize = 1;
	int maxPoolSize = 10;
	long keepAliveTime = 5000;

	public CellNotificator(Cell cell) {
		this.cell = cell;
	}

	private ExecutorService threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

	@Override
	public synchronized void notifySubscribers(List<String> subscribers, String caller, Datapoint subscribedData) {
		threadPoolExecutor.execute(new WorkerThread(cell.getFunctionHandler(), cell.getCommunicator(), subscribers, caller, cell.getLocalName(), subscribedData));

	}

	public void shutDown() {
		log.debug("Shut down notificator");
		threadPoolExecutor.shutdown();
		while (!threadPoolExecutor.isTerminated()) {

		}
	}

	private class WorkerThread implements Runnable {

		private final List<String> subscribers;
		private final String caller;
		private final Datapoint subscribedData;
		private final CellFunctionHandler activationHandler;
		private final Communicator communicator;
		private final String localName;

		public WorkerThread(CellFunctionHandler activationHandler, Communicator communicator, List<String> subscribers, String caller, String localName, Datapoint subscribedData) {
			this.communicator = communicator;
			this.activationHandler = activationHandler;
			this.subscribers = subscribers;
			this.caller = caller;
			this.localName = localName;
			this.subscribedData = subscribedData;
		}

		@Override
		public void run() {
			log.trace("running subscription notification for data={} and subscribers={}", this.subscribedData.getAddress(), this.subscribers);
			//			if (subscribers.contains(this.localName)) {
			//				log.trace("activate local behaviors for agent={}", this.localName);
			//
			//				this.activationHandler.activateNotifySubscribers(this.localName, subscribedData);
			//
			//				// Revove it from the list before sending to external
			//				// application
			//				// because this agent does not subscribe through external
			//				// subscriptions
			//				subscribers.remove(this.localName);
			//			}
			//
			//			// Remove the caller itself because the caller is writing this
			//			// datapoint
			//			if (subscribers.contains(caller)) {
			//				log.debug("caller is writing a subscribed datapoint. Remove the caller. No subscription necessary");
			//				subscribers.remove(caller);
			//			}

			// Notify external agents that subscribe a value from this data
			// storage
			if (subscribers.isEmpty() == false) {
				subscribers.forEach(s -> {
					try {
						this.communicator.notifySubscriber(subscribedData, s);
					} catch (Exception e) {
						log.error("Cannot notify datapoint={} to subscriber={}", subscribedData, s, e);
					}
				});
			}

		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("WorkerThread [subscribers=");
			builder.append(subscribers);
			builder.append(", caller=");
			builder.append(caller);
			builder.append(", subscribedData=");
			builder.append(subscribedData);
			builder.append(", activationHandler=");
			builder.append(activationHandler);
			builder.append(", communicator=");
			builder.append(communicator);
			builder.append(", localName=");
			builder.append(localName);
			builder.append("]");
			return builder.toString();
		}

	}

}
