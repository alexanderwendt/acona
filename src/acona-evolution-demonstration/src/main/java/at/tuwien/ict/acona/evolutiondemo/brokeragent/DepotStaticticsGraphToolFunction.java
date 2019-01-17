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
	private TimeSeriesGraph graph2;

	private List<SpeciesType> species;
	private List<AgentValue> values;
	private Day day;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		graph = new TimeSeriesGraph("Species Count");
		graph.pack();
		RefineryUtilities.positionFrameRandomly(graph);
		graph.setVisible(true);
		
		graph2 = new TimeSeriesGraph("Agent value");
		graph2.pack();
		RefineryUtilities.positionFrameRandomly(graph2);
		graph2.setVisible(true);

		log.info("Species count Graph function initialized");
	}

	@Override
	protected void executeFunction() throws Exception {
		for (SpeciesType t : species) {
			this.graph.updateDataset(t.getType(), day, (int)t.getNumber());
		}
		
		for (AgentValue t : values) {
			this.graph2.updateDataset(t.getName(), day, t.getValue());
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
			object = this.getValueFromJsonDatapoint(data).getAsJsonObject();

			// Get the date
			String date = object.getAsJsonPrimitive("date").getAsString();
			Calendar cal = Calendar.getInstance();
			//SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			cal.setTime(dateFormat.parse(date));
			day = new Day(cal.getTime());

			// Get the tree
			species = (new Gson()).fromJson(object.get("types"), new TypeToken<List<SpeciesType>>() {}.getType());
			values = (new Gson()).fromJson(object.get("values"), new TypeToken<List<AgentValue>>() {}.getType());

			this.setStart();

		} catch (Exception e) {
			log.error("Cannot read value", e);
		}
		
	}

}
