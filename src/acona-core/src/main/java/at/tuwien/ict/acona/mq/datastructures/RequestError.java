package at.tuwien.ict.acona.mq.datastructures;

public class RequestError {
	private final String error;

	public RequestError(String error) {
		super();
		this.error = error;
	}

	public String getMessage() {
		return error;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RequestError [error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}
}
