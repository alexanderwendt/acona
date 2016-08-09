package at.tuwien.ict.acona.cell.core.helpers;

import at.tuwien.ict.acona.cell.core.InspectorCell;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;

public class CellWithFSMBehaviour extends InspectorCell {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void internalInit() {
		
		FSMBehaviour executeBehaviour = new FSMBehaviour();
		executeBehaviour.registerFirstState(new OneShotBehaviour(this) {

			@Override
			public void action() {
				log.info("Start state 1");
				block();
			}
			
		}, "N1");
		
		executeBehaviour.registerState(new OneShotBehaviour(this) {

			@Override
			public void action() {
				log.info("Start state 2");
				block();
			}
			
		}, "N2");
		
		executeBehaviour.registerLastState(new OneShotBehaviour(this) {

			@Override
			public void action() {
				log.info("Start state 3");
			}
			
		}, "N3");
		
		executeBehaviour.registerDefaultTransition("N1", "N2");
		executeBehaviour.registerDefaultTransition("N2", "N3");
		
		CellBehaviour b = new CellBehaviour(executeBehaviour);
		this.addBehaviour(b);
		
		
		
		//Start the behaviour and wait for input
		b.setTestPassed(false);
		b.restart();
		b.setTestPassed(false);
		b.restart();
		b.setTestPassed(true);
		b.restart();
		b.setTestPassed(true);
		b.restart();
		b.setTestPassed(true);
		b.restart();
		
		this.addBehaviour(new WakerBehaviour(this, 100000) {
			
		});
		
	}
	
	private class testConditionBehaviour extends OneShotBehaviour {

		private boolean testPassed = false;
		
		@Override
		public void action() {
			this.testPassed = (boolean) parent.getDataStore().get("testPassed");
			log.info("Test passed={}", this.testPassed);
			if (this.testPassed==false) {
				//this.block();
			} else {
				log.debug("Continue with execution");
			}
		}
		
		public int onEnd() {
			if (this.testPassed==true) {
				log.debug("set transition state 2");
				return 2;
			} else {
				log.debug("Set transition state 1");
				return 1;
			}
		}
	}
	
	private class CellBehaviour extends FSMBehaviour {
		private boolean testPassed = false;
		
		public CellBehaviour(Behaviour b) {
			//Not necessary but good thing to do.
			this.setDataStore(new DataStore());
			
			this.registerFirstState(new testConditionBehaviour(), "TESTCONDITION");
			
			this.registerLastState(new OneShotBehaviour(this.myAgent) {

				@Override
				public void action() {
					log.debug("CellMessage finished, restart behaviour");

				}
				
				public int onEnd() {
					this.parent.reset();
					return super.onEnd();
				}
				
			}, "CLEAN");
			
			this.registerState(b, "EXECUTE");
			
			this.registerDefaultTransition("TESTCONDITION", "EXECUTE");
			this.registerTransition("TESTCONDITION", "TESTCONDITION", 1);
			this.registerDefaultTransition("EXECUTE", "CLEAN");
		}
			
		public int onEnd() {
			//myAgent.doDelete();
			log.info("Agent end");
			return super.onEnd();
		}
			
		public void setTestPassed(boolean value) {
			this.testPassed = value;
			this.getDataStore().put("testPassed", value);
			log.debug("Added value to JADE data store={}", value);
		}
	}

}
