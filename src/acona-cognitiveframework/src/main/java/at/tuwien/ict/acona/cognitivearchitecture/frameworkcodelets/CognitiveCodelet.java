package at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cognitivearchitecture.CognitiveProcess;
import at.tuwien.ict.acona.cognitivearchitecture.datastructures.Goal;
import at.tuwien.ict.acona.cognitivearchitecture.datastructures.Option;

public abstract class CognitiveCodelet extends CellFunctionCodelet {
	private final static Logger log = LoggerFactory.getLogger(CognitiveCodelet.class);

	public final static String ATTRIBUTEGOALNAME = "goalname";

	public final static String OPTIONNAMESUFFIX = "OPTION";

	// private final List<String> states = new
	// ArrayList<String>(Arrays.asList(Vocabulary.States.RULESETAVAILABLE,
	// Vocabulary.States.RULESETRETURNED));

	// public static final String goaladdress = "goal";
	// public static final String episodeaddress = "episode";
	// public static final String OPTIONADDRESS = "option";
	// public static final String requestaddress = "request";
	// public static final String history = "state.history";

	public CognitiveCodelet() {
		// public static final String goaladdress = "goal";
		// public static final String episodeaddress = "episode";
		// public static final String optionaddress = "option";
		// public static final String requestaddress = "request";
	}

	protected Goal getGoal(Option o) throws Exception {
		Goal result = this.getCommunicator()
				.read(o.getGoalAddress())
				.getValue(Goal.class);
		return result;
	}

	/**
	 * Get the reference goal
	 * 
	 * @return
	 * @throws Exception
	 */
	protected List<Datapoint> getReferenceGoals(String preState, String postState) throws Exception {
		// Read all goals
		String completeAddress = CognitiveProcess.GOALSPREFIXADDRESS + "*";
		List<Datapoint> goals;
		try {
			goals = this.getCommunicator()
					.readWildcard(completeAddress);
		} catch (Exception e) {
			log.error("Cannot read address={}", completeAddress);
			throw e;
		}

		log.debug("Got goals={} from={}", goals, completeAddress);
		// Search for the endstate in all goals. If the endgoal
		// String goalAddress = "";

		List<Datapoint> goalReferenceAddress = new ArrayList<>();
		// ArrayList<String> goalReferences = new ArrayList<String>();

		for (Datapoint dp : goals) {
			Goal goal = dp.getValue(Goal.class); // Chunk.newChunk(dp.getValue().getAsJsonObject());

			// The option can only be linked to a goal if its precondition state matches the
			// current state && its post condition matches the required state
			log.debug("Goal current state={}, required state={}); this pre state={}, post state={}", goal.getCurrentState(), goal.getRequireState(), preState, postState);
			if (preState.equals(goal.getCurrentState()) && postState.equals(goal.getRequireState())) {
				goalReferenceAddress.add(dp);
				log.debug("Found matching goal={}", goal.getName());

				break;
			}
		}

		return goalReferenceAddress;
	}

	// protected List<Datapoint> getDatapointGroup(boolean isWorkingMemory, String
	// subgroup) throws Exception {
	// String episodeGroupAddress = "";
	//
	// if (isWorkingMemory==true) {
	// episodeGroupAddress = this.getWorkingMemoryAddress() + "." + subgroup + "*";
	// } else {
	// episodeGroupAddress = this.getInternalStateMemoryAddress() + "." + subgroup +
	// "*";
	// }
	//
	// List<Datapoint> episodes =
	// this.getCommunicator().readWildcard(episodeGroupAddress);
	// return episodes;
	// }

	protected String getLastAction() throws Exception {
		JsonArray historyData = this.getCommunicator()
				.read(CognitiveProcess.ACTIONHISTORYADDRESS)
				.getValueOrDefault(new JsonArray())
				.getAsJsonArray();

		String result = "";
		if (historyData.size() > 0) {
			result = historyData.get(historyData.size() - 1)
					.getAsString();
		}

		return result;
	}

	protected boolean containsActionInHistory(String action, int numberOfLastEntries) throws Exception {
		JsonArray historyData = this.getCommunicator()
				.read(CognitiveProcess.ACTIONHISTORYADDRESS)
				.getValueOrDefault(new JsonArray())
				.getAsJsonArray();

		boolean result = false;

		int endValue = 0;

		if (historyData.size() > numberOfLastEntries) {
			endValue = historyData.size() - numberOfLastEntries;
		}

		for (int i = historyData.size() - 1; i >= endValue; i--) {
			String record = historyData.get(i)
					.getAsJsonPrimitive()
					.getAsString();

			if (record.equals(action) == true) {
				result = true;
				break;
			}
		}

		return result;
	}

	// protected String[] updateHistory(String[]) {
	// //Update
	// Datapoint history =
	// this.getCommunicator().read(CognitiveProcessUtil.ACTIONHISTORYADDRESS);
	// log.debug("Old history={}", history);
	// JsonArray historyData;
	// if (history.hasEmptyValue()==false) {
	// historyData = history.getValue().getAsJsonArray();
	// } else {
	// historyData = new JsonArray();
	// }
	//
	// historyData.add(action);
	//
	// while (historyData.size()>=20) {
	// historyData.remove(0);
	// }
	// }

}
