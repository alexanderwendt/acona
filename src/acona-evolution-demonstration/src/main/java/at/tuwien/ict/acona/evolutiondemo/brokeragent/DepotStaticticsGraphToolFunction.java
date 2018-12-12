package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jfree.data.time.Day;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;

public class DepotStaticticsGraphToolFunction extends CellFunctionThreadImpl {

	private final static Logger log = LoggerFactory.getLogger(DepotStaticticsGraphToolFunction.class);

	private TimeSeriesGraph graph;

	private List<Types> species;
	private Day day;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		graph = new TimeSeriesGraph("Species Count");
		graph.pack();
		RefineryUtilities.positionFrameRandomly(graph);
		graph.setVisible(true);

		log.info("Species count Graph function initialized");
	}

	@Override
	protected void executeFunction() throws Exception {
		for (Types t : species) {
			this.graph.updateDataset(t.getType(), day, t.getNumber());
		}
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
		// Read datapoint
		JsonObject object;
		try {
			object = data.getAsJsonObject();

			// Get the date
			String date = object.getAsJsonPrimitive("date").getAsString();
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
			cal.setTime(dateFormat.parse(date));
			day = new Day(cal.getTime());

			// Get the tree
			species = (new Gson()).fromJson(object.get("types"), new TypeToken<List<Types>>() {}.getType());

			this.setStart();

		} catch (Exception e) {
			log.error("Cannot read value", e);
		}
		
	}

}
