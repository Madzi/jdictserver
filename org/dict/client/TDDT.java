package org.dict.client;

//import org.dict.server.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.dict.kernel.Database;
import org.dict.kernel.DatabaseConfiguration;
import org.dict.kernel.DictEngine;
import org.dict.kernel.IAnswer;
import org.dict.kernel.IDatabase;
import org.dict.kernel.IDictEngine;
import org.dict.kernel.IRequest;
import org.dict.kernel.IWordPosition;
import org.dict.kernel.SimpleRequest;

/**
 * Insert the type's description here.
 * Creation date: (28.02.2002 23:18:11)
 * @author:
 */
public class TDDT extends JFrame {
	
	class AL implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String s = getInputField().getText();
			if (matches.getSelectedIndex() >= 0) {
				s = matches.getSelectedValue().toString();
			}
			lookup(s, false);
		}
	}

	class DBAdd implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			addDatabases();
		}
	}
	
    class DL implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
			findMatches();
		}

		public void insertUpdate(DocumentEvent e) {
			findMatches();
		}

		public void removeUpdate(DocumentEvent e) {
			findMatches();
		}
    }
	
	class FontAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//setFont(this.face, this.style, this.size);
			fontChooser.setVisible(true);
			//setDisplayFont(fontChooser.getSelectedFont());
		}
	}
	
	class FontDialog extends JDialog implements ItemListener, ActionListener {
		JComboBox names = new JComboBox(FONT_NAMES);
		JComboBox sizes = new JComboBox(FONT_SIZES);
		JButton ok = new JButton("Close");
		public FontDialog(JFrame owner, String title) {
			super(owner, title);
			//currentFont = current;
			setModal(true);
			names.setSelectedItem("Verdana");
			names.setEditable(true);
			names.addItemListener(this);
			sizes.setSelectedItem("12");
			sizes.addItemListener(this);
			ok.addActionListener(this);
			JPanel up = new JPanel();
			up.add(names);
			up.add(sizes);
			JPanel low = new JPanel();
			low.add(ok);
			JPanel main = new JPanel(new GridLayout(0, 1));
			main.add(up);
			main.add(low);
			getContentPane().add(main);
		}
		
		public Dimension getPreferredSize() {
			return new Dimension(240, 120);
		}
		
		public void itemStateChanged(ItemEvent e) {
			String face = (String)names.getSelectedItem();
			int size = Integer.parseInt((String)sizes.getSelectedItem());
			setDisplayFont(new Font(face, Font.PLAIN, size));
		}

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

    class History implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showHistory();
        }
    }
    
    class IL implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				databaseChanged();
			}
		}
    }
    
    class InputMethodAL implements ActionListener {
		String mode;
		public InputMethodAL(String method) {
			mode = method;
		}
		public void actionPerformed(ActionEvent e) {
			setInputMethod(getInputField(), this.mode);
		}
    }
	
	class ListKL extends KeyAdapter {

		public void keyPressed(KeyEvent e) {
			int c = e.getKeyCode();
			if (c == KeyEvent.VK_DOWN || c == KeyEvent.VK_PAGE_DOWN) {
				if (matches.getSelectedIndex() >= matches.getLastVisibleIndex()) {
					e.consume();
					return;
				}
			} else if (c == KeyEvent.VK_ENTER && matches.getSelectedIndex() >= 0) {
				lookup(matches.getSelectedValue().toString(), false);
			}			
		}
	}
    
    class ListML extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2 && matches.getSelectedIndex() >= 0) {
				lookup(matches.getSelectedValue().toString(), false);
			}
		}
	}
    
    class LSL implements ListSelectionListener {
	   public void valueChanged(javax.swing.event.ListSelectionEvent e) {
		   lookup(matches.getSelectedValue().toString(), false);
	   }
    }
 
    class ML extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                lookup(getSelectedText(), false);
            }
        }
    }
	
	class ScrollAL implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			//System.out.println(scrollBar.getValue()+" / "+scrollBar.getMaximum());
			if (db.getSelectedIndex() < 0 || engine.getDatabases().length == 0) {
				return;
			}
			int size = engine.getDatabases()[0].getSize();
			int pos = scrollBar.getValue();
			String dbID = engine.getDatabases()[0].getID();
			IAnswer[] a = engine.defineMatch(dbID, null, ""+pos, false, IDatabase.STRATEGY_NONE);
			showMatches(a, false);
		}
	}
	
    class WL extends WindowAdapter {
        public void windowActivated(WindowEvent e) {
	        //lookup();
	        getInputField().requestFocus();
        }
        public void windowClosing(WindowEvent e) {
	        System.exit(0);
        }
    }
    
    static final String[] FONT_NAMES = new String[]{"Verdana", "Arial", "Courier New", "Arial Unicode MS"};
	static final String[] FONT_SIZES = new String[]{"10", "12", "14", "16", "20", "24"};
	
	private static String getClipboardText(Clipboard c) {
		Transferable content = c.getContents(null);
		if (content == null) {
			return null;
		}
		try {
			String s = (String) content.getTransferData(DataFlavor.stringFlavor);
			return s;
		} catch (Exception e) {
			return null;
		}
	}
	public static void main(String[] args) {
		String[] cfg = args;
		IDictEngine engine = new DictEngine();
		//Database.MAX_MATCHES = 60;
		TDDT runner = new TDDT(engine);
		for (int i = 0; i < cfg.length; i++){
			//runner.addDatabases(cfg[i]);
		}
		runner.loadSettings();
		runner.setTitle("TDDT");
		runner.setSize(640, 450);
		runner.validate();
		runner.setVisible(true);
	}
	
	Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	JComboBox db;
	JButton dbAdd;
	//int fontSize = Integer.getInteger("fontSize", 12).intValue();
	//int delay = Integer.getInteger("delay", 20).intValue();
	IDictEngine engine;
	java.util.Vector currentDatabases = new java.util.Vector();
	JTextArea fDisplay;
	//JFrame fFrame;
	String[] history = new String[20];
	JTextField inputField;
	//JTextField statusField;
	JList matches;
	JCheckBox mode;
	int pos = 0;
	JScrollBar scrollBar;
	JSplitPane splitPane;
	String value = getClipboardText(clipboard);
	//duc.util.gui.SwingVietKey vietKey;
	Hashtable vietKeys = new Hashtable();
	FontDialog fontChooser;
	/**
	 * ClipboardTest constructor comment.
	 */
	public TDDT(IDictEngine e) {
		super();
		this.engine = e;
		initialize();
	}
	/**
	 * ClipboardTest constructor comment.
	 */
	public TDDT(String cfg) {
		this(org.dict.server.DatabaseFactory.getEngine(cfg));
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
	
	private void addDatabases(String cfg) {
		DatabaseConfiguration[] cfgs = DatabaseConfiguration.readConfiguration(cfg);
		for (int i = 0; i < cfgs.length; i++) {
			currentDatabases.addElement(cfgs[i]);
		}
		int k = updateDatabaseList();
		if (k >= 0 && k < db.getItemCount()) {
			db.setSelectedIndex(k);
		}
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem item;
		fileMenu.add(item = new JMenuItem("Add database"));
		item.addActionListener(new DBAdd());
		fileMenu.add(item = new JMenuItem("Remove database"));
		fileMenu.add(new JSeparator());
		fileMenu.add(item = new JMenuItem("Exit"));
		item.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mb.add(fileMenu);
		JMenu settingMenu = new JMenu("Settings");
		JMenu inputMenu = new JMenu("Viet input");
		ButtonGroup groupInputMethod = new ButtonGroup();		
		JRadioButtonMenuItem imButton;
		inputMenu.add(imButton = new JRadioButtonMenuItem("None", true));
		groupInputMethod.add(imButton);
		imButton.addActionListener(new InputMethodAL("None"));
		inputMenu.add(imButton = new JRadioButtonMenuItem("Telex", false));
		groupInputMethod.add(imButton);
		imButton.addActionListener(new InputMethodAL("Telex"));
		inputMenu.add(imButton = new JRadioButtonMenuItem("VNI", false));
		groupInputMethod.add(imButton);
		imButton.addActionListener(new InputMethodAL("VNI"));
		inputMenu.add(imButton = new JRadioButtonMenuItem("VIQR", false));
		groupInputMethod.add(imButton);
		imButton.addActionListener(new InputMethodAL("VIQR"));
		settingMenu.add(inputMenu);
		settingMenu.add(item = new JMenuItem("Select font"));
		item.addActionListener(new FontAction());
		settingMenu.add(item = new JMenuItem("Save settings"));
		item.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveSettings();
			}
		});
		mb.add(settingMenu);
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(item = new JMenuItem("About"));
		final String about = "TDDT - Java program for accessing dictionary databases in DICT format\n\u00A9 2004 Ho Ngoc Duc (http://come.to/duc)";
		item.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, about, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		mb.add(helpMenu);
		return mb;
	}
	
	protected JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		JButton b;
		toolBar.add(b = new JButton("Clip"));
		toolBar.add(b = new JButton("History"));
		toolBar.add(b = new JButton("Back"));
		toolBar.add(b = new JButton("Forward"));
		return toolBar;
	}
	
	protected void findMatches() {
		String s = getInputField().getText();
		IAnswer[] a = engine.defineMatch("*", s, null, false, IDatabase.STRATEGY_NONE);
		showMatches(a, true);
	}
	
	protected JTextArea getDisplay() {
		return fDisplay;
	}
	protected JTextField getInputField() {
		return inputField;
	}
	private String getSelectedText() {
		return getDisplay().getSelectedText();
	}
	
	private void initialize() {
		fDisplay = new JTextArea();
		fDisplay.setBackground(Color.white);
		fDisplay.setForeground(Color.black);
		fDisplay.setEditable(false);
		fDisplay.addMouseListener(new ML());
		fDisplay.setLineWrap(true);
		fDisplay.setWrapStyleWord(true);
		fDisplay.setTabSize(4);
		//fDisplay.setBackground(Color.lightGray);
		JScrollPane scrollPane = new JScrollPane(fDisplay);
		inputField = new JTextField(12);
		inputField.addActionListener(new AL());
		inputField.getDocument().addDocumentListener(new DL());
		setInputMethod(inputField, "NONE");
		DefaultListModel model = new DefaultListModel();
		matches = new JList(model);
		matches.addMouseListener(new ListML());
		matches.addKeyListener(new ListKL());
		scrollBar = new JScrollBar();
		scrollBar.addAdjustmentListener(new ScrollAL());
		JScrollPane scrollList = new JScrollPane(matches);
		//JButton prev = new JButton("History");
		//prev.addActionListener(new History());
		dbAdd = new JButton("Add");
		dbAdd.addActionListener(new DBAdd());
		mode = new JCheckBox("Auto", false);
		db = new JComboBox();
		db.addItemListener(new IL());
		Font ff = db.getFont();
		//System.out.println(ff);
		db.setFont(new Font("Arial", ff.getStyle(),ff.getSize()));
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		JPanel north = new JPanel(new GridLayout(1, 0));
		//north.setLayout(new FlowLayout());
		//north.setBackground(Color.lightGray);
		JPanel wordPanel = new JPanel(fl);
		//wordPanel.add(new JLabel("Word:"));
		wordPanel.add(inputField);
		north.add(wordPanel);
		JPanel dbPanel = new JPanel();
		dbPanel.add(new JLabel("Database:"));
		dbPanel.add(db);
		//dbPanel.add(dbAdd);
		north.add(dbPanel);
		//north.add(createToolBar());
		JPanel left = new JPanel(new BorderLayout());
		//left.add(inputField, BorderLayout.NORTH);
		JTextField tf = new JTextField(12);
		tf.setEditable(false);
		left.add(tf, BorderLayout.SOUTH);
		left.add(matches, BorderLayout.CENTER);
		left.add(scrollBar, BorderLayout.EAST);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(left);
		splitPane.setRightComponent(scrollPane);
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(north, BorderLayout.NORTH);
		jp.add(splitPane, BorderLayout.CENTER);
		this.getContentPane().add(jp);
		JMenuBar mb = createMenuBar();
		this.setJMenuBar(mb);
		setDisplayFont(new Font("Verdana", Font.PLAIN, 12));
		fontChooser = new FontDialog(this, "Select font");
		fontChooser.setSize(240, 120);
		fontChooser.pack();
		this.addWindowListener(new WL());
		//fFrame.validate();
		//fFrame.pack();
		//fFrame.setVisible(true);
	}
	
	private KeyListener createVietKey(JTextComponent tc, String mode) {
		try {
			Class vkClass = Class.forName("duc.util.gui.SwingVietKey");
			Class[] params = new Class[]{String.class, JTextComponent.class};
			Constructor c = vkClass.getConstructor(params);
			Object[] args = {mode, tc};
			Object ret = c.newInstance(args);
			return (KeyListener) ret;
		} catch (Throwable t) {
			//t.printStackTrace();
			return null;
		}
	}
	
	private void lookup() {
		String s = getClipboardText(clipboard);
		if (s != null && !s.equals(this.value)) {
			this.value = s;
			lookup(s, false);
		}
	}
	protected void lookup(String s, boolean append) {
		if (s == null || s.length() == 0 || s.length() > 25) {
			return;
		}
		history[pos++ % history.length] = s;
		//statusField.setText("Lookup: "+s);
		//getFrame().toFront();
		IAnswer[] a = engine.defineMatch("*", s, null, true, IDatabase.STRATEGY_NONE);
		StringWriter w = new StringWriter();
		PrintWriter out = new PrintWriter(w);
		IRequest req = new SimpleRequest("", "word="+s);
		try {
			org.dict.kernel.answer.printer.PlainPrinter.printAnswers(engine, req, a, false, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace(out);
		}
		showMatches(a, true);
		//inputField.setText(s);
		if (append) {
			getDisplay().setText(getDisplay().getText()+w.toString());
		} else {
			getDisplay().setText(w.toString());
			getDisplay().setCaretPosition(0);
		}
	}
	
	void msg(String s) {
		getDisplay().setText(s);
	}
	
	private int updateDatabaseList() {
		int k = db.getSelectedIndex();
		db.removeAllItems();
		for (int i = 0; i < currentDatabases.size(); i++) {
			DatabaseConfiguration dc = (DatabaseConfiguration)currentDatabases.elementAt(i);
			db.addItem(dc.getDisplayString());
		}
		return k;
	}
	
	private void databaseChanged() {
		if (currentDatabases.size() == 0) {
			return;
		}
		int k = db.getSelectedIndex();
		if (k < 0) {
			return;
		}
		DatabaseConfiguration dc = (DatabaseConfiguration)currentDatabases.elementAt(k);
		IDatabase myDB = null;
		if (engine.getDatabases().length > 0) {
			myDB = engine.getDatabases()[0];
		}
		if (myDB != null && myDB.getID().equalsIgnoreCase(dc.getId())) {
			return;
		}
		try {
			myDB = Database.createDatabase(dc);
		} catch (Exception e) {
			msg(e.toString());
			currentDatabases.removeElementAt(k);
			db.removeItemAt(k);
			return;
		}
		engine = new DictEngine();
		engine.addDatabase(myDB);
		int size = myDB.getSize();
		scrollBar.setMaximum(size+scrollBar.getVisibleAmount()-1);
		scrollBar.setValue(0);
		scrollBar.validate();
		int count = matches.getLastVisibleIndex()-matches.getFirstVisibleIndex();
		if (count > 1) {
			scrollBar.setVisibleAmount(count-1);
			scrollBar.setBlockIncrement(count-1);
		}
		System.gc();
	}
	private void setDisplayFont(Font fnt) {
		getDisplay().setFont(fnt);
		getInputField().setFont(fnt);
		matches.setFont(fnt);
	}
	
	private void setInputMethod(JTextComponent tc, String mode) {
		KeyListener vk = (KeyListener) vietKeys.get(tc);
		tc.removeKeyListener(vk);
		vk = createVietKey(getInputField(), mode);
		if (vk != null) {
			tc.addKeyListener(vk);
			vietKeys.put(tc, vk);
		}
	}
	
	private void showHistory() {
		DefaultListModel model = (DefaultListModel) matches.getModel();
		model.removeAllElements();
		for (int i = 0; i < history.length; i++){
			if (history[i] != null) model.addElement(history[i]);
		}
	}
	
	private void showMatches(IAnswer[] a, boolean slide) {
		DefaultListModel model = (DefaultListModel) matches.getModel();
		model.removeAllElements();
		for (int i = 0; i < a.length; i++){
			//IWordPosition[] n = a[i].getAdjacentWords().getWordPositions();
			int pos = a[i].getPosition();
			if (pos < 0) {
				pos = -pos-1;
			}
			for (int j = 0; j < 50; j++) {
				IWordPosition wp = a[i].getDatabase().getKey(pos+j);
				model.addElement(wp.getKey());
			}
			int count = matches.getLastVisibleIndex()-matches.getFirstVisibleIndex();
			/*
			for (int k = 0; k < n.length; k++) {
				if (n[k].getPosition() >= pos) {
					model.addElement(n[k].getKey());
				} 
			}
			*/
			if (slide) {
				int size = (a[i].getDatabase()).getSize();
				scrollBar.setValue(pos);
			}
		}
		if (model.getSize() > 0) {
			matches.setSelectedIndex(0);
		}
		//System.out.println(count);
	}
	
	private void saveSettings() {
		try {
			PrintWriter p = new PrintWriter(new FileWriter("TDDT.properties"));
			Vector v = new Vector();
			for (int i = 0; i < currentDatabases.size(); i++) {
				DatabaseConfiguration dc = (DatabaseConfiguration) currentDatabases.elementAt(i);
				if (!v.contains(dc.getCfgFile())) {
					v.addElement(dc.getCfgFile());
				}
			}
			p.println("# Database configurations");
			File f = new File(".").getCanonicalFile();
			for (int i = 0; i < v.size(); i++) {
				File cfg = ((File)v.elementAt(i)).getCanonicalFile();
				//System.out.println(f+" <> "+cfg.getParentFile());
				String name = "";
				if (f.equals(cfg.getParentFile())) {
					name = cfg.getName();
				} else {
					name = cfg.getAbsolutePath();
				}
				p.println("db."+i+" = "+name);
			}
			p.println();
			p.flush();
			p.close();
			JOptionPane.showMessageDialog(this, "Settings saved!");
		} catch (Throwable e) {
			//e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Cannot save settings: "+e);
		}
	}
	
	private void loadSettings() {
		try {
			FileInputStream is = new FileInputStream("TDDT.properties");
			PropertyResourceBundle rb = new PropertyResourceBundle(is);
			Enumeration keysEnum = rb.getKeys();
			while (keysEnum.hasMoreElements()) {
				String key = (String) keysEnum.nextElement();
				if (key.startsWith("db.")) {
					String val = rb.getString(key);
					addDatabases(val);
				}
			}
			is.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error loading settings:"+e);
		}
	}
}
