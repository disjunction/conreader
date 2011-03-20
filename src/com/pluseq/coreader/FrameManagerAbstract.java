package com.pluseq.coreader;
import java.util.HashMap;
import java.util.Hashtable;

import android.util.Log;


/**
 * Builds/caches new BookFrames
 * 
 * Should be implemented separately for different view medias
 * @author or
 */
public abstract class FrameManagerAbstract extends HashMap<FrameMapping, BookFrame> {
	private static final long serialVersionUID = 1L;
	/**
	 * stores frameMapping, according to their order for current settings
	 */
	protected Hashtable<Integer, FrameMapping> orderedFrameMappings;
	public Integer cursorOrder = null;
	public ParagraphCache pc = new ParagraphCache();
	
	protected PageManager pm;
	
	public FrameManagerAbstract(PageManager pm) {
		super();
		this.pm = pm;
	}
	
	public BookFrame get(FrameMapping fm) {
		BookFrame bf = super.get(fm);
		return bf;
	}
	
	@Override
	public synchronized void clear() {
		super.clear();
		orderedFrameMappings = new Hashtable();
	}
	
	public void setCursor(FrameMapping fm) {
		cursorOrder = 0;
		clear();
		orderedFrameMappings.put(cursorOrder, fm);
	}
	
	public BookFrame getNext() {
		FrameMapping nextFrameMapping;
		cursorOrder++;
		if (orderedFrameMappings.containsKey(cursorOrder)) {
			nextFrameMapping = orderedFrameMappings.get(cursorOrder);
		} else {
			if (orderedFrameMappings.containsKey(cursorOrder-1)) {
				BookFrame bf = get(orderedFrameMappings.get(cursorOrder-1));
				nextFrameMapping = bf.getNextFrameMapping();
				orderedFrameMappings.put(cursorOrder, nextFrameMapping);
			} else {
				Log.w("FrameManagerAbstract", "bad... we don't have next frame");
				return null;
			}
		}
		return get(nextFrameMapping);
	}
	

	public BookFrame getPrevious() {
		if (orderedFrameMappings.containsKey(cursorOrder-1)) {
			cursorOrder--;
			FrameMapping fm = orderedFrameMappings.get(cursorOrder);
			CoConfig.setCurrentFrameMapping(fm);
			return get(fm);
		}

		return null;
	}
	
	public FrameMapping getCursorFrameMapping() {
		return orderedFrameMappings.get(cursorOrder);
	}
}
