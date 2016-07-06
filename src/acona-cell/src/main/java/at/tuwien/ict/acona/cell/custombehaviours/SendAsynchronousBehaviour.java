package at.tuwien.ict.acona.cell.custombehaviours;

import java.util.Map;

import at.tuwien.ict.acona.cell.core.CellFunctionBehaviourImpl;
import at.tuwien.ict.acona.cell.core.behaviours.SendDatapointOnDemandBehavior;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import jade.core.AID;

public class SendAsynchronousBehaviour extends CellFunctionBehaviourImpl {

	private static final String RECEIVERDATAPOINTADDRESS = "receivername";
	private static final String DATAPOINTADDRESS = "datapointaddress";
	private static final String ACONASERVICEADDRESS = "aconaservice";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void function(Map<String, Datapoint> data) {
		String receiverID = data.get("receiver").getValue().getAsString();
		Datapoint value = data.get("datapoint");
		
		//this.myAgent.addBehaviour(new SendDatapointOnDemandBehavior(msg.getSender(), this.callerCell.getDataStorage().read(dp.getAddress()), AconaService.WRITE));
		// TODO Auto-generated method stub
		
		//Create an activator that listens to two predefined datapoints. Any data, which is put there is sent to that
		//agent name. One datapoint is for the receiver of the message and one datapoint contains the datapoint that shall be sent
		
		//Execute the sendondemandbehaviour
		//this.caller.addBehaviour(new SendDatapointOnDemandBehavior(AID.), value, AconaService.WRITE));
		
	}


}
