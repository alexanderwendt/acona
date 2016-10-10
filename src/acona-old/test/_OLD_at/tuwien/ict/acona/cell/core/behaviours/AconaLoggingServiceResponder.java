package _OLD_at.tuwien.ict.acona.cell.core.behaviours;

import _OLD_at.tuwien.ict.acona.cell.testing.GlobalLogger;
import at.tuwien.ict.acona.cell.core.CellImpl;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;

public class AconaLoggingServiceResponder extends AconaServiceResponder {
	private static final long serialVersionUID = -7190877302628323689L;

	public AconaLoggingServiceResponder(CellImpl agent, AconaServiceInterface<CellImpl> service) {
		super(agent, service);
	}

	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		GlobalLogger.log(getAgent(), request);

		return super.handleRequest(request);
	}
}
