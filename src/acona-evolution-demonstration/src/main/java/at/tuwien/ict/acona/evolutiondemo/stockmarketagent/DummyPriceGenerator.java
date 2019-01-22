package at.tuwien.ict.acona.evolutiondemo.stockmarketagent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.jfree.data.time.Day;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;

/**
 * This function generates a highest, lowest and close price for the system and writes it into the working memory of itself. On trigger, 
 * it creates these 3 numbers, adds to the history and puts in the memory
 * 
 * @author wendt
 *
 */
public class DummyPriceGenerator extends CellFunctionCodelet {
	
	private final static Logger log = LoggerFactory.getLogger(DummyPriceGenerator.class);
	
	private double high, low, close, open;
	
	private int currentPeriod=0;
	
	//Fixed varibales for the calculation
	private double periodlength = 20;
	private double amplitude = 50;
	private double offset= 100;
	
	private final String dataAddress = "data";
	private JsonObject functionResult;
	
	public final static String ATTRIBUTESTOCKNAME = "stockname";
	public final static String ATTRIBUTEMODE = "mode";
	
	private String stockName = "";
	private int mode = 0;
	
	private OHLCGraph demo;
	
	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		stockName = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKNAME, "");
		this.mode = this.getFunctionConfig().getProperty(ATTRIBUTEMODE, Integer.class);
		
		this.addManagedDatapoint(DatapointConfig.newConfig(dataAddress, dataAddress, SyncMode.WRITEONLY));
		
		 //demo = new OHLCGraph("XY Series Demo");
		 //demo.pack();
		 //RefineryUtilities.centerFrameOnScreen(demo);
		 //demo.setVisible(true);
		
	}

	@Override
	protected void executeFunction() throws Exception {
		
		if (this.mode==0) {
			this.executeSinusFunction();
		} else if (this.mode==1) {
			this.executeConstantFunction();
		}
		
		String untildate="2000-01-01";//can take any date in current format    
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");   
		Calendar cal = Calendar.getInstance();    
		cal.setTime(dateFormat.parse(untildate));    
		cal.add(Calendar.DATE, this.currentPeriod);
		
		functionResult = new JsonObject();
		this.functionResult.addProperty("name", stockName);
		this.functionResult.addProperty("run", this.currentPeriod);
		this.functionResult.addProperty("date", dateFormat.format(cal.getTime()));
		this.functionResult.addProperty("open", this.close);
		this.functionResult.addProperty("close", this.close);
		this.functionResult.addProperty("high", this.high);
		this.functionResult.addProperty("low", this.low);

		
		//Day day = new Day(cal.getTime());
		
		//this.demo.updateDataset(day, open, high, low, close);
		
		this.currentPeriod++;
		
		log.info("Generated price={}. Put it on address={}", functionResult, this.getCellName() + ":" + dataAddress);
		
		this.getValueMap().put(dataAddress, this.getDatapointBuilder().newDatapoint(dataAddress).setValue(functionResult));
	}
	
	/**
	 * The program shall create a sinus curve to test the system on
	 */
	private void executeSinusFunction() {
		this.close = offset + amplitude * Math.sin((double)currentPeriod/periodlength*Math.PI);
		this.high = this.close + 2;
		this.low = this.close - 2;
		this.open = close;
	}
	
	/**
	 * Create a constant function that only produces one price
	 */
	private void executeConstantFunction() {
		this.close = offset;
		this.high = offset + 2;
		this.low = offset - 2;
		this.open = close;
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
