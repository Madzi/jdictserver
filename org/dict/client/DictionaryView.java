package org.dict.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.dict.kernel.DictEngine;
import org.dict.kernel.IAnswer;
import org.dict.kernel.IDatabase;
import org.dict.kernel.IDictEngine;
import org.dict.kernel.IRequest;
import org.dict.kernel.SimpleRequest;
import org.dict.server.DatabaseFactory;

public class DictionaryView {
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

	class IL implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			setFont();
		}
	}

	class LinkFollower implements HyperlinkListener {

		public LinkFollower() {
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
		@Override
		public void windowActivated(WindowEvent e) {
			inputField.requestFocus();
			try {
				Thread.sleep(100);
			} catch (Throwable t) {
			}
			lookup(getClipboardText(clipboard), null);
		}

		@Override
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			// inputField.setText("");
		}
	}

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

	public static void main(String[] args) {
		new DictionaryView();
		// me.startRunner();
	}

	Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	String value = "";// getClipboardText(clipboard);
	IDictEngine engine = new DictEngine();

	JFrame fFrame;

	JTextPane fDisplay;

	// JCheckBox mode;
	JTextField inputField;

	JButton dbAdd;
	// JTextField statusField;
	// JList matches;
	JComboBox db, fontChoice, sizeChoice;
	String[] history = new String[20];

	int pos = 0;

	// int fontSize = Integer.getInteger("fontSize", 12).intValue();
	// int delay = Integer.getInteger("delay", 20).intValue();
	public DictionaryView() {
		super();
		initialize();
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

	protected JTextPane getDisplay() {
		return fDisplay;
	}

	protected JFrame getFrame() {
		return fFrame;
	}

	protected JTextField getInputField() {
		return inputField;
	}

	private void initDatabases() {
		for (int i = 0; i < engine.getDatabases().length; i++) {
			String name = engine.getDatabases()[i].getName();
			if (name.length() > 20)
				name = name.substring(0, 17) + "...";
			db.addItem(name);
		}
	}

	private void initialize() {
		fFrame = new JFrame("Intelligent dictionary lookup");
		fFrame.setSize(600, 400);
		fDisplay = new JTextPane();
		fDisplay.setBackground(Color.white);
		fDisplay.setForeground(Color.black);
		fDisplay.setEditable(false);
		fDisplay.setContentType("text/html");
		// fDisplay.setEditorKit(new HTMLEditorKit());
		fDisplay.addHyperlinkListener(new LinkFollower());
		JScrollPane scrollPane = new JScrollPane(fDisplay);
		inputField = new JTextField(15);
		inputField.addActionListener(new AL());
		db = new JComboBox();
		initDatabases();
		dbAdd = new JButton("Add DB");
		dbAdd.addActionListener(new DBAdd());
		// db.addItem("Any database");
		String[] faces = new String[] { "Arial", "Verdana", "Courier New", "Arial Unicode MS" };
		fontChoice = new JComboBox(faces);
		fontChoice.setEditable(true);
		fontChoice.addItemListener(new IL());
		sizeChoice = new JComboBox(new String[] { "10", "12", "14", "16", "18" });
		// sizeChoice.setEditable(true);
		sizeChoice.addItemListener(new IL());
		JPanel north = new JPanel();
		north.setBackground(Color.lightGray);
		// north.add(mode);
		// north.add(prev);
		north.add(inputField);
		north.add(db);
		// JPanel south = new JPanel();
		// south.add(statusField);
		north.add(fontChoice);
		north.add(sizeChoice);
		north.add(dbAdd);
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(north, "North");
		jp.add(scrollPane, "Center");
		// jp.add(south, "South");
		// jp.add(scrollList, "West");
		fFrame.getContentPane().add(jp);
		fFrame.addWindowListener(new WL());
		// db.setSelectedItem("Any database");
		fontChoice.setSelectedItem("Verdana");
		sizeChoice.setSelectedItem("12");
		// fFrame.pack();
		fFrame.setVisible(true);
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
		// statusField.setText("Lookup: "+s);
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
		IRequest req = new SimpleRequest("", "fmt=u&word=" + s);
		try {
			org.dict.kernel.answer.printer.HTMLPrinter.printAnswers(engine, req, a, true, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace(out);
		}
		getDisplay().setText(w.toString());
		getDisplay().setCaretPosition(0);
	}

	private void setFont() {
		String face = (String) fontChoice.getSelectedItem();
		String size = (String) sizeChoice.getSelectedItem();
		Font fnt = new Font(face, Font.PLAIN, Integer.parseInt(size));
		getDisplay().setFont(fnt);
		getInputField().setFont(fnt);
		/*
		 * SimpleAttributeSet set = new SimpleAttributeSet();
		 * StyleConstants.setFontFamily(set, face);
		 * StyleConstants.setFontSize(set, Integer.parseInt(size)); String s =
		 * getDisplay().getText(); try { getDisplay().getDocument().remove(0,
		 * s.length()); getDisplay().getDocument().insertString(0, s, set); }
		 * catch (Exception e) { }
		 */
	}

}
