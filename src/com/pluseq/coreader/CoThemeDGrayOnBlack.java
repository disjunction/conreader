package com.pluseq.coreader;

import java.util.Hashtable;

public class CoThemeDGrayOnBlack extends CoTheme {
	public CoThemeDGrayOnBlack() {
		color = new Hashtable<Integer, String>();
		color.put(BookElement.CANVAS_BACKGROUND, "#000000");
		color.put(BookElement.NORMAL, "#666660");
		
		color.put(BookElement.STATUS_BACKGROUND, "#222222");
		color.put(BookElement.PAGINATION, "#999999");
		color.put(BookElement.BATTERY, "#777777");
		color.put(BookElement.BATTERY_CRYTICAL, "#ffaaaa");
	}
	
	public String getName() {
		return "DGrayOnBlack";
	}
}