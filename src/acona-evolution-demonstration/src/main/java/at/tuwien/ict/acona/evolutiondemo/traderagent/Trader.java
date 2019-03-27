package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Depot;
import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunction;
import at.tuwien.ict.acona.mq.core.agentfunction.ControlCommand;
import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletImpl;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class Trader extends CodeletImpl {

	private final static Logger log = LoggerFactory.getLogger(Trader.class);

	public final static String ATTRIBUTESTOCKMARKETADDRESS = "stockmarketaddress";
	public final static String ATTRIBUTEBROKERADDRESS = "brokeraddress";
	public final static String ATTRIBUTECONTROLLERADDRESS = "controlleraddress";
	public final static String ATTRIBUTESIGNALADDRESS = "signaladdress";

	public final static String ATTRIBUTESTARTSIZE = "startsize";
	public final static String ATTRIBUTEAGENTTYPE = "type";
	public final static String ATTRIBUTESTOCKNAME = "stockname";
	
	public final static String ATTRIBUTEMULTIPLY = "multiple";
	public final static String ATTRIBUTEMUTATE = "allowmutations";

	private final static String IDPRICE = "price";
	private final Gson gson = new Gson();

	private String initBrokerAddress = "Broker:BrokerService";
	private String initStockmarketAddress = "Stockmarket:Price";
	private String initSignalAddress = "EmaSignalFunction";
	private String initControllerAddress = "controller";

	private String localDepotAddress = "localdepot";

	private double initStartSize = 10000;
	private String initType = "type1";
	private String initStockName = "Fingerprint";

	// === static values ===//
	private String brokerAddress = "";
	private String stockMarketAddress = "";
	private String signalAddress = "";
	private String controllerAddress = "";

	private String agentType = "";
	private String stockName = "";
	private double startSize = 0;
	private double multiplyLimit = 0;
	private double deathLimit = 0;
	
	private boolean multiply = false;
	private boolean allowMutations = true;

	// Dynamic values
	private Depot depot = null;
	private double highPrice = 0;
	private double lowPrice = 0;
	private double closePrice = 0;
	private boolean buySignal = false;
	private boolean sellSignal = false;
	private boolean killSignal = false;

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		stockMarketAddress = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKMARKETADDRESS, initStockmarketAddress);
		brokerAddress = this.getFunctionConfig().getProperty(ATTRIBUTEBROKERADDRESS, initBrokerAddress);
		signalAddress = this.getFunctionConfig().getProperty(ATTRIBUTESIGNALADDRESS, initSignalAddress);
		controllerAddress = this.getFunctionConfig().getProperty(ATTRIBUTECONTROLLERADDRESS, initControllerAddress);

		this.startSize = Double.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTESTARTSIZE, String.valueOf(initStartSize)));

		multiply = Boolean.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTEMULTIPLY, "false"));
		allowMutations = Boolean.valueOf(this.getFunctionConfig().getProperty(Trader.ATTRIBUTEMUTATE, "true"));
		
		this.multiplyLimit = startSize * 1.3;
		this.deathLimit = startSize * 0.3;

		this.agentType = this.getFunctionConfig().getProperty(ATTRIBUTEAGENTTYPE, initType);
		this.stockName = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKNAME, initStockName);

		// Add subscription to the stock market price
		log.debug("Read from address={}", stockMarketAddress);
		this.addManagedDatapoint(DatapointConfig.newConfig(IDPRICE, this.getDatapointBuilder().newDatapoint(stockMarketAddress).getAgent() + ":" + this.getDatapointBuilder().newDatapoint(stockMarketAddress).getAddress(), SyncMode.READONLY));

		// Create a depot
		this.createDepot();

	}

