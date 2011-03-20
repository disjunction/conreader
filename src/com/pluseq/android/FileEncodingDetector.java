package com.pluseq.android;
import java.io.*;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import com.pluseq.coreader.*;

/**
 * Detects cyrillic encoding for a given file
 * @author or
 */
public class FileEncodingDetector {
	private String[] encodingArray = {"UTF-8", "KOI8-R", "windows-1251"};
	
	/**
	 * this method works the same way as SourceFileSystem,
	 * so the file reading might look a bit messy
	 * @param file
	 * @param encoding
	 * @return String
	 * @throws Exception 
	 */
	protected String getContents(File file, String encoding) throws Exception {		
		FileInputStream fIn = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fIn, encoding);
		BufferedReader in = new BufferedReader(isr, 5000);
		Scrape scrape;
		
		CharBuffer cb = CharBuffer.allocate(5000);
		int read = in.read(cb);
		
		in.close();
		isr.close();
		fIn.close();
		
		if (read > 0) {
			scrape= new Scrape(cb, read);
			return scrape.toString();
		} else {
			return null;
		}
	}
	
	private final String ruLetters = "абвгдеёжзийклмнопрстуфхцчшщьыъэюя";
	private String ruCapitals = ruLetters.toUpperCase();
	private String[] ruShortWords = new String[]{"я", "он", "и", "под", "над", "в", "за", "а", "о", "по"};
	private List<String> ruShortWordsList = Arrays.asList(ruShortWords);
	
	protected int weightWord(String word) {
		if (ruShortWordsList.contains(word)) {
			return 200;
		}
		
		if (ruShortWordsList.contains(word.toLowerCase())) {
			return 20;
		}
		
		if (word.length() < 3) {
			return 0;
		}

		int result = 0;
		
		// word contains a cyrillic letter
		if (ruLetters.contains(word.substring(0, 1).toLowerCase())) {
			result += 2;
			
			// word starts with a capital letter
			if (ruCapitals.contains(word.substring(0, 1))) {
				result += 10;
				
				// the second letter after the capital is cyrillic lower case letter
				if (ruLetters.contains(word.substring(0, 2))) {
					result += 200;
				}
			}
		}
		
		return result;
	}
	
	public int weightText(File file, String encoding) {
		try {
			String contents = getContents(file, encoding);
			String[] chunks = contents.split("[\\s\\n\\r\\t.,\\[\\]{}()!?@#$%^&*!\"]");
			int result = 0; 
			for (String chunk : chunks) {
				result += weightWord(chunk);
			}
			return result;
		} catch (Exception e) {
			// Log.e("EncodingDetector", "exception while reading file", e);
			
			// no matter what happened, we weight it as "-1" because it was an error
			return -1;	
		}
	}
	
	public String detect(File file) {
		String bestEncoding = "iso-8859-1";
		int weight = 0;
		int newWeight = 0;
		for (String encoding : encodingArray) {
			newWeight = weightText(file, encoding);
			if (newWeight > weight) {
				bestEncoding = encoding;
				weight = newWeight;
			}
		}
		
		return bestEncoding;
	}
}
