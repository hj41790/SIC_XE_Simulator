import java.io.File;

import javax.swing.JOptionPane;

public class SicSimulator {
	
	ResourceManager rManager;
	SicLoader loader;
	InstructionTable instTab;
	
	byte[] curInst;
	int currentLocation;
	int targetAddress;
	byte device;
	
	boolean isDev;
	
	String programName;
	int startAddr;
	int programLength;
	
	public boolean initialize(File objFile, ResourceManager rMgr){
		
		rManager = rMgr;
		
		loader = new SicLoader();
		if(!loader.load(objFile, rMgr)) return false;
		
		instTab = InstructionTable.getInstance();
	
		currentLocation = loader.startAddr;
		targetAddress = 0;
		device = 0;
		isDev = false;
		
		nextInstruction();
		
		return true;
	}
	
	public void nextInstruction(){
		
		int opcode;
		int format;
		
		currentLocation = rManager.getRegister(ResourceManager.PC);	// PC register
		if(currentLocation==ResourceManager.EXITADDR) {
			rManager.closedev();
			return;
		}
		
		// first get 2 byte from current location
		curInst = rManager.getMemory(currentLocation, 2);
		
		// get instruction format from opcode
		opcode = curInst[0]&0xFC;
		format = instTab.getFormat((byte)opcode);
		if(format==3 && (curInst[1] & 0x10 ) > 0) format = 4;
		
		// get instruction from memory
		curInst = rManager.getMemory(currentLocation, format);
		rManager.setRegister(ResourceManager.PC, currentLocation+format);
		
		int ni=0, bp=0, addr=0;
		boolean x=true, e=true;
		
		// set additional information for format 3/4 
		if(curInst.length>2){
			ni 	= curInst[0]&0x03;
			x 	= (((curInst[1]>>7) & 0x01) > 0) ? true : false;
		 	bp 	= (curInst[1]>>5) & 0x03;
		 	e 	= (((curInst[1]>>4) & 0x01) > 0) ? true : false;
		 	
		 	addr += curInst[1]&0x0F;
			for(int i=2; i<curInst.length; i++){
				addr = addr<<8 | curInst[i];
			}
			
			targetAddress = calTargetAddr(ni, x, bp, e, addr);
			
			// if using device, set device number
			if(opcode==0xD8 || opcode==0xDC || opcode==0xE0){
				if(ni==1) device = (byte)targetAddress;
				else device = rManager.getMemory(targetAddress,1)[0];
			}
		}
		else
			targetAddress = 0;
		
		
		// update section information
		Section s = loader.search_section(currentLocation);
		programName = s.getName();
		startAddr = s.getStartAddr();
		programLength = s.getLength();
	}
	
