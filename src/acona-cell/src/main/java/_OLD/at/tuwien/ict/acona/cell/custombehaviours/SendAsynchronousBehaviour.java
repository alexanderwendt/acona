package _OLD.at.tuwien.ict.acona.cell.custombehaviours;

import java.util.Map;

import _OLD.at.tuwien.ict.acona.cell.activator.jadebehaviour.CellFunctionBehaviourImpl;
import _OLD_at.tuwien.ict.acona.cell.core.behaviours.SendDatapointOnDemandBehavior;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.AID;

public class SendAsynchronousBehaviour extends CellFunctionBehaviourImpl {

	private static final String RECEIVERDATAPOINTADDRESS = "receivernameaddress";
	private static final String DATAPOINTSOURCEADDRESS = "datapointsourceaddress";
	private static final String DATAPOINTTARGETADDRESS = "datapointtargetaddress";
	private static final String ACONASERVICEADDRESS = "aconaserviceaddress";
	private static final String DEFAULTACONASERVICE = "defaultservice";
	
	private static final String STATEADDRESS = "stateaddress";
	private static final String MYSTATEID = "mystateid";
	private static final String SUCCESSID = "successstateid";
	private static final String NOSUCCESSID = "nosuccessid";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void subInit() {
		//Write the default acona service, which is used until another service is found for that
		this.writeToDataStorage(Datapoint.newDatapoint(conf.getAsJsonPrimitive(ACONASERVICEADDRESS).getAsString()).setValue(conf.get(DEFAULTACONASERVICE)));
	}
	
	@Override
	public void function(Map<String, Datapoint> data) {
		AID name = new AID(this.readFromDataStorage(conf.get(RECEIVERDATAPOINTADDRESS).getAsString()).getValue().getAsString(), AID.ISLOCALNAME);
		Datapoint dp = this.readFromDataStorage(conf.get(DATAPOINTSOURCEADDRESS).getAsString());
		String targetdatapoint = conf.get(DATAPOINTTARGETADDRESS).getAsString();
		AconaServiceType service = AconaServiceType.valueOf(this.readFromDataStorage(conf.get(ACONASERVICEADDRESS).getAsString()).getValue().getAsString());
		
		//Create target datapoint
		Datapoint targetdp = Datapoint.newDatapoint(targetdatapoint).setValue(dp.getValue());
		
		this.myAgent.addBehaviour(new SendDatapointOnDemandBehavior(name, dp, service));
		log.debug("Send to agent={}, datapoint={}, service={}", name, targetdp, service);
		
	}

}
