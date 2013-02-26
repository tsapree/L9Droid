package pro.oneredpixel.l9droid;

import java.util.ArrayList;

public class History {
	ArrayList<String> commands;
	
	History() {
		commands=new ArrayList<String>(); 
	}
	
	void add(String cmd) {
		if (cmd==null) return;
		String s=cmd.trim();
		commands.remove(s);
		commands.add(s);
	}
	
	ArrayList<String> getHistory() {
		return commands;
	}
	
	void clear() {
		commands.clear();
	}
}
