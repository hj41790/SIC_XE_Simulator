
public class Instruction {
	
	private String 	name;
	private int	 	opcode;
	private int 	format;
	private int 	num_operand;
	
	public Instruction(String n, int o, int f, int op){
		name = n;
		opcode = o;
		format = f;
		num_operand = op;
	}
	
	public boolean isName(String n){
		if(n.equals(name)) return true;
		else return false;
	}

	public String getName() {
		return name;
	}

	public int getOpcode() {
		return opcode;
	}

	public int getFormat() {
		return format;
	}

	public int getNum_operand() {
		return num_operand;
	}
	
	public void print(){
		
		System.out.format("%6s %2d %2X %d\n", name, format, opcode, num_operand);
		
	}

}
