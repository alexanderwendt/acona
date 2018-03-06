package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;

public class Evaluator extends CellFunctionCodelet {

	private static final Logger log = LoggerFactory.getLogger(Evaluator.class);

	public final static String STATISTICSCOLLECTORSERVICENAME = "statservicename";
	public final static String STATISTICSDATAPOINTNAME = "statisticsdatapointname";

	private String statServiceName = "";
	private String statisticsDatapointName = "";

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		statServiceName = this.getFunctionConfig().getProperty(STATISTICSCOLLECTORSERVICENAME);
		statisticsDatapointName = this.getFunctionConfig().getProperty(STATISTICSDATAPOINTNAME);
	}

	@Override
	protected void executeFunction() throws Exception {
		// Read the statistics
		JsonRpcRequest req = new JsonRpcRequest("any", 0);

		JsonElement statistics = this.getCommunicator().execute(statServiceName, req).getResult();

		// Write the statistics to a datapoint
		this.getCommunicator().write(DatapointBuilder.newDatapoint(statisticsDatapointName).setValue(statistics));
		log.debug("Written stats={} to {}", statistics, statisticsDatapointName);

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
