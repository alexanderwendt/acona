package at.tuwien.ict.acona.cell.cellfunction;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.domain.FIPANames;

public abstract class CellFunctionImpl implements CellFunction {

	private static Logger log = LoggerFactory.getLogger(CellFunctionImpl.class);
	//protected static final String SYNCMODEPUSH = "push";
	//protected static final String SYNCMODEPULL = "pull";

	/**
	 * Cell, which executes this function
	 */
	private Cell cell;
	private CellFunctionConfig config;

	/**
	 * Name of the activator
	 */
	private String name;

	/**
	 * List of datapoints that shall be subscribed
	 */
	private final Map<String, DatapointConfig> subscriptions = new HashMap<>(); // Variable, datapoint
	private final Map<String, DatapointConfig> readDatapoints = new HashMap<>(); // Variable, datapoint
	private final Map<String, DatapointConfig> syncDatapoints = new HashMap<>();
	private final Map<String, DatapointConfig> writeDatapoints = new HashMap<>();

	private ServiceState currentServiceState = ServiceState.INITIALIZING;

	@Override
	public CellFunction init(CellFunctionConfig config, Cell caller) throws Exception {
		try {
			this.setServiceState(ServiceState.INITIALIZING);
			// === Extract fundamental settings ===//
			// Extract settings
			this.config = config;
			this.cell = caller;

			// Get the settings but set also default values

			// Get name
			this.name = this.config.getName();

			// === Internal init ===//

			// Possibility to add more subscriptions and to overwrite default
			// settings
			cellFunctionInit();

			// === Extract execution settings ===//

			// Get responder if it exists
			if (this.getFunctionConfig().getGenerateReponder() == null) {
				this.config.setGenerateReponder(false);
			}

			if (this.getFunctionConfig().getResponderProtocol().equals("") == true) {
				// Set default responderprotocol
				this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			}

			// Get subscriptions from config and add to subscription list
			this.config.getSyncDatapoints().forEach(s -> {
				if (s.getSyncMode().equals(SyncMode.push)) {
					this.subscriptions.put(s.getId(), s);
				} else if (s.getSyncMode().equals(SyncMode.pushreturn)) {
					this.subscriptions.put(s.getId(), s);
					this.writeDatapoints.put(s.getId(), s);
				} else if (s.getSyncMode().equals(SyncMode.pull)) {
					this.readDatapoints.put(s.getId(), s);
				} else if (s.getSyncMode().equals(SyncMode.pullreturn)) {
					this.readDatapoints.put(s.getId(), s);
					this.writeDatapoints.put(s.getId(), s);
				} else {
					try {
						throw new Exception("No syncmode=" + s.getSyncMode() + ". only pull and push available");
					} catch (Exception e) {
						log.error("Cannot set sync mode", e);
					}
				}

				syncDatapoints.put(s.getId(), s);

			});

			// === Register in the function handler ===//
			this.cell.getFunctionHandler().registerCellFunctionInstance(this);
		} catch (Exception e) {
			log.error("Cannot init function with config={}", config);
			throw new Exception(e.getMessage());
		}

		this.setServiceState(ServiceState.IDLE);

		log.debug("Function={} initialized. Sync datapoints={}", this.getFunctionName(), this.syncDatapoints);

		return this;
	}

	protected abstract void cellFunctionInit() throws Exception;

	@Override
	public void shutDown() {
		// Unsubscribe all datapoints
		// this.getCell().getFunctionHandler().deregisterActivatorInstance(this);

		// Execute specific functions
		this.shutDownImplementation();

		//Execute general deregister
		this.getCell().getFunctionHandler().deregisterActivatorInstance(this);
	}

	protected abstract void shutDownImplementation();

	@Override
	public void updateSubscribedData(Map<String, Datapoint> data, String caller) {
		// Create datapointmapping ID to datapoint with new value
		Map<String, Datapoint> subscriptions = new HashMap<>();
		this.getSubscribedDatapoints().forEach((k, v) -> {
			if (data.containsKey(v.getAddress())) {
				subscriptions.put(k, data.get(v.getAddress()));
			}
		});

		this.updateDatapointsById(subscriptions);
	}

	protected abstract void updateDatapointsById(Map<String, Datapoint> data);

	@Override
	public String getFunctionName() {
		return this.name;
	}

	@Override
	public Map<String, DatapointConfig> getSubscribedDatapoints() { // ID config
		return subscriptions;
	}

	@Override
	public CellFunctionConfig getFunctionConfig() {
		return this.config;
	}

	private void setFunctionConfig(JsonObject config) throws Exception {
		// Set new config
		this.config = CellFunctionConfig.newConfig(config);

		// Restart system
		this.shutDown();

		this.init(this.config, this.cell);
	}

	// === read and write shortcuts ===//

	protected Communicator getCommunicator() {
		return this.getCell().getCommunicator();
	}

	protected void writeLocal(Datapoint datapoint) throws Exception {
		this.getCommunicator().write(datapoint);
	}

	protected <DATATYPE> void writeLocal(String address, DATATYPE datapoint) throws Exception {
		Gson gson = new Gson();
		String value = gson.toJson(datapoint);
		this.getCommunicator().write(Datapoint.newDatapoint(address).setValue(value));
	}

	protected Datapoint readLocal(String address) throws Exception {
		return this.getCommunicator().read(address);
	}

	protected JsonElement readLocalAsJson(String address) throws Exception {
		return this.getCommunicator().read(address).getValue();
	}

	protected <T> T readLocalById(String id, Class<T> type) throws Exception {
		Gson gson = new Gson();
		JsonElement value = this.readLocal(this.getFunctionConfig().getSyncDatapointsAsMap().get(id).getAddress())
				.getValue();
		T convertedValue = gson.fromJson(value, type);

		return convertedValue;
	}

	protected Datapoint readLocalId(String id) throws Exception {
		return this.readLocal(this.getSyncDatapoints().get(id).getAddress());
	}

	protected <T> void writeLocalSyncDatapointById(String id, T value) throws Exception {
		// Gson gson = new Gson();

		JsonElement writeValue = new Gson().toJsonTree(value);
		this.writeLocal(Datapoint.newDatapoint(this.getFunctionConfig().getSyncDatapointsAsMap().get(id).getAddress())
				.setValue(writeValue));
	}

	protected <T> T getCustomSetting(String key, Class<T> type) {
		return this.getFunctionConfig().getProperty(name, type);
	}

	protected Cell getCell() {
		return cell;
	}

	/**
	 * Return the subscribed datapoint based on its ID in the function
	 * 
	 * @param data:
	 *            inputmap from subscribed data
	 * @param id:
	 *            datapoint id defined in the config or in the code
	 * @return
	 */
	protected Datapoint getDatapointFromId(Map<String, Datapoint> data, String id) {
		return data.get(this.getSyncDatapoints().get(id).getAddress());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CellFunctionImpl [name=");
		builder.append(name);
		builder.append(", subscriptions=");
		builder.append(subscriptions);
		builder.append("]");
		return builder.toString();
	}

	protected Map<String, DatapointConfig> getSyncDatapoints() {
		return syncDatapoints;
	}

	protected Map<String, DatapointConfig> getReadDatapoints() {
		return readDatapoints;
	}

	protected Map<String, DatapointConfig> getWriteDatapoints() {
		return writeDatapoints;
	}

	@Override
	public ServiceState getCurrentState() {
		return null;

	}

	protected void setServiceState(ServiceState serviceState) {
		this.currentServiceState = serviceState;
	}

}
