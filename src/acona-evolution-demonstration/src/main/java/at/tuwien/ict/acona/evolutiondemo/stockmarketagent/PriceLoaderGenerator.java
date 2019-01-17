package at.tuwien.ict.acona.evolutiondemo.stockmarketagent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions.Builder;

/**
 * This function generates a highest, lowest and close price for the system and writes it into the working memory of itself. On trigger, 
 * it creates these 3 numbers, adds to the history and puts in the memory
 * 
 * @author wendt
 *
 */
public class PriceLoaderGenerator extends CellFunctionCodelet {
	
	private final static Logger log = LoggerFactory.getLogger(PriceLoaderGenerator.class);
	
	//private double high, low, close, open;
	
	private Table prices;
	
	private int index=0;
	private int cycleCounter = 0;
	
	private final String dataAddress = "data";
	private JsonObject functionResult;
	
	public final static String ATTRIBUTESTOCKNAME = "stockname";
	//public final static String ATTRIBUTEMODE = "mode";
	
	private String stockName = "";
	
	//private OHLCGraph demo;
	
	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		stockName = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKNAME, "");
		//this.mode = this.getFunctionConfig().getProperty(ATTRIBUTEMODE, Integer.class);
		
		this.addManagedDatapoint(DatapointConfig.newConfig(dataAddress, dataAddress, SyncMode.WRITEONLY));
		
		 //demo = new OHLCGraph("XY Series Demo");
		 //demo.pack();
		 //RefineryUtilities.centerFrameOnScreen(demo);
		 //demo.setVisible(true);
		
		//Load a table with all prices
		Builder builder = 
				CsvReadOptions.builder("C:\\Projekte\\19_SoC_SAVE\\SAVE_Workspace\\acona\\data\\Stock_Market_Data\\OMXS30_19970820-20090409.CSV")
					.separator(';')			// table is tab-delimited
					.header(true)				// header
					.dateFormat("yyyy-MM-dd");  // the date format to use. 

		CsvReadOptions options = builder.build();
		prices = Table.read().csv(options);
		
		log.info("Price loader generator initialized. Table={}", prices.shape());
}
	
//	private List<List<String>> readCSV(String filePath, String separator) throws FileNotFoundException, IOException {
//		List<List<String>> records = new ArrayList<>();
//		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//		    String line;
//		    while ((line = br.readLine()) != null) {
//		        String[] values = line.split(separator);
//		        records.add(Arrays.asList(values));
//		    }
//		}
//		
//		return records;
//	}

	@Override
	protected void executeFunction() throws Exception {
		
		//Get the data from the table
		double closeValue = Double.valueOf(this.prices.column(1).get(index).toString());
		
		String untildate="2000-01-01";//can take any date in current format    
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");   
		Calendar cal = Calendar.getInstance();    
		cal.setTime(dateFormat.parse(untildate));    
		cal.add(Calendar.DATE, cycleCounter + this.index);
		
		functionResult = new JsonObject();
		this.functionResult.addProperty("name", stockName);
		this.functionResult.addProperty("run", this.index);
		this.functionResult.addProperty("date", dateFormat.format(cal.getTime()));
		this.functionResult.addProperty("open", closeValue);
		this.functionResult.addProperty("close", closeValue);
		this.functionResult.addProperty("high", closeValue);
		this.functionResult.addProperty("low", closeValue);

		
		//Day day = new Day(cal.getTime());
		//this.demo.updateDataset(day, open, high, low, close);
		
		if (this.prices.column(1).size()-1>this.index) {
			this.index++;
		} else {
			//Reset if bad
			this.cycleCounter += this.index+1;
			this.index = 0;
		}
		
		
		log.info("Generated price={}. Put it on address={}", functionResult, this.getCellName() + ":" + dataAddress);
		log.info("Long cycle={}, short cycle={}",this.cycleCounter, this.index);
		
		this.getValueMap().put(dataAddress, this.getDatapointBuilder().newDatapoint(dataAddress).setValue(functionResult));
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
	
	}

}
