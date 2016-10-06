package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.helpers.DummyCell;
import at.tuwien.ict.acona.cell.core.helpers.DummyFunction;

public class CellBuilderTester {

	private static Logger log = LoggerFactory.getLogger(CellBuilderTester.class);

	// private static final String CELLNAME = "cellname";
	// private static final String CELLCLASS = "cellclass";
	// private static final String CELLCONDITIONS = "conditions";
	// private static final String CELLBEHAVIOURS = "cellbehaviours";
	// private static final String CELLACTIVATORS = "activators";

	// private static final String CONDITIONNAME = "conditionname";
	// private static final String CONDITIONCLASS = "conditionclass";

	// private static final String BEHAVIOURNAME = "behaviourname";
	// private static final String BEHAVIOURCLASS = "behaviourclass";

	// private static final String ACTIVATORNAME = "activatorname";
	// private static final String ACTIVATORMAP = "activatormap";
	// private static final String ACTIVATORBEHAVIOUR = "activatorbehaviour";
	// private static final String ACTIVATORLOGIC = "activatorlogic";

	@Before
	public void setUp() throws Exception {
		log.info("Start cell activator tester");
		try {

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void createCellFromConfig() {
		try {
			// Create the cell
			CellConfig cellConfig = CellConfig.newConfig("testcell", DummyCell.class);
			cellConfig.addProperty("testproperty1", "10000");
			cellConfig.addCellfunction(CellFunctionConfig.newConfig("function1", DummyFunction.class)
					.addSyncDatapoint(DatapointConfig.newConfig("ID1", "agent1.dp1.value", )).setExecuteOnce(true)
					.setExecuterate(500).setProperty("TESTPROPERTY1", "valuesuccess"));

			log.info("Config={}", cellConfig);

			// Create config
			// JsonObject cellConfig = new JsonObject();
			// JsonObject conditionAlwaysTrueConfig = new JsonObject();
			// JsonObject conditionAlwaysFalseConfig = new JsonObject();
			// JsonObject behaviourToExecuteConfig = new JsonObject();
			// JsonObject activatorConfig = new JsonObject();
			//
			// conditionAlwaysTrueConfig.addProperty(CONDITIONNAME,
			// "BuilderTestAlwaysTrue");
			// conditionAlwaysTrueConfig.addProperty(CONDITIONCLASS,
			// "at.tuwien.ict.acona.cell.activator.helper.ConditionAlwaysTrue");
			// conditionAlwaysTrueConfig.addProperty("option1", "testTrue2");
			// conditionAlwaysTrueConfig.addProperty("option2", "testTrue2");
			//
			// conditionAlwaysFalseConfig.addProperty(CONDITIONNAME,
			// "BuilderTestAlwaysFalse");
			// conditionAlwaysFalseConfig.addProperty(CONDITIONCLASS,
			// "at.tuwien.ict.acona.cell.activator.helper.ConditionAlwaysFalse");
			// conditionAlwaysFalseConfig.addProperty("option1", "test2false");
			// conditionAlwaysFalseConfig.addProperty("option2", "test2false");
			//
			// behaviourToExecuteConfig.addProperty(BEHAVIOURNAME,
			// "BuilderTestDummyBehaviour");
			// behaviourToExecuteConfig.addProperty(BEHAVIOURCLASS,
			// "at.tuwien.ict.acona.cell.activator.helper.DummyBehaviour");
			// behaviourToExecuteConfig.addProperty("option1", "testXXTrue2");
			// behaviourToExecuteConfig.addProperty("option2", "testXXTrue2");
			//
			// activatorConfig.addProperty(ACTIVATORNAME,
			// "builderTestActivator");
			// activatorConfig.addProperty(ACTIVATORLOGIC, "NOTHING");
			// activatorConfig.addProperty(ACTIVATORBEHAVIOUR,
			// "BuilderTestDummyBehaviour");
			//
			// Gson gson = new GsonBuilder().create();
			// String input = "[{\"test.data1\":\"BuilderTestAlwaysTrue\"},"
			// + "{\"test.data2\":\"BuilderTestAlwaysFalse\"}]";
			// JsonArray activatorMapping = gson.fromJson(input,
			// JsonArray.class);
			// //JsonArray activatorMapping = new JsonArray();
			// //JsonObject map1 = new JsonObject
			// activatorConfig.add(ACTIVATORMAP, activatorMapping);
			//
			// cellConfig.addProperty(CELLNAME, "buildertestcell");
			// cellConfig.addProperty(CELLCLASS,
			// "at.tuwien.ict.acona.cell.activator.helper.DummyCell");
			// cellConfig.add(CELLCONDITIONS, new JsonArray());
			// cellConfig.add(CELLBEHAVIOURS, new JsonArray());
			// cellConfig.add(CELLACTIVATORS, new JsonArray());
			// cellConfig.getAsJsonArray(CELLCONDITIONS).add(conditionAlwaysFalseConfig);
			// cellConfig.getAsJsonArray(CELLCONDITIONS).add(conditionAlwaysTrueConfig);
			// cellConfig.getAsJsonArray(CELLBEHAVIOURS).add(behaviourToExecuteConfig);
			// cellConfig.getAsJsonArray(CELLACTIVATORS).add(activatorConfig);

			DummyCell cell = new DummyCell();
			CellBuilder builder = new CellBuilder();

			builder.initializeCellConfig(cellConfig, cell);// .initializeCellConfig(cellConfig,
															// cell);
			String actualResult = cell.getFunctionHandler().getCellFunctionMapping().get("agent1.dp1.value").get(0)
					.getFunctionConfig().getProperty("TESTPROPERTY1");

			// Get the name of one of the activators

			log.debug("expected result={}, actual result={}", "valuesuccess", actualResult);
			assertEquals("valuesuccess", actualResult);
			log.info("Test passed");

		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}

	// @Test
	// public void createEmptyCellFromConfig() {
	// try {
	// //Create config
	// JsonObject cellConfig = new JsonObject();
	//
	// DummyCell cell = new DummyCell();
	// CellBuilder builder = new CellBuilder();
	//
	//
	// builder.initializeCellConfig(cellConfig, cell);
	// //boolean actualResult = cell.getActivatorMap().isEmpty();
	// //Get the name of one of the activators
	//
	//
	// assertEquals(true, actualResult);
	// log.info("Test passed");
	//
	// } catch (Exception e) {
	// log.error("Cannot init system", e);
	// fail("Error");
	// }
	// }

}
