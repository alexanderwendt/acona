package at.tuwien.ict.acona.cell.communicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFQuery;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CommunicatorImpl extends BaseCommunicatorImpl implements Communicator {

	protected static Logger log = LoggerFactory.getLogger(CommunicatorImpl.class);

	private static final String READSERVICENAME = "read";
	private static final String WRITESERVICENAME = "write";
	private static final String SUBSCRIBESERVICENAME = "subscribe";
	private static final String UNSUBSCRIBESERVICENAME = "unsubscribe";
	private static final String NOTIFYSERVICENAME = "notify";

	//private int defaultTimeout = 10000;

	//private final CellImpl cell;
	//private final DataStorage datastorage;
	//private final CellFunctionHandler cellFunctions;
	private final static Gson gson = new Gson();
	//private final ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	public CommunicatorImpl(CellImpl cell) {
		super(cell);
		//this.cell = cell;
		//this.datastorage = this.cell.getDataStorage();
		//this.cellFunctions = this.cell.getFunctionHandler();

		// Add responders to the agent
		// this.createBasicServiceBehaviors(this.cell);
	}

	// /**
	// * Create the basic services of the cell
	// *
	// * @param useThreadedBehaviours
	// */
	// private void createBasicServiceBehaviors(CellImpl cell) {
	// List<Behaviour> behaviours = new ArrayList<Behaviour>();
	// behaviours.add(new AconaServiceBehaviour(cell, AconaServiceType.READ));
	// behaviours.add(new AconaServiceBehaviour(cell, AconaServiceType.WRITE));
	// behaviours.add(new AconaServiceBehaviour(this.cell,
	// AconaServiceType.SUBSCRIBE));
	// behaviours.add(new AconaServiceBehaviour(this.cell,
	// AconaServiceType.UNSUBSCRIBE));
	// behaviours.add(new AconaServiceBehaviour(cell, AconaServiceType.QUERY));
	//
	// ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
	// behaviours.forEach(b -> {
	// if (useThreadedBehaviours == true) {
	// cell.addBehaviour(tbf.wrap(b));
	// } else {
	// cell.addBehaviour(b);
	// }
	// });
	// }

	@Override
	public List<Datapoint> read(List<String> datapoints) throws Exception {
		return read(datapoints, this.getLocalAgentName(), defaultTimeout);
	}

	@Override
	public List<Datapoint> read(List<String> datapointaddress, String agentName, int timeout) throws Exception {
		final List<Datapoint> result = new ArrayList<>();
		List<Datapoint> inputList = new ArrayList<>();

		try {
			//Make the inputlist a parameter
			datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			result.addAll(this.execute(agentName, READSERVICENAME, inputList, timeout));

			if (result.isEmpty() == false && result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot read values. Error returned from destination");
			}
		} catch (Exception e) {
			log.error("Cannot read value for addresses={} of agent={}", datapointaddress, agentName);
			throw new Exception(e.getMessage());
		}

		return result;

		//		// If a local data storage is meant, then write it there, else a foreign
		//		// data storage is meant.
		//		if (agentName.equals(this.getLocalAgentName()) == true) {
		//			// readDatapoints = new ArrayList<Datapoint>();
		//			datapointaddress.forEach(dp -> {
		//				result.add(this.cell.getDataStorage().read(dp));
		//			});
		//
		//			result.forEach(dp -> {
		//				this.datastorage.write(dp, this.getLocalAgentName());
		//			});
		//		} else {
		//			// Create a InitiatorBehaviour to write the datapoints to the target
		//			// agent if that agent is external
		//			ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
		//			requestMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		//			requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		//			requestMsg.setOntology(AconaServiceType.READ.toString());
		//
		//			// Conversion from datapoints to JSON
		//			// Type listOfTestObject = new
		//			// TypeToken<List<Datapoint>>(){}.getType();
		//			// String serializedDatapoints = gson.toJson(datapoints,
		//			// listOfTestObject);
		//			JsonArray object = new JsonArray();
		//
		//			datapointaddress.forEach(dp -> {
		//				object.add(Datapoint.newDatapoint(dp).toJsonObject());
		//			});
		//			// String serializedDatapoints = gson.toJson(datapoints);
		//			// List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
		//
		//			requestMsg.setContent(object.toString());
		//
		//			// Blocking read and write
		//			SynchronousQueue<List<Datapoint>> queue = new SynchronousQueue<List<Datapoint>>();
		//			this.cell.addBehaviour(tbf.wrap(new ReadDatapointBehaviour(this.cell, requestMsg, queue)));
		//			try {
		//				result.addAll(queue.poll(timeout, TimeUnit.MILLISECONDS));
		//				if (result.isEmpty()) {
		//					throw new Exception("Operation timed out after " + timeout + "ms.");
		//				}
		//			} catch (InterruptedException e) {
		//				log.warn("Queue interrupted");
		//			}
		//		}
		//
		//		return result;
	}

	@Override
	public Datapoint read(String datapoint, String agentName) throws Exception {
		return read(datapoint, agentName, defaultTimeout);
	}

	@Override
	public Datapoint read(String datapoint, String agentName, int timeout) throws Exception {
		List<Datapoint> list = read(Arrays.asList(datapoint), agentName, timeout);

		Datapoint result = null;
		if (list.isEmpty()) {
			throw new Exception("Cannot read datapoint" + datapoint);
		} else {
			result = list.get(0);
		}

		return result;
	}

	@Override
	public Datapoint read(String datapointName) throws Exception {
		return this.read(datapointName, this.getLocalAgentName());
	}

	@Override
	public void write(List<Datapoint> datapoints) throws Exception {
		this.write(datapoints, this.getLocalAgentName(), defaultTimeout, true);
	}

	//	@Override
	//	public void writeNonblocking(Datapoint datapoint, String agentName) throws Exception {
	//		this.write(Arrays.asList(datapoint), agentName, this.defaultTimeout, false);
	//	}

	@Override
	public void write(List<Datapoint> datapoints, String agentComplementedName, int timeout, boolean blocking) throws Exception {
		// If a local data storage is meant, then write it there, else a foreign
		// data storage is meant.
		//		String agentName = agentComplementedName;
		//		if (agentComplementedName == null || agentComplementedName.isEmpty() || agentComplementedName.equals("")) {
		//			agentName = this.getLocalAgentName();
		//		}

		List<Datapoint> result;

		try {
			result = this.execute(agentComplementedName, WRITESERVICENAME, datapoints, timeout);

			if (result.isEmpty() == false && result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot write values. Error returned from destination");
			}
		} catch (Exception e) {
			log.error("Cannot write value for addresses={} of agent={}", datapoints, agentComplementedName);
			throw new Exception(e.getMessage());
		}

		//		if (agentName.equals(this.cell.getLocalName()) == true) {
		//			datapoints.forEach(dp -> {
		//				this.datastorage.write(dp, this.cell.getLocalName());
		//			});
		//		} else {
		//			// Create a InitiatorBehaviour to write the datapoints to the target
		//			// agent if that agent is external
		//			ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
		//			requestMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		//			requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		//			requestMsg.setOntology(AconaServiceType.WRITE.toString());
		//
		//			// Conversion from datapoints to JSON
		//			// Type listOfTestObject = new
		//			// TypeToken<List<Datapoint>>(){}.getType();
		//			// String serializedDatapoints = gson.toJson(datapoints,
		//			// listOfTestObject);
		//			JsonArray object = new JsonArray();
		//			datapoints.forEach(dp -> {
		//				object.add(dp.toJsonObject());
		//			});
		//			// String serializedDatapoints = gson.toJson(datapoints);
		//			// List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
		//
		//			requestMsg.setContent(object.toString());
		//
		//			// Blocking read and write
		//			SynchronousQueue<Boolean> queue = new SynchronousQueue<Boolean>();
		//
		//			this.cell.addBehaviour(tbf.wrap(new WriteDatapointBehaviour(this.cell, requestMsg, queue)));
		//			if (blocking == true) {
		//				try {
		//					boolean writeBehaviourFinished = queue.poll(timeout, TimeUnit.MILLISECONDS);
		//					if (writeBehaviourFinished == false) {
		//						throw new Exception("Operation timed out after " + timeout + "ms.");
		//					}
		//				} catch (InterruptedException e) {
		//					log.warn("Queue interrupted");
		//				}
		//			} else {
		//				// If nonblocking, then no queue is used
		//				// queue.put(true);
		//			}
		//
		//		}

	}

	@Override
	public void write(Datapoint datapoint) throws Exception {
		this.write(Arrays.asList(datapoint), this.getLocalAgentName(), defaultTimeout, true);

	}

	@Override
	public void write(Datapoint datapoint, String agentName) throws Exception {
		this.write(Arrays.asList(datapoint), agentName, defaultTimeout, true);
	}

	@Override
	public List<Datapoint> subscribe(List<String> datapointaddress, String agentName) throws Exception {
		final List<Datapoint> result = new ArrayList<>();

		List<Datapoint> inputList = new ArrayList<>();

		try {
			datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			result.addAll(this.execute(agentName, SUBSCRIBESERVICENAME, inputList, this.defaultTimeout, true));

			if (result.isEmpty() == true || result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot subscribe values. Error returned from destination");
			}

			//Add subscription
			//FIXME: Not clean solution
			//this.cellFunctions.addSubscription(functionName, address);

		} catch (Exception e) {
			log.error("Cannot subscribe value for addresses={} of agent={}", datapointaddress, agentName, e);
			throw new Exception(e.getMessage());
		}

		//		// In any matter, for a subscribed datapoint, the local storage must be
		//		// subscribed, else the datapoint is updated from
		//		// another agent, but the internal function is not notified. The
		//		// subscriber uses the writefunction to write to the local
		//		// database. Therefore, the local database has to be subscribed too.
		//		datapoints.forEach(dp -> {
		//			// Subscribe
		//			this.cell.getDataStorage().subscribeDatapoint(dp, this.getLocalAgentName());
		//			// Read the value and add to result list
		//			result.add(this.cell.getDataStorage().read(dp));
		//		});
		//
		//		// //If a local data storage is meant, then write it there, else a
		//		// foreign data storage is meant.
		//		// if (agentName.equals(this.cell.getLocalName())==true) {
		//		// //readDatapoints = new ArrayList<Datapoint>();
		//		// datapoints.forEach(dp->{
		//		// //Subscribe
		//		// this.cell.getDataStorage().subscribeDatapoint(dp, agentName);
		//		// //Read the value and add to result list
		//		// result.add(this.cell.getDataStorage().read(dp));
		//		// });
		//		//
		//		// result.forEach(dp->{this.datastorage.write(dp,
		//		// this.cell.getLocalName());});
		//		// } else {
		//		if (agentName.equals(this.getLocalAgentName()) == false) {
		//			// Create a InitiatorBehaviour to write the datapoints to the target
		//			// agent if that agent is external
		//			ACLMessage requestMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
		//			requestMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		//			requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		//			requestMsg.setOntology(AconaServiceType.SUBSCRIBE.toString());
		//
		//			// Conversion from datapoints to JSON
		//			// Type listOfTestObject = new
		//			// TypeToken<List<Datapoint>>(){}.getType();
		//			// String serializedDatapoints = gson.toJson(datapoints,
		//			// listOfTestObject);
		//			JsonArray object = new JsonArray();
		//			datapoints.forEach(dp -> {
		//				object.add(Datapoint.newDatapoint(dp).toJsonObject());
		//			});
		//			// String serializedDatapoints = gson.toJson(datapoints);
		//			// List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
		//
		//			requestMsg.setContent(object.toString());
		//
		//			// Blocking read and write
		//			SynchronousQueue<List<Datapoint>> queue = new SynchronousQueue<List<Datapoint>>();
		//			this.cell.addBehaviour(this.tbf.wrap(new SubscribeDatapointBehaviour(this.cell, requestMsg, queue)));
		//			try {
		//				List<Datapoint> list = queue.poll(this.defaultTimeout, TimeUnit.MILLISECONDS);
		//				if (list == null) {
		//					throw new Exception("Operation timed out after " + defaultTimeout + "ms.");
		//				}
		//				result.addAll(list);
		//			} catch (InterruptedException e) {
		//				log.warn("Queue interrupted");
		//			}
		//		}

		return result;
	}

	@Override
	public Datapoint subscribe(String datapointName, String agentName) throws Exception {
		Datapoint result = null;
		List<Datapoint> datapoints = this.subscribe(Arrays.asList(datapointName), agentName);
		if (datapoints.isEmpty() == false) {
			result = datapoints.get(0);
		} else {
			throw new Exception("Datapoint " + datapointName + " could not be subscribed from agent " + agentName);
		}

		return result;
	}

	@Override
	public void unsubscribe(List<String> datapointaddress, String agentName) throws Exception {

		List<Datapoint> result;

		try {
			List<Datapoint> inputList = new ArrayList<>();
			datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			result = this.execute(agentName, UNSUBSCRIBESERVICENAME, inputList, this.defaultTimeout);

			if (result.isEmpty() == false && result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot unsubscribe values. Error returned from destination");
			}
		} catch (Exception e) {
			log.error("Cannot unsubscribe value for addresses={} of agent={}", datapointaddress, agentName);
			throw new Exception(e.getMessage());
		}

		//		
		//		// All local datapoints have to be unsubscribed too, just as they have
		//		// been subscribed
		//		datapoints.forEach(dp -> {
		//			this.datastorage.unsubscribeDatapoint(dp, this.getLocalAgentName());
		//		});
		//
		//		// if (agentName.equals(this.cell.getLocalName())==true) {
		//		// datapoints.forEach(dp->{this.datastorage.unsubscribeDatapoint(dp,
		//		// this.cell.getLocalName());});
		//		// } else {
		//		if (agentName.equals(this.getLocalAgentName()) == false) {
		//			// Create a InitiatorBehaviour to write the datapoints to the target
		//			// agent if that agent is external
		//			ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
		//			requestMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		//			requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		//			requestMsg.setOntology(AconaServiceType.UNSUBSCRIBE.toString());
		//
		//			// Conversion from datapoints to JSON
		//			// Type listOfTestObject = new
		//			// TypeToken<List<Datapoint>>(){}.getType();
		//			// String serializedDatapoints = gson.toJson(datapoints,
		//			// listOfTestObject);
		//			JsonArray object = new JsonArray();
		//			datapoints.forEach(dp -> {
		//				object.add(Datapoint.newDatapoint(dp).toJsonObject());
		//			});
		//			// String serializedDatapoints = gson.toJson(datapoints);
		//			// List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
		//
		//			requestMsg.setContent(object.toString());
		//
		//			// Blocking read and write
		//			SynchronousQueue<Boolean> queue = new SynchronousQueue<Boolean>();
		//			this.cell.addBehaviour(this.tbf.wrap(new UnsubscribeDatapointBehaviour(this.cell, requestMsg, queue)));
		//			try {
		//				boolean writeBehaviourFinished = queue.poll(defaultTimeout, TimeUnit.MILLISECONDS);
		//				if (writeBehaviourFinished == false) {
		//					throw new Exception("Operation timed out after " + defaultTimeout + "ms.");
		//				}
		//			} catch (InterruptedException e) {
		//				log.warn("Queue interrupted");
		//			}
		//		}
	}

	@Override
	public void unsubscribe(String datapointName, String name) throws Exception {
		this.unsubscribe(Arrays.asList(datapointName), name);

	}

	@Override
	public void notifySubscriber(Datapoint datapoint, String agentName) throws Exception {
		//Datapoint result;

		try {
			this.execute(agentName, NOTIFYSERVICENAME, Arrays.asList(datapoint), this.defaultTimeout);

			//			if (result.isEmpty() == false && result.get(0).getValueAsString().equals(CommVocabulary.ERROR)) {
			//				throw new Exception("Cannot write values. Error returned from destination");
			//			}
		} catch (Exception e) {
			log.error("Cannot write value for addresses={} of agent={}", datapoint, agentName);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String resultAddress, int timeout) throws Exception {
		return this.queryDatapoints(writeAddress, content, this.getLocalAgentName(), resultAddress, this.getLocalAgentName(), timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String resultAddress, int timeout) throws Exception {
		return this.queryDatapoints(writeAddress, new JsonPrimitive(content), resultAddress, timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception {
		return this.queryDatapoints(writeAddress, new JsonPrimitive(content), writeAgentName, resultAddress, resultAgentName, timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception {
		//TemporarySubscription subscription = null;
		Datapoint result = null;

		try {
			result = CFQuery.newQuery(writeAgentName, writeAddress, content, resultAgentName, resultAddress, timeout, this.getCell());

			//			// SynchronousQueue<Datapoint> queue = new
			//			// SynchronousQueue<Datapoint>();
			//			// Subscribe the result and init queue
			//			subscription = new TemporarySubscription(this.getCell(), datapointwithresult.getAddress(), agentNameResult, timeout);
			//
			//			// Write the datapoint
			//			this.write(datapointtowrite, agentNameToWrite);
			//
			//			// Wait for result
			//			result = subscription.getDatapoint(); // queue.poll(timeout, // TimeUnit.MILLISECONDS);

		} catch (Exception e) {
			log.error("Cannot execute query", e);
		}

		return result;
	}
	//
	//	// === INNER CLASSES ====//
	//
	//	private class ServiceExecuteBehaviour extends SimpleAchieveREInitiator {
	//
	//		private final SynchronousQueue<String> queue;
	//		private final String serviceName;
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = 1L;
	//
	//		public ServiceExecuteBehaviour(Agent a, ACLMessage msg, SynchronousQueue<String> queue) {
	//			super(a, msg);
	//			//msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	//			this.serviceName = msg.getOntology();
	//			this.queue = queue;
	//			log.trace("Service {}>Ready to send execute request to agent={}, message={}", this.serviceName, Lists.newArrayList(msg.getAllReceiver()), msg.getContent());
	//		}
	//
	//		@Override
	//		protected void handleAgree(ACLMessage msg) {
	//			log.info("service {}>Execute agreed. Waiting for completion notification...", serviceName);
	//		}
	//
	//		@Override
	//		protected void handleInform(ACLMessage msg) {
	//			//log.info("Write operation successfully completed");
	//
	//			log.info("Service {}>Received result={}", this.serviceName, msg.getContent());
	//			releseQueue(msg.getContent());
	//		}
	//
	//		@Override
	//		protected void handleNotUnderstood(ACLMessage msg) {
	//			log.info("Service {}>Request not understood by engager agent", this.serviceName);
	//			releseQueue(msg.getContent());
	//		}
	//
	//		@Override
	//		protected void handleFailure(ACLMessage msg) {
	//			log.info("Service {}>Service failed", this.serviceName);
	//			// Get the failure reason and communicate it to the user
	//			try {
	//				AbsPredicate absPred = (AbsPredicate) myAgent.getContentManager().extractContent(msg);
	//
	//				log.warn("Service {}>The reason is: " + absPred.getTypeName(), this.serviceName);
	//			} catch (Codec.CodecException fe) {
	//				log.error("Service {}>FIPAException reading failure reason: " + fe.getMessage(), this.serviceName);
	//			} catch (OntologyException oe) {
	//				log.error("Service {}>OntologyException reading failure reason: " + oe.getMessage(), this.serviceName);
	//			}
	//
	//			releseQueue(msg.getContent());
	//		}
	//
	//		private void releseQueue(String dp) {
	//			try {
	//				queue.put(dp);
	//			} catch (InterruptedException e) {
	//				log.error("Service {}>Cannot release queue", this.serviceName, e);
	//			}
	//		}
	//	}
	//
	//	private class WriteDatapointBehaviour extends SimpleAchieveREInitiator {
	//
	//		private final SynchronousQueue<Boolean> queue;
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = 1L;
	//
	//		public WriteDatapointBehaviour(Agent a, ACLMessage msg, SynchronousQueue<Boolean> queue) {
	//			super(a, msg);
	//			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	//			this.queue = queue;
	//			log.trace("Ready to send write to agent={}, message={}", msg.getAllReceiver(), msg.getContent());
	//		}
	//
	//		@Override
	//		protected void handleAgree(ACLMessage msg) {
	//			log.info("Write operation agreed. Waiting for completion notification...");
	//		}
	//
	//		@Override
	//		protected void handleInform(ACLMessage msg) {
	//			log.info("Write operation successfully completed");
	//
	//			log.info("Received acknowledge={}", msg.getContent());
	//			releseQueue();
	//		}
	//
	//		@Override
	//		protected void handleNotUnderstood(ACLMessage msg) {
	//			log.info("Write request not understood by engager agent");
	//			releseQueue();
	//		}
	//
	//		@Override
	//		protected void handleFailure(ACLMessage msg) {
	//			log.info("Write failed");
	//			// Get the failure reason and communicate it to the user
	//			try {
	//				AbsPredicate absPred = (AbsPredicate) myAgent.getContentManager().extractContent(msg);
	//
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading failure reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading failure reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue();
	//		}
	//
	//		@Override
	//		protected void handleRefuse(ACLMessage msg) {
	//			log.info("Write refused");
	//			// Get the refusal reason and communicate it to the user
	//			try {
	//				AbsContentElementList list = (AbsContentElementList) myAgent.getContentManager().extractAbsContent(msg);
	//				AbsPredicate absPred = (AbsPredicate) list.get(1);
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading refusal reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading refusal reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue();
	//		}
	//
	//		private void releseQueue() {
	//			try {
	//				queue.put(true);
	//			} catch (InterruptedException e) {
	//				log.error("Cannot release queue", e);
	//			}
	//		}
	//	}
	//
	//	private class ReadDatapointBehaviour extends SimpleAchieveREInitiator {
	//
	//		private final SynchronousQueue<List<Datapoint>> queue;
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = 1L;
	//
	//		public ReadDatapointBehaviour(Agent a, ACLMessage msg, SynchronousQueue<List<Datapoint>> queue) {
	//			super(a, msg);
	//			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	//			this.queue = queue;
	//			log.trace("Ready to send read message.");
	//		}
	//
	//		@Override
	//		protected void handleAgree(ACLMessage msg) {
	//			log.info("Read operation agreed. Waiting for completion notification...");
	//		}
	//
	//		@Override
	//		protected void handleInform(ACLMessage msg) {
	//			log.info("Read operation successfully completed");
	//
	//			log.info("Received acknowledge={}", msg.getContent());
	//			String datapointListAsString = msg.getContent();
	//
	//			JsonArray object = gson.fromJson(datapointListAsString, JsonArray.class);
	//			// log.info("Received acknowledge={}", object);
	//			List<Datapoint> datapointList = new ArrayList<Datapoint>();
	//			object.forEach(e -> {
	//				datapointList.add(Datapoint.toDatapoint(e.getAsJsonObject()));
	//			});
	//
	//			releseQueue(datapointList);
	//		}
	//
	//		@Override
	//		protected void handleNotUnderstood(ACLMessage msg) {
	//			log.info("Write request not understood by engager agent");
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		@Override
	//		protected void handleFailure(ACLMessage msg) {
	//			log.info("Write failed");
	//			// Get the failure reason and communicate it to the user
	//			try {
	//				AbsPredicate absPred = (AbsPredicate) myAgent.getContentManager().extractContent(msg);
	//
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading failure reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading failure reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		@Override
	//		protected void handleRefuse(ACLMessage msg) {
	//			log.info("Write refused");
	//			// Get the refusal reason and communicate it to the user
	//			try {
	//				AbsContentElementList list = (AbsContentElementList) myAgent.getContentManager().extractAbsContent(msg);
	//				AbsPredicate absPred = (AbsPredicate) list.get(1);
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading refusal reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading refusal reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		private void releseQueue(List<Datapoint> list) {
	//			try {
	//				queue.put(list);
	//			} catch (InterruptedException e) {
	//				log.error("Cannot relese queue", e);
	//			}
	//		}
	//	}
	//
	//	private class SubscribeDatapointBehaviour extends SimpleAchieveREInitiator {
	//
	//		private final SynchronousQueue<List<Datapoint>> queue;
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = 1L;
	//
	//		public SubscribeDatapointBehaviour(Agent a, ACLMessage msg, SynchronousQueue<List<Datapoint>> queue) {
	//			super(a, msg);
	//			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
	//			this.queue = queue;
	//			log.trace("Ready to send subscribe message.");
	//		}
	//
	//		@Override
	//		protected void handleAgree(ACLMessage msg) {
	//			log.info("Subscribe operation agreed. Waiting for completion notification...");
	//		}
	//
	//		@Override
	//		protected void handleInform(ACLMessage msg) {
	//			log.info("Subscribe operation successfully completed");
	//
	//			log.info("Received initial values of the subscription and acknowledge={}", msg.getContent());
	//			String datapointListAsString = msg.getContent();
	//
	//			JsonArray object = gson.fromJson(datapointListAsString, JsonArray.class);
	//			// log.info("Received acknowledge={}", object);
	//			List<Datapoint> datapointList = new ArrayList<Datapoint>();
	//			object.forEach(e -> {
	//				datapointList.add(Datapoint.toDatapoint(e.getAsJsonObject()));
	//			});
	//
	//			releseQueue(datapointList);
	//		}
	//
	//		@Override
	//		protected void handleNotUnderstood(ACLMessage msg) {
	//			log.info("Subscribe request not understood by engager agent");
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		@Override
	//		protected void handleFailure(ACLMessage msg) {
	//			log.info("Subscribe failed");
	//			// Get the failure reason and communicate it to the user
	//			try {
	//				AbsPredicate absPred = (AbsPredicate) myAgent.getContentManager().extractContent(msg);
	//
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading failure reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading failure reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		@Override
	//		protected void handleRefuse(ACLMessage msg) {
	//			log.info("Subscribe refused");
	//			// Get the refusal reason and communicate it to the user
	//			try {
	//				AbsContentElementList list = (AbsContentElementList) myAgent.getContentManager().extractAbsContent(msg);
	//				AbsPredicate absPred = (AbsPredicate) list.get(1);
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading refusal reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading refusal reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		private void releseQueue(List<Datapoint> list) {
	//			try {
	//				queue.put(list);
	//			} catch (InterruptedException e) {
	//				log.error("Cannot relese queue", e);
	//			}
	//		}
	//	}
	//
	//	private class UnsubscribeDatapointBehaviour extends SimpleAchieveREInitiator {
	//
	//		private final SynchronousQueue<Boolean> queue;
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = 1L;
	//
	//		public UnsubscribeDatapointBehaviour(Agent a, ACLMessage msg, SynchronousQueue<Boolean> queue) {
	//			super(a, msg);
	//			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	//			this.queue = queue;
	//			log.trace("Ready to send unsubscribe message.");
	//		}
	//
	//		@Override
	//		protected void handleAgree(ACLMessage msg) {
	//			log.info("Unsubscribe operation agreed. Waiting for completion notification...");
	//		}
	//
	//		@Override
	//		protected void handleInform(ACLMessage msg) {
	//			log.info("Unsubscribe operation successfully completed");
	//
	//			log.info("Received acknowledge={}", msg.getContent());
	//			releseQueue();
	//		}
	//
	//		@Override
	//		protected void handleNotUnderstood(ACLMessage msg) {
	//			log.info("Unsubscribe request not understood by engager agent");
	//			releseQueue();
	//		}
	//
	//		@Override
	//		protected void handleFailure(ACLMessage msg) {
	//			log.info("Unsubscribe failed");
	//			// Get the failure reason and communicate it to the user
	//			try {
	//				AbsPredicate absPred = (AbsPredicate) myAgent.getContentManager().extractContent(msg);
	//
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading failure reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading failure reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue();
	//		}
	//
	//		@Override
	//		protected void handleRefuse(ACLMessage msg) {
	//			log.info("Unsubscribe refused");
	//			// Get the refusal reason and communicate it to the user
	//			try {
	//				AbsContentElementList list = (AbsContentElementList) myAgent.getContentManager().extractAbsContent(msg);
	//				AbsPredicate absPred = (AbsPredicate) list.get(1);
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading refusal reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading refusal reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue();
	//		}
	//
	//		private void releseQueue() {
	//			try {
	//				queue.put(true);
	//			} catch (InterruptedException e) {
	//				log.error("Cannot relese queue", e);
	//			}
	//		}
	//	}
	//
	//	@Override
	//	public void setDefaultTimeout(int timeout) {
	//		this.defaultTimeout = timeout;
	//	}
	//
	//	@Override
	//	public int getDefaultTimeout() {
	//		return this.defaultTimeout;
	//	}
	//
	//	private class QueryDatapointBehaviour extends SimpleAchieveREInitiator {
	//
	//		private final SynchronousQueue<List<Datapoint>> queue;
	//		/**
	//		 * 
	//		 */
	//		private static final long serialVersionUID = 1L;
	//
	//		public QueryDatapointBehaviour(Agent a, ACLMessage msg, SynchronousQueue<List<Datapoint>> queue) {
	//			super(a, msg);
	//			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	//			this.queue = queue;
	//			log.trace("Ready to send query message.");
	//		}
	//
	//		@Override
	//		protected void handleAgree(ACLMessage msg) {
	//			log.info("Query operation agreed. Waiting for completion notification...");
	//		}
	//
	//		@Override
	//		protected void handleInform(ACLMessage msg) {
	//			log.info("Query operation successfully completed");
	//
	//			log.info("Received message={}", msg.getContent());
	//			String datapointListAsString = msg.getContent();
	//
	//			JsonArray object = gson.fromJson(datapointListAsString, JsonArray.class);
	//			// log.info("Received acknowledge={}", object);
	//			List<Datapoint> datapointList = new ArrayList<Datapoint>();
	//			object.forEach(e -> {
	//				datapointList.add(Datapoint.toDatapoint(e.getAsJsonObject()));
	//			});
	//
	//			releseQueue(datapointList);
	//		}
	//
	//		@Override
	//		protected void handleNotUnderstood(ACLMessage msg) {
	//			log.info("Query request not understood by engager agent");
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		@Override
	//		protected void handleFailure(ACLMessage msg) {
	//			log.info("Qeury failed");
	//			// Get the failure reason and communicate it to the user
	//			try {
	//				AbsPredicate absPred = (AbsPredicate) myAgent.getContentManager().extractContent(msg);
	//
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading failure reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading failure reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		@Override
	//		protected void handleRefuse(ACLMessage msg) {
	//			log.info("Query refused");
	//			// Get the refusal reason and communicate it to the user
	//			try {
	//				AbsContentElementList list = (AbsContentElementList) myAgent.getContentManager().extractAbsContent(msg);
	//				AbsPredicate absPred = (AbsPredicate) list.get(1);
	//				log.warn("The reason is: " + absPred.getTypeName());
	//			} catch (Codec.CodecException fe) {
	//				log.error("FIPAException reading refusal reason: " + fe.getMessage());
	//			} catch (OntologyException oe) {
	//				log.error("OntologyException reading refusal reason: " + oe.getMessage());
	//			}
	//
	//			releseQueue(new ArrayList<Datapoint>());
	//		}
	//
	//		private void releseQueue(List<Datapoint> list) {
	//			try {
	//				queue.put(list);
	//			} catch (InterruptedException e) {
	//				log.error("Cannot relese queue", e);
	//			}
	//		}
	//	}

	//	protected CellFunctionHandler getCellFunctions() {
	//		return cellFunctions;
	//	}

}
