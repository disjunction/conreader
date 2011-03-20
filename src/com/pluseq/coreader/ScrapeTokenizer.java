package com.pluseq.coreader;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.util.Log;

public class ScrapeTokenizer extends StringTokenizer{

	public ArrayList<String> clearPrefixPatterns = new ArrayList<String>();
	
	public ScrapeTokenizer(String string, String separator, boolean bool) {
		super(string, separator, bool);
	}
	
	public ScrapeTokenizer(String string, String separator, ArrayList<CoRule> lineRules) {
		this(string, separator, true);
		if (null != lineRules) {
			for (CoRule rule: lineRules) {
				if (rule.type == CoRule.LEFT_TRIM) {
					clearPrefixPatterns.add(rule.stringArg1);
					Log.i("ScrapeTokenizer", "Added rule with preceder: '" + rule.stringArg1 + "'");
				}
			}
		}
	}
	

	protected int precedingShift = 0;
	
	public int getPrecedingShift() {
		return precedingShift;
	}
	
	@Override
	public String nextToken() {
		String str = super.nextToken();
		precedingShift = 0;
		for (String clearPrefixPattern : clearPrefixPatterns) {
			if (str.length() < clearPrefixPattern.length()) continue;

			String newStr = str.replace("\t", "        ");
			Log.i("ScrapeTokenizer", "1: '" + clearPrefixPattern + "'");
			Log.i("ScrapeTokenizer", "2: '" + str + "'");
			if (newStr.substring(0, clearPrefixPattern.length()).equals(clearPrefixPattern)) {
				int realCounter = 0;
				String composite = "";
				
				while (!composite.equals(clearPrefixPattern)) {
					composite = composite + str.charAt(realCounter);
					composite = composite.replace("\t", "        ");
					realCounter++;
				}
				str = str.substring(realCounter);
				precedingShift = realCounter;
			}
		}
		return str;
	}
	
	public static String getLeftTrimString(String str) {
		int lastPos = 0;
		while (lastPos < str.length() && ParserAbstract.CLASS_SPACES.indexOf(str.charAt(lastPos)) >= 0) {
			lastPos++;
		}
		return str.substring(0, lastPos);
	}
}
