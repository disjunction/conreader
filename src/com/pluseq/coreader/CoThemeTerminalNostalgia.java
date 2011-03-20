package com.pluseq.coreader;

import java.util.Hashtable;

public class CoThemeTerminalNostalgia extends CoTheme {
	public CoThemeTerminalNostalgia() {
		color = new Hashtable<Integer, String>();
		color.put(BookElement.CANVAS_BACKGROUND, "#000000");
		color.put(BookElement.NORMAL, "#99B766");
		
		color.put(BookElement.STATUS_BACKGROUND, "#99B766");
		color.put(BookElement.PAGINATION, "#000000");
		color.put(BookElement.BATTERY, "#000000");
		color.put(BookElement.BATTERY_CRYTICAL, "#550000");
	}
	
	public String getName() {
		return "TerminalNostalgia";
	}
}