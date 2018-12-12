package at.tuwien.ict.acona.evolutiondemo.stockmarketagent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import org.jfree.data.time.Day;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;

public class PriceGraphToolFunction extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(PriceGraphToolFunction.class);
	
	private OHLCGraph graph;
	
	private String seriesName;
	private double open;
	private double close;
	private double high;
	private double low;
	private Day day;
	

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		graph = new OHLCGraph("XY Series Demo");
		graph.pack();
		RefineryUtilities.centerFrameOnScreen(graph);
		graph.setVisible(true);
		
		log.info("OHLC Graph function initialized");
	}

	@Override
	protected void executeFunction() throws Exception {
		this.graph.updateDataset(seriesName, day, open, high, low, close);
		
		
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		//Read datapoint
		//String key = data.keySet().iterator().next();
		JsonObject object;
		try {
			object = data.getAsJsonObject();
			
			//Check if OHLC data
			if (object.has("open")) {
				
				seriesName=id;
				open=object.get("open").getAsDouble();
				close=object.get("close").getAsDouble();
				high=object.get("high").getAsDouble();
				low=object.get("low").getAsDouble();
				String date = object.get("date").getAsString();
				
				Calendar cal = Calendar.getInstance();  
				SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				cal.setTime(dateFormat.parse(date));    
				day = new Day(cal.getTime());
				
				this.setStart();
				
				log.debug("received update={}", object);
			} else {
				log.warn("No valid OHLC value={}", object);
			}
			
		} catch (Exception e) {
			log.error("Cannot read value", e);
		}
		
	}

}
