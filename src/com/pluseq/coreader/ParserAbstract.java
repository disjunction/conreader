package com.pluseq.coreader;

import java.util.ArrayList;
import java.util.Queue;

import android.util.Log;

public abstract class ParserAbstract {

	public static final int STATE_INIT = 0;
	public static final int STATE_HEADER = 1;
	public static final int STATE_BODY = 2;
	protected int state;

	protected ScrapeManagerInterface sm;
	protected PageManager pageManager;
	protected ArrayList<CoRule> lineRules = null;

	public void setScrapeManager(ScrapeManagerInterface sm) {
		this.sm = sm;
		Log.w("ParserAbstract", "number of scrapes" + sm.size());
	}

	public void setPageManager(PageManager pageManager) {
		this.pageManager = pageManager;
		this.pageManager.setParser(this);
	}

	public PageManager getPageManager() {
		return this.pageManager;
	}

	public int getState() {
		return state;
	}

	// ////////////////////////////////////////
	// ///////// CURSOR READING / PARSING
	// ////////////////////////////////////////

	protected int cursorLineLength = 0;
	protected boolean cursorTabChain;
	protected int cursorScrapeIndex;
	protected int cursorScrapeOffset;
	protected ScrapeTokenizer scrapeTokenizer;
	protected Scrape currentScrape;
	protected String currentToken;
	protected Queue<CoBit> currentWordCobits;
	protected boolean isEnd = false;
	protected String currentScrapeString;

	/**
	 * @param length
	 * @return _length_ preceding characters back from cursor
	 */
	protected String getPreceeding(int length) {
		int fromCurrentScrape;
		String prefix = "";
		if (cursorScrapeOffset < length) {
			fromCurrentScrape = 0;
			if (cursorScrapeIndex > 0) {
				String prevScrape = sm.getByIndex(cursorScrapeIndex - 1)
						.toString();
				prefix = prevScrape.substring(prevScrape.length() - length
						+ cursorScrapeOffset);
			}
		} else {
			fromCurrentScrape = cursorScrapeOffset - length;
		}

		return prefix
				+ currentScrapeString.substring(fromCurrentScrape,
						cursorScrapeOffset);
	}

	/**
	 * @return if there is any data
	 */
	protected boolean initCursor() {
		return true;
	}

	/**
	 * @param scrapeIndex
	 * @param scrapeOffset
	 * @return if there is any data
	 */
	public boolean setScrapeCursor(int scrapeIndex, int scrapeOffset) {
		Log.i("ParserAbstract", "Cursor position: " + scrapeIndex + ", "
				+ scrapeOffset);
		this.cursorScrapeIndex = scrapeIndex;
		this.cursorScrapeOffset = scrapeOffset;
		return initCursor();
	}

	public static final String CLASS_SPACES = " \r\n\t";
	public static final String CLASS_SIGNS = "—.,-=)(*:;'\"?!@%^&|\\{}[]";

	public int getTypeByElement(char element) {
		if (CLASS_SIGNS.indexOf(element) >= 0) {
			return CoBit.SIGN;
		}
		if (CLASS_SPACES.indexOf(element) >= 0) {
			// Log.w("Parser", "SPACE CODE " + Integer.valueOf(element));
			return CoBit.SPACE;
		}
		return CoBit.WORD;
	}

	/**
	 * This is useful when creating a page. The page shouldn't end inside a word
	 * @return if scrape cursor is inside a word
	 */
	public boolean isInsideWord() {
		return !((null == currentWordCobits) || currentWordCobits.isEmpty());
	}

	protected CoBit shiftAndReturn(CoBit cobit, int length) {
		cursorScrapeOffset += length;
		// Log.v("Parser", "type: " + cobit.type + ", content: " +
		// cobit.content);
		return cobit;
	}

	protected CoBit shiftAndReturn(CoBit cobit) {
		if (cobit.type == CoBit.SIGN) {
			if (cobit.content.compareTo("--") == 0) {
				cobit.content = "—";
				return shiftAndReturn(cobit, 2);
			}
			if (cobit.content.compareTo("...") == 0) {
				cobit.content = "…";
				return shiftAndReturn(cobit, 3);
			}
		}
		return shiftAndReturn(cobit, cobit.content.length());
	}

	protected CoBit shiftAndReturnSyllable(CoBit cobit) {
		int length = cobit.content.length();
		cursorLineLength += length;
		return shiftAndReturn(cobit, length);
	}

	public int lastCoBitScrapeIndex;
	public int lastCoBitScrapeOffset;

	/**
	 * @return CoBit|null - if end of book reached
	 */
	abstract public CoBit getNextCoBit();

	public ArrayList<CoRule> preparseLineRules() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initRules() {
		// TODO Auto-generated method stub
		
	}

}