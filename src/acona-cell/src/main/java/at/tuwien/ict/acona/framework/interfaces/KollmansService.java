package at.tuwien.ict.acona.framework.interfaces;

import at.tuwien.ict.acona.cell.cellfunction.AconaOndemandFunctionService;

public class KollmansService extends AconaOndemandFunctionService {

	
	private final static String COUNTER = "counter";	//=kollmannsservice.variables.counter.value
	
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

}
