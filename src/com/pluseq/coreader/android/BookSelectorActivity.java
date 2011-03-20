package com.pluseq.coreader.android;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.pluseq.android.FileEncodingDetector;
import com.pluseq.android.FileSelector;
import com.pluseq.coreader.Book;
import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.R;
import com.pluseq.coreader.SourceFileSystem;

public class BookSelectorActivity extends Activity {
	SourceFileSystem source;
	public static int myRequestCode = 5;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	source = new SourceFileSystem(this);
    	setContentView(R.layout.selectbook);
    	LinearLayout ll = (LinearLayout) findViewById(R.id.selectBookSourceSelector);    	
    	LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);    	
    	ll.addView(source.getSourceSelectorView(ll.getContext()), ltp);
    	
    	BookView bv = CoConfig.currentActivity.bv;
    	if (null != bv) {
    		bv.book.getPageManager().setIndexAheadLock(true);
    	}
    }
    
    
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
			case SourceFileSystem.fileSelectorCode:
				if (resultCode == Activity.RESULT_OK) {
					String filePath = data.getStringExtra(FileSelector.filePath);
					source.setFilePath(filePath);
					
					File file = new File(filePath);
					FileEncodingDetector ed = new FileEncodingDetector();
					source.setEncoding(ed.detect(file));
				}
				break;
			case SourceFileSystem.recentFilesCode:
				if (resultCode == Activity.RESULT_OK) {
					Book book = (Book)data.getSerializableExtra("book");
					source.setFilePath(book.fileName);
					source.setEncoding(book.encoding);
				}
				break;
		}
    }
}
