package at.tuwien.ict.acona.cognitivearchitecture.datastructures;

public class Condition extends BasicDataStructure {
	
	public final static String CONDITIONTYPE = "Condition";

//	public static final String HASOPERATOR = "hasOperator";
//	public static final String HASOPERANDA = "hasOperandA";
//	public static final String HASOPERANDB = "hasOperandB";
//	public static final String HASCONDITION = "hasCondition";
//	public static final String TYPE = "Condition";
	
	private String operatorA;
	private String operatorB;
	private String operand;
	
	public Condition(String name) {
		super(name, CONDITIONTYPE);
	}

	public String getOperatorA() {
		return operatorA;
	}

	public void setOperatorA(String operatorA) {
		this.operatorA = operatorA;
	}

	public String getOperatorB() {
		return operatorB;
	}

	public void setOperatorB(String operatorB) {
		this.operatorB = operatorB;
	}

	public String getOperator() {
		return operand;
	}

	public void setOperand(String operand) {
		this.operand = operand;
	}
	
	public static class Operator {
		public final static String EQUAL = "=";
		public final static String GREATERTHAN = ">";
		public final static String SMALLERTHAN = "<";
		public final static String STRINGEQUAL = "Stringequal";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("");
		builder.append(getName());
		builder.append("|");
		builder.append(getType());
		builder.append("condition=");
		builder.append(operatorA);
		builder.append(":");
		builder.append(operand);
		builder.append(":");
		builder.append(operatorB);

		return builder.toString();
	}

}
