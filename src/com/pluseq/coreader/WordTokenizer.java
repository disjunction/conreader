package com.pluseq.coreader;

import java.util.ArrayList;
import java.util.List;


public class WordTokenizer {
	
	public static List<CoBit> breakWord(String word) {
		int syllableSize = 4;
		List<CoBit> syllables = new ArrayList<CoBit>();
		while (word.length() > syllableSize + 2) {
			syllables.add(new CoBit(word.substring(0, syllableSize)));
			word = word.substring(syllableSize);
		}
		if (word.length() > 0) {
			syllables.add(new CoBit(word));
		}
		return syllables;
	}
}
