package com.pluseq.coreader;

import java.util.ArrayList;
import java.util.Hashtable;
import android.content.res.Resources;
import android.graphics.Color;

public abstract class CoTheme {
	public Hashtable<Integer,String> color = null;
	protected static final int[] themeNameResources = new int[] {R.string.themeBlackOnWhite,
					                				     R.string.themeBlackOnGray,
					                				     R.string.themeBlackOnDGray,
					                				     R.string.themeGrayOnBlack,
					                				     R.string.themeDGrayOnBlack,
					                				     R.string.themeTerminalNostlgia};
	
	public static ArrayList<String> getThemeList() {
		ArrayList<String> themes = new ArrayList<String>();
		for (int nameResource : themeNameResources) {
			themes.add(CoConfig.currentActivity.getResources().getString(nameResource));
		}
		return themes;
	}
	
	public static int getResourceByName(String name) {
		Resources r = CoConfig.currentActivity.getResources();
		for (int nameResource : themeNameResources) {
			if (name.equals(r.getString(nameResource))) {
				return  nameResource;
			}
		}
		return 0;
	}
	
	public static CoTheme getThemeByResource (int resourceName) {
		switch (resourceName) {
		case R.string.themeBlackOnWhite:
			return new CoThemeBlackOnWhite();
		case R.string.themeBlackOnGray:
			return new CoThemeBlackOnGray();
		case R.string.themeBlackOnDGray:
			return new CoThemeBlackOnDGray();
		case R.string.themeGrayOnBlack:
			return new CoThemeGrayOnBlack();
		case R.string.themeDGrayOnBlack:
			return new CoThemeDGrayOnBlack();
		case R.string.themeTerminalNostlgia:
			return new CoThemeTerminalNostalgia();
		default:
			return new CoThemeBlackOnWhite();	
		}
	}

	public static int getPositionByName(String name) {
		for (int i=0; i<themeNameResources.length; i++) {
			CoTheme theme = getThemeByResource(themeNameResources[i]);
			if (theme.getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public static CoTheme getThemeByLocalName(String name) {
		return getThemeByResource(getResourceByName(name));
	}
	
	public static CoTheme getThemeByIndex(int index) {
		if (index < 0) {
			return null;
		}
		return getThemeByResource(themeNameResources[index]);
	}
	
	abstract public String getName();
	
	Integer normalColor = null;
	Integer canvasBackground = null;
	Integer statusBackground = null;
	Integer paginationColor = null;
	Integer batteryColor = null;
	Integer batteryCryticalColor = null;
	
	public int getNormalColor() {
		if (normalColor == null) {
			normalColor = Color.parseColor(color.get(BookElement.NORMAL));
		}
		return normalColor;
	}
	
	public int getCanvasBackground() {
		if (canvasBackground == null) {
			canvasBackground = Color.parseColor(color.get(BookElement.CANVAS_BACKGROUND));
		}
		return canvasBackground;
	}
	
	public int getStatusBackground() {
		if (statusBackground == null) {
			statusBackground = Color.parseColor(color.get(BookElement.STATUS_BACKGROUND));
		}
		return statusBackground;
	}
	
	public int getPaginationColor() {
		if (paginationColor == null) {
			paginationColor = Color.parseColor(color.get(BookElement.PAGINATION));
		}
		return paginationColor;
	}
	
	public int getBatteryColor() {
		if (batteryColor == null) {
			batteryColor = Color.parseColor(color.get(BookElement.BATTERY));
		}
		return batteryColor;		
	}
	
	public int getBatteryCryticalColor() {
		if (batteryCryticalColor == null) {
			batteryCryticalColor = Color.parseColor(color.get(BookElement.BATTERY));
		}
		return batteryCryticalColor;		
	}
}