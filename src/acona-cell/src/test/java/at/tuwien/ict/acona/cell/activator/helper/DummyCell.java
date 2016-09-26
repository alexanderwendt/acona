package at.tuwien.ict.acona.cell.activator.helper;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionHandler;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionHandlerImpl;
import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.core.CellInitialization;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import at.tuwien.ict.acona.cell.storage.DataStorageImpl;
import at.tuwien.ict.acona.cell.storage.helpers.DataStorageSubscriberNotificatorMock;
import jade.core.behaviours.Behaviour;

public class DummyCell implements CellInitialization {
	
	//Genotype configuration
	protected CellConfig conf;
	
	//phenotype functions
	//private final Map<String, Condition> conditionMap = new HashMap<String, Condition>();
	//private final Map<String, CellFunction> cellFunctionBehaviourMap = new HashMap<String, CellFunction>();
	//private final Map<String, Activator> activatorMap = new HashMap<String, Activator>();

	private DataStorage data = new DataStorageImpl().init(new DataStorageSubscriberNotificatorMock());
	private Communicator comm = new CommunicatorMock(this);
	private CellFunctionHandler activationHandler = new CellFunctionHandlerImpl();
	
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
		// TODO Auto-generated method stub
		return null;
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

//	@Override
//	public void setConditionMap(Map<String, Condition> conditionMap) {
//		this.conditionMap.putAll(conditionMap);
//	}
//	
//	@Override
//	public void setCellFunctionMap(Map<String, CellFunction> cellFunctionBehaviourMap) {
//		this.cellFunctionBehaviourMap.putAll(cellFunctionBehaviourMap);
//		
//	}
//
//	@Override
//	public void setActivatorMap(Map<String, Activator> activatorMap) {
//		this.activatorMap.putAll(activatorMap);
//	}
//	
//	@Override
//	public Map<String, Condition> getConditionMap() {
//		return this.conditionMap;
//	}
//
//	@Override
//	public Map<String, CellFunction> getCellFunctionMap() {
//		return this.cellFunctionBehaviourMap;
//	}
//
//	@Override
//	public Map<String, Activator> getActivatorMap() {
//		return this.activatorMap;
//	}
	
	@Override
	public JsonObject getConfiguration() {
		//Deepcopy through serialization
		return this.conf.toJsonObject();
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Communicator getCommunicator() {
		return this.comm;
	}

	@Override
	public void setupCellFunctions(CellConfig conf) throws Exception {
		this.conf = conf;
		
	}

}
