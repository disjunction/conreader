package com.pluseq.coreader.android;

import com.pluseq.coreader.CoBit;

public class PlacedCoBit extends CoBit {
	public short x;
	public short y;
	public Integer fontType;
	public float width;
	
	public PlacedCoBit(String content, int type) {
		super(content, type);
	}
	
	public PlacedCoBit(CoBit cobit, int x, int y, Integer fontType, float width) {
		this(cobit, fontType);
		this.x = (short) x;
		this.y = (short) y;
		this.width = width;
	}

	public PlacedCoBit(CoBit cobit, Integer fontType) {
		this.content = cobit.content;
		this.type = cobit.type;
		this.fontType = fontType;
	}
	
}
