package com.pluseq.coreader;

import java.util.Hashtable;

public class CoThemeGrayOnBlack extends CoTheme {
	public CoThemeGrayOnBlack() {
		color = new Hashtable<Integer, String>();
		color.put(BookElement.CANVAS_BACKGROUND, "#000000");
		color.put(BookElement.NORMAL, "#999999");
		
		color.put(BookElement.STATUS_BACKGROUND, "#222222");
		color.put(BookElement.PAGINATION, "#ffffff");
		color.put(BookElement.BATTERY, "#aaaaaa");
		color.put(BookElement.BATTERY_CRYTICAL, "#ffaaaa");
	}
	
	public String getName() {
		return "GrayOnBlack";
	}
}