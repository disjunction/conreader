package com.pluseq.coreader;

import java.util.ArrayList;
import com.pluseq.coreader.CoBit;

/**
 * handmade <tag> parser, supporting scrape idea
 * (partial content) 
 */
public class ParserTagged extends ParserAbstract {

	@Override
	public CoBit getNextCoBit() {
		// TODO Auto-generated method stub
		return null;
	}

	protected int MAX_TAG_SIZE = 3000;
	
	protected ArrayList<String> scrapeBits;
	protected ArrayList<Scrape> scrapeAhead;
	protected int scrapeChainStart = -1;
	protected int scrapeChainOffset;
	
	protected void fillScrapeBits() {
		scrapeChainOffset = cursorScrapeOffset;
		scrapeAhead = new ArrayList<Scrape>();
		
		String processed = sm.getStringByScrapeIndex(cursorScrapeIndex).substring(scrapeChainOffset);
		
		/*
		if (processed.length() < this.MAX_TAG_SIZE) {
			while (sm.hasNext() && processed.length() < this.MAX_TAG_SIZE) {
				Scrape s = sm.getNext();
				processed += s.toString();
				scrapeAhead.add(s);
			}
		}
		*/
		
		

		scrapeBits = new ArrayList<String>();
	}
	
	/**
	 * @return if there is any data
	 */
	@Override
	protected boolean initCursor() {
		if (this.scrapeChainStart != cursorScrapeIndex) {
			fillScrapeBits();
		}
	
		return true;
	}
	
	protected boolean inTag = false; 
}
