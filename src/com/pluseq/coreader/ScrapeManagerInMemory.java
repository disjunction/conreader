package com.pluseq.coreader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ScrapeManagerInMemory  implements ScrapeManagerInterface {

	protected ArrayList<Scrape> memoryArray;
	protected int currentPosition = 0;
	
	
	public ScrapeManagerInMemory() {
		memoryArray = new ArrayList<Scrape>();
		currentPosition = 0;
	}
	
	@Override
	public Scrape getByIndex(int index) {
		currentPosition = index;
		return memoryArray.get(index);
	}

	@Override
	public Scrape getNext() {
		return memoryArray.get(++currentPosition);
	}

	@Override
	public boolean hasNext() {
		return memoryArray.size() > currentPosition+1;
	}
	
	public void add(Scrape scrape) {
		memoryArray.add(scrape);
	}

	@Override
	public boolean hasIndex(int index) {
		return (index < memoryArray.size());
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return memoryArray.size();
	}


	@Override
	public StringTokenizer getLineTokenizerByIndex(int index, int offset) {
		Scrape currentScrape = getByIndex(index);
		String currentScrapeString;
		StringTokenizer scrapeTokenizer;
		try {
			currentScrapeString = currentScrape.toString();
			scrapeTokenizer = new StringTokenizer(currentScrapeString.substring(offset), "\n", true);
		} catch (StringIndexOutOfBoundsException e) {
			return null;
		}
		return scrapeTokenizer;
	}

	protected String hash = "";
	
	@Override
	public String getBookHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public String getStringByScrapeIndex(int scrapeIndex) {
		return this.getByIndex(scrapeIndex).toString();
	}

	public void initIterator() {
		// TODO Auto-generated method stub
		
	}
}
