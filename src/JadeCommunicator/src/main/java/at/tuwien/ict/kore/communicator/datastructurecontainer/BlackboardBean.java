package at.tuwien.ict.kore.communicator.datastructurecontainer;

public class BlackboardBean implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message = new String("");
	private String receiver = new String("");
	private CommunicationMode communicationMode = CommunicationMode.ASYNC;

	//Use builderpattern here instead
	
	public BlackboardBean()	{
		
	}
	
	public void setCommunicationMode(CommunicationMode mode) {
		this.communicationMode = mode;
	}
	
	public boolean isSyncronizedRequest() {
		boolean result = false;
		
		if (this.communicationMode.equals(CommunicationMode.SYNC)==true) {
			result = true;
		}
		
		return result;
	}
	
	public String getMessage()	{
		  	return message;
	}
	
	public void setMessage(String str)	{
		message=str;
	}
	
	public String getReceiver()	{
		return receiver;
	}
	
	public void setReceiver(String receiver)	{
		this.receiver=receiver;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Receiver=");
		builder.append(receiver);
		builder.append(", message=");
		builder.append(message);
		builder.append(", mode=");
		builder.append(communicationMode);
		return builder.toString();
	}

	
}
