package at.tuwien.ict.acona.cell.datastructures;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;


public class DatapointTester {

	private static Logger log = LoggerFactory.getLogger(DatapointTester.class);
	
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
			//create message content
			String address = "testAddress";
			String type = "operand";
			String value = "value of datapoint";
			
			//Create new datapoint
			Datapoint dp = Datapoint.newDatapoint(address).setType(type).setValue(value);
			
			//Convert to String
			String dpString = dp.toJsonObject().toString();
			
			//Convert back to datapoint
			Datapoint resultDp = Datapoint.toDatapoint(dpString);
			
			assertEquals(value, resultDp.getValue().getAsJsonPrimitive().getAsString());
			log.info("Test passed. Result={}", resultDp);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	
	@Test
	public void datapointConversionTest() {
		log.info("Start datapoint conversion tester");
		try {
			String input = "{\"ADDRESS\":\"subscribe.test.address\",\"TYPE\":\"\",\"VALUE\":\"Wrong value\"}";
			//String input = "{\"ADDRESS\":\"subscribe.test.address\",\"TYPE\":\"\",\"VALUE\":\"MuHaahAhaAaahAAHA\"}";
			
			Datapoint dp = Datapoint.toDatapoint(input);
			
			assertEquals("Wrong value", dp.getValue().getAsJsonPrimitive().getAsString());
			log.info("Test passed. Input={}", input);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	
	
}
