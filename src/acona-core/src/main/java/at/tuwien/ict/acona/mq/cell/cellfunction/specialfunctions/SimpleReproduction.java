package at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

/**
 * Replicate the cell by making a copy of the cellfunctionconfig and use it to generate a new cell.
 * This function is topic of genetic programming, where the functions are being manipulated.
 * 
 * @author wendt
 */
public class SimpleReproduction extends CellFunctionThreadImpl {

	protected static Logger log = LoggerFactory.getLogger(SimpleReproduction.class);

	private static int reproductionCount = 0;

	@Override
	protected void cellFunctionThreadInit() throws Exception {

		log.info("{}>Cell reproducer function initialized", this.getFunctionName());
	}

	@Override
	protected void executeFunction() throws Exception {
		//Increment auf every reproduction
		reproductionCount++;
		
		// Get config
		CellConfig config = this.getCell().getConfiguration();
		CellConfig newConfig = CellConfig.newConfig(config.toJsonObject());

		// Modify name, in order not to have dupicate agents
		String oldName = this.getCell().getName();
		String newName = oldName + "Repl" + reproductionCount;
		newConfig.setName(newName);

		SystemControllerImpl controller = SystemControllerImpl.getLauncher();
		controller.createAgent(newConfig);
		


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
