package at.tuwien.ict.acona.cell.core.behaviours;

import java.util.Map;

import at.tuwien.ict.acona.cell.core.CellFunctionBehaviourImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class SendBehaviour extends CellFunctionBehaviourImpl {

	@Override
	public void function(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
		
		//Create an activator that listens to two predefined datapoints. Any data, which is put there is sent to that
		//agent name. One datapoint is for the receiver of the message and one datapoint contains the datapoint that shall be sent
		
		
	}


}
