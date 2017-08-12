
public class Symbol {

	private String name;
	private int addr;
	
	public Symbol(String n, int a)
	{
		name = n;
		addr = a;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setAddr(int addr) {
		this.addr = addr;
	}

	public String getName() {
		return name;
	}

	public int getAddr() {
		return addr;
	}
	
	
	
}
