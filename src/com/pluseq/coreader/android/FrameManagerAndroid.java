package com.pluseq.coreader.android;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import android.graphics.Paint;
import android.util.Log;

import com.pluseq.coreader.BookFrame;
import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.CoPage;
import com.pluseq.coreader.DisplayState;
import com.pluseq.coreader.FrameManagerAbstract;
import com.pluseq.coreader.FrameMapping;
import com.pluseq.coreader.PageManager;
import com.pluseq.coreader.CoBit;

/**
 * Generates new frames and manages frame cache
 * @see BookFrame
 * @author or
 *
 */
public class FrameManagerAndroid extends FrameManagerAbstract {

	private int frameWidth = 200;
	private int frameHeight = 200;
	private static final long serialVersionUID = 1L;
	protected int lineMargin;

	public void setDisplayState() {
		DisplayState displayState = CoConfig.getDisplayState();
		frameWidth = displayState.textWidth - 4;
		frameHeight = displayState.textHeight;
	}
	
	public FrameManagerAndroid(PageManager pm) {
		super(pm);
		fontSettings = CoConfig.getFontSettings();
		lineMargin = 2 * (fontSettings.getFontHeight() + fontSettings.getLineSpacing()) + 5; 
		setDisplayState();
		heatMeasureCache();
	}
	
	private final int ALIGN_LEFT = 0;
	private final int ALIGN_JUSTIFY = 1;

	/**
	 * Currently generated BookFrame
	 */
	BookFrameAndroid bf;
	/**
	 * queue of candidates to BookFrame
	 */
	protected ArrayList<PlacedCoBit> line;
	/**
	 * current draw X
	 */
	protected int x;
	/**
	 * current draw Y
	 */
	protected int y;
	protected boolean lineHasContent = false;
	protected FontSettings fontSettings;
	
	private ArrayList<PlacedCoBit> justifyLine(ArrayList<PlacedCoBit> line) {
		if (0 == line.size()) {
			return line;
		}
		
		int spaces = 0;
		float contentSize = line.get(0).x;
		
		// remove trailing spaces
		for (int i=line.size()-1; i>=0; i--) {
			if (line.get(i).type == CoBit.SPACE) {
				line.remove(i);
			} else {
				break;
			}
		}
		
		if (0 == line.size()) {
			return line;
		}
		
		// Calculate how many spaces we have, and how many space we can spend on them
		for (PlacedCoBit cobit : line ) {
			if (cobit.type == CoBit.SIGN || cobit.type == CoBit.SYLLABLE) {
				contentSize += cobit.width;
			} else if (cobit.type == CoBit.SPACE) {
				spaces++;
			}
		}
		
		float pixelsPerSpace = (frameWidth - contentSize) / spaces;
		float newOffset = line.get(0).x;
		
		ArrayList<PlacedCoBit> newLine = new ArrayList<PlacedCoBit>();
		
		for (PlacedCoBit cobit : line ) {
			if (cobit.type == CoBit.SIGN || cobit.type == CoBit.SYLLABLE) {
				cobit.x = (short) newOffset; 
				newOffset += cobit.width;
				newLine.add(cobit);
			} else {
				newOffset += pixelsPerSpace;
			}
		}
		
		return newLine;
	}
	
	public boolean lineFeed(ArrayList<PlacedCoBit> line, int y, int align) {		
		bf.lineFeed(line, y, align);
		return y + lineMargin <= frameHeight;
	}
	
	private boolean nextLine(int newX, int yShift, int align) {
		if (align == ALIGN_JUSTIFY) {
			line = justifyLine(line);
		}
		boolean doContinue = lineFeed(line, y, align);
		x = newX;
		y += yShift;
		line = new ArrayList<PlacedCoBit>();
		lineHasContent = false;
		return doContinue; 
	}
	
	private Hashtable<String, Float> measureCacheByString = new Hashtable<String, Float>(); 
	
	private void heatMeasureCache() {
		if (measureCacheByString.size() == 0) {
			Paint paint = fontSettings.getPaint(FontSettings.NORMAL);
			String symbols = " ,.:\"!?;'()";
			int length = symbols.length();
			String substr;
			for (int i=0; i<length; i++) {
				substr = symbols.substring(i, i + 1);
				measureCacheByString.put(substr, paint.measureText(substr));
			}
		}
	}
	
