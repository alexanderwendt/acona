package at.tuwien.ict.acona.demowebservice.webservice;

import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class JerseyRestServer  extends CellFunctionImpl {
	private final static Logger log = LoggerFactory.getLogger(JerseyRestServer.class);
	
	private final static int PORT = 8001;
	
	@Override
	protected void cellFunctionInit() throws Exception {
		//Setup the server
		
		Server jettyServer = null;
		try {
			//MyApp app = new MyApp(this);
			
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		    context.setContextPath("/");
		    
		    jettyServer = new Server(PORT);
		    jettyServer.setHandler(context);

		    //context.addServlet()
		    
		    ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		    jerseyServlet.setInitOrder(0);
		    jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", JerseyRestService.class.getCanonicalName());

			//ServletContextHandler handler = new ServletContextHandler(jettyServer, "/kore");
			//handler.addServlet(new ServletHolder(new KoreRestService(this)), "/");
		    FuckingSingletonHack.setFunction(this);
		    
		    jettyServer.start();
		    //jettyServer.join();
		    
	     } catch (Exception e) {
				log.error("Cannot start jersy server", e);
				throw new Exception(e.getMessage());
	     } finally {
	        jettyServer.destroy();
	     }
	}



	@Override
	protected void shutDownImplementation() {
		
		
	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
	}
	
	protected Communicator getCommunicatorFromFunction() {
		return this.getCommunicator();
	}



	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

}
