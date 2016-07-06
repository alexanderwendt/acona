package at.tuwien.ict.acona.cell.core.helpers;

import java.util.Map;

import at.tuwien.ict.acona.cell.core.CellFunctionBehaviourImpl;
import at.tuwien.ict.acona.cell.core.CellUtil;
import at.tuwien.ict.acona.cell.core.behaviours.SendDatapointOnDemandBehavior;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.AID;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;

public class TestReadAndWriteBehaviour extends CellFunctionBehaviourImpl {

	private static final String AGENTNAME = "agentname";
	private static final String READADDRESS = "readaddress";
	private static final String RESULTADDRESS = "result";
	private static final String TIMEOUT = "timeout";
	
	@Override
	public void function(Map<String, Datapoint> data) {
		log.info("Start sync read behaviour");
		
		//Synchronized read from the other agent
		try {
			//CellUtil util = new CellUtil(this.caller);
			//Datapoint readData = this.caller.getCellUtil().remoteRead(this.conf.get(AGENTNAME).getAsString(), this.conf.get(READADDRESS).getAsString(), this.conf.get(TIMEOUT).getAsInt());
			Datapoint readData = null;
			
			//Try to read again
			//ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
			//this.caller.addBehaviour(new SendDatapointOnDemandBehavior(new AID(this.conf.get(AGENTNAME).getAsString(), AID.ISLOCALNAME), Datapoint.newDatapoint(this.conf.get(READADDRESS).getAsString()), AconaService.READ));
			
			this.myAgent.send(ACLUtils.convertToACL(Message.newMessage().addReceiver(this.conf.get(AGENTNAME).getAsString()).setService(AconaService.READ).setContent(Datapoint.newDatapoint(this.conf.get(READADDRESS).getAsString()))));
			
			log.debug("Block behaviour now");
			synchronized (this) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			log.debug("Block released 1");
			block();
			log.debug("Block released 2");
			
			
			
			
			
			
			//Write the received value to the result
			if (readData!=null) {
				this.caller.getDataStorage().write(Datapoint.newDatapoint(RESULTADDRESS).setValue(readData.getValue()), caller.getName());
				log.info("Sync read successfully finished");
			} else {
				log.warn("Read data is null");
			}
			
		
			
		} catch (Exception e) {
			log.error("Cannot read synchronized from remote cell", e);
		}
		
		
		

		
	}


}
