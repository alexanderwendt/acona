package at.tuwien.ict.acona.cognitivearchitecture.datastructures;

import java.text.DecimalFormat;

public class OptionEvaluation extends BasicDataStructure {

	private final static String OPTIONEVALUATIONTYPE = "OptionEvaluation";

	private double evaluation = 0;
	private String evaluationHistory = "";

	public OptionEvaluation(String name) {
		super(name, OPTIONEVALUATIONTYPE);
	}

	public double getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(double evaluation) {
		this.evaluation = evaluation;
	}

	public void increaseEvaluation(double contribution) {
		this.evaluation += contribution;
	}

	public void appendToEvaluationHistory(String identifier, double contribution) {
		if (this.evaluationHistory.equals("") == false) {
			// this.evaluationHistory += ", ";
		}

		DecimalFormat df = new DecimalFormat("#.##");

		this.evaluationHistory = identifier + ":" + df.format(contribution) + ", " + this.evaluationHistory;

		if (this.evaluationHistory.length() > 200) {
			this.evaluationHistory = this.evaluationHistory.substring(0, 200);
		}

	}

	public String getEvaluationHistory() {
		return evaluationHistory.toString();
	}

	public void setEvaluationHistory(String evaluationHistory) {
		this.evaluationHistory = evaluationHistory;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("evaluation=");
		builder.append(evaluation);
		builder.append("|History=");
		builder.append(evaluationHistory);
		// builder.append(", getName()=");
		// builder.append(getName());
		// builder.append(", getType()=");
		// builder.append(getType());
		// builder.append("]");
		return builder.toString();
	}

}
