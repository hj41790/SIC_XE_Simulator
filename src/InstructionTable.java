import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class InstructionTable {
	
	private static InstructionTable table = null;
	
	private HashMap<Byte, Instruction> map = null;
	private String filename = "inst.data";

	
	public static InstructionTable getInstance(){
		
		if(table == null)
			table = new InstructionTable();
		
		return table;
		
	}
	
	public String getInstName(byte opcode){
		
		return ((Instruction)map.get(opcode)).getName();
	}
	
	public int getFormat(byte opcode){
		
		return ((Instruction)map.get(opcode)).getFormat();
	}
	
	public int getOperandNum(byte opcode){
		
		return ((Instruction)map.get(opcode)).getNum_operand();
	}
	
	private InstructionTable(){
		
		map = new HashMap<Byte, Instruction>();
		
		// file read
		try {
			
			File file = new File(filename);
			Scanner scanner = new Scanner(file);

			while(scanner.hasNext()){
				String name = scanner.next();
				int format = scanner.nextInt();
				int opcode = scanner.nextInt(16);
				int num_operand = scanner.nextInt();
				
				Instruction tmp = new Instruction(name, opcode, format, num_operand);

				map.put((byte)opcode, tmp);

			}
			scanner.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
}
