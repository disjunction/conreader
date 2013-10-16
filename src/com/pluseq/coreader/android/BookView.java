/**
 * 
 */
package com.pluseq.coreader.android;

import java.util.ArrayList;
import java.util.Currency;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pluseq.coreader.Book;
import com.pluseq.coreader.BookControllerInterface;
import com.pluseq.coreader.BookElement;
import com.pluseq.coreader.BookFrameEventListener;
import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.CoPage;
import com.pluseq.coreader.CoTheme;
import com.pluseq.coreader.DisplayState;
import com.pluseq.coreader.FrameMapping;
import com.pluseq.coreader.PageManager;
import com.pluseq.coreader.R;
import com.pluseq.coreader.CoBit;

/**
 * The view displaying the book, that is - the current frame of the book 
 * 
 * This is nested in book.xml
 * @author or
 *
 */
public class BookView extends LinearLayout implements BookFrameEventListener, BookControllerInterface {
	protected Book book;
	protected CoPage page;
	protected FrameMapping nextFrameMapping;
	protected FrameMapping previousFrameMapping;
	protected FrameManagerAndroid fm;
	protected DisplayState displayState;

	/**
	 * handles updates of time
	 */
	private Handler timeHandler = new Handler();
	
	/**
	 * handles read-ahead indexing
	 */
	private Handler indexingHandler = new Handler();

	
	public BookView(Context context) {
		super(context);
		//currentFrameMapping = new FrameMapping(0, 0);
	}
	
	protected void updateTimeText() {
		TextView timeText = (TextView)findViewById(R.id.clock);
	    timeText.setText(DateFormat.format("kk:mm", new java.util.Date()));
	    
	    // update bettery level each time, as time is updated
	    TextView tv = (TextView) findViewById(R.id.batteryString);
		tv.setText(" " + String.valueOf(CoConfig.batteryLevel) + "%");
	}
	
	private Runnable updateTimeTask = new Runnable() {
	    public void run() {
	 	    updateTimeText();
	        timeHandler.postDelayed(this, 2000);
	    }
	};
	
	private Runnable indexingTask = new Runnable() {
	    public void run() {
	 	    PageManager pm = book.getPageManager();
	 	    int pageIndex = CoConfig.getCurrentFrameMapping().pageIndex;
	 	    Integer next = pm.indexAhead(pageIndex);
	 	    if (next != null) {
	 	    	TextView indexingText = (TextView)findViewById(R.id.indexingStatus);
	 	    	indexingText.setText("indexing: " + next);
	 	    	indexingHandler.postDelayed(this, 5000);
	 	    }
	    }
	};
	
	public void reloadBook() {
		checkFullScreen(CoConfig.currentActivity.getCoReaderState().fullScreen);		
		reloadFrame();
		book.getPageManager().setIndexAheadLock(false);
	}
	
	public BookView(Context context, Book book, FrameMapping newFM) {
		this(context);
		CoConfig.setCurrentFrameMapping(newFM);
		this.book = book;
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(Color.BLACK);
	}
	
	private void showFrame(BookFrameAndroid bf) {
		
		CoTheme theme = CoConfig.getCoTheme();
		
		if (null == bf) {
			Log.w("BookView", "Cannot load, bookFrame is null ");
			if (fm.containsKey(new Integer(0))) {
				Log.w("BookView", ".... but FM contains zero frame");
			}
			return;
		}
		
		removeAllViews();
				
		LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(displayState.textWidth, displayState.textHeight, 0);
		BookFrameView frameView = new BookFrameView(getContext(), bf);
		frameView.setBookFrameEventListener(this);
		addView(frameView, ltp);
		
		LinearLayout view = (LinearLayout) CoConfig.currentActivity.getLayoutInflater().inflate(R.layout.book_status, null);
		view.setBackgroundColor(theme.getStatusBackground());
		addView(view, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 0));
		TextView tv = (TextView) findViewById(R.id.pageString);
		FrameMapping mapping = CoConfig.getCurrentFrameMapping();
		
		int bookPercent = (mapping.pageIndex + 1) * 100 / (book.getPageManager().getDisplayedLastPageIndex() + 1);
		
		for (int viewId : new int[] {R.id.pageString, R.id.pageStringCaption, R.id.clock}) {
			((TextView) findViewById(viewId)).setTextColor(theme.getPaginationColor());
		}
		tv.setText(": " + String.valueOf(mapping.pageIndex + 1) + " / " + 
				  	  (book.getPageManager().getDisplayedLastPageIndex() + 1) + 
				  	  " (" + bookPercent + "%)"
				  );

		for (int viewId : new int[] {R.id.batteryStringCaption, R.id.batteryString}) {
			((TextView) findViewById(viewId)).setTextColor((CoConfig.batteryLevel < 20)? theme.getBatteryColor() : theme.getBatteryCryticalColor());
		}
		
