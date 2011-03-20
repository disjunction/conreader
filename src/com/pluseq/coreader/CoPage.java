/**
 * 
 */
package com.pluseq.coreader;

import java.util.ArrayList;
import java.util.List;

import com.pluseq.coreader.CoBit;

/**
 * @author or
 *
 */
public class CoPage {

	public List<CoBit> cobits;
	public int pageIndex;
	public int scrapeIndex;
	public int scrapeOffset;
	public int dataSize = 0;

	public int finishScrapeIndex;
	public int finishScrapeOffset;
	
	public CoPage(int pageIndex, int scrapeIndex, int scrapeOffset) {
		this.pageIndex = pageIndex;
		this.scrapeIndex = scrapeIndex;
		this.scrapeOffset = scrapeOffset;
		cobits = null; //new ArrayList<CoBit>();
	}
	
	public void setFinishPosition(int finishScrapeIndex, int finishScrapeOffset) {
		this.finishScrapeIndex = finishScrapeIndex;
		this.finishScrapeOffset = finishScrapeOffset;
	}
	
	/**
	 * @param cobit
	 * @return if the current page is full
	 */
	public boolean addCoBit(CoBit cobit) {
		dataSize += cobit.measureChars();
		cobits.add(cobit);
		return (dataSize < Constants.minCharsPerPage);
	}
	public void clearCoBits() {
		cobits = null;
		dataSize = 0;
	}
}
