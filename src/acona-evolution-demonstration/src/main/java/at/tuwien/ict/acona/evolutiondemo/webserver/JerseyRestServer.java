package at.tuwien.ict.acona.evolutiondemo.webserver;

import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;

/**
 * @author wendt A cell function that starts the jetty server and acts as a gateway for jetty to access the cognitive system
 */
public class JerseyRestServer extends CellFunctionThreadImpl {
	private final static Logger log = LoggerFactory.getLogger(JerseyRestServer.class);

	private final static int PORT = 8001;
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		Server jettyServer = null;
		try {
			// MyApp app = new MyApp(this);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");

			jettyServer = new Server(PORT);
			jettyServer.setHandler(context);

			// context.addServlet()

			ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
			jerseyServlet.setInitOrder(0);
			jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", EvolutionService.class.getCanonicalName());

			// ServletContextHandler handler = new ServletContextHandler(jettyServer, "/kore");
			// handler.addServlet(new ServletHolder(new KoreRestService(this)), "/");
			ServerSingleton.setFunction(this);

			jettyServer.start();
			// jettyServer.join();

			log.info("Started REST server at port={}", PORT);
			log.info("Rest server has been initialized");

		} catch (Exception e) {
			log.error("Cannot start jersy server", e);
			throw new Exception(e.getMessage());
		} finally {
			// jettyServer.destroy();
		}
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeFunction() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
