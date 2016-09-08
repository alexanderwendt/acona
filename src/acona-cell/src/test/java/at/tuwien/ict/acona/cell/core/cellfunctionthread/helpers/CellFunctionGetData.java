package at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers;

import _OLD_at.tuwien.ict.acona.cell.core.behaviours.SendDatapointOnDemandBehavior;
import at.tuwien.ict.acona.cell.activator.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.AID;
import jade.core.behaviours.Behaviour;

public class CellFunctionGetData extends CellFunctionThreadImpl {

	private final String COMMANDDATAPOINT = "drivetrack.controller.command";
	private final String STATUSDATAPOINT = "drivetrack.controller.status";
	
	private final String inputMemoryAgentName = "InputBufferAgent";
	private final String memorydatapoint1 = "inputmemory.variable1";	//put into memory mock agent
	private final String memorydatapoint2 = "inputmemory.variable2";	//put into memory mock agent
	
	public CellFunctionGetData() {
		this.setExecuteOnce(true);	//Run only on demand from controller
	}
	
	@Override
	protected void updateDatapoint(Datapoint subscribedData) {
		//React on the start trigger
		if (subscribedData.getAddress().equals(COMMANDDATAPOINT)) {
			try {
				this.setCommand(subscribedData.getValue().getAsString());
			} catch (Exception e) {
				log.error("Cannot read command", e);
			}
		}
	}

	@Override
	protected void executeFunction() throws Exception {
		this.cell.getDataStorage().write(Datapoint.newDatapoint(STATUSDATAPOINT).setValue("RUNNING"), this.cell.getLocalName());
		
		
		//Read data from the input memory agent
		Behaviour sendBehaviour1 = new SendDatapointOnDemandBehavior(new AID(inputMemoryAgentName, AID.ISLOCALNAME), Datapoint.newDatapoint(memorydatapoint1), AconaServiceType.READ);
		this.cell.addBehaviour(sendBehaviour1);
		log.info("Send message to agent={} to read address={}", inputMemoryAgentName, memorydatapoint1);
		
		Behaviour sendBehaviour2 = new SendDatapointOnDemandBehavior(new AID(inputMemoryAgentName, AID.ISLOCALNAME), Datapoint.newDatapoint(memorydatapoint2), AconaServiceType.READ);
		this.cell.addBehaviour(sendBehaviour2);
		log.info("Send message to agent={} to read address={}", inputMemoryAgentName, memorydatapoint2);
		
		log.info("wait 1000ms...");
		try {
			//Block profile controller
			synchronized (this) {
				this.wait(1000);
			}
			
		} catch (InterruptedException e) {
			log.warn("Wait interrupted client");
		}
		
		log.info("Waiting finished. All values shall be available: Value1={}, Value2={}", this.cell.getDataStorage().read(memorydatapoint1), this.cell.getDataStorage().read(memorydatapoint2));
		
		//If finished, set status datapoint
		this.cell.getDataStorage().write(Datapoint.newDatapoint(STATUSDATAPOINT).setValue("OK"), this.cell.getLocalName());
		
	}

}
