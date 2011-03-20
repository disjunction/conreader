package com.pluseq.coreader;

public class CoRule {
	public static final int NONE = 0;
	
	public static final int KIND_LINE_FILTER = 1;
	public static final int KIND_PARAGRAPHING = 2;
	
	public static final int LEFT_TRIM = 1;
	
	public static final int TEXT = 2;
	public static final int EVERY_LINE = 3;
	
	public int kind = CoRule.NONE;
	public int type = CoRule.NONE;
	
	public int intArg1 = 0;
	public int intArg2 = 0;
	public String stringArg1 = null;
	public String stringArg2 = null;
	
	public FrameMapping startPosition = null;
	public FrameMapping stopPosition = null;
	
	public CoRule(int kind, int type) {
		this.kind = kind;
		this.type = type;
	}
}
