package com.pluseq.coreader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.pluseq.coreader.CoBit;
import com.pluseq.coreader.WordTokenizer;

import android.util.Log;

/**
 * Parses plain TXT files
 * @author or
 *
 */
public class ParserText extends ParserAbstract {
	
	protected ArrayList<CoRule> rules;

	protected final int LONG_LINE = 100;
	
	protected void addLeftTrim(String preceeder) {
		CoRule rule = new CoRule(CoRule.KIND_LINE_FILTER, CoRule.LEFT_TRIM);
		rule.stringArg1 = preceeder;
		rules.add(rule);
	}

	protected void addEveryLineParaphing() {
		CoRule rule = new CoRule(CoRule.KIND_PARAGRAPHING, CoRule.EVERY_LINE);
		rules.add(rule);
	}
	
	protected boolean everyLineParagraphing = false;
	
	@Override
	public void initRules() {
		for (CoRule rule : this.rules) {
			if (rule.kind == CoRule.KIND_PARAGRAPHING && rule.type == CoRule.EVERY_LINE) {
				Log.i(this.getClass().getName(), "Every line paragraphing");
				everyLineParagraphing = true;
			}
		}
	}
	
	/**
	 * This method generates CoRules on parser initialization
	 */
	@Override
	public ArrayList<CoRule> preparseLineRules() {
		rules = new ArrayList<CoRule>();
		
		String scrapeStr = sm.getByIndex(cursorScrapeIndex).toString();
		StringTokenizer st = new StringTokenizer(scrapeStr, "\n");

		/**
		 * no data even in the first scrape
		 */
		if (!st.hasMoreTokens())
			return null;

		// find first line with preceding spacing
		String firstLine;
		do {
			// read line and transform to Unix
			firstLine = st.nextToken().replace("\r", "");
			if (firstLine.length() > LONG_LINE) {
				addEveryLineParaphing();
			}
		} while ((firstLine.length() == 0 || ("\t ").indexOf(firstLine.charAt(0)) < 0)	&& st.hasMoreTokens());

		//Log.i("PareserAbstract", "FIRST SPACE LINE: '" + firstLine + "'");
		firstLine = firstLine.replace("\t","        ");
		
		// all lines are empty or no space preceding
		if (firstLine.length() == 0 || firstLine.indexOf(' ') != 0) {
			Log.i("PareserAbstract", "NO LINE PRECEDER");
			return null;
		}

		String preceder = ScrapeTokenizer.getLeftTrimString(firstLine);
		int iterations = 0;
		while (st.hasMoreTokens()) {			
			String token = st.nextToken().replace("\r", "").replace("\t","        ");
			if (token.length() ==0) continue;

			// if book is more than 100 characters long, then the spaces are not 
			// actually the preceeders but paragraphs
			if (token.length() > LONG_LINE) {
				addEveryLineParaphing();
				iterations = 0;
				break;
			}
			
			//Log.i("PareserAbstract", "PRECEDERLOOP: '" + token + "'");
			String candidate = ScrapeTokenizer.getLeftTrimString(token);
			
			if (null == candidate || candidate.length() == 0) {
				//Log.i("PareserAbstract", "ZERO PRECEDER FOUND");
				return null;
			}
			
			if (!candidate.equals(token)) {
				if (candidate.length() < preceder.length()) {
					preceder = candidate;
				}
				iterations++;
			}
		}
		
		
		if (iterations > 5) {
			addLeftTrim(preceder);
		}
		
		this.lineRules = rules;

		return rules;
	}
	
