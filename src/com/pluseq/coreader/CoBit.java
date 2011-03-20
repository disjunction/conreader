package com.pluseq.coreader;

/**
 * Describes atomic parts - which are components of a page
 * @author or
 */
public class CoBit {
	public static final int NONE = 0;
	public static final int WORD = 1;
	public static final int SYLLABLE = 2;
	public static final int SIGN = 3;
	public static final int SPACE = 4;
	public static final int TAG = 5;
	public static final int SYSTEM = 6;

	public static final String SYSTEM_EOF = "__EOF__";
	
	public static final String TAG_BR = ".br";
	public static final String TAG_P = "p";
	
	public int type;
	public String content = "";
	
	public CoBit() {
		type = CoBit.NONE;
	}
	
	public CoBit(String content) {
		this.content = content;
		type = CoBit.SYLLABLE;
	}
	
	public CoBit(String content, int type) {
		this.content = content;
		this.type = type;
	}
	
	public int measureChars() {
		if (type < CoBit.TAG) {
			return content.length();
		}
		return 0;
	}
}
