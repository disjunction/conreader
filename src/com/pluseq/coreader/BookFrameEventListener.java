package com.pluseq.coreader;
/**
 * Callbacks from BookFrameView (usually clicks and scrolls)
 * @author or
 */
public interface BookFrameEventListener {
	public void gotoNextFrame();
	public void gotoPreviousFrame();
	public void toggleFullscreen();
}
