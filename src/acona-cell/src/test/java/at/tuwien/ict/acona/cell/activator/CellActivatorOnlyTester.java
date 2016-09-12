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
import at.tuwien.ict.acona.cell.activator.helper.ConditionIsOne;
import at.tuwien.ict.acona.cell.activator.helper.DummyBehaviour;
import at.tuwien.ict.acona.cell.activator.helper.DummyCell;
import at.tuwien.ict.acona.cell.activator.jadebehaviour.ActivatorJADEBehaviourImpl;
import at.tuwien.ict.acona.cell.activator.jadebehaviour.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellActivatorOnlyTester {

	private static Logger log = LoggerFactory.getLogger(CellActivatorOnlyTester.class);
	
	private ActivationHandlerImpl handler = null;
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
		log.debug("Start activateBehaviorFalseMultipleConditionsTest");
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
			Activator activator = new ActivatorJADEBehaviourImpl();
			
			//Create behaviour
			DummyBehaviour activateBehaviour = new DummyBehaviour();
			activateBehaviour.init(activatorName, null, cell);
			
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
			activator.initWithConditions(activatorName, conditionMapping, "", activateBehaviour, cell);
			
			this.handler.registerActivatorInstance(activator);
			log.debug("Activator registered in handler. System initialized");
			
			//Test actual behaviour
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource1).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource2).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource3).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource4).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource5).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource6).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource7).setValue(String.valueOf(data)));
			
			assertEquals(false, activateBehaviour.hasRun());
			
		} catch (Exception e) {
			log.error("Cannot test system", e);
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
			Activator activator = new ActivatorJADEBehaviourImpl();
			
			//Create behaviour
			DummyBehaviour activateBehaviour = new DummyBehaviour();
			activateBehaviour.init(activatorName, null, cell);
			
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
			activator.initWithConditions(activatorName, conditionMapping, "", activateBehaviour, cell);
			
			this.handler.registerActivatorInstance(activator);
			log.debug("Activator registered in handler. System initialized");
			
			//Test actual behaviour
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource1).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource2).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource3).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource4).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource5).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource6).setValue(String.valueOf(data)));
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource7).setValue(String.valueOf(data)));
			
			assertEquals(true, activateBehaviour.hasRun());
			log.info("Test passed");
			
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}
	
	/**
	 * The idea of the test is to let the system run once with a constant condition that is only executed once. A IsAlwaysTrue condition is used. The other value is conditional and activates behaviour if it is = 1.0. The variable value is changed 2 times and only if this value is 1.0, 
	 * the behaviour shall execute.
	 * 
	 */
	@Test
	public void constantValueTest() {
		log.debug("Start constant value tester");
		try {
			//Address
			String datapointsource1 = "activator.test.addressconstantvalue";
			String datapointsource2 = "activator.test.addressvariablevalue";
			
			
			String activatorName = "testactivator";
			String conditionNameIsTrue = "isTrue";
			String conditionNameIsOne = "isOne";
			final int constanceData = 12;
			int variableData = 0;
			
			//Create activator
			Activator activator = new ActivatorJADEBehaviourImpl();
			
			//Create behaviour
			DummyBehaviour activateBehaviour = new DummyBehaviour();
			activateBehaviour.init(activatorName, null, cell);
			
			//Create condition
			Condition condition1 = new ConditionAlwaysTrue().init(conditionNameIsTrue, null);
			Condition condition2 = new ConditionIsOne().init(conditionNameIsOne, null);
			
			//condition1.init(conditionName, null);
			Map<String, List<Condition>> conditionMapping = new HashMap<String, List<Condition>>();
			conditionMapping.put(datapointsource1, Arrays.asList(condition1));
			conditionMapping.put(datapointsource2, Arrays.asList(condition2));
			
			//activateBehaviour
			activator.initWithConditions(activatorName, conditionMapping, "", activateBehaviour, cell);
			
			this.handler.registerActivatorInstance(activator);
			log.debug("Activator registered in handler. System initialized");
			
			//Run the first run and init the constant condition of the constant data
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource1).setValue(String.valueOf(constanceData)));
			
			//Run the variable data
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource2).setValue(String.valueOf(variableData)));
			
			//Increment variable data and run again. Now it shall activate the behaviour
			variableData++;
			this.handler.activateLocalBehaviours(Datapoint.newDatapoint(datapointsource2).setValue(String.valueOf(variableData)));			
			
			assertEquals(true, activateBehaviour.hasRun());
			log.info("Test passed");
			
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}
	
	/**
	 * Test if activator can register and deregister correctly. Register 2 activators and deregister one of them. After that, the list shall be with count 1
	 */
	@Test
	public void registerDeregisterTest() {
		log.debug("Start register/deregister tester");
		try {
			//Address
			String datapointsource1 = "activator.test.addressconstantvalue";
			String datapointsource2 = "activator.test.addressvariablevalue";
			
			
			String activatorName1 = "testactivator1";
			String activatorName2 = "testactivator2";
			String conditionNameIsTrue = "isTrue";
			String conditionNameIsOne = "isOne";
			
			//Create activator
			Activator activator1 = new ActivatorJADEBehaviourImpl();
			Activator activator2 = new ActivatorJADEBehaviourImpl();
			
			//Create behaviour
			DummyBehaviour activateBehaviour1 = new DummyBehaviour();
			activateBehaviour1.init(activatorName1, null, cell);
			Condition condition1 = new ConditionAlwaysTrue().init(conditionNameIsTrue, null);
			Map<String, List<Condition>> conditionMapping1 = new HashMap<String, List<Condition>>();
			conditionMapping1.put(datapointsource1, Arrays.asList(condition1));
			
			activator1.initWithConditions(activatorName1, conditionMapping1, "", activateBehaviour1, cell);
			this.handler.registerActivatorInstance(activator1);
			
			DummyBehaviour activateBehaviour2 = new DummyBehaviour();
			activateBehaviour2.init(activatorName2, null, cell);
			Condition condition2 = new ConditionIsOne().init(conditionNameIsOne, null);
			Map<String, List<Condition>> conditionMapping2 = new HashMap<String, List<Condition>>();
			conditionMapping2.put(datapointsource2, Arrays.asList(condition2));

			activator2.initWithConditions(activatorName1, conditionMapping2, "", activateBehaviour2, cell);
			this.handler.registerActivatorInstance(activator2);
			log.debug("Activator registered in handler. System initialized with activators={}", this.handler.getActivatorMap());
			
			//Remove both activations
			this.handler.deregisterActivatorInstance(activator1);
			//this.handler.deregisterActivatorInstance(activator2);
			
			assertEquals(1, this.handler.getActivatorMap().size(), 0.0);
			log.info("Test passed");
			
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}
	
}
