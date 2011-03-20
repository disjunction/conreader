package com.pluseq.coreader;

import java.util.StringTokenizer;

/**
 * @author or
 */
public interface ScrapeManagerInterface {
	public String getBookHash();
	public Scrape getByIndex(int index);
	public boolean hasIndex(int index);
	public Scrape getNext();
	public boolean hasNext();
	public int size();
	public StringTokenizer getLineTokenizerByIndex(int index, int offset);
	public String getStringByScrapeIndex(int scrapeIndex);
}