//	@Override
//	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void executeCodeletPreprocessing() throws Exception {
		// Read depot
		Response response = this.getCommunicator().execute(this.brokerAddress + "/" + "getdepotinfo", (new Request()).setParameter("agentname", this.getAgent().getName()), 200000);
		if (response.hasError()) {
			log.warn("First try failed. Try a second time");
			response = this.getCommunicator().execute(this.brokerAddress + "/" + "getdepotinfo", (new Request()).setParameter("agentname", this.getAgent().getName()), 200000);
		}
		
		
		// Write to broker a new depot
		//JsonRpcRequest req = new JsonRpcRequest("getdepotinfo", 1);
		//req.setParameters(this.getCell().getName());
		
		JsonElement e = response.getResult();
		depot = gson.fromJson(e, Depot.class); //response.getResult();//this.getCommunicator().execute(this.brokerAddress, req).getResult(new TypeToken<Depot>() {});

		// Update prices
		log.debug("Value map={}", this.getValueMap());
		this.closePrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("close").getAsDouble();
		this.highPrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("high").getAsDouble();
		this.lowPrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("low").getAsDouble();
	}

	@Override
	protected void executeFunction() throws Exception {

		log.debug("{}:{}>Start agent calculation", this.getAgentName(), this.agentType);
		// Program logic
		// 2. Check depot death
		this.killSignal = this.killAgentOnDepotDeath();
		if (this.killSignal == false) {
			// 1. Split depot if necessary
			if (this.multiply==true) {
				this.multiplyAgent();
			}
			// 3. Calculate indicator
			// this.calculateIndicator();
			// 4. Calculate signal
			this.calculateSignal();
			// 5. Execute signal
			this.executeTrade();

			log.info("{}:{}>Depot: {}", this.getAgentName(), this.agentType, this.depot);

		} else {
			// If the kill signal has been set, the system shall exit.
			this.setCommand(ControlCommand.EXIT);

			DelayedCellShutDown killSwitch = new DelayedCellShutDown();
			killSwitch.killSwitch(50, this.getAgent());
			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {
					
				}
			}
			log.info("Cell will also be shut down");
		}
	}

	@Override
	public void executeCodeletPostprocessing() throws Exception {
		// Reset signals
		this.buySignal = false;
		this.sellSignal = false;

		// Write the local depot
		JsonElement jsonDepot = gson.toJsonTree(depot);
		this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.localDepotAddress).setValue(jsonDepot));
	}


	@Override
	public void shutDownCodelet() throws Exception {
		// Delete the depot
		try {
			this.deleteDepot();
		} catch (Exception e) {
			log.error("Cannot delete depot. Error:", e);
		}
		

		log.info("{}>Agent is killed", this.getAgent().getName());
		// Then, agent is killed
	}

