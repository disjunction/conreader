/**
 * 
 */
package com.pluseq.coreader.android;
import com.pluseq.coreader.Book;
import com.pluseq.coreader.CoReaderStateInterface;
import com.pluseq.coreader.DisplayState;
import com.pluseq.coreader.FrameMapping;
import com.pluseq.coreader.R;

/**
 * Stores state details, like orientation and book position
 * @author or
 */
public class CoReaderState implements CoReaderStateInterface {
	public FrameMapping currentFrameMapping;
	public DisplayState currentDisplayState;
	public Book book = null;
	public int currentWindow = 0;
	public boolean fullScreen = false;
	
	public CoReaderState() {
		currentWindow = R.layout.main;
		currentFrameMapping = new FrameMapping(0, 0);
	}
}
