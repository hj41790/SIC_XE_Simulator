
public class Modification {
	
	private int location;
	private int size;
	private boolean operation;
	private String symbol;
	
	public Modification(int l, int s, boolean o, String sym){
		location = l;
		size = s;
		operation = o;
		symbol = sym;
	}

	public int getLocation() {
		return location;
	}

	public int getSize() {
		return size;
	}

	public boolean getOperation() {
		return operation;
	}

	public String getSymbol() {
		return symbol;
	}
	
	public void print(){
		System.out.println(String.format("%X",location) + "  " + size + "  "+ operation + "  "+symbol);
	}

}