	/**
	 * @return if there is any data
	 */
	@Override
	protected boolean initCursor() {
		currentWordCobits = null;
		currentToken = null;
		currentScrape = null;
		isEnd = false;
		cursorTabChain = false;

		this.currentScrape = sm.getByIndex(cursorScrapeIndex);

		try {
			currentScrapeString = currentScrape.toString();
			scrapeTokenizer = new ScrapeTokenizer(currentScrapeString
					.substring(cursorScrapeOffset),
			// this.CLASS_SPACES + this.CLASS_SIGNS,
					"\n", lineRules);
		} catch (StringIndexOutOfBoundsException e) {
			return false;
		}

		boolean ret = scrapeTokenizer.hasMoreTokens();
		if (ret) {
			Log.v("Parser", "scrape contained " + scrapeTokenizer.countTokens()
					+ " tokens");
			currentToken = scrapeTokenizer.nextToken();
		}

		String preceding = getPreceeding(Constants.preceedingChars);
		int lastLB = preceding.lastIndexOf('\n');
		cursorLineLength = (lastLB >= 0) ? preceding.length() - lastLB
				: Constants.preceedingChars;

		Log.v("Parser", "cursorLineLength initialized as " + cursorLineLength);

		return ret;
	}
	
	/**
	 * That's where the real magic happens
	 */
	public CoBit getNextCoBit() {

		lastCoBitScrapeIndex = cursorScrapeIndex;
		lastCoBitScrapeOffset = cursorScrapeOffset;

		try {
			if (currentWordCobits != null && !currentWordCobits.isEmpty()) {
				return this.shiftAndReturnSyllable(currentWordCobits.poll());
			}

			while (currentToken == null || currentToken.length() == 0) {
				currentToken = scrapeTokenizer.nextToken();
				cursorScrapeOffset += scrapeTokenizer.getPrecedingShift();
			}

			String contentPiece = "";
			int charCount = 1;
			char element;
			int type = CoBit.NONE;
			int elementType;
			int currentTokenLength = currentToken.length();

			while (true) {
				element = currentToken.charAt(charCount - 1);
				elementType = getTypeByElement(element);

				if (type == CoBit.NONE) {
					type = elementType;
				}

				if (type == elementType) {
					contentPiece += element;
				}

				if (currentTokenLength <= charCount || type != elementType) {
					currentToken = currentToken.substring(contentPiece.length());
					currentTokenLength = currentToken.length();
					if (type == CoBit.WORD) {
						List<CoBit> syllables = WordTokenizer.breakWord(contentPiece);
						currentWordCobits = new LinkedList<CoBit>();
						for (CoBit syllable : syllables) {
							currentWordCobits.add(syllable);
						}
						return shiftAndReturnSyllable(currentWordCobits.poll());
					}

					if (type == CoBit.SPACE) {
						if (0 == cursorLineLength) {
							if (!cursorTabChain) {
								cursorTabChain = true;
								return shiftAndReturn(new CoBit(CoBit.TAG_P,
										CoBit.TAG), contentPiece.length());
							} else {
								cursorScrapeOffset += contentPiece.length();
								return getNextCoBit();
							}
						} else {
							cursorTabChain = false;
						}
					}
					if (element == '\t') {
						cursorLineLength = 0;
						return shiftAndReturn(
								new CoBit(CoBit.TAG_P, CoBit.TAG), 1);
					}
					if (element == '\n') {
						if (0 == cursorLineLength || everyLineParagraphing) {
							cursorLineLength = 0;
							return shiftAndReturn(new CoBit(CoBit.TAG_P,
									CoBit.TAG), 1);
						} else {
							cursorLineLength = 0;
							return shiftAndReturn(new CoBit(" ", CoBit.SPACE));
						}
					}

					return shiftAndReturn(new CoBit(contentPiece, type));
				}
				charCount++;
			}

		} catch (NoSuchElementException e) {

			// this shouldn't happen, but just in case
			if (isEnd) {
				return null;
			}

			// if no more tokens in current scrape, then move to the next one
			if (sm.hasIndex(cursorScrapeIndex + 1)) {
				setScrapeCursor(cursorScrapeIndex + 1, 0);
				return getNextCoBit();

			} else {
				// end of book
				isEnd = true;
				return new CoBit(CoBit.SYSTEM_EOF, CoBit.SYSTEM);
			}
		}
	}
}
