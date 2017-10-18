package at.tuwien.ict.acona.demowebservice.launcher;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class GraphServer {

	private int runningPort;
	private String outputString = "TEST";
	//private GraphServerConnection connection;
	
	public GraphServer() throws Exception {
		this(8000);
	}
	public GraphServer(final int runningPort) throws Exception {
		if (runningPort <= 0) {
		    throw new IllegalArgumentException("Server port may not be negative or null");
		  }
		this.runningPort = runningPort;
		
		//this.connection = connection;
		//this.connection.put("haha");
		
		Server server = new Server(this.runningPort);
		//ServletHandler handler = new ServletHandler(); 
		//server.setHandler(handler);    
        //handler.addServletWithMapping(HelloServlet.class, "/*");
        outputString="<H1>Initialized Graph Server</H1>";
        server.setHandler(new SimpleHandlerEx());
		
		server.start();
        //server.join();
	}
	public void setString(String newString) {
		outputString = newString;
	}
	public class SimpleHandlerEx extends AbstractHandler{
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {    
		        //response.setContentType("text/html;charset=utf-8");
	        	response.setContentType("application/json;charset=utf-8");
		        response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.setStatus(HttpServletResponse.SC_OK);
		        baseRequest.setHandled(true);
		        PrintWriter writerA = response.getWriter();
		        response.resetBuffer();
		        writerA.println(outputString);
		    }
	}
}


