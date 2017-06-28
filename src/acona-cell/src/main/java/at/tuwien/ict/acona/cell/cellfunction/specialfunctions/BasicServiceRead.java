package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

/**
 * @author wendt
 * 
 *         A list of datapoints is read. Automatically, wildcards are used. If a
 *         name is not complete, all completing names
 *
 */
public class BasicServiceRead extends CellFunctionBasicService implements ReadDatapoint {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	//private static Logger log = LoggerFactory.getLogger(BasicServiceRead.class);

	//	public static final String READMETHOD = "read";
	//
	//	private static final String ACKNOWLEDGE = "OK";
	//	private static final String ERROR = "ERROR";
	//	private static final String PARAMETERRESULT = "result";
	//	private static final String PARAMETERSENDER = "sender";
	//	private static final String METHOD = "method";
	//	private static final String PARAMETERDATAPOINTS = "datapoints";

	//private String currentCaller = this.getCell().getLocalName();

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameter, String caller) {
		List<Datapoint> result = new ArrayList<>();
		try {

			List<Datapoint> datapoints = Lists.newArrayList(parameter.values());
			result.addAll(this.read(datapoints));

		} catch (Exception e) {
			log.error("Cannot perform operation", e);
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
		}

		return result;
	}

	@Override
	public List<Datapoint> read(final List<Datapoint> datapointList) {
		List<Datapoint> result = new ArrayList<>();

		datapointList.forEach(dp -> {
			result.addAll(this.getCell().getDataStorage().read(dp.getAddress()));
		});

		return result;
	}

}
