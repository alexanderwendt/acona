package at.tuwien.ict.acona.cell.cellfunction;

public enum ServiceState {
	BUILDING, //At the creation of the function
	INITIALIZING, //At the initliaization of the function variables
	IDLE, //Ready to be used
	ERROR, //Error
	UNDEFINED, //Also error
	RUNNING //Currently in action

}
