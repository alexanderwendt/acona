package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class DelayedCellShutDown extends CellFunctionImpl {

	protected static Logger log = LoggerFactory.getLogger(DelayedCellShutDown.class);

	private int finaldelay = 1000;

	public void killSwitch(int delay, Cell cell) throws Exception {
		try {
			this.finaldelay = delay;
			// create and register instance
			String name = cell.getLocalName() + "_KillSwitch";
			this.init(CellFunctionConfig.newConfig(name, DelayedCellShutDown.class), cell);

			Runnable t = new Runnable() {

				@Override
				public void run() {
					synchronized (this) {
						try {
							this.wait(finaldelay);
						} catch (Exception e) {

						}
					}

					getCell().takeDownCell();
				}

			};

			Thread x = new Thread(t);
			x.start();

		} catch (Exception e) {
			log.error("Query error");
			throw new Exception(e);
		} finally {
			// Deregister
			this.shutDownFunction();
		}
	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest param, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void cellFunctionInit() throws Exception {

	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
