package at.tuwien.ict.acona.cell.communicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFQuery;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFSubscribeLock;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CommunicatorImpl extends AgentCommunicatorImpl implements BasicServiceCommunicator {

	protected static Logger log = LoggerFactory.getLogger(CommunicatorImpl.class);

	private static final String READSERVICENAME = "read";
	private static final String WRITESERVICENAME = "write";
	private static final String SUBSCRIBESERVICENAME = "subscribe";
	private static final String UNSUBSCRIBESERVICENAME = "unsubscribe";
	private static final String NOTIFYSERVICENAME = "notify";

	//private int defaultTimeout = 10000;

	//private final CellImpl cell;
	//private final DataStorage datastorage;
	//private final CellFunctionHandler cellFunctions;
	private final static Gson gson = new Gson();
	//private final ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	public CommunicatorImpl(CellImpl cell) {
		super(cell);
	}

	@Override
	public List<Datapoint> read(List<String> datapoints) throws Exception {
		return read(datapoints, this.getLocalAgentName(), defaultTimeout);
	}

	@Override
	public List<Datapoint> read(List<String> datapointaddress, String agentName, int timeout) throws Exception {
		final List<Datapoint> result = new ArrayList<>();
		List<Datapoint> inputList = new ArrayList<>();

		try {
			//Make the inputlist a parameter
			datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			result.addAll(this.execute(agentName, READSERVICENAME, inputList, timeout));

			if (result.isEmpty() == false && result.get(0).getValue().isJsonPrimitive() == true && result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot read values. Error returned from destination");
			}
		} catch (Exception e) {
			log.error("Cannot read value for addresses={} of agent={}", datapointaddress, agentName);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	public Datapoint read(String datapoint, String agentName) throws Exception {
		return read(datapoint, agentName, defaultTimeout);
	}

	@Override
	public Datapoint read(String datapoint, String agentName, int timeout) throws Exception {
		List<Datapoint> list = read(Arrays.asList(datapoint), agentName, timeout);

		Datapoint result = null;
		if (list.isEmpty()) {
			throw new Exception("Cannot read datapoint" + datapoint);
		} else {
			result = list.get(0);
		}

		return result;
	}

	@Override
	public Datapoint read(String datapointName) throws Exception {
		Datapoint result = null;

		//If the datapoint has the following addressformat: [Agent]:[Address], then replace the address and read from an agent
		if (datapointName.contains(":") == true) {
			result = this.read(datapointName.split(":")[1], datapointName.split(":")[0]);
		} else {
			result = this.read(datapointName, this.getLocalAgentName());
		}
		return result;
	}

	@Override
	public void write(List<Datapoint> datapoints) throws Exception {
		this.write(datapoints, this.getLocalAgentName(), defaultTimeout, true);
	}

	@Override
	public void write(List<Datapoint> datapoints, String agentComplementedName, int timeout, boolean blocking) throws Exception {
		// If a local data storage is meant, then write it there, else a foreign
		// data storage is meant.
		//		String agentName = agentComplementedName;
		//		if (agentComplementedName == null || agentComplementedName.isEmpty() || agentComplementedName.equals("")) {
		//			agentName = this.getLocalAgentName();
		//		}

		List<Datapoint> result;

		try {
			result = this.execute(agentComplementedName, WRITESERVICENAME, datapoints, timeout);

			if (result.isEmpty() == false && result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot write values. Error returned from destination");
			}
		} catch (Exception e) {
			log.error("Cannot write value for addresses={} of agent={}", datapoints, agentComplementedName);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	public void write(Datapoint datapoint) throws Exception {
		//If the datapoint has the following addressformat: [Agent]:[Address], then replace the address and write to the agent
		if (datapoint.getAddress().contains(":") == true) {
			String agent = datapoint.getAddress().split(":")[0];
			String address = datapoint.getAddress().split(":")[1];
			Datapoint writeDatapoint = Datapoint.newDatapoint(address).setValue(datapoint.getValue());

			this.write(Arrays.asList(writeDatapoint), agent, defaultTimeout, true);
		} else {
			this.write(Arrays.asList(datapoint), this.getLocalAgentName(), defaultTimeout, true);
		}

	}

	@Override
	public void write(Datapoint datapoint, String agentName) throws Exception {
		this.write(Arrays.asList(datapoint), agentName, defaultTimeout, true);
	}

	@Override
	public List<Datapoint> subscribe(List<String> datapointaddress, String agentName) throws Exception {
		final List<Datapoint> result = new ArrayList<>();

		List<Datapoint> inputList = new ArrayList<>();

		try {
			datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			result.addAll(this.execute(agentName, SUBSCRIBESERVICENAME, inputList, this.defaultTimeout, true));

			if (result.isEmpty() == true || result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot subscribe values. Error returned from destination");
			}

			//Register in function handler

			//Add subscription
			//FIXME: Not clean solution
			//this.cellFunctions.addSubscription(functionName, address);

		} catch (Exception e) {
			log.error("Cannot subscribe value for addresses={} of agent={}", datapointaddress, agentName, e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	public Datapoint subscribe(String datapointName, String agentName) throws Exception {
		Datapoint result = null;
		List<Datapoint> datapoints = this.subscribe(Arrays.asList(datapointName), agentName);
		if (datapoints.isEmpty() == false) {
			result = datapoints.get(0);
		} else {
			throw new Exception("Datapoint " + datapointName + " could not be subscribed from agent " + agentName);
		}

		return result;
	}

	@Override
	public void unsubscribe(List<String> datapointaddress, String agentName) throws Exception {

		List<Datapoint> result;

		try {
			List<Datapoint> inputList = new ArrayList<>();
			datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			result = this.execute(agentName, UNSUBSCRIBESERVICENAME, inputList, this.defaultTimeout);

			if (result.isEmpty() == false && result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot unsubscribe values. Error returned from destination");
			}
		} catch (Exception e) {
			log.error("Cannot unsubscribe value for addresses={} of agent={}", datapointaddress, agentName);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void unsubscribe(String datapointName, String name) throws Exception {
		this.unsubscribe(Arrays.asList(datapointName), name);

	}

	@Override
	public void notifySubscriber(Datapoint datapoint, String agentName) throws Exception {
		//Datapoint result;

		try {
			this.execute(agentName, NOTIFYSERVICENAME, Arrays.asList(datapoint), this.defaultTimeout);

			//			if (result.isEmpty() == false && result.get(0).getValueAsString().equals(CommVocabulary.ERROR)) {
			//				throw new Exception("Cannot write values. Error returned from destination");
			//			}
		} catch (Exception e) {
			log.error("Cannot write value for addresses={} of agent={}", datapoint, agentName);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public Datapoint subscribeDatapoint(String agentId, String datapointAddress, CellFunction callingCellfunctionName) throws Exception {
		String id = "subsciption" + System.currentTimeMillis();
		this.cellFunctionHandler.addSubscription(callingCellfunctionName, DatapointConfig.newConfig(id, datapointAddress, agentId, SyncMode.SUBSCRIBEONLY));
		return null;
	}

	@Override
	public Datapoint unsubscribeDatapoint(String agentid, String address, CellFunction callingCellFunctionName) throws Exception {

		this.cellFunctionHandler.removeSubscription(callingCellFunctionName, address, agentid);

		return null;
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String resultAddress, int timeout) throws Exception {
		return this.queryDatapoints(writeAddress, content, this.getLocalAgentName(), resultAddress, this.getLocalAgentName(), timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String resultAddress, int timeout) throws Exception {
		return this.queryDatapoints(writeAddress, new JsonPrimitive(content), resultAddress, timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception {
		return this.queryDatapoints(writeAddress, new JsonPrimitive(content), writeAgentName, resultAddress, resultAgentName, timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception {
		//TemporarySubscription subscription = null;
		Datapoint result = null;

		try {
			result = CFQuery.newQuery(writeAgentName, writeAddress, content, resultAgentName, resultAddress, timeout, this.getCell());
		} catch (Exception e) {
			log.error("Cannot execute query", e);
		}

		return result;
	}

	@Override
	public Datapoint executeServiceQueryDatapoints(String writeAgentName, String serviceName, List<Datapoint> serviceParameter, String resultAgentName, String resultAddress, int timeout) throws Exception {
		Datapoint result = null;

		try {
			result = CFSubscribeLock.newServiceExecutionAndSubscribeLock(writeAgentName, serviceName, serviceParameter, resultAgentName, resultAddress, timeout, this.getCell());
		} catch (Exception e) {
			log.error("Cannot execute query", e);
		}

		return result;
	}

}
