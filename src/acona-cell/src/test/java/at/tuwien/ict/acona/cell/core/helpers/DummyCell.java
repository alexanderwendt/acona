package at.tuwien.ict.acona.cell.core.helpers;

import at.tuwien.ict.acona.cell.communicator.BasicServiceCommunicator;
import at.tuwien.ict.acona.cell.communicator.CellFunctionHandler;
import at.tuwien.ict.acona.cell.communicator.CellFunctionHandlerImpl;
import at.tuwien.ict.acona.cell.communicator.SubscriptionHandler;
import at.tuwien.ict.acona.cell.communicator.SubscriptionHandlerImpl;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import at.tuwien.ict.acona.cell.storage.DataStorageImpl;
import at.tuwien.ict.acona.cell.storage.helpers.DataStorageSubscriberNotificatorMock;
import jade.core.behaviours.Behaviour;

public class DummyCell implements Cell {

	// Genotype configuration
	protected CellConfig conf;

	// phenotype functions
	// private final Map<String, Condition> conditionMap = new HashMap<String,
	// Condition>();
	// private final Map<String, CellFunction> cellFunctionBehaviourMap = new
	// HashMap<String,
	// CellFunction>();
	// private final Map<String, Activator> activatorMap = new HashMap<String,
	// Activator>();

	private DataStorage data = new DataStorageImpl().init(new DataStorageSubscriberNotificatorMock());
	private BasicServiceCommunicator comm = new CommunicatorMock(this);
	private CellFunctionHandler activationHandler = new CellFunctionHandlerImpl();
	private SubscriptionHandler subscriptionHandler = new SubscriptionHandlerImpl();

	public DummyCell(CellConfig conf) throws Exception {
		setupCellFunctions(conf);
		activationHandler.init(this);
		subscriptionHandler.init(activationHandler, getLocalName());
	}

	@Override
	public DataStorage getDataStorage() {
		return data;
	}

	@Override
	public CellFunctionHandler getFunctionHandler() {
		return this.activationHandler;
	}

	@Override
	public String getName() {
		return this.conf.getName();
	}

	@Override
	public void addBehaviour(Behaviour b) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DummyCell [data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}

	// @Override
	// public void setConditionMap(Map<String, Condition> conditionMap) {
	// this.conditionMap.putAll(conditionMap);
	// }
	//
	// @Override
	// public void setCellFunctionMap(Map<String, CellFunction>
	// cellFunctionBehaviourMap) {
	// this.cellFunctionBehaviourMap.putAll(cellFunctionBehaviourMap);
	//
	// }
	//
	// @Override
	// public void setActivatorMap(Map<String, Activator> activatorMap) {
	// this.activatorMap.putAll(activatorMap);
	// }
	//
	// @Override
	// public Map<String, Condition> getConditionMap() {
	// return this.conditionMap;
	// }
	//
	// @Override
	// public Map<String, CellFunction> getCellFunctionMap() {
	// return this.cellFunctionBehaviourMap;
	// }
	//
	// @Override
	// public Map<String, Activator> getActivatorMap() {
	// return this.activatorMap;
	// }

	@Override
	public CellConfig getConfiguration() {
		// Deepcopy through serialization
		return this.conf;
	}

	@Override
	public String getLocalName() {
		return this.conf.getName();
	}

	@Override
	public BasicServiceCommunicator getCommunicator() {
		return this.comm;
	}

	@Override
	public void setupCellFunctions(CellConfig conf) throws Exception {
		this.conf = conf;

	}

	@Override
	public void registerService(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void takeDownCell() {
		// TODO Auto-generated method stub

	}

	@Override
	public SubscriptionHandler getSubscriptionHandler() {
		return subscriptionHandler;
	}

	@Override
	public void addCellFunction(CellFunctionConfig cellFunctionConfig) throws Exception {
		// TODO Auto-generated method stub

	}

}
