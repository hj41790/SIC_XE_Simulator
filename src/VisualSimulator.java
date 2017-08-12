import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class VisualSimulator {

	JFrame frame;
	JTextField filename;
	
	JTextField H_name, H_start_addr, H_length;
	JTextField E_addr;	
	JTextField Register_F, Register_SW;
	JTextField[] Register_dec;
	JTextField[] Register_hex;
	JTextField start_memory, target_addr;
	JTextField device;
	JTextField currentInstruction;

	JTextArea memory;
	Highlighter hl;
	
	JButton btn_open, btn_onestep, btn_allstep, btn_reset, btn_close;
	
	File openFile;
	SicSimulator simulator;
	ResourceManager rManager;
	
	int start, end;
	
	
	public VisualSimulator(){
		
		init_GUI();
	}
	
	public void initialize(File objFile, ResourceManager rMgr){
		
		rManager = rMgr;
		
		simulator = new SicSimulator();
		if(!simulator.initialize(objFile, rMgr)) return;
		
		memory.setText(simulator.getMemory());
		hl = memory.getHighlighter();
		
		update();
	}
	
	public void oneStep(){
		
		if(openFile == null) return;

		simulator.oneStep();
		
		update();
	}
	
	public void allStep(){

		if(openFile == null) return;

		simulator.allStep();
		
		update();
	}
	
	public void update(){
		
		// set header record panel value
		H_name.setText(simulator.getProgramName());
		H_start_addr.setText(String.format("%08X",simulator.getStartAddr()).substring(2));
		H_length.setText(String.format("%X",simulator.getProgramLength()));
		
		// set registers' values 
		for(int i=0; i<10; i++){
			int reg = rManager.getRegister(i);
			
			if(i==7) continue;
			else if(i==6) {
				Register_F.setText(String.format("%08X", reg).substring(2));
			}
			else if(i==9) {
				Register_SW.setText(String.format("%08X", reg).substring(2));
			}
			else{
				Register_dec[i].setText(Integer.toString(reg));
				Register_hex[i].setText(String.format("%08X", reg).substring(2));
			}
		}
		
		// set return addr
		E_addr.setText(String.format("%08X",simulator.getReturnAddr()).substring(2));
		
		// set additional data
		start_memory.setText(String.format("%08X", simulator.getCurrentLocation()).substring(2));
		target_addr.setText(String.format("%08X", simulator.getTargetAddr()).substring(2));
		device.setText(String.format("%02X", simulator.getDevice()));
		
		// set current instruction
		currentInstruction.setText(simulator.getCurrentInstruction());
		
		// set memory
		memory.setText(simulator.getMemory());
		
		// highlight current instruction
		int current = simulator.getCurrentLocation()*2;
		int length = simulator.getCurrentLength()*2;
		
		int row = current/32;
		int col = current%32;
		int space = 9 + col/8;
		
		int start = row*45 + col + space;
		int end = start + length;
		int start1 = -1;
		int end1 = -1;
		
		
		if(start%45 + length > 45){
			// different row
			end = (row + 1) * 45;
			length -= (44 - start%45);
			start1 = (row+1)*45 + 9;
			end1 = start1 + length;
		}
		else if(col/8 != (col+length)/8 && (col+length)%8!=0) {
			// different column -> space+1
			end++; 
		}
		
		try {
			
			memory.requestFocus();
			memory.setCaretPosition(start);
			
			hl.removeAllHighlights();
			hl.addHighlight(start, end, DefaultHighlighter.DefaultPainter);
			if(start1>=0)
				hl.addHighlight(start1, end1, DefaultHighlighter.DefaultPainter);
			
			
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void init_GUI(){
		
		Color bgColor = new Color(250,250,252);
		
		frame = new JFrame();
		frame.setLocation(700, 150);
		frame.setSize(600, 850);
		frame.setBackground(new Color(255,255,255));
		frame.setResizable(false);
		frame.setTitle("SIC/XE Machine Simulator (made by CHJ)");
		
		filename = new JTextField(25);
		filename.setEditable(false);
		filename.setBackground(Color.WHITE);
		
		H_name = new JTextField(6);
		H_name.setEditable(false);
		H_name.setBackground(Color.white);
		
		H_start_addr = new JTextField(6);
		H_start_addr.setEditable(false);
		H_start_addr.setBackground(Color.WHITE);
		
		H_length = new JTextField(6);
		H_length.setEditable(false);
		H_length.setBackground(Color.white);
		
		E_addr = new JTextField(20);
		E_addr.setEditable(false);
		E_addr.setBackground(Color.white);
		E_addr.setHorizontalAlignment(JTextField.CENTER);
		
		start_memory = new JTextField(10);
		start_memory.setEditable(false);
		start_memory.setBackground(Color.white);
		
		target_addr = new JTextField(10);
		target_addr.setEditable(false);
		target_addr.setBackground(Color.white);
		
		device = new JTextField(10);
		device.setEditable(false);
		device.setBackground(Color.white);
		
		currentInstruction = new JTextField(20);
		currentInstruction.setEditable(false);
		currentInstruction.setHorizontalAlignment(JTextField.CENTER);
		currentInstruction.setBackground(Color.WHITE);
		
		Register_F = new JTextField(13);
		Register_F.setEditable(false);
		Register_F.setBackground(Color.white);
		
		Register_SW = new JTextField(13);
		Register_SW.setEditable(false);
		Register_SW.setBackground(Color.white);
		
		Register_dec = new JTextField[10];
		Register_hex = new JTextField[10];
		for(int i=0; i<10; i++){
			Register_dec[i] = new JTextField(6);
			Register_dec[i].setEditable(false);
			Register_dec[i].setBackground(Color.white);
			
			Register_hex[i] = new JTextField(6);
			Register_hex[i].setEditable(false);
			Register_hex[i].setBackground(Color.white);
		}
		
		memory = new JTextArea();
		memory.setEditable(false);
		memory.setFont(new Font("Consolas",Font.PLAIN,15));

		JScrollPane scroll2 = new JScrollPane();
		scroll2.setPreferredSize(new Dimension(550,300));
		scroll2.setBackground(Color.white);
		scroll2.setViewportBorder(BorderFactory.createEmptyBorder(3,7,3,3));
		scroll2.setViewportView(memory);
		
		btn_open = new JButton("Open");
		btn_open.setPreferredSize(new Dimension(70, 25));
		btn_open.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				FileDialog dialog = new FileDialog(frame,"Open",FileDialog.LOAD);
				dialog.setDirectory(".");
				dialog.setVisible(true);
				

				if(dialog.getFile()==null) return;
				
				filename.setText(dialog.getFile());
				
				System.out.println(dialog.getDirectory()+dialog.getFile());
				
				openFile = new File(dialog.getDirectory()+dialog.getFile());

				// initialize
				initialize(openFile, new ResourceManager());
			}
		});
		
		btn_reset = new JButton("Reset");
		btn_reset.setPreferredSize(new Dimension(70, 25));
		btn_reset.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				if(openFile != null)
					initialize(openFile, new ResourceManager());
				
			}
		});
		
		btn_onestep = new JButton("One Step");
		btn_onestep.setHorizontalAlignment(JButton.CENTER);
		btn_onestep.setPreferredSize(new Dimension(90,50));
		btn_onestep.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
				oneStep();
			}
		});
		
		btn_allstep = new JButton("All Step");
		btn_allstep.setHorizontalAlignment(JButton.CENTER);
		btn_allstep.setPreferredSize(new Dimension(90,50));
		btn_allstep.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
				
				allStep();
			}
		});
		
		btn_close = new JButton("Close");
		btn_close.setPreferredSize(new Dimension(90,50));
		btn_close.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
		
		
		// file selection panel
		JPanel p_file = new JPanel();
		p_file.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		p_file.setBackground(bgColor);
		p_file.add(new Label("File name :"));
		p_file.add(filename);
		p_file.add(btn_open);
		p_file.add(btn_reset);
		
		// header record group box
		JPanel p_header = new JPanel();
		p_header.setBackground(bgColor);
		p_header.setLayout(new BoxLayout(p_header, BoxLayout.Y_AXIS));
		p_header.setBorder(BorderFactory.createTitledBorder(" H (Header Record) "));
		
		JPanel p_h1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		p_h1.setBackground(bgColor);
		p_h1.setBorder(BorderFactory.createEmptyBorder(3,15,0,20));
		p_h1.add(new Label("Program Name :"));
		p_h1.add(H_name);

		JPanel p_h2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		p_h2.setBackground(bgColor);
		p_h2.setBorder(BorderFactory.createEmptyBorder(0,15,0,20));
		p_h2.add(new Label("Start Address(Hex) :"));
		p_h2.add(H_start_addr);
		
		JPanel p_h3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		p_h3.setBackground(bgColor);
		p_h3.setBorder(BorderFactory.createEmptyBorder(0,15,3,20));
		p_h3.add(new Label("Length(Hex) :"));
		p_h3.add(H_length);
		
		p_header.add(p_h1);
		p_header.add(p_h2);
		p_header.add(p_h3);
		
		
		// Registers group box
		JPanel p_register = new JPanel();
		p_register.setBackground(bgColor);
        p_register.setLayout(new GridBagLayout()); 
		p_register.setBorder(BorderFactory.createTitledBorder(" Registers (Dec/Hex) "));
		
		String[] label_r1 = new String[]{	"A (#0) ", "X (#1) ", "L (#2) ", "B (#3) ", "S (#4) ",
				"T (#5) ", "F (#6) ", "", "PC (#8) ", "SW (#9) "};
		
       
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
		
		for(int i=0; i<10; i++){
			
			if(i==7) continue;
			else if(i==6){
				addGrid(p_register,gbc,new Label(label_r1[i],Label.RIGHT),0,i,1,1,0,0);
				addGrid(p_register,gbc,Register_F,1,i,2,1,0,0);
			}
			else if(i==9){
				addGrid(p_register,gbc,new Label(label_r1[i],Label.RIGHT),0,i,1,1,0,0);
				addGrid(p_register,gbc,Register_SW,1,i,2,1,0,0);
			} 
			else {
				addGrid(p_register,gbc,new Label(label_r1[i],Label.RIGHT),0,i,1,1,0,0);
				addGrid(p_register,gbc,Register_dec[i],1,i,1,1,0,0);
				addGrid(p_register,gbc,Register_hex[i],2,i,1,1,0,0);
			}
			
		}
		
		// end record group box
		JPanel p_end = new JPanel(new FlowLayout(FlowLayout.CENTER,0,10));
		p_end.setBackground(bgColor);
		p_end.setPreferredSize(new Dimension(130, 80));
		p_end.setBorder(BorderFactory.createTitledBorder(" E (End Record) "));
		p_end.add(new Label("Address of First Instruction in Object program", Label.CENTER));
		p_end.add(E_addr);
		
		// address panel
		JPanel p_addr = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p_addr.setBackground(bgColor);
		p_addr.setBorder(BorderFactory.createTitledBorder(" Additional Data "));
		p_addr.setPreferredSize(new Dimension(130, 120));
		
		JPanel p_a1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p_a1.setBackground(bgColor);
		p_a1.setBorder(BorderFactory.createEmptyBorder(3,0,0,20));
		p_a1.add(new Label("Start Memory Address"));
		p_a1.add(start_memory);
		
		JPanel p_a2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p_a2.setBackground(bgColor);
		p_a2.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));		
		p_a2.add(new Label("Target Address"));
		p_a2.add(target_addr);		
		
		JPanel p_a3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p_a3.setBackground(bgColor);
		p_a3.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));
		p_a3.add(new Label("Device Number"));
		p_a3.add(device);
		
		p_addr.add(p_a1);
		p_addr.add(p_a2);
		p_addr.add(p_a3);
		
		// instruction panel
		JPanel p_inst = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 0));
		p_inst.setBorder(BorderFactory.createEmptyBorder(20,0,10,0));
		p_inst.setBackground(bgColor);
		p_inst.add(new Label("Current Instruction"));
		p_inst.add(currentInstruction);
		
		
		// button panel
		JPanel p_btn = new JPanel();
		p_btn.setBackground(bgColor);
		p_btn.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		p_btn.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		p_btn.setPreferredSize(new Dimension(130, 50));
		p_btn.add(btn_onestep);
		p_btn.add(btn_allstep);
		p_btn.add(btn_close);

		// left center panel
		JPanel left = new JPanel();
		left.setBackground(bgColor);
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		left.setPreferredSize(new Dimension(250, 400));
		left.add(p_header);
		left.add(p_register);
		
		// right center panel
		JPanel right = new JPanel();
		right.setBackground(bgColor);
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		right.setPreferredSize(new Dimension(310, 400));
		right.add(p_end);
		right.add(p_addr);
		right.add(p_inst);
		right.add(p_btn);	
		
		// memory panel
		JPanel p_mem = new JPanel();
		p_mem.setBackground(bgColor);
		p_mem.setLayout(new BoxLayout(p_mem, BoxLayout.Y_AXIS));
		p_mem.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		p_mem.setPreferredSize(new Dimension(550,320));
		Label l = new Label("  Memory", Label.LEFT);
		l.setFont(new Font("",Font.BOLD, 12));
		p_mem.add(l);
		p_mem.add(scroll2);
		

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(bgColor);
		mainPanel.add(p_file);
		mainPanel.add(left);
		mainPanel.add(right);
		mainPanel.add(p_mem);
		
		frame.add(mainPanel);
		frame.setVisible(true);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new VisualSimulator();
		
	}
	
	private void addGrid(JPanel panel, GridBagConstraints gbc, Component c,  
            int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty) {
	
		
		gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        
        gbc.insets = new Insets(1,3,1,3);
        
        panel.add(c, gbc);
	}

}































