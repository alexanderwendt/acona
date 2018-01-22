package at.tuwien.ict.acona.cognitivearchitecture.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Goal extends BasicDataStructure {

	public final static String GOALTYPE = "Goal";

	// public final static String TYPE = "Goal";
	//
	// public final static String HASGOALSTATE = "hasGoalState";
	// public final static String HASORIGIN = "hasOrigin";
	// public final static String HASCONDITION = "hasConditon";
	// public final static String HASIMPORTANCE = "hasImportance";

	// private String goalState;
	private String originAddress;
	private Condition[] condition;
	private double hasImportance;
	private String postState;
	private String preState;
	private String currentState;
	private String requireState;

	public Goal(String name) {
		super(name, GOALTYPE);
	}

	// public String getGoalState() {
	// return goalState;
	// }
	//
	//
	// public void setGoalState(String hasGoalState) {
	// this.goalState = hasGoalState;
	// }

	public String getOriginAddress() {
		return originAddress;
	}

	public void setOriginAddress(String hasOriginAddress) {
		this.originAddress = hasOriginAddress;
	}

	public Condition[] getConditions() {
		return condition;
	}

	public Condition getFirstCondition(String conditionName) {
		Condition result = null;

		Optional<Condition> condition = this.getConditionsAsList().stream().filter(c -> c.getName().equals(conditionName)).findFirst();
		if (condition.isPresent()) {
			result = condition.get();
		}

		return result;
	}

	public List<Condition> getConditionsAsList() {
		Condition[] result = this.condition;

		if (result == null) {
			result = new Condition[0];
		}
		return Arrays.asList(result);
	}

	public void addCondition(Condition newcondition) {
		List<Condition> list = new ArrayList<>();

		if (this.condition != null) {
			list = new ArrayList<>(Arrays.asList(this.condition));
		}

		list.add(newcondition);
		Condition[] conditionArray = new Condition[list.size()];
		list.toArray(conditionArray);
		this.setCondition(conditionArray);
	}

	public void setCondition(Condition[] hasCondition) {
		this.condition = hasCondition;
	}

	public double getImportance() {
		return hasImportance;
	}

	public void setImportance(double hasImportance) {
		this.hasImportance = hasImportance;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("");
		builder.append(getName());
		builder.append("|");
		builder.append(getType());
		builder.append("|origin=");
		builder.append(originAddress);
		builder.append("|");
		builder.append(hasImportance);
		builder.append("|prestate=");
		builder.append(preState);
		builder.append("|poststate=");
		builder.append(postState);
		builder.append("|currstate=");
		builder.append(currentState);
		builder.append("|reqstate=");
		builder.append(this.requireState);
		builder.append("|conditions=[");
		builder.append(Arrays.toString(condition));
		builder.append("]");
		return builder.toString();
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

	public String getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public String getRequireState() {
		return requireState;
	}

	public void setRequireState(String requireState) {
		this.requireState = requireState;
	}
}
