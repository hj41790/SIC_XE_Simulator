import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Device {
	
	String dev;
	FileInputStream is;
	FileOutputStream os;
	
	File f;
	FileReader reader;
	FileWriter writer;
	
	
	public boolean initialize(String devName){
		
		dev = devName;
		
		try {
			
			f = new File(devName+".txt");
			reader = new FileReader(f);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block

			try {
				
				writer = new FileWriter(devName+".txt");
			
			} catch (IOException e1) {
				// TODO Auto-generated catch block

				return false;
			}
			
			
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return true;
	}
	
	public boolean write(byte[] tmp){
		
		
		try {
	
			writer = new FileWriter(dev+".txt");
			
			System.out.println("write : "+(char)tmp[0]);
			
//			writer.write(String.format("%c", tmp[0]));
			writer.append(String.format("%c", tmp[0]));
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		
		
		return true;
	}
	
	public byte[] read(){
		
		byte[] res = null;
		
		
		try {
			int a = (int) reader.read();
			
			if(a<0) res = new byte[]{0x00};
			else res = new byte[]{(byte)a};
			

			System.out.println("read : "+a + "  "+ String.format("%X", (int)a)+ "  "+(char)res[0]);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block

			return null;
		}
		
		
		return res;
	}
	
	public void close(){
		try {
			reader.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
