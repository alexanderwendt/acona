package at.tuwien.ict.acona.visualization;

public class Position {

	int x = 0;
	int y = 0;
	String state = "";

	public Position(int x, int y, String state) {
		super();
		this.x = x;
		this.y = y;
		this.state = state;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}
