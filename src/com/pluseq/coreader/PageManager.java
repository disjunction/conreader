package com.pluseq.coreader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import com.pluseq.coreader.CoBit;

import android.database.Cursor;
import android.os.Handler;
import android.util.Log;


/**
 * manages portions of book, corresponding to normal book page (CoPage)
 * @author or
 */
public class PageManager extends Hashtable<Integer, CoPage> {
	private static final long serialVersionUID = 1L;
	protected ParserAbstract parser;
	public String bookHash = null;
	protected int lastPageIndexCounter = 0;
	
	/**
	 * this is used to send messages about currently indexed page
	 */
	private Handler handler = null; 
	
	public PageManager() {
		super();
	}
	
	public PageManager(String bookHash) {
		this();
		seed(bookHash);
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void setParser(ParserAbstract parser) {
		this.parser = parser;
	}
	
	public ParserAbstract getParser() {
		return this.parser;
	}
	
	public CoPage get(int i) {
		return  get(new Integer(i));
	}
	
	protected void readCobitsFromCursor(CoPage p) {
		p.cobits = new ArrayList<CoBit>();
		CoBit cobit;
		
		while (((cobit = parser.getNextCoBit()) != null) && p.addCoBit(cobit)) {}
		
		if (cobit == null) {
			setAsLast(p.pageIndex);
			Log.i("PageManager", "last while reading" + p.pageIndex);
		}
		
		while (((cobit = parser.getNextCoBit()) != null) && parser.isInsideWord()) {
			p.addCoBit(cobit);
		}
	}
	
	private boolean indexAheadLock = false;
	
	public void setIndexAheadLock(boolean lock) {
		indexAheadLock = lock;
	}
	
	/**
	 * Ahead indexing fill Page table with data about where each page starts
	 * (scrapeIndex and  shift)
	 * @return page index, that needs to be parsed next, or null if end of book reached
	 */
	public Integer indexAhead(int scanFrom)	{
		if (areAllParsed()) {
			return null;
		}
		
		/**
		 * count of pages indexed at a time
		 */
		int packetSize = 3;
		
		/**
		 * find the first page to scan for
		 */
		int i = scanFrom;
		while (super.containsKey(new Integer(i))) i++;
		
		if (indexAheadLock) {
			return  i;
		}
		
		int j = 0;
		for (; j<packetSize; j++) {
			CoPage p = get(i + j);
			if (p == null) {
				break;
			}
		}
		
		// we found end of book, no more parsing needed
		if (areAllParsed()) {
			return null;
		}
		
		return i + j + 1;
	}
	
	protected CoPage readFromScrapeCursor(Integer i, int scrapeIndex, int scrapeOffset) {
		if (!parser.setScrapeCursor(scrapeIndex, scrapeOffset)) {
			return null;
		}
		CoPage p = new CoPage(i, parser.cursorScrapeIndex, parser.cursorScrapeOffset);
		Log.i("PageManager", "+++ reading from: " + p.scrapeIndex + ", " + p.scrapeOffset);
		readCobitsFromCursor(p);
		p.setFinishPosition(parser.lastCoBitScrapeIndex , parser.lastCoBitScrapeOffset);
		
		save(i, p);
		return p;
	}
	
	protected static int optimizeCounter = 0;
	
	public CoPage put(Integer i, CoPage p) {
		optimizeCounter++;
		if (optimizeCounter > Constants.pageOptimizeFreq) {
			optimizeCache();
			optimizeCounter = 0;
		}
		
		if (i > lastPageIndexCounter) {
			lastPageIndexCounter = i;
		}
		
		super.put(i, p);
		return p;
	}
	
	public CoPage save(Integer i, CoPage p) {
		if (!containsKey(i) && null != bookHash) {
			CoStorage storage = CoConfig.getStorage();
			storage.savePage(bookHash, p.pageIndex, p.scrapeIndex, p.scrapeOffset, p.finishScrapeIndex, p.finishScrapeOffset);
			Log.i("PageManager", "put page position: " + p.scrapeIndex + ", " + p.scrapeOffset);
		}
		put(i, p);
		return p;
	}
	
	public CoPage get(Integer i) {
		if (allParsed && i>lastPageIndexCounter) {
			return null;
		}
		
		CoPage p = null;
		if (!containsKey(i)) {
			
			Log.i("Page Manager", "Creating page: " + i);
			
			int z = i;
			
			// to save the memory we avoid tail recursion via DO-WHILE loop
			do {
				p = super.get(z);
				if (p == null && z == 0) {
					p = readFromScrapeCursor(i, 0, 0);
				}
				z--;
			} while (p == null);
			
			z++;
			
			if (z == i) {
				return p;
			}
		
			do {
				z++;
				
				// send message to progress bar
				if (handler != null) {
					handler.sendMessage(handler.obtainMessage(z));
				}
				
				p = readFromScrapeCursor(z, p.finishScrapeIndex, p.finishScrapeOffset);
				
				//end of book
				if (p == null) {
					setAsLastAndSave(z-1);
					return null;
				}
			} while (z < i);
			
			return p;	
		}
		return super.get(i);
	}
	
	//////// FRAME FEED AND FRAME MAPPING STUFF
		
	private Integer cursorPageIndex;
	private int cursorCoBitIndex;
	private CoPage cursorPage;

	public void setCursor(FrameMapping fm, int offset) {
		setCursor(fm);
		
		if (null == cursorPage) {
			Log.w("PageManager", "setting cursor in empty page");
			return;
		}
		
		boolean doContinue;
		
		do {
			doContinue = false;
			cursorCoBitIndex = cursorCoBitIndex + offset;
			if (cursorPage.cobits.size() <= cursorCoBitIndex) {
				cursorCoBitIndex = 0;
				offset -= cursorPage.cobits.size() + cursorCoBitIndex; 
				cursorPageIndex++;
				cursorPage = get(cursorPageIndex);
				doContinue = true;
			} else if (cursorCoBitIndex<0) {
				offset = cursorCoBitIndex;
				if (cursorPageIndex > 0) {
					cursorPageIndex--;
					cursorPage = get(cursorPageIndex);
					checkCursorCobits();
					cursorCoBitIndex = cursorPage.cobits.size()-1;
					doContinue = true;
				} else {
					cursorCoBitIndex = 0;
					cursorPage = get(0);				
				}
			}
		} while (doContinue);
	}

	public void setCursor(FrameMapping fm) {
		cursorPageIndex = new Integer(fm.pageIndex);
		cursorCoBitIndex = fm.cobitIndex;
		cursorPage = get(cursorPageIndex);
	}

	/**
	 * this hash simplifies hash table creation to map specific cobits
	 * 
	 * The other usage is easier compare of two Mappings
	 */
	public Integer getCursorHash() {
		return cursorPageIndex * Constants.maxCharsPerPage + cursorCoBitIndex;
	}
	
	public void goBack(int cobitNumber) {
		if (cobitNumber > cursorCoBitIndex) {
			cobitNumber -= cursorCoBitIndex;
			cursorPageIndex--;
			cursorPage = null;
			checkCursorPage();
			cursorCoBitIndex = cursorPage.cobits.size()-1;
			goBack(cobitNumber);
		} else {
			cursorCoBitIndex -= cobitNumber; 
		}
	}

	private boolean checkCursorCobits() {
		if (null == cursorPage.cobits) {			
			Log.i("PageManager",  "rescanning page contents. Index: " + cursorPageIndex);
			if (!parser.setScrapeCursor(cursorPage.scrapeIndex, cursorPage.scrapeOffset)) {
				return false;
			}
			readCobitsFromCursor(cursorPage);
		}
		return null != cursorPage.cobits;
	}
	
	private boolean checkCursorPage() {
		if (null == cursorPage) {
			cursorPage = get(cursorPageIndex);
			cursorCoBitIndex = 0;
			// end of book or incorrect page index
			if (null == cursorPage) {
				Log.i("PageManager",  "pm returned null");
				return false;
			} else {
				Log.i("PageManager",  "page switched");
			}
			
		}
		return true;
	}
	
	public FrameMapping getCursorFrameMapping() {
		return new FrameMapping(cursorPageIndex, cursorCoBitIndex);
	}
	
	public void optimizeCache() {
		if (null != cursorPageIndex) {
			Enumeration<Integer> keys = keys();
			Integer i;
			int minMargin = cursorPageIndex - Constants.cachePageNeighbourhood;
			int maxMargin = cursorPageIndex + Constants.cachePageNeighbourhood;
			
			while (keys.hasMoreElements()) {
				i = keys.nextElement();
				if (i < minMargin || i > maxMargin) {
					super.get(i).clearCoBits();
				}
			}
		}
	}
	
	/**
	 * FIXME ?
	 * doesnt shift any cursors
	 */
	public CoBit getPreviousCoBit() {
		CoPage page = null;
		int cobitIndex = cursorCoBitIndex-1;
		if (cobitIndex<0) {
			if (cursorPageIndex == 0) {
				return null;
			} else {
				page = get(cursorPageIndex-1);
				if (page.cobits == null) {

				}
				return page.cobits.get(page.cobits.size()-1);
			}
		}
		page = get(cursorPageIndex);
		if (page.cobits == null) {
			if (!parser.setScrapeCursor(page.scrapeIndex, page.scrapeOffset)) {
				return null;
			}
			readCobitsFromCursor(page);
		}
		return page.cobits.get(cobitIndex);
	}
	
	public CoBit getNextCoBit() {
		CoBit cobit;
				
		if (!checkCursorPage()) {
			return null;
		}
		
		try {
			cobit = cursorPage.cobits.get(cursorCoBitIndex);
		} catch (NullPointerException e) {
			return (checkCursorCobits()) ? getNextCoBit() : null; 
		} catch (IndexOutOfBoundsException e) {

			//end of page reached - go to next page
			cursorPageIndex++;
			cursorPage = null;
			return getNextCoBit();
		}

		cursorCoBitIndex++;
		return cobit;
	}
	
	/**
	 * Adds empty page mapping from DB cursor
	 * @param c
	 */
	protected void seed(String bookHash) {
		this.bookHash = bookHash; 
		CoStorage storage = CoConfig.getStorage(); 
		Cursor c = storage.loadAllPages(bookHash);
		while (!c.isAfterLast()) {
			CoPage p = new CoPage(c.getInt(0), c.getInt(1), c.getInt(2));
			p.setFinishPosition(c.getInt(3), c.getInt(4));
			p.cobits = null;
			put(c.getInt(0), p);
			/*
			Log.i("PageManager", "----------- seeding page: " + c.getInt(0));
			Log.i("PageManager", "position: " + c.getInt(1) + ", " + c.getInt(2));
			Log.i("PageManager", "finishPosition: " + c.getInt(3) + ", " + c.getInt(4));
			*/
			c.moveToNext();
		}
		c.close();
	}
	
	private boolean allParsed = false;
	public boolean areAllParsed() {
		return allParsed;
	}
	
	public int getLastPageIndex() {
		return lastPageIndexCounter;
	}

	public int getDisplayedLastPageIndex() {
		if (allParsed) {
			return lastPageIndexCounter;
		} else if (null != estimatedLastPageIndex) {
			return estimatedLastPageIndex;
		} else {
			return lastPageIndexCounter;
		}
	}
	
	public void setAsLast(Integer i) {
		lastPageIndexCounter = i;
		allParsed = true;
		Log.i("PageManager", "set as last " + i);
	}
	
	/**
	 * FIXME dependency on current book
	 * @param i
	 */
	private void setAsLastAndSave(Integer i) {
		setAsLast(i);
		CoStorage storage = CoConfig.getStorage();
		storage.saveBookLastPageIndex(CoConfig.currentActivity.getCoReaderState().book.hash, i);
	}
	
	private Integer estimatedLastPageIndex = null;
	public void estimateLastPage() {
		int counter = 0;
		CoPage p = null;
		while (counter < 10 && ((p=get(counter++)) != null));
		if (p == null) {
			setAsLast(counter-1);
		} else {
			int fullsize = parser.sm.size() * Constants.charsPerScrape;
			int parsedSize = p.scrapeIndex * Constants.charsPerScrape + p.scrapeOffset;
			estimatedLastPageIndex = fullsize * 9 / parsedSize;
		}
	}
}