	public boolean executeInstruction(){
		
		int opcode = curInst[0]&0xFC;
		
		int ni=0, bp=0, addr=0;
		boolean x=true, e=true;
		
		if(curInst.length>2){
			ni 	= curInst[0]&0x03;
			x 	= (((curInst[1]>>7) & 0x01) > 0) ? true : false;
		 	bp 	= (curInst[1]>>5) & 0x03;
		 	e 	= (((curInst[1]>>4) & 0x01) > 0) ? true : false;
		 	
		 	addr += curInst[1]&0x0F;
			for(int i=2; i<curInst.length; i++)
				addr = addr<<8 | curInst[i];
			
			// calculate target address
			targetAddress = calTargetAddr(ni, x, bp, e, addr);
			
			// error check
			if(ni==0) {
				JOptionPane.showMessageDialog(null, "Not SIC/XE Machine code","Error!",JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else if(bp==3){
				JOptionPane.showMessageDialog(null, "Cannot select both of Base/PC relative","Error!",JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
		}
		
		if(targetAddress == ResourceManager.EXITADDR) return false;
		
		
		byte[] bytedata, tmp;
		int res;
		
		switch(opcode){
		case 0x00 :		// LDA
			if(ni==1) rManager.setRegister(ResourceManager.A, targetAddress);
			else {
				res = ByteToInt(rManager.getMemory(targetAddress, 3));
				rManager.setRegister(ResourceManager.A, res);
			}
			break;
			
		case 0x0C : 	// STA
			bytedata = IntToByte(rManager.getRegister(ResourceManager.A));
			rManager.setMemory(targetAddress, bytedata, 3);
			break;
			
		case 0x10 :		// STX
			bytedata = IntToByte(rManager.getRegister(ResourceManager.X));
			rManager.setMemory(targetAddress, bytedata, 3);
			break;
			
		case 0x14 : 	// STL
			bytedata = IntToByte(rManager.getRegister(ResourceManager.L));
			rManager.setMemory(targetAddress, bytedata, 3);
			break;
			
		case 0x28 : 	// COMP
			if(ni==1) {
				int a = rManager.getRegister(ResourceManager.A);
				if(targetAddress == a) rManager.setZeroFlag();
				else if(targetAddress < 0) rManager.setSignFlag(true);
				else rManager.setSignFlag(false);
			}
			else{
				res = ByteToInt(rManager.getMemory(targetAddress, 3));
				int a = rManager.getRegister(ResourceManager.A);
				if(res == a) rManager.setZeroFlag();
				else if(res < 0) rManager.setSignFlag(true);
				else rManager.setSignFlag(false);
			}
			break;
			
		case 0x30 : 	// JEQ
			res = rManager.getRegister(ResourceManager.SW);
			if((res&0x100)>0)
				rManager.setRegister(ResourceManager.PC, targetAddress);
			rManager.resetSW();
			break;
			
		case 0x38 : 	// JLT
			res = rManager.getRegister(ResourceManager.SW);
			if((res&0x10)>0)
				rManager.setRegister(ResourceManager.PC, targetAddress);
			rManager.resetSW();
			break;
			
		case 0x3C : 	// J
			rManager.setRegister(ResourceManager.PC, targetAddress);
			break;
			
		case 0x48 : 	// JSUB
			rManager.setRegister(ResourceManager.L, rManager.getRegister(ResourceManager.PC));
			rManager.setRegister(ResourceManager.PC, targetAddress);
			break;
			
		case 0x4C : 	// RSUB
			rManager.setRegister(ResourceManager.PC, rManager.getRegister(ResourceManager.L));
			break;
			
		case 0x50 : 	// LDCH
			if(ni==1) rManager.setRegister(ResourceManager.A, targetAddress);
			else {
				res = ByteToInt(rManager.getMemory(targetAddress, 1)) & 0x0000FF;
				rManager.setRegister(ResourceManager.A, res);
			}
			break;
			
		case 0x54 : 	// STCH
			bytedata = IntToByte(rManager.getRegister(ResourceManager.A));
			tmp = new byte[]{bytedata[2]};
			rManager.setMemory(targetAddress, tmp, 1);
			break;
			
		case 0x74 : 	// LDT
			if(ni==1) rManager.setRegister(ResourceManager.T, targetAddress);
			else {
				res = ByteToInt(rManager.getMemory(targetAddress, 3));
				rManager.setRegister(ResourceManager.T, res);
			}
			break;
			
		case 0xA0 : 	// COMPR
			int r1 = rManager.getRegister((curInst[1]&0xF0)>>4);
			int r2 = rManager.getRegister(curInst[1]&0x0F);
			
			if(r1 == r2) rManager.setZeroFlag();
			else if(r1 < r2) rManager.setSignFlag(false);
			else rManager.setSignFlag(true);
			
			break;
			
		case 0xB4 : 	// CLEAR
			rManager.setRegister((curInst[1]&0xF0)>>4, 0);
			break;
			
		case 0xB8 : 	// TIXR
			int reg_x = rManager.getRegister(ResourceManager.X);
			int comp = rManager.getRegister((curInst[1]&0xF0)>>4);
			
			if(reg_x == comp) rManager.setZeroFlag();
			else if(reg_x < comp) rManager.setSignFlag(false);
			else rManager.setSignFlag(true);
			
			rManager.setRegister(ResourceManager.X, rManager.getRegister(ResourceManager.X)+1);
			
			break;
		
		case 0xD8 :		// RD
			if(ni==1) device = (byte)targetAddress;
			else device = rManager.getMemory(targetAddress,1)[0];
			
			byte[] tmp11 = rManager.readDevice(String.format("%08X", device).substring(6), 1);
			if(tmp11==null) {
				res = rManager.getRegister(ResourceManager.A) & 0xFFFF00;
				break;
			}

			res = rManager.getRegister(ResourceManager.A) & 0xFFFF00;
			res |= rManager.readDevice(String.format("%08X", device).substring(6), 1)[0] & 0x0000FF;
			rManager.setRegister(ResourceManager.A, res);
			
			break;
			
		case 0xDC : 	// WD
			if(ni==1) device = (byte)targetAddress;
			else device = rManager.getMemory(targetAddress,1)[0];
			
			bytedata = IntToByte(rManager.getRegister(ResourceManager.A));
			tmp = new byte[]{bytedata[2]};
			
			rManager.writeDevice(String.format("%08X", device).substring(6), tmp, 1);
			break;
			
		case 0xE0 : 	// TD
			if(ni==1) device = (byte)targetAddress;
			else device = rManager.getMemory(targetAddress,1)[0];
			
			if(!rManager.initialDevice(String.format("%08X", device).substring(6))) {
				rManager.setZeroFlag();
			}
			
			break;
			
		default : 
			JOptionPane.showMessageDialog(null, "Undefined Instruction","Error!",JOptionPane.ERROR_MESSAGE);
			return false;
				
		}
		
		return true;
	}
	
	private int calTargetAddr(int ni, boolean x, int bp, boolean e, int disp){
		
		int res = disp;
		
		if(bp==1){
			// pc relative -> positive/negative offset
			if(!e && (disp&0x800)>0) disp |= 0xFFFFF000;
			res += rManager.getRegister(ResourceManager.PC);
		}
		else if(bp==2){
			// base relative -> positive offset
			res += rManager.getRegister(ResourceManager.B);
		}
		
		if(x) res += rManager.getRegister(ResourceManager.X);
		
		if(res == ResourceManager.EXITADDR) return res;

		if(ni==2){
			// indirect addressing
			byte[] tmp = rManager.getMemory(res, 3);
			res = 0;
			for(int i=0; i<3; i++)
				res = res<<8 | tmp[i];
		}
		
		return res;
	}
	
	public void oneStep(){
		
		if(currentLocation==ResourceManager.EXITADDR) {
			JOptionPane.showMessageDialog(null, "Program Exited","Message",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// execute instruction
		if(!executeInstruction()) {
			JOptionPane.showMessageDialog(null, "Program Exited","Message",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// next instruction
		nextInstruction();
		
	}
	
	public void allStep(){
		
		while(currentLocation!=ResourceManager.EXITADDR){
			
			// execute instruction
			if(!executeInstruction()) break;
			
			// next instruction
			nextInstruction();
		}

		JOptionPane.showMessageDialog(null, "Program Exited","Message",JOptionPane.INFORMATION_MESSAGE);
		
	}

	
	
	private int ByteToInt(byte[] value){
		int data = 0;
		for(int i=0; i<value.length; i++)
			data = data<<8 | value[i];
		return data;
	}
	
	private byte[] IntToByte(int value){
		byte[] data = new byte[3];
		
		data[0] = (byte)(value>>16);
		data[1] = (byte)(value>>8);
		data[2] = (byte)value;		
		
		return data;
	}
		
	public String getCurrentInstruction() {

		if(curInst==null) return "";
		
		String str = instTab.getInstName((byte)(curInst[0]&0xFC));
		
		if(curInst.length==2) {
			str += " " + rManager.getRegisterName((curInst[1]>>4 & 0x0F));
			
			if(instTab.getOperandNum((byte)(curInst[0]&0xFC))==2)
				str += ", " + rManager.getRegisterName(curInst[1] & 0x0F);
		}
		
		return str;
	}

	
	public String getProgramName() {
		return programName;
	}

	public int getStartAddr() {
		return startAddr;
	}

	public int getProgramLength() {
		return programLength;
	}

	public int getReturnAddr(){
		return loader.returnAddr;
	}
	
	public String getMemory(){
		return rManager.getMemory();
	}


	public int getCurrentLocation() {
		// TODO Auto-generated method stub
		return currentLocation;
	}

	public int getTargetAddr() {
		// TODO Auto-generated method stub
		return targetAddress;
	}

	public int getCurrentLength() {
		// TODO Auto-generated method stub
		return curInst.length;
	}

	public byte getDevice() {
		// TODO Auto-generated method stub
		return device;
	}
	
}
