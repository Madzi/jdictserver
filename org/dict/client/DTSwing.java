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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.dict.kernel.DictEngine;
import org.dict.kernel.IAnswer;
import org.dict.kernel.IDatabase;
import org.dict.kernel.IDictEngine;
import org.dict.kernel.IRequest;
import org.dict.kernel.SimpleRequest;
import org.dict.server.DatabaseFactory;

public class DTSwing {
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

	class IL implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			setFont();
		}
	}

	class ML extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				lookup(getSelectedText());
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
			lookup(getClipboardText(clipboard));
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
		DTSwing me = new DTSwing();
		for (int i = 0; i < args.length; i++) {
			me.addDatabases(args[i]);
		}
		// me.startRunner();
	}

	Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	String value = "";// getClipboardText(clipboard);
	IDictEngine engine = new DictEngine();
	JFrame fFrame;

	JTextArea fDisplay;

	// JCheckBox mode;
	JTextField inputField;

	JButton dbAdd;

	// JTextField statusField;
	JList matches;
	JComboBox db, fontChoice, sizeChoice;
	String[] history = new String[20];

	int pos = 0;

	// int fontSize = Integer.getInteger("fontSize", 12).intValue();
	// int delay = Integer.getInteger("delay", 20).intValue();
	/**
	 * ClipboardTest constructor comment.
	 */
	public DTSwing() {
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
		addDatabases(ini);
	}

	private void addDatabases(String ini) {
		DatabaseFactory.addDatabases(engine, ini);
		initDatabases();
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
		fDisplay = new JTextArea();
		fDisplay.setBackground(Color.white);
		fDisplay.setForeground(Color.black);
		// fDisplay.setPreferredSize(new Dimension(600, 400));
		fDisplay.addMouseListener(new ML());
		JScrollPane scrollPane = new JScrollPane(fDisplay);
		inputField = new JTextField(10);
		inputField.addActionListener(new AL());
		db = new JComboBox();
		initDatabases();
		dbAdd = new JButton("Add DB");
		dbAdd.addActionListener(new DBAdd());
		// db.addItem("Any database");
		fontChoice = new JComboBox(new String[] { "Arial", "Verdana", "Courier New", "Arial Unicode MS" });
		fontChoice.setEditable(true);
		fontChoice.addItemListener(new IL());
		sizeChoice = new JComboBox(new String[] { "10", "12", "14", "16", "18" });
		// sizeChoice.setSize(fontChoice.getWidth()/2, fontChoice.getHeight());
		// sizeChoice.setEditable(true);
		sizeChoice.addItemListener(new IL());
		JPanel north = new JPanel();
		north.setBackground(Color.lightGray);
		// north.add(mode);
		// north.add(prev);
		north.add(db);
		// JPanel south = new JPanel();
		// south.add(statusField);
		north.add(fontChoice);
		north.add(sizeChoice);
		north.add(dbAdd);
		JPanel center = new JPanel(new BorderLayout());
		center.add(north, BorderLayout.NORTH);
		center.add(scrollPane, BorderLayout.CENTER);
		JPanel west = new JPanel(new BorderLayout());
		// jp.add(south, "South");
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
		// db.setSelectedItem("Any database");
		fontChoice.setSelectedItem("Verdana");
		sizeChoice.setSelectedItem("12");
		// fFrame.pack();
		fFrame.setVisible(true);
	}

	protected void lookup(String s) {
		if (s == null || s.length() == 0 || s.length() > 25 || s.equals(value)) {
			return;
		}
		if (engine == null || engine.getDatabases().length == 0) {
			// getDisplay().setText("Please add databases first!");
			return;
		}
		history[pos++ % history.length] = s;
		// statusField.setText("Lookup: "+s);
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
		IRequest req = new SimpleRequest("", "word=" + s);
		try {
			org.dict.kernel.answer.printer.PlainPrinter.printAnswers(engine, req, a, true, out);
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
	}
}
