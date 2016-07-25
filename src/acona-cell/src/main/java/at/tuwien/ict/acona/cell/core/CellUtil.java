package at.tuwien.ict.acona.cell.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.ActivatorImpl;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty;
import at.tuwien.ict.acona.cell.config.BehaviourConfig;
import at.tuwien.ict.acona.cell.core.behaviours.SendDatapointOnDemandBehavior;
import at.tuwien.ict.acona.cell.custombehaviours.SynchronizedReadBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.AID;
import jade.core.behaviours.ThreadedBehaviourFactory;

public class CellUtil {
	private final static String CONDITIONNAME = "temporaryactivatorcondition";
	private final static String BEHAVIOURNAME = "temporaryactivatorbehaviour";
	private final static String ACTIVATORNAME = "temporaryactivator";
	private final static Logger log = LoggerFactory.getLogger(CellUtil.class);
		
	private final Cell cell;
	
	public CellUtil(Cell cell) {
		this.cell = cell;
	}
	
	/**
	 * A remote procedure call is done at a foreign agent. The agent sends a read request to the other agent. It returns a write request to the same datapoint in this agent. This datapoint is subscribed by this method through a queue.
	 * If the value is returned within a certain time frame, the value is return, else timeout error. 
	 * 
	 * @param targetAgent as a String. It is then converted to an AID
	 * @param datapointAddress as a String
	 * @param timeout in ms
	 * @return
	 */
	public Datapoint remoteRead(String targetAgentName, String datapointAddress, int timeout) throws InterruptedException {
		Datapoint result = null;	
		
		//Create new activator with only one condition as a handler for received messages
		Activator activator = new ActivatorImpl();
		
		BehaviourConfig behaviourConfig = BehaviourConfig.newConfig(BEHAVIOURNAME, "at.tuwien.ict.acona.cell.custombehaviours.SynchronizedReadBehaviour").setProperty("timeout", String.valueOf(timeout));
		SynchronizedReadBehaviour activateBehaviour = new SynchronizedReadBehaviour();
		activateBehaviour.init(BEHAVIOURNAME, behaviourConfig.toJsonObject(), this.cell);
		
		//Create condition
		Condition condition = new ConditionIsNotEmpty().init(CONDITIONNAME, new JsonObject());
		Map<String, List<Condition>> conditionMapping = new HashMap<String, List<Condition>>();
		conditionMapping.put(datapointAddress, Arrays.asList(condition));
		activator.init(ACTIVATORNAME, conditionMapping, "", activateBehaviour, this.cell);
		//activator.registerCondition(new ConditionIsNotEmpty());
		this.cell.getActivationHandler().registerActivatorInstance(activator);
		
		//Send read to target cell
		//ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
		//this.cell.addBehaviour(tbf.wrap(new SendDatapointOnDemandBehavior(id, Datapoint.newDatapoint(datapointAddress), AconaService.READ)));
		
		//Send read to target cell
		AID id = new AID(targetAgentName, AID.ISLOCALNAME);
		ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
		this.cell.addBehaviour(new SendDatapointOnDemandBehavior(id, Datapoint.newDatapoint(datapointAddress), AconaServiceType.READ));
		
		try {
			result = activateBehaviour.poll();	//timeout in the config
		} catch (Exception e) {
			log.error("Timeout", e);
			throw new InterruptedException(e.getMessage());
		} finally {
			
			//Clean up when finished
			this.cell.getActivationHandler().deregisterActivatorInstance(activator);
		}
		
		//SynchronousQueue<Datapoint> blockingQueue = new SynchronousQueue<Datapoint>();
		
		//1. Subscribe the datapoint to read from in this agent
		//this.cell.getDataStorage().subscribeDatapoint(datapointAddress, this.cell.getName());
		
		//2. Send a message to the target agent to read.
		
		//3. Read the queue as blocking until timeout
//		try {
//			blockingQueue.poll(timeout, TimeUnit.MILLISECONDS);
//		} catch (InterruptedException e) {
//			log.warn("Timeout");
//		}
		
		log.trace("Behaviour finished");
		return result;
	}
}
