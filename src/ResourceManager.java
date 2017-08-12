import java.io.IOException;
import java.util.ArrayList;

public class ResourceManager {
	
	byte[] memory;
	int[] register;

	private int usedMem;
	private final int memSize = 0xFFFFF;
	public static int EXITADDR = -1;
	
	
	ArrayList<Device> dev;
	
	public ResourceManager(){
		
		dev = new ArrayList<Device>();
		
		initializeMemory();
		initializeRegister();
		usedMem = 0;
	}
	
	public void initializeMemory(){
		
		memory = new byte[memSize];
		for(int i=0; i<memSize; i++) memory[i] = 0;
		
	}
	
	public void initializeRegister(){

		register = new int[10];
		for(int i=0; i<10; i++) register[i] = 0;
		
	}

	
	public boolean initialDevice(String devName){
		
		for(Device d : dev){
			if(d.dev.equals(devName))
				return true;
		}
		
		
		
		Device tmp = new Device();
		if(tmp.initialize(devName)) {
			dev.add(tmp);
			return true;
		}
		
		return false;
	}

	public void writeDevice(String devName, byte[] data, int size){
		
		
		for(Device d : dev){
			if(d.dev.equals(devName)){
				d.write(data);
				break;
			}
		}
		
	}
	
	public byte[] readDevice(String devName, int size){
		
		byte[] res = null;
		
		for(Device d : dev){
			if(d.dev.equals(devName)){
				res = d.read();
				break;
			}
		}
		

		return res;
	}
	
	public void closedev(){
		for(Device d : dev){
			d.close();
		}
	}

	
	public void resetSW(){
		register[SW] = 0;
	}
	
	public void setZeroFlag(){
		register[SW] = 0x100;
	}
	
	public void setSignFlag(boolean res){
		if(!res) register[SW] = 0x10;
		else register[SW] = 0x00;
	}
	
	
	public int malloc(int size){
		
		int current = usedMem;
		usedMem += size;
		
		return current;
	}
	
	public void modifyMemory(int locate, int data, int _size, boolean op){

		int size = (_size%2==1)? _size/2+1 : _size/2;

		int value = 0;
		value += (_size%2==1) ? memory[locate]&0x0F : memory[locate];
		
//		System.out.println("location : " + String.format("%X", locate));
		
		for(int i=1; i<size; i++)
			value = value<<8 | memory[locate+i];
		
//		System.out.format("before : %06X\n", value);
		
		if(op) value += data;
		else value -= data;

//		System.out.format("after : %06X\n\n", value);
		
		if(_size%2==1) memory[locate] &= 0xF0;
		else memory[locate] = 0;
		memory[locate] += value>>((size-1)*8);
		
		for(int i=1; i<size; i++)
			memory[locate + i] = (byte)((value>>((size-1-i)*8))&0x000000FF);

	
	}
		
	public void setMemory(int locate, byte[] data, int size){
		
		if(locate==EXITADDR) return;
		
		for(int i=0; i<size; i++)
			memory[locate+i] = data[i];

	}
	
	public void setRegister(int regnum, int value){
		register[regnum] = value;
	}
	
	public byte[] getMemory(int locate, int size){

		if(locate==EXITADDR) return null;
		
		byte[] tmp = new byte[size];
		for(int i=0; i<size; i++)
			tmp[i] = memory[locate+i];
		
		return tmp;
	}
	
	public int getRegister(int regNum){
		return register[regNum];
	}
	
	public String getMemory(){
		
		String result = "";
		int showMem = usedMem;
		
		if(showMem%16!=0)
			while(showMem%16!=0) showMem++;
		
		
		for(int i=0; i<showMem; i++){
			if(i%16==0) {
				if(i>0) result += '\n';
				result += String.format("%06X   %02X", i, memory[i]);
			}
			else if(i%4==0) result += String.format(" %02X", memory[i]);
			else result += String.format("%02X", memory[i]);
			
		}
		
		return result;
	}

	public String getRegisterName(int i) {
		return REGNAME[i];
	}


	
	public static int A = 0;
	public static int X = 1;
	public static int L = 2;
	public static int B = 3;
	public static int S = 4;
	public static int T = 5;
	public static int F = 6;
	public static int PC = 8;
	public static int SW = 9;
	

	private String[] REGNAME = new String[]{"A","X","L","B","S","T","F","","PC","SW"};

	
}
