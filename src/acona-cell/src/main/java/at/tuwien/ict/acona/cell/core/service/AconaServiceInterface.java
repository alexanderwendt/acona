package at.tuwien.ict.acona.cell.core.service;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.lang.acl.MessageTemplate;

public interface AconaServiceInterface <AGENT_TYPE extends CellImpl> {
	AconaServiceType getType();
	MessageTemplate getMatchingTemplate();
	Message perform(AGENT_TYPE agent, Message message);
	int getError();
}
