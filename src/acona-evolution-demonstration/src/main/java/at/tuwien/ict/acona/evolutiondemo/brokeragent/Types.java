package at.tuwien.ict.acona.evolutiondemo.brokeragent;

public class Types {
	private String type;
	private int number;
	
	public Types(String type, int number) {
		super();
		this.type = type;
		this.number = number;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Types [type=");
		builder.append(type);
		builder.append(", number=");
		builder.append(number);
		builder.append("]");
		return builder.toString();
	}
}
