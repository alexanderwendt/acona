package at.tuwien.ict.acona.cell.custombehaviors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.core.CellFunctionBehavior;
import at.tuwien.ict.acona.cell.datastructures.Datapackage;

public class AdditionBehavior extends CellFunctionBehavior {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AdditionBehavior(Cell caller) {
		super(caller);
	}

	protected static Logger log = LoggerFactory.getLogger(AdditionBehavior.class);

	@Override
	public void function(Datapackage data) {
		
		
		log.info("execute behavior addition!!!!");
		
	}

}
