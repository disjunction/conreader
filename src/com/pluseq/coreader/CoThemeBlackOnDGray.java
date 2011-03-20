package com.pluseq.coreader;

import java.util.Hashtable;

public class CoThemeBlackOnDGray extends CoTheme {
	public CoThemeBlackOnDGray() {
		color = new Hashtable<Integer, String>();
		color.put(BookElement.CANVAS_BACKGROUND, "#999990");
		color.put(BookElement.NORMAL, "#000000");
		
		color.put(BookElement.STATUS_BACKGROUND, "#222222");
		color.put(BookElement.PAGINATION, "#777777");
		color.put(BookElement.BATTERY, "#777777");
		color.put(BookElement.BATTERY_CRYTICAL, "#ffaaaa");
	}
	
	public String getName() {
		return "BlackOnDGray";
	}
}