package com.pluseq.coreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Just a book :)
 * @author or
 *
 */
public class Book implements Serializable {
	
	// book settings
	private static final long serialVersionUID = -7393528359939031646L;
	/**
	 * index of current page in PageManager
	 */
	public int currentPageIndex = 0;
	public int currentCoBitIndex = 0;
	public Integer lastPageIndex;
	
	// general book information
	public String author = "folk";
	public String title = "Unknown";
	public String languageCode = "ru";
	public String encoding = "UTF-8";
	public String annotation = "-";
	public String hash;
	public ArrayList<CoRule> rules = new ArrayList<CoRule>();
	
	
	// file details
	public int fileSize = 0;
	public String fileName;
	
	// page manager used to read the book
	protected PageManager pageManager = null;
	
	public void setPageManager(PageManager pm) {
		this.pageManager = pm;
	}
	
	public PageManager getPageManager() {
		return this.pageManager;
	}
	
	public String getSimpleFileName() {
		StringTokenizer st = new StringTokenizer(this.fileName, "/");
		String result = "(undefined)";
		while (st.hasMoreElements()) {
			result = st.nextToken();
		}
		return result;
	}
}
