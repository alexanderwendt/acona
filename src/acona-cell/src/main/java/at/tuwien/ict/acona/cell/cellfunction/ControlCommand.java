package at.tuwien.ict.acona.cell.cellfunction;

public enum ControlCommand {
	START,
	STOP,
	PAUSE,
	EXIT;
	
	public static boolean isCommand(String s) {
		boolean result =false;
		if (s.equals(ControlCommand.PAUSE.toString())==true || s.equals(ControlCommand.STOP.toString())==true || s.equals(ControlCommand.START.toString())==true || s.equals(ControlCommand.EXIT.toString())==true) {
			result = true;
		}
		
		return result;
	}
}
