package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.mq.datastructures.Request;

public class Evaluator extends CellFunctionCodelet {

	private static final Logger log = LoggerFactory.getLogger(Evaluator.class);

	public final static String STATISTICSCOLLECTORSERVICENAME = "statservicename";
	public final static String STATISTICSDATAPOINTNAME = "statisticsdatapointname";

	private String statServiceAddress = "";
	private String statisticsDatapointAddress = "";

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		statServiceAddress = this.getFunctionConfig().getProperty(STATISTICSCOLLECTORSERVICENAME);
		statisticsDatapointAddress = this.getFunctionConfig().getProperty(STATISTICSDATAPOINTNAME);
	}

	@Override
	protected void executeFunction() throws Exception {
		// Read the statistics
		log.info("Start evaluator to read agent statistics. Read from={}", statServiceAddress + "/" + StatisticsCollector.GETSTATISTICSSUFFIX);
		JsonElement statistics = this.getCommunicator().execute(statServiceAddress, new Request()).getResult();
		
		

		// Write the statistics to a datapoint
		this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(statisticsDatapointAddress).setValue(statistics));
		log.info("Written stats={} to {}", statistics, statisticsDatapointAddress);

	}

	@Override
	public void resetCodelet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDownCodelet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