//	@Override
//	protected void shutDownExecutor() throws Exception {
//		//Delete the depot
//		this.deleteDepot();
//		
//		log.info("{}>Agent is killed", this.getCell().getLocalName());
//		//Then, agent is killed
//		
//	}

	private void createDepot() throws Exception {
		// Write to broker a new depot
//		JsonRpcRequest req = new JsonRpcRequest("registerdepot", 0);
//		req.setParameters(this.getCell().getName(), this.agentType);
//		JsonRpcResponse result1 = this.getCommunicator().execute(this.brokerAddress, req);
		Response result1 = this.getCommunicator().execute(this.brokerAddress + "/" + "registerdepot", (new Request()).setParameter("agentname", this.getAgent().getName()).setParameter("agenttype", this.agentType), 200000);
		
		
		// Add money to broker
		if (result1.hasError() == true) {
			throw new Exception("Cannot create depot. " + result1.getError().getMessage());
		}

		//JsonRpcRequest req2 = new JsonRpcRequest("addmoney", 0);
		//req2.setParameters(this.getCell().getName(), this.startSize);
		//JsonRpcResponse result2 = this.getCommunicator().execute(this.brokerAddress, req2);
		Response result2 = this.getCommunicator().execute(this.brokerAddress + "/" + "addmoney", (new Request()).setParameter("agentname", this.getAgent().getName()).setParameter("amount", this.startSize), 200000);
		
		if (result2.hasError() == true) {
			throw new Exception("Cannot add money= " + this.startSize + " to depot. " + result2.getError().getMessage());
		}

		this.depot = (new Gson()).fromJson(result2.getResult(), Depot.class);
		log.debug("Depot created={}", this.depot);
	}

	private void deleteDepot() throws Exception {
		// Sell everything
		//String agentName = req.getParameter("agentname", String.class); 
		//String stockName = req.getParameter("stockname", String.class);  
		//double price = req.getParameter("price", Double.class);  
		//int volume = req.getParameter("volume", Integer.class); 
		
		
		this.depot.getAssets().forEach(a -> {
//			JsonRpcRequest req = new JsonRpcRequest("sell", 0);
//			req.setParameters(this.getCell().getName(), a.getStockName(), this.closePrice, a.getVolume());
//			JsonRpcResponse result1 = null;
			Response result1;
			
			try {
				result1 = this.getCommunicator().execute(this.brokerAddress + "/" + "sell", (new Request())
						.setParameter("agentname", this.getAgent().getName())
						.setParameter("stockname", a.getStockName())
						.setParameter("price", this.closePrice)
						.setParameter("volume", a.getVolume()), 200000);
				
				
				//result1 = this.getCommunicator().execute(this.brokerAddress, req);
			} catch (Exception e) {
				log.error("Cannot sell stock={} due to error", a.getStockName(), e);
			}
		});

		// Addregister
		//JsonRpcRequest req = new JsonRpcRequest("unregisterdepot", 0);
		//req.setParameters(this.getCell().getName());
		//JsonRpcResponse result1 = this.getCommunicator().execute(this.brokerAddress, req);
		
		Response result2 = this.getCommunicator().execute(this.brokerAddress + "/" + "unregisterdepot", (new Request())
				.setParameter("agentname", this.getAgent().getName())
				, 200000);

		// Check if unregister error
		if (result2.hasError() == true) {
			throw new Exception("Cannot delete depot. " + result2.getError().getMessage());
		}

		this.depot = null;
		log.debug("Depot deleted={}", depot);
	}

	private void multiplyAgent() throws Exception {
		try {
			// If depot size > 2x start size
			if (this.depot.getTotalCurrentValue(Map.of(this.stockName, this.closePrice)) >= this.multiplyLimit) {
				log.info("Time to split and create new cells");
				//Sell everything and remove 1000 money
				if (this.depot.getAssets().isEmpty()==false) {
					this.sellDefaultStock(this.depot.getAssets().get(0).getVolume());
				}
				
				//Remove init startsize money
				this.removeMoneyFromDepot(startSize * 0.3);
				
				//Modify the configuration
				AgentConfig newCellConfig = this.getAgent().getConfiguration();
				FunctionConfig newSignalFunctionConfig = newCellConfig.getCellFunction(signalAddress);
				FunctionConfig newTraderFunctionConfig = newCellConfig.getCellFunction("TraderFunction");
				
				//Get old values and modify them
				int emaShort = Integer.valueOf(newSignalFunctionConfig.getProperty(EMAIndicator.ATTRIBUTEEMASHORT));
				int emaLong = Integer.valueOf(newSignalFunctionConfig.getProperty(EMAIndicator.ATTRIBUTEEMALONG));
				
				int newEmaShort;
				int newEmaLong;
				if (this.allowMutations==true) {
					//Generate new parameters with 30% probability
					newEmaShort = generateVariation(emaShort, 20, 1, emaLong-1, 0.3);
					newEmaLong = generateVariation(emaLong, 40, newEmaShort+1, 1000, 0.3);
				} else {
					newEmaShort = emaShort;
					newEmaLong = emaLong;
				}
				
				String type = "L" + newEmaLong + "S" + newEmaShort;
				
				//ExecutionOrder 1-10
				int executionOrder = (int)(Math.random()*90)+1;
				
				//Set new config
				newSignalFunctionConfig
					.setProperty(EMAIndicator.ATTRIBUTEEMALONG, newEmaLong)
					.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, newEmaShort);
				
				newTraderFunctionConfig.setProperty(Trader.ATTRIBUTEAGENTTYPE, type);
				newTraderFunctionConfig.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, executionOrder);
				newCellConfig.setName("T" + type);
				
				newCellConfig.replaceCellFunctionConfig(newTraderFunctionConfig);
				newCellConfig.replaceCellFunctionConfig(newSignalFunctionConfig);
				
				this.getCommunicator().execute(this.getAgentName() + ":" + "reproduce" + "/" + "executereplication", 
						(new Request()).setParameter("config", newCellConfig.toJsonObject()), 100000);
				
				synchronized (this) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {
						
					}
				}
				
