package com.pluseq.coreader;

import java.util.Hashtable;

public class CoThemeBlackOnWhite extends CoTheme {
	public CoThemeBlackOnWhite() {
		color = new Hashtable<Integer, String>();
		color.put(BookElement.CANVAS_BACKGROUND, "#ffffff");
		color.put(BookElement.NORMAL, "#000000");
		
		color.put(BookElement.STATUS_BACKGROUND, "#222222");
		color.put(BookElement.PAGINATION, "#aaaaaa");
		color.put(BookElement.BATTERY, "#aaaaaa");
		color.put(BookElement.BATTERY_CRYTICAL, "#ffaaaa");
	}
	
	public String getName() {
		return "BlackOnWhite";
	}
}
