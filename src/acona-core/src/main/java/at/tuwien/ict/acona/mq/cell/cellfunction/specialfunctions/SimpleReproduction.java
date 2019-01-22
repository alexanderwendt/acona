package at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

/**
 * Replicate the cell by making a copy of the cellfunctionconfig and use it to generate a new cell.
 * This function is topic of genetic programming, where the functions are being manipulated.
 * 
 * @author wendt
 */
public class SimpleReproduction extends CellFunctionThreadImpl {

	public final static String EXECUTEREPLICATION = "executereplication";
	
	protected static Logger log = LoggerFactory.getLogger(SimpleReproduction.class);

	private CellConfig replicationConfig = null;
	
	private static int reproductionCount = 0;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		log.info("{}>Cell reproducer function initialized", this.getFunctionName());
		
		this.addRequestHandlerFunction(EXECUTEREPLICATION, (Request input) -> executeReplication(input));
	}
	
	private Response executeReplication(Request req) {
		Response result = null;
		
		log.debug("Execute the codelet handler");
		try {
			if (req.hasParameter("config")) {
				JsonObject inputConfig = req.getParameter("config", JsonObject.class);
				this.replicationConfig = CellConfig.newConfig(inputConfig);
			} else {
				this.replicationConfig = this.getCell().getConfiguration();
			} 
			
			//Start replication
			this.setStart();
			result = new Response(req).setResultOK(); 
		} catch (Exception e) {
			log.error("Cannot start codelet handler", e);
			result = new Response(req).setError("Cannot replicate");
			result.setError(e.getMessage());
		}
		
		return result;
	}

	@Override
	protected void executeFunction() throws Exception {
		//Increment auf every reproduction
		reproductionCount++;
		
		// Get config
		if (replicationConfig==null) {
			replicationConfig = this.getCell().getConfiguration();
			log.debug("Replication by copying the current configuration");
		} 
		
		CellConfig newConfig = CellConfig.newConfig(replicationConfig.toJsonObject());

		// Modify name, in order not to have dupicate agents
		String oldName = this.getCell().getName();
		String newName = newConfig.getName();
		if (oldName.equals(newName)) {
			newName = oldName + "_" + reproductionCount;
		} else {
			newName += "_" + reproductionCount;
		}
		newConfig.setName(newName);

		SystemControllerImpl controller = SystemControllerImpl.getLauncher();
		controller.createAgent(newConfig);
		
		
		//Reset replication config
		replicationConfig = null;

		log.info("{}>Reproduced and created new agent={}", this.getCell().getName(), newName);
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