//				this.getCommunicator().execute(this.getCellName() + ":" + "reproduce" + "/" + "command", 
//						(new Request())
//						.setParameter("command", ControlCommand.START)
//						.setParameter("blocking", true), 100000);
				
				
			}
		} catch (Exception e) {
			log.error("Error: Cannot mulitply agent", e);
			throw new Exception(e.getMessage());
			
		}
	}
	
	/**
	 * Generate a variation of a variable
	 * 
	 * @param startValue
	 * @param sigma
	 * @param limitBottom
	 * @param limitTop
	 * @param modificationProbability
	 * @return
	 */
	private int generateVariation(int startValue, int sigma, int limitBottom, int limitTop, double modificationProbability) {
		int result = startValue;
		
		if (Math.random() <= modificationProbability) {
			do {
				Random r = new Random();
				result = (int)((double)startValue + (double)sigma * r.nextGaussian());
			} while (limitBottom>result || limitTop<result);
		}
		
		return result;
	}
	
	private void removeMoneyFromDepot(double amount) throws Exception {
		try {
			if (depot.getLiquid() >= amount) {
				//JsonRpcRequest request1 = new JsonRpcRequest("buy", 0);
				// request1.setParameterAsValue(0, traderAgentName);
				// request1.setParameterAsValue(1, traderType);
				//request1.setParameters(this.getCell().getName(), this.stockName, this.closePrice, 1);
				//JsonRpcResponse result = this.getCommunicator().execute(this.brokerAddress, request1);
				
				Request req = (new Request())
						.setParameter("agentname", this.getAgent().getName())
						.setParameter("amount", amount);
				
				Response result1 = this.getCommunicator().execute(this.brokerAddress + "/" + "removemoney", req, 200000);
				
				if (result1.hasError()) {
					throw new Exception("Cannot remove money. " + result1.getError().getMessage());
				}

				this.depot = (new Gson()).fromJson(result1.getResult(), Depot.class);
				log.info("Stock bought={}. Depot change={}", req, this.depot);
			} else {
				log.debug("Not enough money on the depot.");
			}
		} catch (Exception e) {
			log.error("Cannot remove money from depot", e);
			throw new Exception(e.getMessage());
		}
		
	}

	private boolean killAgentOnDepotDeath() throws Exception {
		boolean isKilled = false;

		if (this.depot.getTotalBuyValue() < this.deathLimit) {
			log.info("Agent dies. Depot={}, deathlimit={}", this.depot.getTotalBuyValue(), this.deathLimit);
			// this.shutDownCodelet();
			// this.getCell().takeDownCell();
			isKilled = true;
			// this.setAllowedToRun(false);
			// this.setActive(false);
			// log.debug("Agent is killed");
		}

		return isKilled;
	}

	private void calculateSignal() throws Exception {
		// Calculate buy or sell signal based on indicators
		//JsonRpcRequest req = new JsonRpcRequest("any", 0);
		//JsonRpcResponse result = this.getCommunicator().execute(this.signalAddress, req);
		
		Response result = this.getCommunicator().execute(this.signalAddress + "/" + "generatesignal", new Request(), 200000);
		

		this.buySignal = result.getResult().getAsJsonObject().getAsJsonPrimitive("buy").getAsBoolean();
		this.sellSignal = result.getResult().getAsJsonObject().getAsJsonPrimitive("sell").getAsBoolean();

	}

	private void executeTrade() throws Exception {
		log.info("Buy={}; sell={}", this.buySignal, this.sellSignal);
		if (this.buySignal == true) {
			int amount = (int)(depot.getLiquid()/this.closePrice);
			buyDefaultStock(amount);

		}

		if (this.sellSignal == true) {
			//Sell all
			if (this.depot.getAssets().stream().filter(a -> a.getStockName().equals(this.stockName)).findFirst().isPresent()
					&& (this.depot.getAssets().stream().filter(a -> a.getVolume() >= 1)).findFirst().isPresent()) {
				int amount = this.depot.getAssets().get(0).getVolume();
				sellDefaultStock(amount);
			}
			

		}
	}

	private void buyDefaultStock(int amount) throws Exception {
		if (depot.getLiquid() > this.closePrice * 1) {
			//JsonRpcRequest request1 = new JsonRpcRequest("buy", 0);
			// request1.setParameterAsValue(0, traderAgentName);
			// request1.setParameterAsValue(1, traderType);
			//request1.setParameters(this.getCell().getName(), this.stockName, this.closePrice, 1);
			//JsonRpcResponse result = this.getCommunicator().execute(this.brokerAddress, request1);
			
			Request req = (new Request())
					.setParameter("agentname", this.getAgent().getName())
					.setParameter("stockname", this.stockName)
					.setParameter("price", this.closePrice)
					.setParameter("volume", amount);
			
			Response result1 = this.getCommunicator().execute(this.brokerAddress + "/" + "buy", req, 200000);
			
			if (result1.hasError()) {
				throw new Exception("Cannot buy stock. " + result1.getError().getMessage());
			}

			this.depot = (new Gson()).fromJson(result1.getResult(), Depot.class);
			log.info("Stock bought={}. Depot change={}", req, this.depot);
		} else {
			log.debug("No enough money, no buy signal");
		}
	}

	private void sellDefaultStock(int amount) throws Exception {
		if (this.depot.getAssets().stream().filter(a -> a.getStockName().equals(this.stockName)).findFirst().isPresent()
				&& (this.depot.getAssets().stream().filter(a -> a.getVolume() >= 1)).findFirst().isPresent()) {			
			
			Request req = (new Request())
					.setParameter("agentname", this.getAgent().getName())
					.setParameter("stockname", this.stockName)
					.setParameter("price", this.closePrice)
					.setParameter("volume", amount);
			
			Response result1 = this.getCommunicator().execute(this.brokerAddress + "/" + "sell", req, 200000);
			
			if (result1.hasError()) {
				throw new Exception("Cannot buy stock. " + result1.getError().getMessage());
			}

			this.depot = (new Gson()).fromJson(result1.getResult(), Depot.class);
			log.info("Stock sold={}. Depot change={}", req, this.depot);

		} else {
			log.debug("No sell signal as the volume of stock is not enough");
		}
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
