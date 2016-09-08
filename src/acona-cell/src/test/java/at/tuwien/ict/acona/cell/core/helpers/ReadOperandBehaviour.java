package at.tuwien.ict.acona.cell.core.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _OLD_at.tuwien.ict.acona.cell.core.behaviours.SendDatapointOnDemandBehavior;
import at.tuwien.ict.acona.cell.activator.jadebehaviour.CellFunctionBehaviourImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.AID;
import jade.core.behaviours.Behaviour;

public class ReadOperandBehaviour extends CellFunctionBehaviourImpl {
	
	protected static Logger log = LoggerFactory.getLogger(ReadOperandBehaviour.class);
	
	private static final String OPERAND1AGENTNAME = "op1agent";
	private static final String OPERAND1ADDRESS  ="op1address";
	private static final String OPERAND2AGENTNAME = "op2agent";
	private static final String OPERAND2ADDRESS  ="op2address";
	
	private static final String CURRENTSTATEADDRESS = "stateaddress";
	private static final String SUCCESSID = "successstateid";
	private static final String NOSUCCESSID = "nosuccessid";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReadOperandBehaviour() {
		super();
	}

	@Override
	public void function(Map<String, Datapoint> data) {
		//Send first message
		Behaviour sendBehaviour1 = new SendDatapointOnDemandBehavior(new AID(conf.get(OPERAND1AGENTNAME).getAsString(), AID.ISLOCALNAME), Datapoint.newDatapoint(conf.get(OPERAND1ADDRESS).getAsString()), AconaServiceType.READ);
		this.caller.addBehaviour(sendBehaviour1);
		log.info("Send message to agent={} to read address={}", conf.get(OPERAND1AGENTNAME), conf.get(OPERAND1ADDRESS));
		
		//Send second message
		Behaviour sendBehaviour2 = new SendDatapointOnDemandBehavior(new AID(conf.get(OPERAND2AGENTNAME).getAsString(), AID.ISLOCALNAME), Datapoint.newDatapoint(conf.get(OPERAND2ADDRESS).getAsString()), AconaServiceType.READ);
		this.caller.addBehaviour(sendBehaviour2);
		log.info("Send message to agent={} to read address={}", conf.get(OPERAND2AGENTNAME), conf.get(OPERAND2ADDRESS));
		
		
		
		//ACLMessage msg = ACLUtils.convertToACL(Message.newMessage().addReceiver(conf.get(OPERAND2AGENTNAME).toString()).setContent(Datapoint.newDatapoint(conf.get(OPERAND2ADDRESS).getAsString())).setService(AconaServiceType.READ));
		
//		this.myAgent.send(msg);
//		log.debug("Message finished");
		
		//Update state
		//log.debug("Current state of the behaviour is={}. State id received from address={}, value={}", this.getName(), data.get(conf.get(CURRENTSTATEADDRESS)).getAddress(), data.get(conf.get(CURRENTSTATEADDRESS)).getValue());
		//this.writeToDataStorage(Datapoint.newDatapoint(CURRENTSTATEADDRESS).setValue(conf.get(SUCCESSID).getAsString()));
		
	}

}
