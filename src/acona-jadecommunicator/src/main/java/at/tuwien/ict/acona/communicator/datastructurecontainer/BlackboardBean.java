package at.tuwien.ict.acona.communicator.datastructurecontainer;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Message;

public class BlackboardBean implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Message message = Message.newMessage();
	//private String message = new String("");
	//private String receiver = new String("");
	//private String type = new String("");

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
	
	public Message getMessage()	{
		return message;
	}
	
	public String getMessageBodyAsString() {
		JsonObject messageBody = this.message.getContent().getAsJsonObject();
		return messageBody.toString();
	}	
	
	public void setMessage(JsonObject message)	{
		this.message=Message.toMessage(message);
	}
	
	public void setMessage(Message message)	{
		this.message=message;
	}
	
//	public String getReceiver()	{
//		return receiver;
//	}
	
//	public void setReceiver(String receiver)	{
//		this.receiver=receiver;
//	}
	
//	public String getType() {
//		return type;
//	}

//	public void setType(String type) {
//		this.type = type;
//	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message=");
		builder.append(message);
		builder.append(", mode=");
		builder.append(communicationMode);
		return builder.toString();
	}

	
}
