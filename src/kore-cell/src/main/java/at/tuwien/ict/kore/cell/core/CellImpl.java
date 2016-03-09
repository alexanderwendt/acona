package at.tuwien.ict.kore.cell.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.cell.core.behaviors.ReadDataServiceBehavior;
import at.tuwien.ict.kore.cell.storage.DataStorage;
import at.tuwien.ict.kore.cell.storage.DataStorageImpl;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class CellImpl extends Agent implements Cell {

	//private String identifier = "";
	private DataStorage dataStorage = new DataStorageImpl();
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	protected static Logger log = LoggerFactory.getLogger(CellImpl.class);

	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	protected void setup() {
		 // Register the basic cell service in the yellow pages
		 DFAgentDescription dfDescriptionAgent = new DFAgentDescription();
		 dfDescriptionAgent.setName(getAID());
		 ServiceDescription sericeDescription = createServiceDescription();
		 dfDescriptionAgent.addServices(sericeDescription);
		 try {
			 DFService.register(this, dfDescriptionAgent);
		 } catch (FIPAException fe) {
			 fe.printStackTrace();
		 }
		 
		 //Create behaviors
		 this.createBasicBehaviors();
	}
	
	private void createBasicBehaviors() {
		//Create readbehavior
		ReadDataServiceBehavior readDataService = new ReadDataServiceBehavior();
		this.addBehaviour(readDataService);
		
	}
	
	private ServiceDescription createServiceDescription() {
		ServiceDescription sericeDescription = new ServiceDescription();
		sericeDescription.setType("Cell");
		sericeDescription.setName("BasicService");
		return sericeDescription;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	protected void takeDown() {
		 // Deregister from the yellow pages
		 try {
			 DFService.deregister(this);
		 }
		 catch (FIPAException fe) {
			 fe.printStackTrace();
		 }
		 	// Printout a dismissal message
		 	log.info("Cell agent" +getAID().getName()+ " is terminating.");
		 }

	
	@Override
	public DataStorage getDataStorage() {
		return this.dataStorage;
	}
	
	
	
	
}
