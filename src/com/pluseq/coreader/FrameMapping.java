/**
 * 
 */
package com.pluseq.coreader;

/**
 * Defines a mapping of frame within a book. 
 * 
 * This is used as a key for FrameManager hash table
 * @author or
 *
 */
public class FrameMapping {
	public FrameMapping(Integer pageIndex, int cobitIndex) {
		this.pageIndex = pageIndex;
		this.cobitIndex = cobitIndex;
	}
	public FrameMapping(int pageIndex, int cobitIndex) {
		this.pageIndex = pageIndex;
		this.cobitIndex = cobitIndex;
	}
	public FrameMapping() {
		this(0, 0);
	}
	/*
	public FrameMapping(int pageIndex, int cobitIndex, int orderNumber) {
		this(pageIndex, cobitIndex);
		this.orderNumber = orderNumber;
	}
	*/
	public Integer getCursorHash() {
		return pageIndex * Constants.maxCharsPerPage + cobitIndex;
	}
	
	public int pageIndex;
	public int cobitIndex;
	public Integer orderNumber = null;
}