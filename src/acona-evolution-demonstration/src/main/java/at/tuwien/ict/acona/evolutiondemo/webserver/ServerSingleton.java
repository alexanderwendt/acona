package at.tuwien.ict.acona.evolutiondemo.webserver;

public class ServerSingleton {
	private static JerseyRestServer function = null;
	
	public static void setFunction(JerseyRestServer function) {
		ServerSingleton.function = function;
	}
	
	public static JerseyRestServer getFunction() {
		return function;
	}
}
