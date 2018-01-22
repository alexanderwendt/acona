package at.tuwien.ict.acona.cognitivearchitecture.datastructures;

public final class Vocabulary {
	
	public final class Episode {
		public final static String TYPE = "Episode";
		
		public final static String HASEVALUATION = "hasEvaluation";
		public final static String HASADDRESS = "hasAddress";
		public final static String HASSIMULATIONID = "hasSimulationId";
		public final static String HASONTOLOGYID = "hasOntologyId";
		public final static String HASRULESTRUCTURE = "hasRulestructure";
		
	}
	
	public final class Evaluation {
		public final static String TYPE = "Evaluation";
		
		public final static String CO2EVALUATION = "CO2Evaluation";
		public final static String ENERGYEVALUATION = "EnergyEvaluation";
		public final static String PENALTYEVALUATION = "PenaltyEvaluation";
	}
	
	public final class Goal {
		public final static String TYPE = "Goal";
		
		public final static String HASGOALSTATE = "hasGoalState";
		public final static String HASORIGIN = "hasOrigin";
		public final static String HASCONDITION = "hasConditon";
		public final static String HASIMPORTANCE = "hasImportance";
		
	}
	
	public final class Condition {
		public static final String HASOPERATOR = "hasOperator";
		public static final String HASOPERANDA = "hasOperandA";
		public static final String HASOPERANDB = "hasOperandB";
		public static final String HASCONDITION = "hasCondition";
		public static final String TYPE = "Condition";
		
		public final class Operator {
			public final static String EQUAL = "=";
			public final static String GREATERTHAN = ">";
			public final static String SMALLERTHAN = "<";
			public final static String STRINGEQUAL = "Stringequal";
		}
	}
	
	public final class Option {
		public static final String TYPE = "Option";
		public static final String HASORIGIN = "hasOrigin";
		public static final String HASGOAL = "hasGoal";
		public static final String HASEVALUTION = "hasEvaluation";
		public static final String HASCURRENTSTATE = "hasCurrentState";
		public static final String HASNEXTSTATE = "hasNextState";
		public static final String HASACTION = "hasAction";
		public static final String HASSTATES = "hasStates";
		
		public final class Evaluation {
			public final static String TYPE = "OptionEvaluation";
			public final static String HASEVALUATION = "hasEvaluation";
			public final static String HASEVALUATIONHISTORY = "hasEvaluationHistory";
		}
	}
	
	public final class Action {
		public static final String TYPE = "Action";
		public static final String HASPRECONDITIONSTATE = "hasPreconditionState";
		public static final String HASPOSTCONDITIONSTATE = "hasPostConditionState";
	}
	
	public final class States {
		public static final String RULESETRETURNED = "rulesetreturned";
		public static final String RULESETAVAILABLE = "rulesetavailable";
		public static final String NEWRULETEMPLATE = "newruletemplate";
		public static final String RULESTRUCTUREAVAILABLE = "rulestructureavailable";
		public static final String RULEPARAMETERAVAILABLE = "ruleparameteravailable";
		public static final String INITIALIZED = "initialized";
	}
}
