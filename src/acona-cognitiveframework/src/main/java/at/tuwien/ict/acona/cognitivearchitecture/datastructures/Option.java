package at.tuwien.ict.acona.cognitivearchitecture.datastructures;

public class Option extends BasicDataStructure {

	public static final String OPTIONTYPE = "Option";

	private String optionType = "";
	private String originAddress = "";
	private String goalAddresses = "";
	private OptionEvaluation evaluation;
	private String actionServiceName = "";
	private String actionMethod = "";
	private String[] actionParameter;

	private String postState;
	private String preState;
	private String currentState;
	private String requireState;

	public Option(String name) {
		super(name, OPTIONTYPE);
		this.setEvaluation(new OptionEvaluation(name + "Evaluation"));
	}

	public String getOriginAddress() {
		return originAddress;
	}

	public void setOriginAddress(String originAddress) {
		this.originAddress = originAddress;
	}

	public String getGoalAddress() {
		return goalAddresses;
	}

	public void setGoalAddress(String goalAddresses) {
		this.goalAddresses = goalAddresses;
	}

	public OptionEvaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(OptionEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	public String getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public String getRequiredState() {
		return requireState;
	}

	public void setRequiredState(String nextState) {
		this.requireState = nextState;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("");
		builder.append(getName());
		builder.append("|");
		builder.append(getType());
		builder.append("|");
		builder.append(getOptionType());
		builder.append("|origin=");
		builder.append(originAddress);
		builder.append("|goals=");
		builder.append(goalAddresses);
		builder.append("|evaluation=");
		builder.append(evaluation);
		builder.append("|prestate=");
		builder.append(preState);
		builder.append("|poststate=");
		builder.append(postState);
		builder.append("|currstate=");
		builder.append(currentState);
		builder.append("|reqstate=");
		builder.append(requireState);
		builder.append("|action=");
		builder.append(actionServiceName);
		builder.append("|method=");
		builder.append(this.actionMethod);
		builder.append("|parameter=");
		builder.append(this.actionParameter);
		builder.append("");
		return builder.toString();
	}

	public String getOptionType() {
		return optionType;
	}

	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}

	public String getPostState() {
		return postState;
	}

	public void setPostState(String postState) {
		this.postState = postState;
	}

	public String getPreState() {
		return preState;
	}

	public void setPreState(String preState) {
		this.preState = preState;
	}

	public String getActionServiceName() {
		return actionServiceName;
	}

	public void setActionServiceName(String actionServiceName) {
		this.actionServiceName = actionServiceName;
	}

	public String[] getActionParameter() {
		return actionParameter;
	}

	public void setActionParameter(String[] actionParameter) {
		this.actionParameter = actionParameter;
	}

	public String getActionMethod() {
		return actionMethod;
	}

	public void setActionMethod(String actionMethod) {
		this.actionMethod = actionMethod;
	}

}
