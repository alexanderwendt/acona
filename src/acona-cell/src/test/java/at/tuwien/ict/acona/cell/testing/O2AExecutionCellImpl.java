package at.tuwien.ict.acona.cell.testing;

import at.tuwien.ict.acona.cell.core.CellImpl;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;

public class O2AExecutionCellImpl extends CellImpl {
	private static final long serialVersionUID = 542744983990374158L;

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		super.setup();
		setEnabledO2ACommunication(true, 0);
		
		addBehaviour((new ThreadedBehaviourFactory()).wrap(new CyclicBehaviour() {
			private static final long serialVersionUID = 4631905934882462654L;

			@Override
			public void action() {
				Object obj = getAgent().getO2AObject();
				if(obj != null) {
					if(obj instanceof O2AExecuteable) {
						((O2AExecuteable)obj).run(getAgent());
					} else {
						throw new IllegalArgumentException("The O2AExecutionCellImpl Behaviour expects all objects passed to the agent via putO2AObject to be instance of O2AExecuteable but this object is not:\n" + obj.toString());
					}
				} else {
					block();
				}
			}
		}));
	}	
}
