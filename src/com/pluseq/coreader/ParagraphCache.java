package com.pluseq.coreader;

import java.util.ArrayList;

public class ParagraphCache extends ArrayList<FrameMapping> {
	private static final long serialVersionUID = 1L;
	public void addFrameMapping(FrameMapping fm) {
		this.set(fm.getCursorHash(), fm);
	}
	
	public void addFrameMapping(int pageIndex, int cobitIndex) {
		FrameMapping fm = new FrameMapping(pageIndex, cobitIndex);
		this.add(fm);
	}
}
