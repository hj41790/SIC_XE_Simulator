import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class SicLoader {
	
	private ResourceManager rManager;
	
	private ArrayList<Symbol> SYMTAB;
	private ArrayList<Modification> MODTAB;
	private ArrayList<Section> SECTAB;
	
	public String programName;
	public int returnAddr;
	public int startAddr;
	public int length;
	
	
	public boolean load(File objFile, ResourceManager rMgr){

		rManager = rMgr;
		SYMTAB = new ArrayList<Symbol>();
		MODTAB = new ArrayList<Modification>();
		SECTAB = new ArrayList<Section>();
		
		try {
			
			FileReader fr = new FileReader(objFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			while((line=br.readLine())!=null){
				
//				System.out.println(line);
				
				if(line.equals("")) continue;
				else if(line.charAt(0)=='H'){
					H_header(line);
				}
				else if(line.charAt(0)=='D'){
					D_header(line);
				}
				else if(line.charAt(0)=='R'){
					R_header(line);
				}
				else if(line.charAt(0)=='T'){
					T_header(line);
				}
				else if(line.charAt(0)=='M'){
					M_header(line);
				}
				else if(line.charAt(0)=='E'){
					E_header(line);
				}
				else{
					JOptionPane.showMessageDialog(null, "Object Code Worng Foramt","Error!",JOptionPane.ERROR_MESSAGE);
					return false;
				}
			
			}

/*			
			for(Symbol s : SYMTAB)
				System.out.println(s.getName() + "  " + String.format("%06X", s.getAddr()));
			
			System.out.println("\n");
			
			for(Modification m : MODTAB)
				m.print();
*/		

			modify_all();
			
			rMgr.setRegister(ResourceManager.L, ResourceManager.EXITADDR); 	// means program exit
			rMgr.setRegister(ResourceManager.PC, returnAddr);
			
			Section s = search_section(returnAddr);
			programName = s.name;
			startAddr = s.startAddr;
			length = s.length;
			
			br.close();
			fr.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
	
	public Section search_section(int addr){
		for(Section s : SECTAB){
			if(s.startAddr <= addr && s.startAddr+s.length > addr)
				return s;
		}
		return null;
	}
	
	private void modify_all(){
		
		int value;
		int location;
		int size;
		
		for(Modification m : MODTAB){
			
			value = search_symbol(m.getSymbol()).getAddr();
			location = m.getLocation();
			size = m.getSize();
			
			// 수정레코드 적용
			rManager.modifyMemory(location, value, size, m.getOperation());
		}
		
		
	}
	
	
	private void H_header(String line){

		String name = trim(line.substring(1, 7));
		String addr = line.substring(7,13);
		String programLength = line.substring(13, 19);
		
		programName = name;
		length 		= Integer.parseInt(programLength, 16);
		startAddr 	= rManager.malloc(length);
		
		// GUI 출력을 위해 섹션 정보 저장
		SECTAB.add(new Section(name, startAddr, length));
		
		// 프로그램(섹션) 이름으로 심볼 저장
		Symbol s;
		if((s=search_symbol(programName))==null)
			SYMTAB.add(new Symbol(programName, startAddr + Integer.parseInt(addr, 16)));
		else
			s.setAddr(startAddr + Integer.parseInt(addr, 16));
			
		
	}

	private void D_header(String line){
		
		for(int i=1; i<line.length(); i+=12){
			String name = trim(line.substring(i, i+6));
			int addr = startAddr + Integer.parseInt(line.substring(i+6, i+12), 16);
			
			Symbol s;
			if((s=search_symbol(name))==null)
				SYMTAB.add(new Symbol(name, addr));
			else
				s.setAddr(addr);	// 이미 존재하면 주소값만 설정
		}
		
		
	}

	private void R_header(String line){
		
		for(int i=1; i<line.length(); i+=6){
			String name = trim(line.substring(i, i+6));
			Symbol s;
			if((s=search_symbol(name))==null)
				SYMTAB.add(new Symbol(name, -1));	// 존재하지 않으면 일단 이름만 등록
		}
	}

	private void T_header(String line){

		int start = Integer.parseInt(line.substring(1,7),16);
		int size = Integer.parseInt(line.substring(7,9),16);
		
		char c1, c2;
		byte[] data = new byte[size];
		for(int i=0; i<size; i++){
			
			// 2글자씩 묶기
			c1 = line.charAt(9 + 2*i);
			c2 = line.charAt(10 + 2*i);
			
			if(c1>='0' && c1<='9') c1 -= '0';
			else c1 = (char) (c1 - 'A' + 10);

			if(c2>='0' && c2<='9') c2 -= '0';
			else c2 = (char) (c2 - 'A' + 10);
			
			data[i] = (byte)(c1*16 + c2);
			
		}
		
		rManager.setMemory(startAddr + start, data, size);
		
	}

	private void M_header(String line){

		int location = startAddr + Integer.parseInt(line.substring(1, 7), 16);
		int size = Integer.parseInt(line.substring(7, 9), 16);
		boolean op = (line.charAt(9)=='+') ? true : false;
		String name = trim(line.substring(10));
		
		// 모든 심볼정보가 모인 뒤 한 번에 수정
		MODTAB.add(new Modification(location, size, op, name));
		
	}

	private void E_header(String line){
		if(line.length()>1){
			// return address가 존재하는 경우 설정
			returnAddr = startAddr + Integer.parseInt(line.substring(1),16);
		}
	}
	
	
	/* 이름으로 심볼을 찾아 반환 */
	private Symbol search_symbol(String name){
		for(Symbol s : SYMTAB)
			if(s.getName().equals(name)) return s;
		return null;
	}
	
	/* 심볼 이름 끝의 공백 제거 */
	private String trim(String str){
		
		String result = "";
		for(char c : str.toCharArray()){
			if(c==' ') break;
			result += c;
		}
		return result;
	}
}
