package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.mq.datastructures.Request;

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

		JsonElement statistics = this.getCommunicator().execute(statServiceName + "/" + StatisticsCollector.GETSTATISTICSSUFFIX, new Request()).getResult();
		
		

		// Write the statistics to a datapoint
		this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(statisticsDatapointName).setValue(statistics));
		log.debug("Written stats={} to {}", statistics, statisticsDatapointName);

	}

	@Override
	public void resetCodelet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
