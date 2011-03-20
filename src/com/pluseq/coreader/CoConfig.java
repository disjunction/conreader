/**
 * 
 */
package com.pluseq.coreader;

import android.database.Cursor;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import com.pluseq.coreader.android.ConReader;
import com.pluseq.coreader.android.CoReaderState;
import com.pluseq.coreader.android.FontSettings;
import com.pluseq.coreader.android.SQLiteStorage;

/**
 * Static class which returns current settings and links to static resources
 * @author or
 */
public class CoConfig {
	
	public static ConReader currentActivity;
	protected static DisplayState displayState = null;
	protected static FontSettings fontSettings = null;
	protected static CoTheme theme = null;
	protected static int statusBarHeight = 0;
	protected static String jailDirectory = "/sdcard";
	protected static int profileIndex = 1;
	
	public static Integer batteryLevel = 0;
	
	public static void resetStatic() {
		displayState = null;
	}
	
	public CoConfig() {
		displayState = new DisplayState();
	}

	public static void setCoTheme(CoTheme theme) {
		CoConfig.theme = theme;
		if (null != CoConfig.fontSettings) {
			fontSettings.setCoTheme(theme);
			fontSettings.initFonts();
		}
	}
	
	public static CoTheme getCoTheme() {
		if (null == CoConfig.theme) {
			CoConfig.theme = new CoThemeBlackOnWhite();
		}
		return CoConfig.theme;
	}
	
	public static void readDisplayState(boolean fullScreen) {
		DisplayMetrics dm = new DisplayMetrics(); 
		currentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		if (statusBarHeight == 0)  {
			Rect rectgle= new Rect();
			Window window= currentActivity.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
			statusBarHeight = rectgle.top;
		}
		
		Log.i("StatusHeight", String.valueOf(statusBarHeight));
		
		CoConfig.displayState = new DisplayState();
		displayState.textWidth = dm.widthPixels;
		displayState.textHeight = dm.heightPixels - 14;
		if (!fullScreen) {
			displayState.textHeight -= statusBarHeight;
		}
	}
	
	public static DisplayState getDisplayState() {
		if (null == CoConfig.displayState) {
			readDisplayState(false);
		}
		return CoConfig.displayState;
	}
	
	public static FontSettings getFontSettings() {
		if (null == CoConfig.fontSettings) {
			CoConfig.fontSettings = new FontSettings(getCoTheme());
		}
		return CoConfig.fontSettings;
	}
	
	protected static CoStorage hardSource = null;
	public static CoStorage getStorage() {
		if (null == hardSource) {
			hardSource = new SQLiteStorage(currentActivity);
			hardSource.open();
		}
		return hardSource;
	}
	
	public static void saveState() {
		CoReaderState state = currentActivity.getCoReaderState();
		if (state.currentWindow == R.layout.book) {
			getStorage().saveState(state.currentWindow , state.book.hash);
		} else {
			getStorage().saveState(state.currentWindow , "");
		}
	}
	
	protected static void reloadTheme()
	{
		CoStorage storage = getStorage();
		
		FontSettings fs = getFontSettings();			
		String fontFactor = storage.loadSetting("fontFactor", "17", getProfileIndex());
		fs.setFontFactor(Integer.valueOf(fontFactor));

		String themeName = 	storage.loadSetting("theme", "BlackOnGray", getProfileIndex());
		CoTheme theme = CoTheme.getThemeByIndex(CoTheme.getPositionByName(themeName));
		if (null != theme) {
			setCoTheme(theme);
		}
	}
	
	public static void loadState() {
		CoStorage coStorage = getStorage();
		
		Cursor c = coStorage.loadState();
		if (null != c && c.getCount() > 0) {
			Log.i("CoConfig", "trying to restore state...");
			CoReaderState state = currentActivity.getCoReaderState();
			state.currentWindow = c.getInt(0);
			if (state.currentWindow == R.layout.book) {
				BookManager bm = new BookManager();
				Book book = bm.loadByHash(c.getString(1));
				state.book = book;
			}
			
			reloadTheme();
			
			state.fullScreen = c.getInt(4)>0;
			Log.i("COCONFIG", "FULLSCREEN SET: " + ((state.fullScreen) ? 1 : 0));
		}
		c.close();
		
		setJailDirectory(coStorage.loadSetting("jailDirectory", "/sdcard", 0));
	}
	
	public static void setCurrentFrameMapping(FrameMapping fm) {
		currentActivity.getCoReaderState().currentFrameMapping = fm;
	}
	
	public static FrameMapping getCurrentFrameMapping() {
		return currentActivity.getCoReaderState().currentFrameMapping;
	}
	
	public static void setProfileIndex(int profileIndex) {
		CoConfig.profileIndex = profileIndex;
		getStorage().saveSetting("profileIndex", String.valueOf(getProfileIndex()), 0);
	}
	public static int getProfileIndex() {
		return profileIndex == 2 || profileIndex == 1? profileIndex : 1;
	}
	
	public static void setJailDirectory(String jailDirectory) {
		CoConfig.jailDirectory = jailDirectory;
		getStorage().saveSetting("jailDirectory", jailDirectory, 0);
	}
	public static String getJailDirectory() {
		return jailDirectory;
	}
	public static void switchProfile() {
		setProfileIndex(getProfileIndex() == 1 ? 2 : 1);
		getStorage().flush();
		reloadTheme();		
	}
}
