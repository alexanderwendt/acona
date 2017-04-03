package at.tuwien.ict.acona.framework.interfaces;

import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.cellfunction.OndemandFunctionService;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class KollmansService extends OndemandFunctionService {

	private final static String COUNTER = "counter"; //=kollmannsservice.variables.counter.value

	@Override
	protected void serviceInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeFunction() throws Exception {
		//Config lesen
		String configvalue = this.getCustomSetting("test", String.class);

		//Daten zugreifen
		double value = this.readLocalById(COUNTER, Double.class);

		//Operation execute
		value++;

		this.writeLocalSyncDatapointById(COUNTER, value);

		//Custom settings zugreifen

	}

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void shutDownExecutor() {
		// TODO Auto-generated method stub

	}

}
