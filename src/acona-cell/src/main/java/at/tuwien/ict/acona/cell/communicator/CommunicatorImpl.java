package at.tuwien.ict.acona.cell.communicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFQuery;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFSubscribeLock;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CommunicatorImpl extends AgentCommunicatorImpl implements BasicServiceCommunicator {

	protected static Logger log = LoggerFactory.getLogger(CommunicatorImpl.class);

	private static final String READSERVICENAME = "read";
	private static final String WRITESERVICENAME = "write";
	private static final String SUBSCRIBESERVICENAME = "subscribe";
	private static final String UNSUBSCRIBESERVICENAME = "unsubscribe";
	private static final String NOTIFYSERVICENAME = "notify";
	private static final String REMOVESERVICENAME = "remove";

	public CommunicatorImpl(CellImpl cell) {
		super(cell);
	}

	@Override
	public List<Datapoint> read(List<String> datapoints) throws Exception {
		return read(this.getLocalAgentName(), datapoints, this.getDefaultTimeout());
	}

	@Override
	public List<Datapoint> read(String agentName, List<String> datapointaddress, int timeout) throws Exception {
		//TODO: Change the syntax that the agentname is part of the read address with the format agent:datapoint. If no agentname is present, the current agentname is the default agent name.
		final List<Datapoint> result = new ArrayList<>();

		//The agent name in the parameter decides where to read 

		try {
			//Make the inputlist a parameter
			//Create the request

			JsonRpcRequest request = new JsonRpcRequest(READSERVICENAME, false, new Object[1]);
			request.setParameterAsList(0, datapointaddress);

			//datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			JsonRpcResponse response = this.execute(agentName, READSERVICENAME, request, timeout);

			if (response.getError() != null) {
				throw new Exception("Cannot read values. Error returned from destination. Error:" + response.getError());
			}

			//Get the result and convert it to datapoints
			//response.getResultAsList(new TypeToken<List<Datapoint>>(){}.getType());
			List<Datapoint> responseList = response.getResult(new TypeToken<List<Datapoint>>() {
			});
			result.addAll(responseList);

		} catch (Exception e) {
			log.error("Cannot read value for addresses={} of agent={}", datapointaddress, agentName, e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	public List<Datapoint> readWildcard(String datapointName) throws Exception {
		Datapoint dp = Datapoints.newDatapoint(datapointName);

		//String agentName = this.getLocalAgentName();
		//String address = datapointName;

		//String[] completeAddress = datapointName.split(":");

		//if (completeAddress.length == 2) {
		//	agentName = completeAddress[0];
		//	address = completeAddress[1];
		//}

		List<Datapoint> list = read(dp.getAgent(this.getLocalAgentName()), Arrays.asList(dp.getAddress()), this.getDefaultTimeout());

		return list;
	}

	@Override
	public Datapoint read(String agentName, String datapoint) throws Exception {
		return read(agentName, datapoint, this.getDefaultTimeout());
	}

	@Override
	public Datapoint read(String agentName, String datapoint, int timeout) throws Exception {
		Datapoint dp = Datapoints.newDatapoint(datapoint);

		List<Datapoint> list = read(agentName, Arrays.asList(dp.getAddress()), timeout);

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

		Datapoint dp = Datapoints.newDatapoint(datapointName);
		result = this.read(dp.getAgent(this.getLocalAgentName()), dp.getAddress());

		//		//If the datapoint has the following addressformat: [Agent]:[Address], then replace the address and read from an agent
		//		if (datapointName.contains(":") == true) {
		//			result = this.read(datapointName.split(":")[1], datapointName.split(":")[0]);
		//		} else {
		//			result = this.read(datapointName, this.getLocalAgentName());
		//		}
		return result;
	}

	@Override
	public void remove(String datapoint) throws Exception {
		this.remove(Arrays.asList(datapoint));

	}

	@Override
	public void remove(List<String> datapoints) throws Exception {
		this.remove(this.getLocalAgentName(), datapoints, this.getDefaultTimeout());

	}

	@Override
	public void remove(String agentName, List<String> datapoints, int timeout) throws Exception {
		//JsonRpcResponse result;

		try {
			//Make the inputlist a parameter
			//Create the request
			JsonRpcRequest request = new JsonRpcRequest(REMOVESERVICENAME, 1);
			request.setParameterAsList(0, datapoints);

			JsonRpcResponse response = this.execute(agentName, REMOVESERVICENAME, request, timeout);

			if (response.getError() != null) {
				throw new Exception("Cannot remove values. Error returned from destination. Error:" + response.getError());
			}

		} catch (Exception e) {
			log.error("Cannot remove value for addresses={} of agent={}", datapoints, agentName);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void write(List<Datapoint> datapoints) throws Exception {
		this.write(this.getLocalAgentName(), datapoints, this.getDefaultTimeout(), true);
	}

	@Override
	public void write(String agentComplementedName, List<Datapoint> datapoints, int timeout, boolean blocking) throws Exception {
		try {
			JsonRpcRequest request = new JsonRpcRequest(WRITESERVICENAME, false, new Object[1]);
			request.setParameterAsList(0, datapoints);

			//log.warn("Execute service={}, agent={}, request={}", WRITESERVICENAME, agentComplementedName, request);
			JsonRpcResponse result = this.execute(agentComplementedName, WRITESERVICENAME, request, timeout);

			if (result.getError() != null) {
				throw new Exception("Cannot write values. Error returned from destination. Error:" + result.getError());
			}
		} catch (Exception e) {
			log.error("Cannot write value for addresses={} of agent={}", datapoints, agentComplementedName);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	public void write(Datapoint datapoint) throws Exception {
		//If the datapoint has the following addressformat: [Agent]:[Address], then replace the address and write to the agent

		this.write(datapoint.getAgent(this.getLocalAgentName()), datapoint);

		//		if (datapoint.getAddress().contains(":") == true) {
		//			String agent = datapoint.getAddress().split(":")[0];
		//			String address = datapoint.getAddress().split(":")[1];
		//			Datapoint writeDatapoint = Datapoints.newDatapoint(address).setValue(datapoint.getValue());
		//
		//			this.write(agent, Arrays.asList(writeDatapoint), defaultTimeout, true);
		//		} else {
		//			this.write(this.getLocalAgentName(), Arrays.asList(datapoint), defaultTimeout, true);
		//		}
	}

	@Override
	public void write(String agentName, Datapoint datapoint) throws Exception {
		this.write(agentName, Arrays.asList(datapoint), this.getDefaultTimeout(), true);
	}

	@Override
	public List<Datapoint> subscribe(String agentName, List<String> datapointaddress) throws Exception {
		final List<Datapoint> result = new ArrayList<>();

		//List<Datapoint> inputList = new ArrayList<>();

		try {
			JsonRpcRequest request = new JsonRpcRequest(SUBSCRIBESERVICENAME, false, new Object[1]);
			request.setParameterAsList(0, datapointaddress);

			JsonRpcResponse response = this.execute(agentName, SUBSCRIBESERVICENAME, request, this.getDefaultTimeout(), true);

			if (response.getError() != null) {
				throw new Exception("Cannot subscribe values. Error returned from destination. Error: " + response.getError());
			}

			//Get the result and convert it to datapoints
			result.addAll(response.getResult(new TypeToken<List<Datapoint>>() {
			}));

		} catch (Exception e) {
			log.error("Cannot subscribe value for addresses={} of agent={}", datapointaddress, agentName, e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	public Datapoint subscribe(String completeAddress) throws Exception {
		Datapoint result = null;

		Datapoint dp = Datapoints.newDatapoint(completeAddress);
		List<Datapoint> datapoints = this.subscribe(dp.getAgent(this.getLocalAgentName()), Arrays.asList(dp.getAddress()));
		if (datapoints.isEmpty() == false) {
			result = datapoints.get(0);
		} else {
			throw new Exception("Datapoint " + completeAddress);
		}

		return result;
	}

	@Override
	public void unsubscribe(String agentName, List<String> datapointaddress) throws Exception {

		//List<Datapoint> result;

		try {
			//List<Datapoint> inputList = new ArrayList<>();
			//datapointaddress.forEach(s -> inputList.add(Datapoint.newDatapoint(s)));

			JsonRpcRequest request = new JsonRpcRequest(UNSUBSCRIBESERVICENAME, false, new Object[1]);
			request.setParameterAsList(0, datapointaddress);

			JsonRpcResponse result = this.execute(agentName, UNSUBSCRIBESERVICENAME, request, this.getDefaultTimeout());

			if (result.getError() != null) {
				throw new Exception("Cannot unsubscribe values. Error returned from destination. Error: " + result.getError());
			}
		} catch (Exception e) {
			log.error("Cannot unsubscribe value for addresses={} of agent={}", datapointaddress, agentName);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void unsubscribe(String completeAddress) throws Exception {
		Datapoint dp = Datapoints.newDatapoint(completeAddress);
		this.unsubscribe(dp.getAgent(this.getLocalAgentName()), Arrays.asList(dp.getAddress()));

	}

	@Override
	public void notifySubscriber(String agentName, Datapoint datapoint) throws Exception {
		//Datapoint result;

		try {
			JsonRpcRequest request = new JsonRpcRequest(NOTIFYSERVICENAME, true, new Object[1]);
			request.setParameterAsValue(0, datapoint.toJsonObject());

			JsonRpcResponse response = this.execute(agentName, NOTIFYSERVICENAME, request, this.getDefaultTimeout());

			if (response.getError() != null) {
				throw new Exception("Cannot notify values. Error returned from destination. Error: " + response.getError());
			}
		} catch (Exception e) {
			log.error("Cannot write value for addresses={} of agent={}", datapoint, agentName, e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public Datapoint subscribeDatapoint(String key, String callingCellfunctionName) throws Exception {
		//Create subscription from subscription config
		//Subscribe the value
		Datapoint completeKey = Datapoints.newDatapoint(key);
		completeKey.setAgentIfAbsent(this.getLocalAgentName());

		Datapoint result = this.subscribe(completeKey.getCompleteAddress());

		//Add to subscription handler
		this.getSubscriptionHandler().addSubscription(callingCellfunctionName, completeKey.getCompleteAddress());

		//String id = "subscription" + System.currentTimeMillis();
		//this.cellFunctionHandler.addSubscription(callingCellfunctionName, DatapointConfig.newConfig(id, datapointAddress, agentId, SyncMode.SUBSCRIBEONLY));
		return result;
	}

	@Override
	public void unsubscribeDatapoint(String key, String callingCellFunctionName) throws Exception {
		Datapoint completeKey = Datapoints.newDatapoint(key);
		completeKey.setAgentIfAbsent(this.getLocalAgentName());

		this.unsubscribe(completeKey.getCompleteAddress());

		this.getSubscriptionHandler().removeSubscription(callingCellFunctionName, completeKey.getCompleteAddress());
		//this.cellFunctionHandler.removeSubscription(callingCellFunctionName.getFunctionName(), address, agentid);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String resultAddress, JsonElement resultContent, int timeout) throws Exception {
		return this.queryDatapoints(this.getLocalAgentName(), writeAddress, content, this.getLocalAgentName(), resultAddress, resultContent, timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String resultAddress, String resultContent, int timeout) throws Exception {
		return this.queryDatapoints(writeAddress, new JsonPrimitive(content), resultAddress, new JsonPrimitive(resultContent), timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String writeAgentName, String resultAddress, String resultAgentName, JsonElement resultContent, int timeout) throws Exception {
		return this.queryDatapoints(writeAgentName, writeAddress, new JsonPrimitive(content), resultAgentName, resultAddress, resultContent, timeout);
	}

	@Override
	public Datapoint queryDatapoints(String writeAgentName, String writeAddress, JsonElement sendContent, String resultAgentName, String resultAddress, JsonElement resultContent, int timeout) throws Exception {
		//TemporarySubscription subscription = null;
		Datapoint result = null;

		try {
			//The write command always need a list of datapoints.

			CFQuery query = new CFQuery();
			result = query.newQuery(writeAgentName, writeAddress, sendContent, resultAgentName, resultAddress, resultContent, timeout, this.getCell());
		} catch (Exception e) {
			log.error("Cannot execute query", e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	public Datapoint executeServiceQueryDatapoints(String writeAgentName, String serviceName, JsonRpcRequest serviceParameter, String resultAgentName, String resultAddress, JsonElement expectedResult, int timeout) throws Exception {
		Datapoint result = null;

		try {
			CFSubscribeLock lock = new CFSubscribeLock();
			result = lock.newServiceExecutionAndSubscribeLock(writeAgentName, serviceName, serviceParameter, resultAgentName, resultAddress, expectedResult, timeout, this.getCell());
		} catch (Exception e) {
			log.error("Cannot execute query", e);
		}

		return result;
	}

}
