package at.tuwien.ict.acona.cell.activator;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.activator.helper.ConditionAlwaysFalse;
import at.tuwien.ict.acona.cell.activator.helper.ConditionAlwaysTrue;
import at.tuwien.ict.acona.cell.activator.helper.DummyBehaviour;
import at.tuwien.ict.acona.cell.activator.helper.DummyCell;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.core.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellActivatorOnlyTester {

	private static Logger log = LoggerFactory.getLogger(CellActivatorOnlyTester.class);
	
	private ActivationHandler handler = null;
	private Cell cell = new DummyCell();

	@Before
	public void setUp() throws Exception {
		log.info("Start cell activator tester");
		try {
			//Setup activationhandler
			handler = new ActivationHandlerImpl();
			
			
		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void activateBehaviorFalseMultipleConditionsTest() {
		try {
			//Address
			String datapointsource1 = "activator.test.address1";
			String datapointsource2 = "activator.test.address2";
			String datapointsource3 = "activator.test.address3";
			String datapointsource4 = "activator.test.address4";
			String datapointsource5 = "activator.test.address5";
			String datapointsource6 = "activator.test.address6";
			String datapointsource7 = "activator.test.address7";
			
			
			String activatorName = "testactivator";
			String conditionName = "isNotEmpty";
			int data = 0;
			
			//Create activator
			Activator activator = new ActivatorImpl();
			
			//Create behaviour
			DummyBehaviour activateBehaviour = new DummyBehaviour();
			activateBehaviour.init(activatorName, null);
			
			//Create condition
			Condition condition1 = new ConditionAlwaysTrue().init(conditionName, null);
			Condition condition2 = new ConditionAlwaysFalse().init(conditionName, null);;
			Condition condition3 = new ConditionAlwaysTrue().init(conditionName, null);;
			Condition condition4 = new ConditionAlwaysFalse().init(conditionName, null);;
			Condition condition5 = new ConditionAlwaysTrue().init(conditionName, null);;
			Condition condition6 = new ConditionAlwaysFalse().init(conditionName, null);;
			Condition condition7 = new ConditionAlwaysTrue().init(conditionName, null);;
			
			//condition1.init(conditionName, null);
			Map<String, List<Condition>> conditionMapping = new HashMap<String, List<Condition>>();
			conditionMapping.put(datapointsource1, Arrays.asList(condition1));
			conditionMapping.put(datapointsource2, Arrays.asList(condition2));
			conditionMapping.put(datapointsource3, Arrays.asList(condition3));
			conditionMapping.put(datapointsource4, Arrays.asList(condition4));
			conditionMapping.put(datapointsource5, Arrays.asList(condition5));
			conditionMapping.put(datapointsource6, Arrays.asList(condition6));
			conditionMapping.put(datapointsource7, Arrays.asList(condition7));
			
			
			//activateBehaviour
			activator.init(activatorName, conditionMapping, "", activateBehaviour, cell);
			
			this.handler.registerActivatorInstance(activator);
			log.debug("Activator registered in handler. System initialized");
			
			//Test actual behaviour
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource1).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource2).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource3).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource4).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource5).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource6).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource7).setValue(String.valueOf(data)));
			
			assertEquals(false, activateBehaviour.hasRun());
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void activateBehaviorTrueMultipleConditionsTest() {
		try {
			//Address
			String datapointsource1 = "activator.test.address1";
			String datapointsource2 = "activator.test.address2";
			String datapointsource3 = "activator.test.address3";
			String datapointsource4 = "activator.test.address4";
			String datapointsource5 = "activator.test.address5";
			String datapointsource6 = "activator.test.address6";
			String datapointsource7 = "activator.test.address7";
			
			
			String activatorName = "testactivator";
			String conditionName = "isNotEmpty";
			int data = 0;
			
			//Create activator
			Activator activator = new ActivatorImpl();
			
			//Create behaviour
			DummyBehaviour activateBehaviour = new DummyBehaviour();
			activateBehaviour.init(activatorName, null);
			
			//Create condition
			Condition condition1 = new ConditionAlwaysTrue().init(conditionName, null);
			Condition condition2 = new ConditionAlwaysTrue().init(conditionName, null);;
			Condition condition3 = new ConditionAlwaysTrue().init(conditionName, null);;
			Condition condition4 = new ConditionAlwaysTrue().init(conditionName, null);;
			Condition condition5 = new ConditionAlwaysTrue().init(conditionName, null);;
			Condition condition6 = new ConditionAlwaysTrue().init(conditionName, null);;
			Condition condition7 = new ConditionAlwaysTrue().init(conditionName, null);;
			
			//condition1.init(conditionName, null);
			Map<String, List<Condition>> conditionMapping = new HashMap<String, List<Condition>>();
			conditionMapping.put(datapointsource1, Arrays.asList(condition1));
			conditionMapping.put(datapointsource2, Arrays.asList(condition2));
			conditionMapping.put(datapointsource3, Arrays.asList(condition3));
			conditionMapping.put(datapointsource4, Arrays.asList(condition4));
			conditionMapping.put(datapointsource5, Arrays.asList(condition5));
			conditionMapping.put(datapointsource6, Arrays.asList(condition6));
			conditionMapping.put(datapointsource7, Arrays.asList(condition7));
			
			
			//activateBehaviour
			activator.init(activatorName, conditionMapping, "", activateBehaviour, cell);
			
			this.handler.registerActivatorInstance(activator);
			log.debug("Activator registered in handler. System initialized");
			
			//Test actual behaviour
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource1).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource2).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource3).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource4).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource5).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource6).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviors(Datapoint.newDatapoint(datapointsource7).setValue(String.valueOf(data)));
			
			assertEquals(true, activateBehaviour.hasRun());
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
}
