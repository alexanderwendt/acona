package at.tuwien.ict.acona.koreuserinterface;

import java.text.DecimalFormat;
import java.util.Map;

import org.junit.experimental.theories.DataPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class KoreDataStructureGeneratorMock extends CellFunctionThreadImpl {

	private final static Logger log = LoggerFactory.getLogger(KoreDataStructureGeneratorMock.class);
	private DecimalFormat df = new DecimalFormat("#.#");
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		log.info("KORE data structure generator. The generator is triggered by any incoming datapoint");

		
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void executeFunction() throws Exception {
		//Generate KORE data structures
		
		Gson gson = new Gson();
		String kore = 
			"{\"Requests\":[{\"simulationid\":\"Simulation1\",\"usecaseid\":\"Ontology1\",\"breakevaluationco2\":1,\"breakevaluationenergy\":1,\"breakevaluationpenalty\":0.2,\"breaksimulatorrun\":4000,\"name\":\"getsatisfactoryruleset\",\"type\":\"request\"}],\"Episodes\":[{\"address\":\"uri://ontology.namespace.anything.1\",\"simulationId\":\"Simulation1\",\"usecaseId\":\"Ontology1\",\"ruleStrategy\":\"rulestructure1\",\"evaluation\":{\"co2Evaluation\":0.2,\"energyEvaluation\":0.95,\"penaltyEvaluation\":0.5,\"name\":\"Episode3Evaluation\",\"type\":\"EpisodeEvaluation\"},\"name\":\"Episode3\",\"type\":\"Episode\"},{\"address\":\"uri://ontology.namespace.anything.1\",\"simulationId\":\"Simulation1\",\"usecaseId\":\"Ontology1\",\"ruleStrategy\":\"rulestructure1\",\"evaluation\":{\"co2Evaluation\":0.7,\"energyEvaluation\":0.6,\"penaltyEvaluation\":0.8,\"name\":\"Episode2Evaluation\",\"type\":\"EpisodeEvaluation\"},\"name\":\"Episode2\",\"type\":\"Episode\"},{\"address\":\"uri://ontology.namespace.anything.1\",\"simulationId\":\"Simulation1\",\"usecaseId\":\"Ontology1\",\"ruleStrategy\":\"rulestructure1\",\"parameterSet\":\"param3\",\"evaluation\":{\"co2Evaluation\":0.4,\"energyEvaluation\":0.6,\"penaltyEvaluation\":0.2,\"name\":\"Episode1Evaluation\",\"type\":\"EpisodeEvaluation\"},\"name\":\"Episode1\",\"type\":\"Episode\"}],\"Goals\":[{\"originAddress\":\"workingmemory.request.requestOptimize\",\"condition\":[{\"operatorA\":\"Ontology1\",\"operand\":\"Stringequal\",\"name\":\"usecaseid\",\"type\":\"Condition\"},{\"operatorA\":\"Simulation1\",\"operand\":\"Stringequal\",\"name\":\"simulationid\",\"type\":\"Condition\"},{\"operatorA\":\"4000\",\"operand\":\">\",\"name\":\"breaksimulatorrun\",\"type\":\"Condition\"},{\"operatorA\":\"1.0\",\"operand\":\">\",\"name\":\"breakevaluationco2\",\"type\":\"Condition\"},{\"operatorA\":\"1.0\",\"operand\":\">\",\"name\":\"breakevaluationenergy\",\"type\":\"Condition\"},{\"operatorA\":\"0.2\",\"operand\":\">\",\"name\":\"breakevaluationpenalty\",\"type\":\"Condition\"}],\"hasImportance\":1,\"postState\":\"RETURNEDTOUSER\",\"preState\":\"REQUESTAVAILABLE\",\"currentState\":\"REQUESTAVAILABLE\",\"requireState\":\"RETURNEDTOUSER\",\"name\":\"getsatisfactoryruleset\",\"type\":\"Goal\"}],\"Options\":[{\"originAddress\":\"workingmemory.episode.Episode3\",\"goalAddresses\":\"internalstatememory.goal.getsatisfactoryruleset\",\"evaluation\":{\"evaluation\":0.44999999999999996,\"evaluationHistory\":\"breakevaluationco2:-0.8, breakevaluationenergy:-0.050000000000000044, breakevaluationpenalty:1.3, breaksimulatorrun:0.0\",\"name\":\"Episode3OPTIONEvaluation\",\"type\":\"OptionEvaluation\"},\"actionServiceName\":\"actionReturnToUser\",\"actionParameter\":[\"workingmemory.episode.Episode3\",\"workingmemory.request\"],\"postState\":\"RETURNEDTOUSER\",\"preState\":\"REQUESTAVAILABLE\",\"currentState\":\"EVALUATEDEPISODE\",\"requireState\":\"RETURNEDTOUSER\",\"name\":\"Episode3OPTION\",\"type\":\"Option\"},{\"originAddress\":\"workingmemory.episode.Episode1\",\"goalAddresses\":\"internalstatememory.goal.getsatisfactoryruleset\",\"evaluation\":{\"evaluation\":0,\"evaluationHistory\":\"breakevaluationco2:-0.6, breakevaluationenergy:-0.4, breakevaluationpenalty:1.0, breaksimulatorrun:0.0\",\"name\":\"Episode1OPTIONEvaluation\",\"type\":\"OptionEvaluation\"},\"actionServiceName\":\"actionReturnToUser\",\"actionParameter\":[\"workingmemory.episode.Episode1\",\"workingmemory.request\"],\"postState\":\"RETURNEDTOUSER\",\"preState\":\"REQUESTAVAILABLE\",\"currentState\":\"EVALUATEDEPISODE\",\"requireState\":\"RETURNEDTOUSER\",\"name\":\"Episode1OPTION\",\"type\":\"Option\"},{\"originAddress\":\"workingmemory.episode.Episode2\",\"goalAddresses\":\"internalstatememory.goal.getsatisfactoryruleset\",\"evaluation\":{\"evaluation\":0.9,\"evaluationHistory\":\"breakevaluationco2:-0.30000000000000004, breakevaluationenergy:-0.4, breakevaluationpenalty:1.6, breaksimulatorrun:0.0\",\"name\":\"Episode2OPTIONEvaluation\",\"type\":\"OptionEvaluation\"},\"actionServiceName\":\"actionReturnToUser\",\"actionParameter\":[\"workingmemory.episode.Episode2\",\"workingmemory.request\"],\"postState\":\"RETURNEDTOUSER\",\"preState\":\"REQUESTAVAILABLE\",\"currentState\":\"EVALUATEDEPISODE\",\"requireState\":\"RETURNEDTOUSER\",\"name\":\"Episode2OPTION\",\"type\":\"Option\"},{\"originAddress\":\"internalstatememory.goal.getsatisfactoryruleset\",\"goalAddresses\":\"internalstatememory.goal.getsatisfactoryruleset\",\"evaluation\":{\"evaluation\":2,\"evaluationHistory\":\"getsatisfactoryruleset:2.0, getsatisfactoryruleset:2.0, getsatisfactoryruleset:2.0\",\"name\":\"RuleGenerationOption_getsatisfactoryrulesetEvaluation\",\"type\":\"OptionEvaluation\"},\"actionServiceName\":\"actionEvaluateStrategy\",\"actionParameter\":[],\"postState\":\"RETURNEDTOUSER\",\"preState\":\"REQUESTAVAILABLE\",\"currentState\":\"NEWPARAMETER\",\"requireState\":\"EVALUATEDEPISODE\",\"name\":\"RuleGenerationOption_getsatisfactoryruleset\",\"type\":\"Option\"}],\"SelectedOption\":[{\"originAddress\":\"internalstatememory.goal.getsatisfactoryruleset\",\"goalAddresses\":\"internalstatememory.goal.getsatisfactoryruleset\",\"evaluation\":{\"evaluation\":2,\"evaluationHistory\":\"getsatisfactoryruleset:2.0, getsatisfactoryruleset:2.0, getsatisfactoryruleset:2.0\",\"name\":\"RuleGenerationOption_getsatisfactoryrulesetEvaluation\",\"type\":\"OptionEvaluation\"},\"actionServiceName\":\"actionEvaluateStrategy\",\"actionParameter\":[],\"postState\":\"RETURNEDTOUSER\",\"preState\":\"REQUESTAVAILABLE\",\"currentState\":\"NEWPARAMETER\",\"requireState\":\"EVALUATEDEPISODE\",\"name\":\"RuleGenerationOption_getsatisfactoryruleset\",\"type\":\"Option\"}],\"Simulationcount\":2432,\"GeneratedStrategy\":{\"hasName\":\"Strategy\",\"hasType\":\"STRATEGYWITHPARAMETER\",\"StrategyUri\":\"uri:link/to/rule/generation/namespace712587125\",\"Parameters\":\"a=44;b=445;c=33773;d=7272;PL=2\"},\"SimulationResult\":\"http://uri/for/the/finished/simulator\",\"FinalResult\":{}}";
		
		JsonObject obj = gson.fromJson(kore, JsonObject.class);
		
		log.debug("Generated KORE string={}", obj);
		
		this.getCommunicator().write(DatapointBuilder.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue(obj));
		
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
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		if (this.isSystemDatapoint(data)==false) {
			log.debug("Got start command from={}", data);
			this.setStart();
		}
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
