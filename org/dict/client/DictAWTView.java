package org.dict.client;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import org.dict.kernel.*;
/**
 * Insert the type's description here.
 * Creation date: (28.02.2002 23:18:11)
 * @author:
 */
public class DictAWTView implements Runnable {
  Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  String value = "";
  String selected = "";
  IDictEngine engine;
  Frame f;
  TextArea ta;
  Checkbox mode;
  Choice db, fontChoice, sizeChoice;
  String[] history = new String[20];
  int pos = 0;
  //int fontSize = Integer.getInteger("fontSize", 12).intValue();
  int delay = Integer.getInteger("delay", 20).intValue();

    class FL extends FocusAdapter {
        public void focusGained(FocusEvent e) {
            DictAWTView.this.lookup();
        }
    }
    class ML extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                DictAWTView.this.lookup(DictAWTView.this.getSelectedText(), false);
            }
        }
    }
    class AL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    class History implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            DictAWTView.this.showHistory();
        }
    }
    class IL implements ItemListener {
	    public void itemStateChanged(ItemEvent e) {
		    DictAWTView.this.setFont();
	    }
    }
/**
 * ClipboardTest constructor comment.
 */
public DictAWTView(String cfg) {
	super();
	IDictEngine e = org.dict.server.DatabaseFactory.getEngine(cfg);
	this.engine = e;
	f = new Frame("Dictionary lookup");
	f.setSize(600, 400);
	f.setLayout(new BorderLayout());
	ta = new TextArea();
	ta.setBackground(Color.white);
	ta.setForeground(Color.black);
	ta.addMouseListener(new ML());
	ta.addFocusListener(new FL());
	Button prev = new Button("History");
	prev.addActionListener(new History());
	mode = new Checkbox("Automatic", false);
	db = new Choice();
	for (int i = 0; i < e.getDatabases().length; i++){
		db.add(e.getDatabases()[i].getName());
	}
	db.add("Any database");
	fontChoice = new Choice();
	fontChoice.add("serif");
	fontChoice.add("monospaced");
	fontChoice.add("dialog");
	fontChoice.addItemListener(new IL());
	sizeChoice = new Choice();
	sizeChoice.add("10");
	sizeChoice.add("12");
	sizeChoice.add("15");
	sizeChoice.addItemListener(new IL());
	Panel north = new Panel();
	north.setBackground(Color.lightGray);
	north.add(mode);
	north.add(db);
	north.add(fontChoice);
	north.add(sizeChoice);
	north.add(prev);
	f.add(north, "North");
	f.add(ta, "Center");
	f.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e) {System.exit(0);}
	});
	f.setVisible(true);
}
private String getSelectedText() {
    return ta.getSelectedText();
}
private void lookup() {
    Transferable content = clipboard.getContents(this);
    if (content == null) {
        return;
    }
    try {
        String s = (String) content.getTransferData(DataFlavor.stringFlavor);
        if (!this.value.equals(s)) {
            this.value = s;
            lookup(s, false);
        }
    } catch (Exception e) {
    }
}
void lookup(String s, boolean append) {
    if (s.length() > 25) {
        return;
    }
    history[pos++ % history.length] = s;
    f.toFront();
    //ta.requestFocus();
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
        org.dict.kernel.PlainPrinter.printAnswers(engine, req, a, false, out);
        out.flush();
    } catch (Exception e) {
        e.printStackTrace(out);
    }
    if (append) {
        ta.append(w.toString());
    } else {
        ta.setText(w.toString());
    }
}
private void loop() {
    while (true) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
        if (mode.getState()) {
            lookup();
        }
    }
}
public static void main(String[] args) {
	if (args.length < 1) {
		System.out.println("Usage: java ...DictAWTView cfg");
		System.exit(0);
	}
	DictAWTView runner = new DictAWTView(args[0]);
	Thread th = new Thread(runner);
	th.start();
}
public void run() {
    loop();
}
private void setFont() {
	String face = fontChoice.getSelectedItem();
	String size = sizeChoice.getSelectedItem();
    ta.setFont(new Font(face, Font.PLAIN, Integer.parseInt(size)));
}
private void showHistory() {
	ta.setText("");
    for (int i = 0; i < history.length; i++){
	    String sep = i == 10 ? "\n" : " | ";
    	if (history[i] != null) ta.append(history[i] + sep);
    }
}
}
