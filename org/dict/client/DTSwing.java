package org.dict.client;

import org.dict.server.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import org.dict.kernel.*;

public class DTSwing {
	Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	String value = "";//getClipboardText(clipboard);
	IDictEngine engine = new DictEngine();
	JFrame fFrame;
	JTextArea fDisplay;
	//JCheckBox mode;
	JTextField inputField;
	JButton dbAdd;
	//JTextField statusField;
	JList matches;
	JComboBox db, fontChoice, sizeChoice;
	String[] history = new String[20];
	int pos = 0;
	//int fontSize = Integer.getInteger("fontSize", 12).intValue();
	//int delay = Integer.getInteger("delay", 20).intValue();
	
	private static String getClipboardText(Clipboard c) {
		try {
			Transferable content = c.getContents(null);
			if (content == null) {
				return null;
			}
			String s = (String) content.getTransferData(DataFlavor.stringFlavor);
			return s;
		} catch (Exception e) {
			return null;
		}
	}

	class AL implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			lookup(getInputField().getText());
		}
	}

	class DBAdd implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			addDatabases();
		}
	}
 
    class ML extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                lookup(getSelectedText());
            }
        }
    }
    class WL extends WindowAdapter {
        public void windowActivated(WindowEvent e) {
        	inputField.requestFocus();
        	try {
				Thread.sleep(100);
			} catch (Throwable t) {
			}
	        lookup(getClipboardText(clipboard));
        }
        
        public void windowDeactivated(WindowEvent e) {
			//inputField.setText("");
        }
        public void windowClosing(WindowEvent e) {
	        System.exit(0);
        }
    }
    class IL implements ItemListener {
	    public void itemStateChanged(ItemEvent e) {
		    setFont();
	    }
    }
    
	/**
	 * ClipboardTest constructor comment.
	 */
	public DTSwing() {
		super();
		initialize();
	}
	protected JTextArea getDisplay() {
		return fDisplay;
	}
	protected JFrame getFrame() {
		return fFrame;
	}
	protected JTextField getInputField() {
		return inputField;
	}
	private String getSelectedText() {
		return getDisplay().getSelectedText();
	}
	private void initialize() {
		fFrame = new JFrame("Intelligent dictionary lookup");
		fFrame.setSize(600, 400);
		fDisplay = new JTextArea();
		fDisplay.setBackground(Color.white);
		fDisplay.setForeground(Color.black);
		//fDisplay.setPreferredSize(new Dimension(600, 400));
		fDisplay.addMouseListener(new ML());
		JScrollPane scrollPane = new JScrollPane(fDisplay);
		inputField = new JTextField(10);
		inputField.addActionListener(new AL());
		db = new JComboBox();
		initDatabases();
		dbAdd = new JButton("Add DB");
		dbAdd.addActionListener(new DBAdd());
		//db.addItem("Any database");
		fontChoice = new JComboBox(new String[]{"Arial", "Verdana", "Courier New", "Arial Unicode MS"});
		fontChoice.setEditable(true);
		fontChoice.addItemListener(new IL());
		sizeChoice = new JComboBox(new String[]{"10", "12", "14", "16", "18"});
		//sizeChoice.setSize(fontChoice.getWidth()/2, fontChoice.getHeight());
		//sizeChoice.setEditable(true);
		sizeChoice.addItemListener(new IL());
		JPanel north = new JPanel();
		north.setBackground(Color.lightGray);
		//north.add(mode);
		//north.add(prev);
		north.add(db);
		//JPanel south = new JPanel();
		//south.add(statusField);
		north.add(fontChoice);
		north.add(sizeChoice);
		north.add(dbAdd);
		JPanel center = new JPanel(new BorderLayout());
		center.add(north, BorderLayout.NORTH);
		center.add(scrollPane, BorderLayout.CENTER);
		JPanel west = new JPanel(new BorderLayout());
		//jp.add(south, "South");
		west.add(inputField, BorderLayout.NORTH);
		matches = new JList();
		matches.setPrototypeCellValue("1234567890");
		JScrollPane scrollList = new JScrollPane(matches);
		west.add(scrollList, BorderLayout.CENTER);
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(west, BorderLayout.WEST);
		jp.add(center, BorderLayout.CENTER);
		fFrame.getContentPane().add(jp);
		fFrame.addWindowListener(new WL());
		//db.setSelectedItem("Any database");
		fontChoice.setSelectedItem("Verdana");
		sizeChoice.setSelectedItem("12");
		//fFrame.pack();
		fFrame.setVisible(true);
	}
	
	private void initDatabases() {
		for (int i = 0; i < engine.getDatabases().length; i++){
			String name = engine.getDatabases()[i].getName();
			if (name.length() > 20) name = name.substring(0,17)+"...";
			db.addItem(name);
		}
	}

	protected void lookup(String s) {
		if (s == null || s.length() == 0 || s.length() > 25 || s.equals(value)) {
			return;
		}
		if (engine == null || engine.getDatabases().length == 0) {
			//getDisplay().setText("Please add databases first!");
			return;
		}
		history[pos++ % history.length] = s;
		//statusField.setText("Lookup: "+s);
		getFrame().toFront();
		inputField.setText(s);
		value = s;
		String dbID = "*";
		int dbIndex = db.getSelectedIndex();
		if (dbIndex < engine.getDatabases().length) {
			dbID = engine.getDatabases()[dbIndex].getID();
		}
		IAnswer[] a = engine.defineMatch(dbID, s, null, true, IDatabase.STRATEGY_NONE);
		StringWriter w = new StringWriter();
		PrintWriter out = new PrintWriter(w);
		IRequest req = new SimpleRequest("", "word="+s);
		try {
			org.dict.kernel.PlainPrinter.printAnswers(engine, req, a, true, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace(out);
		}
		getDisplay().setText(w.toString());
		getDisplay().setCaretPosition(0);
	}

	public static void main(String[] args) {
		DTSwing me = new DTSwing();
		for (int i = 0; i < args.length; i++) {
			me.addDatabases(args[i]);
		}
		//me.startRunner();
	}
	
	private void addDatabases() {
		JFileChooser fc = new JFileChooser(new File("."));
		fc.showOpenDialog(null);
		File f = fc.getSelectedFile();
		if (f == null) {
			return;
		}
		String ini = f.getAbsolutePath();
		addDatabases(ini);
	}
	
	private void addDatabases(String ini) {
		DatabaseFactory.addDatabases(engine, ini);
		initDatabases();
	}
	
	private void setFont() {
		String face = (String) fontChoice.getSelectedItem();
		String size = (String) sizeChoice.getSelectedItem();
		Font fnt = new Font(face, Font.PLAIN, Integer.parseInt(size));
		getDisplay().setFont(fnt);
		getInputField().setFont(fnt);
	}
}
