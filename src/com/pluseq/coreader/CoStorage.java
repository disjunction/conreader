package com.pluseq.coreader;

import android.database.Cursor;



public interface CoStorage {

	CoReaderStateInterface getCoReaderState();
	CoStorage open();
	void close();
	
	void saveBook(String fileId, String hash, FrameMapping position);
	void saveBookPosition(String hash, int pageIndex, int pageOffset);
	Cursor loadBookByHash(String hash);
	
	void saveState(int currentWindow, String bookHash);
	Cursor loadState();
	
	void savePage(String bookHash, int pageIndex, int scrapeIndex,
			int scrapeOffset, int finishScrapeIndex, int finishScrapeOffset);
	Cursor loadAllPages(String bookHash);
	Cursor getRecentBooks();
	void saveBookLastPageIndex(String hash, int lastPageIndex);
	void saveStateSettings(int fontFactor, String colorThemeName);
	void saveStateFullscreen(boolean fullscreen);
	
	void saveSetting(String name, String value, int profileIndex);
	String loadSetting(String name, String defaultValue, int profileIndex);
	void clearPages();
	void flush();
}
