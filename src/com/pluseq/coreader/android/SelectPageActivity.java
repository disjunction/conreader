package com.pluseq.coreader.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.pluseq.coreader.*;

public class SelectPageActivity extends Activity implements OnClickListener{
    protected void setupHandlers() {
    	Button button = (Button) findViewById(R.id.SelectPageGo);
    	button.setOnClickListener(this);
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	BookView bv = CoConfig.currentActivity.bv;
    	if (null != bv) {
    		bv.book.getPageManager().setIndexAheadLock(true);
    	}
    	
    	setContentView(R.layout.select_page);
    	setupHandlers();
	}
    
	protected ProgressDialog progress;
	private Handler handler = new Handler() { 
		public void handleMessage(Message msg) {
			progress.setMessage(getString(R.string.indexing) + " " +
								(msg.what + 1) + " " +
								getString(R.string.of) + " " +
								(targetPage + 1));
		}
	};
	
	private Integer targetPage = 1;
	
	@Override
	public void onClick(View v) {
		EditText et = (EditText) findViewById(R.id.PageNumber);
		
		try {
			targetPage = Integer.valueOf(et.getText().toString()) - 1;
			if (targetPage < 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			new AlertDialog.Builder(this).setMessage("Incorrect page number").show();
			return;
		}
		
		CoConfig.currentActivity.bv.book.getPageManager().setHandler(handler);
		
		progress = ProgressDialog.show(this, getString(R.string.loading), "indexing...");
		Thread thread =  new Thread(null, GotoPageInBG, "loadBook");
        thread.start();

	}
	
	/*
	 * This thread loads book while progress bar is shown.
	 * No views can be touched during the process
	 */
    private Runnable GotoPageInBG = new Runnable(){
        public void run(){
        	CoConfig.currentActivity.bv.gotoPage(targetPage);
			handler.post(DoReturnRes);
	    }
    };
    
	/**
	 * Handles data returned by LoadBookInBG
	 */
	private Runnable DoReturnRes = new Runnable(){
        public void run(){
        	CoConfig.currentActivity.bv.reloadFrame();
        	CoConfig.currentActivity.bv.book.getPageManager().setHandler(null);
        	CoConfig.currentActivity.bv.book.getPageManager().optimizeCache();
    		progress.dismiss();
    		
    		CoConfig.currentActivity.bv.book.getPageManager().setIndexAheadLock(false);

    		finish();
        }
    };
	
}
