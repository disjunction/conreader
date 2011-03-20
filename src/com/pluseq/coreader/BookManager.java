/**
 * 
 */
package com.pluseq.coreader;

import java.util.StringTokenizer;

import android.database.Cursor;
import android.util.Log;

/**
 * General strategies to work with the books
 * @author or
 *
 */
public class BookManager {
	
	CoStorage storage = CoConfig.getStorage();
	
	public Book create(String fileId, int pageIndex, int cobitIndex) {
		try {
			StringTokenizer st = new StringTokenizer(fileId, ";");
			Book book = new Book();
			book.fileName = ((String) st.nextElement()).substring(7);
			book.encoding = ((String) st.nextElement()).substring(8);
			book.currentPageIndex = pageIndex;
			book.currentCoBitIndex = cobitIndex;
			Log.i("BookManager", "fileName=" + book.fileName);
			Log.i("BookManager", "charset=" + book.encoding);
			Log.i("BookManager", "pageIndex=" + book.currentPageIndex);
			return book;
		} catch (Exception e) {
			Log.e("BookManager", "Failed to load by fileId=" + fileId, e);
		}
		return null;
	}
	
	public Book loadBookFromScrapeManager(ScrapeManagerInterface sm, Book book) {
		String bookHash = sm.getBookHash();
		PageManager pm = new PageManager(bookHash);
		
		CoConfig.currentActivity.getCoReaderState().book = book;
		
		int parserType = getParserType(sm, book);
		Log.i(this.getClass().getName(), "parser type: " + parserType);
		
		ParserAbstract parser;
		if (parserType == PARSER_TYPE_TEXT) {
			parser = new ParserText();
		} else {
			parser = new ParserTagged();
		}
		
		parser.setPageManager(pm);
		parser.setScrapeManager(sm);
		book.rules = parser.preparseLineRules();
		parser.initRules();
		
		if (null != book.lastPageIndex) {
			pm.setAsLast(book.lastPageIndex);
		}
		if (!pm.areAllParsed()) {
			pm.estimateLastPage();
		}
		
		book.setPageManager(parser.getPageManager());
		book.hash = bookHash;
		return book;
	}
	
	public Book loadByHash(String hash){
		CoStorage storage = CoConfig.getStorage();
		Log.i("BookManager", "loading by hash: " + hash);
		Cursor c = storage.loadBookByHash(hash);
		if (null != c && c.getCount() > 0) {
				Log.i("BookManager", "loaded fileId = " + c.getString(0));
				Book book =  create(c.getString(0), c.getInt(1), c.getInt(2));
				book.lastPageIndex = (c.isNull(3))? null : c.getInt(3);
				c.close();
				return book;
		}
		c.close();
		return null;
	}
	
	public static int PARSER_TYPE_TEXT = 1;
	public static int PARSER_TYPE_TAGGED = 2;
	
	public int getParserType(ScrapeManagerInterface sm, Book book) {
		if (book.fileName == null) {
			Log.e(this.getClass().getName(), "no fileName");
			return PARSER_TYPE_TEXT;
		}
		
		if (book.fileName.matches("html$")) {
			return PARSER_TYPE_TAGGED;
		} else {
			Log.i(this.getClass().getName(), "tagged not matched");
			return PARSER_TYPE_TEXT;
		}
	}
}
