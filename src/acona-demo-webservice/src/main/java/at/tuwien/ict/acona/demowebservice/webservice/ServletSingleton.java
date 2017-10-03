package at.tuwien.ict.acona.demowebservice.webservice;

public class ServletSingleton {
	private static JerseyRestServer function = null;
	
	public static void setFunction(JerseyRestServer function) {
		ServletSingleton.function = function;
	}
	
	public static JerseyRestServer getFunction() {
		return function;
	}
}
