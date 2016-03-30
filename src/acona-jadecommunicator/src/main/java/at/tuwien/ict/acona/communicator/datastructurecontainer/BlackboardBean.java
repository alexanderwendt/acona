package at.tuwien.ict.acona.communicator.datastructurecontainer;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;

public class BlackboardBean implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Message message = Message.newMessage();

	private CommunicationMode communicationMode = CommunicationMode.ASYNC;

	//Use builderpattern here instead
	
	public BlackboardBean()	{
		
	}
	
	public void setCommunicationMode(CommunicationMode mode) {
		this.communicationMode = mode;
		this.message.setMode(AconaSync.SYNCHRONIZED);
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
