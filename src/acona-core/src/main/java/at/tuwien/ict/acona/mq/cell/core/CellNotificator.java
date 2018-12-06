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
		// threadPoolExecutor.shutdown();
		// while (!threadPoolExecutor.isTerminated()) {
		//
		// }
	}

//	private class WorkerThread implements Runnable {
//
//		private final List<String> subscribers;
//		private final String caller;
//		private final Datapoint subscribedData;
//		private final CellFunctionHandler activationHandler;
//		private final BasicServiceCommunicator communicator;
//		private final String localName;
//
//		public WorkerThread(CellFunctionHandler activationHandler, BasicServiceCommunicator communicator, List<String> subscribers, String caller, String localName, Datapoint subscribedData) {
//			this.communicator = communicator;
//			this.activationHandler = activationHandler;
//			this.subscribers = subscribers;
//			this.caller = caller;
//			this.localName = localName;
//			this.subscribedData = subscribedData;
//			// Thread.currentThread().setName("Subscriberpool-localName-" + this.hashCode());
//		}
//
//		@Override
//		public void run() {
//			log.trace("running subscription notification for data={} and subscribers={}", this.subscribedData.getAddress() + ":" + this.subscribedData.getValue(), this.subscribers);
//
//			if (subscribers.isEmpty() == false) {
//				subscribers.forEach(s -> {
//					try {
//						this.communicator.notifySubscriber(s, subscribedData);
//					} catch (Exception e) {
//						log.error("Cannot notify datapoint={} to subscriber={}", subscribedData, s, e);
//					}
//				});
//			}
//
//		}

//		@Override
//		public String toString() {
//			StringBuilder builder = new StringBuilder();
//			builder.append("WorkerThread [subscribers=");
//			builder.append(subscribers);
//			builder.append(", caller=");
//			builder.append(caller);
//			builder.append(", subscribedData=");
//			builder.append(subscribedData);
//			builder.append(", activationHandler=");
//			builder.append(activationHandler);
//			builder.append(", communicator=");
//			builder.append(communicator);
//			builder.append(", localName=");
//			builder.append(localName);
//			builder.append("]");
//			return builder.toString();
//		}
//
//	}

}
