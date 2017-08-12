
public class Section {
	
	String name;
	int startAddr;
	int length;
	
	public Section(String n, int addr, int l){
		name = n;
		startAddr = addr;
		length = l;
	}

	public String getName() {
		return name;
	}

	public int getStartAddr() {
		return startAddr;
	}

	public int getLength() {
		return length;
	}

	
	
}
