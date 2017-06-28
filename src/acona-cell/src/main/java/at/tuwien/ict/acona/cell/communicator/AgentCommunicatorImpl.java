package at.tuwien.ict.acona.cell.communicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

public class AgentCommunicatorImpl extends Thread implements AgentCommunicator {

	protected static Logger log = LoggerFactory.getLogger(AgentCommunicatorImpl.class);

	//ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	protected int defaultTimeout = 10000;

	private final CellImpl cell;
	protected final CellFunctionHandler cellFunctionHandler;
	//private final static Gson gson = new Gson();
	private final ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	public AgentCommunicatorImpl(CellImpl cell) {
		this.cell = cell;
		//this.datastorage = this.cell.getDataStorage();
		this.cellFunctionHandler = this.cell.getFunctionHandler();
	}

	protected String getLocalAgentName() {
		return this.cell.getLocalName();
	}

	@Override
	public void setDefaultTimeout(int timeout) {
		this.defaultTimeout = timeout;
	}

	@Override
	public int getDefaultTimeout() {
		return this.defaultTimeout;
	}

	@Override
	public void createResponderForFunction(CellFunction function) {
		ExternalServiceBehaviour responder = new ExternalServiceBehaviour(this.cell, function);

		cell.addBehaviour(tbf.wrap(responder));
	}

	@Override
	public void removeResponderForFunction(CellFunction function) {
		//TODO: Remove a behaviour responder too.

	}

	@Override
	public List<Datapoint> execute(String agentName, String serviceName, List<Datapoint> methodParameters, int timeout) throws Exception {
		return this.execute(agentName, serviceName, methodParameters, timeout, false);
	}

	@Override
	public void executeAsynchronous(String agentName, String serviceName, List<Datapoint> methodParameters) throws Exception {
		//TODO Implement this
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Datapoint> execute(String agentName, String serviceName, List<Datapoint> methodParameters, int timeout, boolean useSubscribeProtocol) throws Exception {

		final List<Datapoint> result = new ArrayList<>();
		// If a local data storage is meant, then write it there, else a foreign
		// data storage is meant.
		if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
			agentName = this.cell.getLocalName();
		}

		if (agentName.equals(this.cell.getLocalName()) == true) {
			// Execute local function
			Map<String, Datapoint> parametermap = new HashMap<>();
			methodParameters.forEach(dp -> parametermap.put(dp.getAddress(), dp));

			result.addAll(this.getCellFunctionHandler().getCellFunction(serviceName).performOperation(parametermap, this.getLocalAgentName()));
		} else {
			// Create a InitiatorBehaviour to write the datapoints to the target
			// agent if that agent is external
			ACLMessage requestMsg;
			if (useSubscribeProtocol == true) {
				requestMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
				requestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
			} else {
				requestMsg = new ACLMessage(ACLMessage.REQUEST);
				requestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			}

			requestMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
			requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
			requestMsg.setOntology(serviceName);

			JsonArray object = (new GsonUtils()).convertListToJsonArray(methodParameters);
			//			methodParameters.values().forEach(dp -> {
			//				object.add(dp.toJsonObject());
			//			});
			// String serializedDatapoints = gson.toJson(datapoints);
			// List<TestObject> list2 = gson.fromJson(s, listOfTestObject);

			requestMsg.setContent(object.toString());

			// Blocking read and write
			SynchronousQueue<String> queue = new SynchronousQueue<>();

			synchronized (this) {
				this.cell.addBehaviour(tbf.wrap(new ServiceExecuteBehaviour(this.cell, requestMsg, queue)));
				String writeBehaviourFinished = "";
				try {
					writeBehaviourFinished = queue.poll(timeout, TimeUnit.MILLISECONDS);
					if (writeBehaviourFinished == null) {
						throw new Exception("Operation timed out after " + timeout + "ms.");
					}

					result.addAll(new Gson().fromJson(writeBehaviourFinished, new TypeToken<List<Datapoint>>() {
					}.getType()));
				} catch (InterruptedException e) {
					log.warn("Queue interrupted");
				}
			}
		}

		return result;
	}

	// === INNER CLASSES ====//

	private class ServiceExecuteBehaviour extends SimpleAchieveREInitiator {

		private final SynchronousQueue<String> queue;
		private final String serviceName;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ServiceExecuteBehaviour(Agent a, ACLMessage msg, SynchronousQueue<String> queue) {
			super(a, msg);
			//msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			this.serviceName = msg.getOntology();
			this.queue = queue;
			log.trace("Service {}>Ready to send execute request to agent={}, message={}", this.serviceName, Lists.newArrayList(msg.getAllReceiver()), msg.getContent());
		}

		@Override
		protected void handleAgree(ACLMessage msg) {
			log.info("service {}>Execute agreed. Waiting for completion notification...", serviceName);
		}

		@Override
		protected void handleInform(ACLMessage msg) {
			//log.info("Write operation successfully completed");

			log.info("Service {}>Received result={}", this.serviceName, msg.getContent());
			releseQueue(msg.getContent());
		}

		@Override
		protected void handleNotUnderstood(ACLMessage msg) {
			log.info("Service {}>Request not understood by engager agent", this.serviceName);
			releseQueue(msg.getContent());
		}

		@Override
		protected void handleFailure(ACLMessage msg) {
			log.info("Service {}>Service failed", this.serviceName);
			// Get the failure reason and communicate it to the user
			try {
				AbsPredicate absPred = (AbsPredicate) myAgent.getContentManager().extractContent(msg);

				log.warn("Service {}>The reason is: " + absPred.getTypeName(), this.serviceName);
			} catch (Codec.CodecException fe) {
				log.error("Service {}>FIPAException reading failure reason: " + fe.getMessage(), this.serviceName);
			} catch (OntologyException oe) {
				log.error("Service {}>OntologyException reading failure reason: " + oe.getMessage(), this.serviceName);
			}

			releseQueue(msg.getContent());
		}

		private void releseQueue(String dp) {
			try {
				queue.put(dp);
			} catch (InterruptedException e) {
				log.error("Service {}>Cannot release queue", this.serviceName, e);
			}
		}
	}

	protected CellFunctionHandler getCellFunctionHandler() {
		return cellFunctionHandler;
	}

	protected CellImpl getCell() {
		return cell;
	}

}
