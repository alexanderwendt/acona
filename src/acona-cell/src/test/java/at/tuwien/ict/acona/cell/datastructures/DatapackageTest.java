package at.tuwien.ict.acona.cell.datastructures;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _OLD.at.tuwien.ict.acona.cell.datastructures.Chunk;
import _OLD.at.tuwien.ict.acona.cell.datastructures.ChunkImpl;
import _OLD.at.tuwien.ict.acona.cell.datastructures.Datapackage;
import _OLD.at.tuwien.ict.acona.cell.datastructures.DatapackageImpl;

@Deprecated
public class DatapackageTest {
	
	private static Logger log = LoggerFactory.getLogger(DatapackageTest.class);
	
	private static final String BODYTYPE = "BODYTYPE";
	private static final String X = "X";
	private static final String Y = "Y";
	private static final String OBJECTID = "OBJECTID";
	private static final String OBJECTNAME = "OBJECTNAME";

	@Test
	public void testChunk() {
		try {
			String expectedValue = "Schnitzel3";
			
			//Create perception
			Datapackage perceptionMap = DatapackageImpl.newDatapackage();
			ChunkImpl.ChunkBuilder perceptionBuilder = ChunkImpl.newChunk("EXTERNALPERCEPTIONADDRESS");
			for (int i=0;i<5;i++) {
				Chunk percept = ChunkImpl.newChunk("name" + i).
						newValue(BODYTYPE, "bodytypeSchnitzel" + i).
						newValue(X, String.valueOf(i)).
						newValue(Y, String.valueOf(i+1)).
						newValue(OBJECTID, "Schnitzel" + i).
						newValue(OBJECTNAME, "Schnitzel").
						build();
				
				perceptionBuilder.addSubChunk(percept, perceptionMap);
			}
			
			Chunk perception = perceptionBuilder.build();
			perceptionMap.setContent(perception);
			
			log.debug("Print data structure {}", perceptionMap);
			log.debug("Print subchunk by ID= {}", perceptionMap.getChunkByID("name03"));
			log.debug("Print all subchunks by name = {}", perceptionMap.get("EXTERNALPERCEPTIONADDRESS").getSubChunks(perceptionMap));
			log.debug("Print value from subchunk by name = {}", perceptionMap.get("EXTERNALPERCEPTIONADDRESS").getSubChunk("name3", perceptionMap).getValue(BODYTYPE));
			//perceptionMap.put(perception.getName(), perception);
			
			//Extract object id from the chunk id3
			//Get the perception chunk
			Chunk perceptionAnswer = perceptionMap.get("EXTERNALPERCEPTIONADDRESS");
			//Get third Schnitzel
			Chunk schnitzel3 = perceptionAnswer.getSubChunk("name3", perceptionMap);
			String actualValue = schnitzel3.getValue(OBJECTID);
			
			log.debug("Expected value={}, actual value={}", expectedValue, actualValue);
			assertTrue(expectedValue.equals(actualValue));
			
			
		} catch (Exception e) {
			log.error("Cannot execute test", e);
			fail("Error");
		}
		
		
		
	}

}
