package at.tuwien.ict.acona.cell.core.helpers;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.jadelauncher.util.ACLUtils;
import jade.core.behaviours.TickerBehaviour;

public class CustomTestCell extends CellImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected void internalInit() {
		final String target = this.conf.get("targetcell").getAsString();
		//Address
		//String datapointsource = "activator.test.address";
//		String activatorName = "testactivator";
//		String conditionName = "isNotEmpty";
//		JsonObject additionBehaviourConf = new JsonObject();
//		additionBehaviourConf.addProperty("operand1", "data.op1");
//		additionBehaviourConf.addProperty("operand2", "data.op2");
//		additionBehaviourConf.addProperty("result", "data.result");
//		
//		Activator activator = new ActivatorImpl();
//		CellFunctionBehaviour activateBehaviour = new AdditionBehaviour().init("AdditionBehaviour", additionBehaviourConf, this);
//		
//		//Create condition
//		Condition condition1 = new ConditionIsNotEmpty().init(conditionName, new JsonObject());
//		Condition condition2 = new ConditionIsNotEmpty().init(conditionName, new JsonObject());
//
//		Map<String, List<Condition>> conditionMapping = new HashMap<String, List<Condition>>();
//		conditionMapping.put(additionBehaviourConf.get("operand1").getAsString(), Arrays.asList(condition1));
//		conditionMapping.put(additionBehaviourConf.get("operand2").getAsString(), Arrays.asList(condition2));
//		//this.addBehaviour(activateBehaviour);
//		
//		
//		//activateBehaviour
//		activator.init(activatorName, conditionMapping, "", activateBehaviour, this);
//		//activator.registerCondition(new ConditionIsNotEmpty());
//		
//		this.getActivationHandler().registerActivatorInstance(activator);
//		
		log.debug("Register a cyclic send behaviour");
		TickerBehaviour b = new TickerBehaviour(this, 1000) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTick() {
				log.debug("START BEHAVIOUR");
				this.myAgent.send(ACLUtils.convertToACL(Message.newMessage().addReceiver(target).setService(AconaServiceType.READ).setContent(Datapoint.newDatapoint("test"))));
				for (int i=0;i<1000;i++) {
					log.debug("Number={}", i);
//					synchronized (this) {
////						try {
////							//Thread.sleep(10);
////						} catch (InterruptedException e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						}
//					}
				}
				log.debug("MESSAGE SENT");			
				
			}
			
		};
		this.addBehaviour(b);
	}

}
