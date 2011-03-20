/**
 * 
 */
package com.pluseq.coreader;

import java.nio.CharBuffer;

/**
 * Atomic part in terms of physical file reading 
 * @author or
 */
public class Scrape {
	protected String data;
	protected CharBuffer cb; 
	int size;
	
	@Override
	public String toString() {
		if (null == data) {
			cb.rewind();
			data = cb.toString().substring(0, size);
		}
		return data;
	}
	
	public Scrape() {
		
	}
	
	public Scrape(String fromString) {
		data = fromString;
	}
	public Scrape(CharBuffer fromCharBuffer, int size) {
		cb = fromCharBuffer;
		this.size = size;
	}
	
}