	private BookFrameAndroid makeBookFrame(FrameMapping fm, boolean useCobithash) {
		pm.setCursor(fm);

		bf = new BookFrameAndroid();
		x = y = 0;
		line = new ArrayList<PlacedCoBit>();
		
		CoBit cobit;
		PlacedCoBit placedCoBit = null;
		PlacedCoBit previous;
		
		Paint paint = fontSettings.getPaint(FontSettings.NORMAL);
		float measure;
		int fontHeight = fontSettings.getFontHeight();
		int lineHeight = fontHeight + fontSettings.getLineSpacing();
		
		boolean doContinue = true;
		
		int cobitCounter = 0;
		do {
			cobitCounter++;
			cobit = pm.getNextCoBit();
			if (null != cobit) {

				switch (cobit.type) {
				case CoBit.SYLLABLE:
					paint = fontSettings.getPaint(FontSettings.NORMAL);
					break;
					
				case CoBit.TAG:
					if (cobitCounter == 1) {
						x = fontSettings.getParagraphOffset();
						continue;
					}
					if (cobit.content == CoBit.TAG_P) {
						doContinue = nextLine(fontSettings.getParagraphOffset(), lineHeight + fontSettings.getParagraphSpacing(), ALIGN_LEFT);
						if (!doContinue) {
							pm.setCursor(pm.getCursorFrameMapping(), -1);
						}
					}
				}
				
				if (doContinue && (cobit.type == CoBit.SPACE || cobit.type == CoBit.SYLLABLE || cobit.type == CoBit.SIGN)) {
					
					//check if we have a predefined width for this
					Float checkMeasure = measureCacheByString.get(cobit.content);
					
					measure = (checkMeasure == null)? paint.measureText(cobit.content) : checkMeasure;
					
					if (x + measure > frameWidth) {
						if (cobit.type == CoBit.SYLLABLE && line.size() > 0) {
							previous = line.get(line.size()-1);
							if (previous.type == CoBit.SYLLABLE) {
								previous.content += "-";
								previous.width = paint.measureText(previous.content);
							}
						}
						doContinue = nextLine(0, lineHeight, ALIGN_JUSTIFY);
					}
					
					
					if (cobit.type == CoBit.SYLLABLE || cobit.type == CoBit.SIGN) {
						lineHasContent = true;
					}
					
					// this protects from spaces to be added in the line beginning
					if (lineHasContent) {
						// if this wasnt critical syllable, then add. Otherwise roll it back
						if (doContinue) {
							placedCoBit = new PlacedCoBit(cobit, x, y, FontSettings.NORMAL, measure);
							line.add(placedCoBit);
							x += measure;
						} else {
							pm.goBack(1);
						}
					}
				}
				
			} else {
				Log.w("FrameManagerAndroid", "null cobit!");
				doContinue = false;
			}
		} while (doContinue);

		// if last cobit was syllable, we should check if it was in the middle of a word
		if (placedCoBit != null && placedCoBit.type == CoBit.SYSTEM) {
			CoBit next = pm.getNextCoBit();
			if (next.type == CoBit.SYLLABLE) {
				placedCoBit.content += "-";	
				placedCoBit.width = paint.measureText(placedCoBit.content);
			}
		}
		
		if (line.size() > 0) {
			lineFeed(line, y, ALIGN_JUSTIFY);			
		}
		bf.nextFrameMapping = pm.getCursorFrameMapping();
		return bf;
	}
		
