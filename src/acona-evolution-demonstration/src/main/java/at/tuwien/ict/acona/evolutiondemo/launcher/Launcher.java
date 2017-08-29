package at.tuwien.ict.acona.evolutiondemo.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.PermanentBuySellIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

/**
 * This class manages the launching of the whole cognitive system
 * 
 * @author wendt
 *
 */
public class Launcher {
	
	private final static Logger log = LoggerFactory.getLogger(Launcher.class);
	
	private static Launcher launcher;
	
	private KoreExternalControllerImpl controller = KoreExternalControllerImpl.getLauncher();

	public static void main(String[] args) {
		log.info("Welcome to the ACONA Stock Market Evolution Demonstrator");
		
		launcher = new Launcher();
		try {
			launcher.init();
		} catch (Exception e) {
			log.error("System initialization failed. Quit", e);
			System.exit(-1);
		}

	}
	
	private void init() throws Exception {
		try {
			//Start JADE
			log.info("Start JADE");
			this.startJade();
			
			String controllerAgentName = "controlleragent";
			
			String stockmarketagentName = "stockmarketagent";
			String stockmarketPriceGeneratorServiceName = "market";
			String brokerAgentName = "Broker";
			String brokerServiceName = "BrokerService";
			String stockName = "Fingerprint";
			String traderAgentName = "Trader1";
			String traderFunction = "TraderFunction";
			
			//http-server
			//Command in the web service: http://localhost:8001/korecogsys/getsatisfactoryruleset?simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
			//Command to get result: http://localhost:8001/korecogsys/readworkingmemoryresult
			//Command to get state: http://localhost:8001/korecogsys/readstate
			
			//Rest server
			//http://128.131.80.12:8001/korecogsys/readstate
			//http://128.131.80.12:8001/korecogsys/readworkingmemoryresult
			//http://localhost:8001/kore/getsatisfactoryruleset?simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
			
			
			//Generate the configuration for the KORE system
			log.info("Generate system configuration");
			log.info("Generate stock market agent");
			CellGatewayImpl stockmarket = this.controller.createAgent(CellConfig.newConfig(stockmarketagentName)
					.addCellfunction(CellFunctionConfig.newConfig(stockmarketPriceGeneratorServiceName, DummyPriceGenerator.class)));
			
			CellGatewayImpl brokerAgent = this.controller.createAgent(CellConfig.newConfig(brokerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName)));
			
			log.info("Generate Controller agent configuration");
			CellGatewayImpl controller = this.controller.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig("userconsole", ConsoleRequestReceiver.class)
							.setProperty(ConsoleRequestReceiver.ATTRIBUTESTOCKMARKETADDRESS, stockmarketagentName + ":" + stockmarketPriceGeneratorServiceName)));
			
			CellGatewayImpl traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(traderFunction, Trader.class)
							.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketagentName + ":" + "data")
							.setProperty(Trader.ATTRIBUTESIGNALADDRESS, "Indicator"))
					.addCellfunction(CellFunctionConfig.newConfig("Indicator", PermanentBuySellIndicator.class)));
			
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					
				}
			}
			
			//controller.getCommunicator().write(Datapoints.newDatapoint(stockmarketagentName + ":" + stockmarketPriceGeneratorServiceName + "." + "command").setValue(ControlCommand.START));
			
			
			log.info("Price generated");
			// Controller
			// Controller
//			CellConfig fileloaderAgentConfig = CellConfig.newConfig(cognsysagentname)
//					//Add the jetty server
//					.addCellfunction(CellFunctionConfig.newConfig(httpServerName, JerseyRestServer.class)
//							.setProperty(JettyHttpServer.attributeCommandAddress, userRequestAddress)
//							.setProperty(JettyHttpServer.attributeResultAddress, resultAddress)
//							.setProperty(JettyHttpServer.attributeSystemStateAddress, requestCodeletHandlerName + ".state")
//							.setProperty(JettyHttpServer.attributeEpisodeLoaderStateAddress, loaderCodeletName + ".state")
//							.setProperty(JettyHttpServer.attributerulegeneratorStateAddress, ruleGenerationCodeletName + ".state"))
//					//Add a request handler that starts the codelet handler
//					.addCellfunction(CellFunctionConfig.newConfig(requestHandlerName, RequestHandler.class)
//							.setProperty("commandaddress", userRequestAddress)
//							.setProperty("resultaddress", resultAddress)
//							.setProperty(RequestHandler.codeletHandlerServiceUriName, cognsysagentname + ":" + requestCodeletHandlerName))
//					//Add a codelet handler for the use request
//					.addCellfunction(CellFunctionConfig.newConfig(requestCodeletHandlerName, CellFunctionCodeletHandler.class))
//					.addCellfunction(CellFunctionConfig.newConfig(loaderCodeletName, EpisodeLoaderAsCodelet.class)
//							.setProperty(EpisodeLoaderAsCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognsysagentname + ":" +requestCodeletHandlerName)
//							.setProperty(EpisodeLoaderAsCodelet.ATTRIBUTEEXECUTIONORDER, "1")
//							.setProperty(EpisodeLoaderAsCodelet.REQUESTADDRESSNAME, userRequestAddress)
//							.setProperty(EpisodeLoaderAsCodelet.SYSTEMSTATEADDRESSNAME, systemStateAddress)
//							.setProperty(EpisodeLoaderAsCodelet.LOADEDEPIODESADDRESSNAME, loadedEpisodesAddress))
//					.addCellfunction(CellFunctionConfig.newConfig(ruleGenerationCodeletName, RuleStructureGeneratorDriverMock.class)
//							.setProperty(RuleStructureGeneratorDriverMock.ATTRIBUTECODELETHANDLERADDRESS, cognsysagentname + ":" + requestCodeletHandlerName)
//							.setProperty(RuleStructureGeneratorDriverMock.ATTRIBUTEEXECUTIONORDER, "2")
//							.setProperty(RuleStructureGeneratorDriverMock.ATTRIBUTEREQUESTADDRESS, userRequestAddress)
//							.setProperty(RuleStructureGeneratorDriverMock.ATTRIBUTERESULTADDRESS, resultAddress));
								
						
						//log.debug("Start agent with config={}", fileloaderAgentConfig);
						//CellGatewayImpl cogsys = this.launcher.createAgent(fileloaderAgentConfig);			
			
			
//			CellConfig fileloaderAgentConfig = CellConfig.newConfig("kore")
//					.addCellfunction(CellFunctionConfig.newConfig("externalinterface", JettyHttpServer.class)
//							.setProperty("commandAddress", "requestinterface.command")
//							.setProperty("resultAddress", "requestinterface.result"))
//					.addCellfunction(CellFunctionConfig.newConfig("requesthandler", RequestHandler.class)
//							.setProperty("commandaddress", "requestinterface.command")
//							.setProperty("resultaddress", "requestinterface.result"));
						
//			log.debug("Start agent with config={}", fileloaderAgentConfig);
//			
//			
//			CellGatewayImpl cogsys = this.controller.createAgent(fileloaderAgentConfig);
			
			
		} catch (Exception e) {
			log.error("Cannot initialize the system", e);
			throw new Exception(e.getMessage());
		}
		
	}
	
	private void startJade() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.controller.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.controller.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	private void stopJade() throws Exception {
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {

			}
		}

		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}
	}

}
