package at.tuwien.ict.acona.demowebservice.webservice;

import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;

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
		    ServletSingleton.setFunction(this);
		    
		    jettyServer.start();
		    //jettyServer.join();
		    
	     } catch (Exception e) {
				log.error("Cannot start jersy server", e);
				throw new Exception(e.getMessage());
	     } finally {
	        jettyServer.destroy();
	     }
	}
	
	protected MqttCommunicator getCommunicatorFromFunction() {
		return this.getCommunicator();
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateDatapointsById(String id, String address, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
