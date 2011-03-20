/**
 * 
 */
package com.pluseq.coreader.android;

import java.util.ArrayList;
import java.util.LinkedList;

import com.pluseq.coreader.BookFrame;
import com.pluseq.coreader.FrameMapping;

/**
 * @author or
 */
public class BookFrameAndroid extends LinkedList<PlacedCoBit> implements BookFrame {

	private static final long serialVersionUID = 1L;
	public FrameMapping nextFrameMapping;
	
	
	public void lineFeed(ArrayList<PlacedCoBit> line, int y, int align) {
		
		for (PlacedCoBit pcobit : line) {
			add(pcobit);
		}
	}

	@Override
	public FrameMapping getNextFrameMapping() {
		return nextFrameMapping;
	}	
}
