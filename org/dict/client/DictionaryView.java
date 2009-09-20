package org.dict.client;

import org.dict.server.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.event.*;
import java.awt.datatransfer.*;
import org.dict.kernel.*;

public class DictionaryView {
	Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	String value = "";//getClipboardText(clipboard);
	IDictEngine engine = new DictEngine();
	JFrame fFrame;
	JTextPane fDisplay;
	//JCheckBox mode;
	JTextField inputField;
	JButton dbAdd;
	//JTextField statusField;
	//JList matches;
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
			lookup(getInputField().getText(), null);
		}
	}

	class DBAdd implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			addDatabases();
		}
	}
 
	class LinkFollower implements HyperlinkListener {

		private JEditorPane pane;

		public LinkFollower(JEditorPane pane) {
			this.pane = pane;
		}

		public void hyperlinkUpdate(HyperlinkEvent evt) {

			if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String desc = evt.getDescription();
				IRequest req = new SimpleRequest("", desc);
				String word = req.getParameter("word");
				String pos = req.getParameter("pos");
				lookup(word, pos);
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
			lookup(getClipboardText(clipboard), null);
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
    
	public DictionaryView() {
		super();
		initialize();
	}
	protected JTextPane getDisplay() {
		return fDisplay;
	}
	protected JFrame getFrame() {
		return fFrame;
	}
	protected JTextField getInputField() {
		return inputField;
	}

	private void initialize() {
		fFrame = new JFrame("Intelligent dictionary lookup");
		fFrame.setSize(600, 400);
		fDisplay = new JTextPane();
		fDisplay.setBackground(Color.white);
		fDisplay.setForeground(Color.black);
		fDisplay.setEditable(false);
		fDisplay.setContentType("text/html");
		//fDisplay.setEditorKit(new HTMLEditorKit());
		fDisplay.addHyperlinkListener(new LinkFollower(fDisplay));
		JScrollPane scrollPane = new JScrollPane(fDisplay);
		inputField = new JTextField(15);
		inputField.addActionListener(new AL());
		db = new JComboBox();
		initDatabases();
		dbAdd = new JButton("Add DB");
		dbAdd.addActionListener(new DBAdd());
		//db.addItem("Any database");
		String[] faces = new String[]{"Arial", "Verdana", "Courier New", "Arial Unicode MS"};
		fontChoice = new JComboBox(faces);
		fontChoice.setEditable(true);
		fontChoice.addItemListener(new IL());
		sizeChoice = new JComboBox(new String[]{"10", "12", "14", "16", "18"});
		//sizeChoice.setEditable(true);
		sizeChoice.addItemListener(new IL());
		JPanel north = new JPanel();
		north.setBackground(Color.lightGray);
		//north.add(mode);
		//north.add(prev);
		north.add(inputField);
		north.add(db);
		//JPanel south = new JPanel();
		//south.add(statusField);
		north.add(fontChoice);
		north.add(sizeChoice);
		north.add(dbAdd);
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(north, "North");
		jp.add(scrollPane, "Center");
		//jp.add(south, "South");
		//jp.add(scrollList, "West");
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

	protected void lookup(String s, String position) {
		if (engine == null) {
			getDisplay().setText("Please add databases first!");
			return;
		}
		if (position == null && (s == null || s.length() == 0 || s.length() > 25 || s.equals(value))) {
			return;
		}
		history[pos++ % history.length] = s;
		//statusField.setText("Lookup: "+s);
		getFrame().toFront();
		inputField.setText(s);
		value = s;
		String dbID = "*";
		int dbIndex = db.getSelectedIndex();
		if (dbIndex >= 0 && dbIndex < engine.getDatabases().length) {
			dbID = engine.getDatabases()[dbIndex].getID();
		}
		IAnswer[] a = engine.defineMatch(dbID, s, position, true, IDatabase.STRATEGY_NONE);
		StringWriter w = new StringWriter();
		PrintWriter out = new PrintWriter(w);
		IRequest req = new SimpleRequest("", "fmt=u&word="+s);
		try {
			org.dict.kernel.HTMLPrinter.printAnswers(engine, req, a, true, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace(out);
		}
		getDisplay().setText(w.toString());
		getDisplay().setCaretPosition(0);
	}
	
	public static void main(String[] args) {
		DictionaryView me = new DictionaryView();
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
		DatabaseFactory.addDatabases(engine, ini);
		initDatabases();
	}
	
	private void setFont() {
		String face = (String) fontChoice.getSelectedItem();
		String size = (String) sizeChoice.getSelectedItem();
		Font fnt = new Font(face, Font.PLAIN, Integer.parseInt(size));
		getDisplay().setFont(fnt);
		getInputField().setFont(fnt);
		/*
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setFontFamily(set, face);
		StyleConstants.setFontSize(set, Integer.parseInt(size));
		String s = getDisplay().getText();
		try {
			getDisplay().getDocument().remove(0, s.length());
			getDisplay().getDocument().insertString(0, s, set);
		} catch (Exception e) {
		}
		*/
	}

}
