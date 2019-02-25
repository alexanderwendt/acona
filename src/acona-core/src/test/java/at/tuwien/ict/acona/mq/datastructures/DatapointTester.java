package at.tuwien.ict.acona.mq.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatapointTester {

	private static Logger log = LoggerFactory.getLogger(DatapointTester.class);

	private final DPBuilder dpb = new DPBuilder();

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void datapointTest() {
		log.info("Start datapoint tester");
		try {
			// create message content
			String address = "testAddress";
			String value = "value of datapoint";

			// Create new datapoint
			Datapoint dp = dpb.newDatapoint(address).setValue(value);

			// Convert to String
			String dpString = dp.toJsonObject().toString();

			// Convert back to datapoint
			Datapoint resultDp = dpb.toDatapoint(dpString);

			assertEquals(value, resultDp.getValue().getAsJsonPrimitive().getAsString());
			log.info("Test passed. Result={}", resultDp);
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	@Test
	public void datapointConversionTest() {
		log.info("Start datapoint conversion tester");
		try {
			String input = "{\"ADDRESS\":\"subscribe.test.address\",\"AGENT\":\"agent1\",\"VALUE\":\"Wrong value\"}";
			// String input = "{\"ADDRESS\":\"subscribe.test.address\",\"TYPE\":\"\",\"VALUE\":\"MuHaahAhaAaahAAHA\"}";

			Datapoint dp = dpb.toDatapoint(input);

			assertEquals("Wrong value", dp.getValue().getAsJsonPrimitive().getAsString());
			log.info("Test passed. Input={}", input);
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	@Test
	public void datapointAgentNameTester() {
		log.info("Start datapoint agent name tester");

		String address = "<agent1>/database/workingmemory/episode1s";

		try {

			Datapoint dp = dpb.newDatapoint(address);

			String agent = dp.getAgent();
			String addressResult = dp.getAddress();
			String topic = dp.getCompleteAddressAsTopic("agent1");

			assertEquals(agent, "agent1");
			assertEquals(addressResult, "database/workingmemory/episode1s");
			assertEquals(topic, address);

			log.info("Test passed. Input={}", address);
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

}
