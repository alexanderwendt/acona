package _OLD.at.tuwien.ict.acona.cell.custombehaviours;

import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import _OLD.at.tuwien.ict.acona.cell.activator.jadebehaviour.CellFunctionBehaviourImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class SynchronizedReadBehaviour extends CellFunctionBehaviourImpl {

	private static final String TIMEOUTNAME = "timeout";
	//private int timeout = 1000;
	
	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<Datapoint>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void function(Map<String, Datapoint> data) {		
		log.debug("Start synchronized read behaviour");
		
		//Get the datapoint
		if (data.isEmpty()==false) {
			Datapoint dp = data.entrySet().iterator().next().getValue();
			
			try {
				this.queue.put(dp);
			} catch (InterruptedException e) {
				
			}
			
			log.debug("Behaviour finished. Value={} put in queue", dp);
		} else {
			try {
				throw new Exception ("No datapoint, although there should be one");
			} catch (Exception e) {
				log.error("Cannot execute behaviour", e);
			}
		}
		
		
		//String receiverID = data.get("receiver").getValue().getAsString();
		//Datapoint value = data.get("datapoint");
		
		// TODO Auto-generated method stub
		
		//Create an activator that listens to two predefined datapoints. Any data, which is put there is sent to that
		//agent name. One datapoint is for the receiver of the message and one datapoint contains the datapoint that shall be sent
		
		//Execute the sendondemandbehaviour
		//this.caller.addBehaviour(new SendDatapointOnDemandBehavior(AID.), value, AconaService.WRITE));
		
		
	}
	
	public Datapoint poll() {
		Datapoint result = null;
		
		try {
			//timeout = this.conf.get(TIMEOUTNAME).getAsInt();	//Get the timeout from the config
			
			//Poll the result
			result = queue.poll(this.conf.get(TIMEOUTNAME).getAsInt(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			
		}
		
		return result;
		
	}



}
