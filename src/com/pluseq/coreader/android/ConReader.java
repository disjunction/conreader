package com.pluseq.coreader.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.pluseq.coreader.Book;
import com.pluseq.coreader.BookManager;
import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.FrameMapping;
import com.pluseq.coreader.R;
import com.pluseq.coreader.ScrapeManagerInterface;
import com.pluseq.coreader.SourceFileSystem;
import com.pluseq.coreader.SourceInterface;

public class ConReader extends Activity {
	
	protected CoReaderState coreaderState = null;
	public BookView bv;
	
    @Override
    public Object onRetainNonConfigurationInstance() 
    {
    	CoConfig.currentActivity = null;
        return(coreaderState);
    }
	
    /**
     * This is called by CoConfig, and thus is accessible by other parts
     * @return
     */
    public CoReaderState getCoReaderState() {
    	return coreaderState;
    }
    
    protected void showCurrentState() {
    	switch (coreaderState.currentWindow) {
		case R.layout.selectbook:
			showBookSelector();
			break;
		case R.layout.book:
			if (null != coreaderState.book) {
				if (null != coreaderState.book.getPageManager()) {
					showBook(coreaderState.book);
				} else {
					setContentView(R.layout.main);
					SourceFileSystem s = new SourceFileSystem(this);
					s.openBook(coreaderState.book);
					return;
				}
			} else {
				Log.w("CoReader", "no book for the current book state");
				setContentView(R.layout.main);
			}
			break;
			
		default:
			setContentView(coreaderState.currentWindow);
			break;
		}
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	CoConfig.resetStatic();
    	CoConfig.currentActivity = this;
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	// CoConfig.getStorage().clearPages();
    	
    	if (null == (coreaderState = (CoReaderState)getLastNonConfigurationInstance())) {
    		coreaderState = new CoReaderState();
    		CoConfig.loadState();
    	}
    	
    	try {
        	showCurrentState();
        } catch (Exception e) {
        	Log.e("CoReader", "onCreate failed", e);
        	new AlertDialog.Builder(this).setMessage("Sorry, I'm still in alpha. Report this exception: (in onCreate) " + e.toString()).show();
        }
    }
   
    protected void showBook(Book book) {
    	setContentView(R.layout.book);
    	
    	LinearLayout ll = (LinearLayout) findViewById(R.id.bookLayout);

		bv = new BookView(ll.getContext(), book, coreaderState.currentFrameMapping);
		bv.reloadBook();
    	LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 0);
    	ll.addView(bv, ltp);
    }
    
    public void onBookSelected(ScrapeManagerInterface sm, Book book) {
    	BookManager bm = new BookManager();
    	coreaderState.book = bm.loadBookFromScrapeManager(sm, book);
    	coreaderState.currentFrameMapping = new FrameMapping(coreaderState.book.currentPageIndex , coreaderState.book.currentCoBitIndex);
    	showBook(coreaderState.book);
    	CoConfig.saveState();
    	finishActivity(BookSelectorActivity.myRequestCode);
    }
    
    /**
     * this keeps the current state up to date
     */
    public void setContentView(int layoutResID) {
    	super.setContentView(layoutResID);
    	coreaderState.currentWindow = layoutResID;
    }
    
    private void showBookSelector() {
    	SourceInterface source = new SourceFileSystem(this);
    	setContentView(R.layout.selectbook);
    	LinearLayout ll = (LinearLayout) findViewById(R.id.selectBookSourceSelector);
    	
    	LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	
    	ll.addView(source.getSourceSelectorView(ll.getContext()), ltp);
    }    
    
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Intent myIntent;
    	switch (item.getItemId()) {
    		case 1:
				myIntent = new Intent();
				if (CoConfig.currentActivity.bv != null) {					
					myIntent.setClassName(this, "com.pluseq.coreader.android.SelectPageActivity");
	 				startActivity(myIntent);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(), "opened book expected", Toast.LENGTH_SHORT);
					toast.show();
				}
    			break;
    		case 2:
				myIntent = new Intent();
				myIntent.setClassName(this, "com.pluseq.coreader.android.SettingsActivity");
 				startActivity(myIntent);   
    			break;
    		case 3:
    			myIntent = new Intent();
				myIntent.setClassName(this, "com.pluseq.coreader.android.BookSelectorActivity");
 				startActivityForResult(myIntent, BookSelectorActivity.myRequestCode);   
    			break;
    		case 4:
    			CoConfig.switchProfile();
    			CoConfig.currentActivity.bv.reloadFrame();
    			Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.switchedToProfile) +
    										 " " + CoConfig.getProfileIndex(), Toast.LENGTH_SHORT);
    	    	toast.show();
    			break;
    	}
    	return true;
    }
    
	public boolean onCreateOptionsMenu(Menu menu) {
	     super.onCreateOptionsMenu(menu);
	     
	     MenuItem item;
	     
	     item = menu.add(0, 1, 1, R.string.gotopage);
	     item.setIcon(android.R.drawable.ic_menu_mylocation);
	     
	     item = menu.add(0, 2, 2, R.string.settings);
	     item.setIcon(android.R.drawable.ic_menu_preferences);
	     
	     item = menu.add(0, 3, 3, R.string.open);
	     item.setIcon(android.R.drawable.ic_menu_search);
	     
	     item = menu.add(0, 4, 4, R.string.switchProfile);
	     item.setIcon(android.R.drawable.ic_menu_rotate);
	     
	     return true;
	}
	
	
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                  int level = intent.getIntExtra("level", 0);
                  int scale = intent.getIntExtra("scale", 100);
                  CoConfig.batteryLevel = level * 100 / scale;
             }
        }
   };
   
   @Override
   public void onResume() {
        super.onResume();
        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
   }
}