package at.tuwien.ict.acona.evolutiondemo.traderagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;

public class DelayedCellShutDown extends CellFunctionThreadImpl {

	protected static Logger log = LoggerFactory.getLogger(DelayedCellShutDown.class);

	private int finaldelay = 50;

	public void killSwitch(int delay, Cell cell) throws Exception {
		try {
			this.finaldelay = delay;
			// create and register instance
			String name = cell.getName() + "_KillSwitch";
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
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// TODO Auto-generated method stub
		
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
