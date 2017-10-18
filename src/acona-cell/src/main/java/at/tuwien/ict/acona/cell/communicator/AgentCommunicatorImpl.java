package at.tuwien.ict.acona.cell.communicator;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
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

	private int defaultTimeout = 10000;

	private final CellImpl cell;
	private final CellFunctionHandler cellFunctionHandler;
	private final SubscriptionHandler subscriptionHandler;
	//private final static Gson gson = new Gson();
	private final ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	public AgentCommunicatorImpl(CellImpl cell) {
		this.cell = cell;
		//this.datastorage = this.cell.getDataStorage();
		this.cellFunctionHandler = this.cell.getFunctionHandler();
		this.subscriptionHandler = this.cell.getSubscriptionHandler();
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
	public JsonRpcResponse execute(String agentName, String serviceName, JsonRpcRequest methodParameters, int timeout) throws Exception {
		return this.execute(agentName, serviceName, methodParameters, timeout, false);
	}

	@Override
	public JsonRpcResponse execute(String agentNameAndService, JsonRpcRequest methodParameters) throws Exception {
		Datapoint dp = DatapointBuilder.newDatapoint(agentNameAndService);
		return this.execute(dp.getAgent(), dp.getAddress(), methodParameters, this.defaultTimeout, false);
	}

	@Override
	public void executeAsynchronous(String agentName, String serviceName, JsonRpcRequest methodParameters) throws Exception {
		//TODO Implement this
		throw new UnsupportedOperationException();
	}

	@Override
	public JsonRpcResponse execute(String agentName, String serviceName, JsonRpcRequest methodParameters, int timeout, boolean useSubscribeProtocol) throws Exception {

		JsonRpcResponse result = new JsonRpcResponse(methodParameters, new JsonRpcError("ExecutionFailure", -1, "Unknown error", "unknown error")); //= new ArrayList<>();
		// If a local data storage is meant, then write it there, else a foreign
		// data storage is meant.
		if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
			agentName = this.cell.getLocalName();
		}

		if (agentName.equals(this.cell.getLocalName()) == true) {
			// Execute local function
			log.debug("Execute local function={}, parameters={}, agent={}. Hashcode={}.", serviceName, methodParameters, this.getLocalAgentName(), this.hashCode());
			result = this.getCellFunctionHandler().getCellFunction(serviceName).performOperation(methodParameters, this.getLocalAgentName());
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

			//JsonArray object = (new GsonUtils()).convertListToJsonArray(methodParameters);

			requestMsg.setContent(methodParameters.toJson().toString());

			// Blocking read and write
			SynchronousQueue<String> queue = new SynchronousQueue<>();

			synchronized (this) {
				this.cell.addBehaviour(tbf.wrap(new ServiceExecuteBehaviour(this.cell, requestMsg, queue)));
				String writeBehaviourFinished = "";
				try {
					writeBehaviourFinished = queue.poll(timeout, TimeUnit.MILLISECONDS);
					if (writeBehaviourFinished == null) {
						throw new Exception("No answer. Operation timed out after " + timeout + "ms. "
								+ "Possible causes: 1: target address agent+service does not exist. "
								+ "Check if the service on the other agent has a responder activated or if the address has been misspelled."
								+ "2: Error at the receiver site so that no message is returned.");
					}

					result = new Gson().fromJson(writeBehaviourFinished, new TypeToken<JsonRpcResponse>() {
					}.getType());
				} catch (InterruptedException e) {
					log.warn("Queue interrupted");
				} catch (JsonSyntaxException e) {
					log.error("Cannot read respond message={}", writeBehaviourFinished);
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
		protected void handleRefuse(ACLMessage msg) {
			log.info("Service {}>Received refused handling of request={}", this.serviceName, msg.getContent());
			releseQueue(msg.getContent());
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
			log.info("Service {}>Service failed. Error received={}", this.serviceName, msg.getContent());
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

		private synchronized void releseQueue(String dp) {
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

	public SubscriptionHandler getSubscriptionHandler() {
		return subscriptionHandler;
	}

}
