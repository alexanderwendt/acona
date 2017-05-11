package at.tuwien.ict.acona.framework.modules;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class OptionSelectorCodelet extends CellFunctionCodelet {

	private final static Logger log = LoggerFactory.getLogger(OptionSelectorCodelet.class);

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		log.info("Option Selector Codelet initialized");

	}

	@Override
	protected void executeFunction() throws Exception {
		log.info("Execute Option Selector Codelet");

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