	@Override
	public BookFrame get(FrameMapping fm) {
		CoConfig.setCurrentFrameMapping(fm);
		Log.w("FrameManagerAndroid", "cursor: " + cursorOrder + ", fm.cobitIndex: " + fm.cobitIndex + ", fm.pageIndex: " + fm.pageIndex);		
		BookFrame bf = super.get(fm);

		if (null == bf) {
			bf = makeBookFrame(fm, false);
			put(fm, bf);
		}
		if (!orderedFrameMappings.containsKey(cursorOrder+1)) {
			FrameMapping nextFrameMapping = pm.getCursorFrameMapping();
			orderedFrameMappings.put(cursorOrder+1, nextFrameMapping);
			Log.w("FrameManagerAndroid", "NEXT::: cursor: " + (cursorOrder+1) + ", fm.cobitIndex: " + nextFrameMapping.cobitIndex + ", fm.pageIndex: " + nextFrameMapping.pageIndex);
			//nextFrameMapping = null;
		}
		return bf;
	}
	
	/**
	 * @FEXME I'm ugly!
	 */
	@Override
	public BookFrame getPrevious() {
		BookFrame bf = super.getPrevious();
		if (null == bf) {
			FrameMapping currentFrameMapping = orderedFrameMappings.get(cursorOrder);
			int currentHash = currentFrameMapping.getCursorHash();
			
			Log.w("FrameManagerAndroid", "currentHash: " + currentHash);
			
			if (currentHash == 0) {
				return super.get(currentFrameMapping);
			}
			
			BookFrameAndroid currentBF = (BookFrameAndroid)get(currentFrameMapping);
			CoPage currentPage = pm.get(currentFrameMapping.pageIndex);
			
			int estimateDifference = 300;
			if (currentBF.nextFrameMapping != null) {
				estimateDifference = currentBF.nextFrameMapping.cobitIndex - currentFrameMapping.cobitIndex;
				if (currentBF.nextFrameMapping.pageIndex > currentFrameMapping.pageIndex) {
					estimateDifference += currentPage.cobits.size();
				}
				if (estimateDifference < 100 || estimateDifference > 1000) {
					estimateDifference = 300;
				}
			}
			
			estimateDifference -= 20;
			
			int newCursorHash;
			FrameMapping newFrameMapping;
			
			int counter = 0;
			do {
				counter++;
				estimateDifference += 20;
				pm.setCursor(currentFrameMapping, -estimateDifference);
				newFrameMapping = pm.getCursorFrameMapping();
				bf = makeBookFrame(newFrameMapping, true);
				newCursorHash = pm.getCursorHash();
				Log.w("FrameManagerAndroid", "backtrack  fm.cobitIndex: " + newFrameMapping.cobitIndex + ", fm.pageIndex: " + newFrameMapping.pageIndex);
			} while (newCursorHash > currentHash && newFrameMapping.getCursorHash() > 0);
			
			counter = 0;
			estimateDifference = 2;
			
			//FrameMapping oldFrameMapping = newFrameMapping;
			
			while (counter < 30 && newCursorHash < currentHash) {
				counter++;
				//oldFrameMapping = newFrameMapping;
				//bf = tryBF;
				pm.setCursor(newFrameMapping, estimateDifference);
				newFrameMapping = pm.getCursorFrameMapping();
				bf = makeBookFrame(newFrameMapping, true);
				newCursorHash = pm.getCursorHash();
				Log.w("FrameManagerAndroid", "forward  fm.cobitIndex: " + newFrameMapping.cobitIndex + ", fm.pageIndex: " + newFrameMapping.pageIndex + ", the hash: " + newCursorHash);
			}
			
			Log.w("FrameManagerAndroid", "Final hash: " + ((BookFrameAndroid)bf).nextFrameMapping.getCursorHash());
			
			cursorOrder--;
			orderedFrameMappings.clear();
			orderedFrameMappings.put(cursorOrder, newFrameMapping);
			put(newFrameMapping, bf);
			CoConfig.setCurrentFrameMapping(newFrameMapping);
			orderedFrameMappings.put(cursorOrder+1, bf.getNextFrameMapping());
		}

		return bf;
	}
	
	/**
	 * This is possibly not needed
	 * @deprecated
	 * @param after - frame cursor
	 */
	public void clearOrderedMapAfter(int after) {
		Enumeration<Integer> keys = orderedFrameMappings.keys();
		while (keys.hasMoreElements()) {
			Integer key = keys.nextElement();
			if (key > after) {
				orderedFrameMappings.remove(key);
			}
		}
	}
}
