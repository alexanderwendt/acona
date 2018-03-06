package at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cognitivearchitecture.CognitiveProcess;
import at.tuwien.ict.acona.cognitivearchitecture.datastructures.Option;

public class SelectionCodelet extends CognitiveCodelet {

	private final static Logger log = LoggerFactory.getLogger(SelectionCodelet.class);

	// private final static String PARAMSELECTIONTHRESHOLD = "selectionthreshold";

	// public final static String ATTRIBUTEOPTIONADDRESSSUFFIX = "optionaddress";
	// public final static String ATTRIBUTESELECTEDOPTIONADDRESSSUFFIX = "selectionaddress";

	// private String optionaddress="option";
	// private String selectionAddress="selection";

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		// optionaddress = this.getFunctionConfig().getProperty(ATTRIBUTEOPTIONADDRESSSUFFIX);
		// selectionAddress = this.getFunctionConfig().getProperty(ATTRIBUTESELECTEDOPTIONADDRESSSUFFIX);

		log.info("Codelet={} initialized with config={}", this.getFunctionName(), this.getFunctionConfig());
	}

	@Override
	protected void executeFunction() throws Exception {
		// Clean up: Remove the current selected option
		this.getCommunicator().remove(CognitiveProcess.SELECTEDOPTIONADDRESS);

		// Read all datappoints in the request namespace
		String completeAddress = CognitiveProcess.OPTIONSPREFIXADDRESS + "*";
		// Get all options and their values in a list
		List<Datapoint> options = this.getCommunicator().readWildcard(completeAddress);

		log.debug("Got options={} from={}", options, completeAddress);

		// options.get(0).getValue(Option.class).getEvaluation().getEvaluation()
		// Sort the list for evaluation
		Collections.sort(options, new Comparator<Datapoint>() {
			@Override
			public int compare(Datapoint o1, Datapoint o2) {
				return Double.compare(o1.getValue(Option.class).getEvaluation().getEvaluation(), o2.getValue(Option.class).getEvaluation().getEvaluation());
			}
		});

		// Select the highest ranked
		if (options.isEmpty() == false) {
			Option selection = options.get(options.size() - 1).getValue(Option.class);
			log.info("Selected option={}", selection);

			this.getCommunicator().write(DatapointBuilder.newDatapoint(CognitiveProcess.SELECTEDOPTIONADDRESS).setValue(selection));
			log.info("Written selected option={}, address={}", selection, CognitiveProcess.SELECTEDOPTIONADDRESS);
		} else {
			log.info("No option was selected. The system will not perform any action");
		}
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