		tv = (TextView) findViewById(R.id.batteryString);
		tv.setText(" " + String.valueOf(CoConfig.batteryLevel) + "%");
				
		refreshDrawableState();
		CoConfig.getStorage().saveBookPosition(book.hash, mapping.pageIndex, mapping.cobitIndex);
		
		// clock in the bottom status
		timeHandler.removeCallbacks(updateTimeTask);
		updateTimeText();
		timeHandler.postDelayed(updateTimeTask, 2000);
		
		// schedule index ahead task
		/*
		if (!book.getPageManager().areAllParsed()) {
			TextView indexingText = (TextView)findViewById(R.id.indexingStatus);
			indexingText.setVisibility(VISIBLE);
			indexingHandler.removeCallbacks(indexingTask);
			indexingHandler.postDelayed(indexingTask, 4000);
		}
		*/
	}
	
	public void reloadFrame() {
		fm = new FrameManagerAndroid(book.getPageManager());
		fm.setCursor(CoConfig.getCurrentFrameMapping());
		displayState = CoConfig.getDisplayState();
		showFrame((BookFrameAndroid)fm.get(CoConfig.getCurrentFrameMapping()));
	}
		
	@Override
	public void gotoNextFrame() {
		showFrame( (BookFrameAndroid)fm.getNext());
	}

	@Override
	public void gotoPreviousFrame() {
		showFrame( (BookFrameAndroid)fm.getPrevious());
	}

	public void checkFullScreen(boolean fs) {
		if (fs) {
			CoConfig.currentActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			CoConfig.currentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		CoConfig.readDisplayState(fs);
		displayState = CoConfig.getDisplayState();
		if (null != fm) {
			fm.setDisplayState();
		}
	}
	
	@Override
	public void toggleFullscreen() {
		CoReaderState state = CoConfig.currentActivity.getCoReaderState();
		state.fullScreen = !state.fullScreen;
		checkFullScreen(state.fullScreen);
		CoConfig.getStorage().saveStateFullscreen(state.fullScreen);
		reloadFrame();
	}
	
	public void gotoPage(Integer pageId) {
		CoConfig.setCurrentFrameMapping(new FrameMapping(pageId, 0));
		fm.setCursor(CoConfig.getCurrentFrameMapping());
		fm.get(CoConfig.getCurrentFrameMapping());
		int lastPageIndex = book.getPageManager().getLastPageIndex();
		
		if (lastPageIndex < pageId) {
			gotoPage(lastPageIndex);
		}		
	}
	
	@Override
	public void switchPage(Integer pageId) {
		gotoPage(pageId);
		reloadFrame();
		book.getPageManager().optimizeCache();
	}
	
	protected class BookFrameView extends View {

		public int currentLine;
		public int currentOffset;
		public ArrayList<CoBit> seedBits;
		public BookFrameAndroid bookFrame;
		protected BookFrameEventListener bookFrameEventListener;
		
		public void setBookFrameEventListener(BookFrameEventListener bookFrameEventListener) {
			this.bookFrameEventListener = bookFrameEventListener;
		}
		
		public void reset() {
			seedBits = new ArrayList<CoBit>();
			currentLine = 0;
			currentOffset  = 0;
		}
		
		public BookFrameView(Context context) {
			super(context);
			reset();
		}

		public BookFrameView(Context context, BookFrameAndroid bookFrame) {
			this(context);
			this.bookFrame = bookFrame;
		}
		
		protected int downX;
		protected int downY;
		
		@Override 
		public boolean dispatchTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
	            downX = (int) event.getX();
	            downY = (int) event.getY();
	            return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
            	int distance = (int)event.getY() - downY;
            	if (Math.abs(distance) > 50) {
            		if ((int)event.getY() > downY) {
            			bookFrameEventListener.gotoPreviousFrame();
            		} else {
            			bookFrameEventListener.gotoNextFrame();
            		}
            		return false;
            	}
            	
            	if ((int)event.getY() < displayState.textHeight/3) {
            		bookFrameEventListener.gotoPreviousFrame();
            	} else if ((int)event.getY() < displayState.textHeight/3*2) {
            		bookFrameEventListener.toggleFullscreen();
            	} else {
            		bookFrameEventListener.gotoNextFrame();
            	}
            	return false;
            }
            
            return true;
		}
		
        @Override 
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.parseColor(CoConfig.getCoTheme().color.get(BookElement.CANVAS_BACKGROUND)));
            //canvas.translate(0, 20);
            
            FontSettings fs = CoConfig.getFontSettings();
            
            for (PlacedCoBit cobit : bookFrame) {
            	Paint mPaint = fs.getPaint(cobit.fontType);
            	if (cobit.type == CoBit.SYLLABLE || cobit.type == CoBit.SIGN) {
            		canvas.drawText(cobit.content , cobit.x + 1, cobit.y, mPaint);
            	}
            }
            
            setKeepScreenOn(true);
        }
	}
}
