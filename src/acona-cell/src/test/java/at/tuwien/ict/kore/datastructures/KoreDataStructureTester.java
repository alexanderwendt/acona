package at.tuwien.ict.kore.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class KoreDataStructureTester {

	private static Logger log = LoggerFactory.getLogger(KoreDataStructureTester.class);

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	/**
	 * Create a drive, serialize it, deserialize it and read the value back
	 */
	@Test
	public void testDrive() {
		try {
			log.info("=== Test Drivedatastructure ===");
			// Create a drive and set the intensity
			Drive drive = Drive.newDrive("comfort", 0.5);
			log.debug("Print the drive={}", drive);

			// Serialize the drive and bring it back
			JsonObject serializedDrive = drive.toJsonObject();

			Drive deserialzedDrive = Drive.newDrive(serializedDrive);

			// Get intensity
			double value = deserialzedDrive.getIntensity();

			log.debug("correct value={}, actual value={}", 0.5, value);

			assertEquals(value, 0.5, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Create a complete perceived state from a use case. Serialize it, put it
	 * into a datapoint, deserialize it, add a reward, serialize it and
	 * deserialize it again and read the just added reward
	 */
	@Test
	public void testConceptAsPerceivedState() {
		try {
			log.info("=== Test Drivedatastructure ===");

			Concept perceivedState = Concept.newConcept("PerceivedState");
			perceivedState
					.addSubconcept(Concept.newConcept("Room1").setValue("TEMPERATURE", 22.0).setValue("CO2", 450.89)
							.setValue("OCCUPANCY", 1))
					.setMetadata(Metadata.newMetadata(perceivedState).addReward(Reward.newReward("COMFORT", 0.0))
							.addReward(Reward.newReward("ENERGY", 0.5)));

			// Create a drive and set the intensity
			Drive lastComfortDrive = Drive.newDrive("COMFORT", 0.5);
			Drive newComfortDrive = Drive.newDrive("COMFORT", 0.8);
			log.debug("Old drive={}, new drive={}", lastComfortDrive, newComfortDrive);

			double newReward = newComfortDrive.getIntensity() - lastComfortDrive.getIntensity();

			log.debug("Perceived state={}", perceivedState);
			perceivedState.getMetadata().getReward().iterator().next().setReward(newReward);

			// Serialize the drive and bring it back
			JsonObject perceivedStateSerialized = perceivedState.toJsonObject();

			// Get it back
			Concept deserializedPerceivedState = Concept.newConcept(perceivedStateSerialized);

			// Set the new reward

			// Get intensity
			double value = deserializedPerceivedState.getMetadata().getReward().iterator().next().getReward();

			log.debug("correct value={}, actual value={}", 0.3, value);

			assertEquals(value, 0.3, 0.00001);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
