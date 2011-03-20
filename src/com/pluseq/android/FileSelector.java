package com.pluseq.android;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.pluseq.coreader.R;

public class FileSelector extends ListActivity {
	
	protected String jailPath = "/sdcard/";
	
	private List<String> items = null;
	private boolean isRoot = true;
	public static final String filePath = "filePath";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("jailPath")) {
        	jailPath = extras.getString("jailPath");
        }
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,  
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        setContentView(R.layout.fs_directory_list);
        fillWithRoot();
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//Log.i("FileSelector", "Selected " + id);
		try {
			if (id == 0 && !isRoot) {
				fillWithRoot();
			} else {
				File file = new File(items.get((int) id));
				if (file.isDirectory())
					fill(file.listFiles(), file.getCanonicalPath().toString() != "/");
				else {
					Intent resultIntent = new Intent();
					resultIntent.putExtra(FileSelector.filePath, file.getCanonicalPath().toString());
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
				}
			}
		} catch (IOException e) {
			Toast toast = Toast.makeText(getApplicationContext(), "failed selecting book", Toast.LENGTH_LONG);
			toast.show();
			Log.w("FileSelector", e);
		}
	}

    @SuppressWarnings("unchecked")
	private class ByFileName implements Comparator {
    	public int compare(Object f1, Object f2) {
			int sdif = ((File)f1).getPath().compareToIgnoreCase(((File)f2).getPath());
			return sdif;
	 	}
	} 
    
    private void fillWithRoot() {
    	try {
    		File root = new File(jailPath);
    		if (!root.isDirectory()) {
    			Toast toast = Toast.makeText(getApplicationContext(), "invalid directory " + jailPath, Toast.LENGTH_LONG);
    			toast.show();
    			finish();
    		}
    		fill(root.listFiles(), false);
    	} catch (Exception e) {
    		Toast toast = Toast.makeText(getApplicationContext(), "failed listing contents of " + jailPath, Toast.LENGTH_LONG);
			toast.show();
    		Log.i("File selector", "oops");
    	}
    }

	@SuppressWarnings("unchecked")
	private void fill(File[] files, boolean addTop) {
		ArrayList<File> fileList = new ArrayList<File>();
		for (File file : files) {
			fileList.add(file);
		}
    	Collections.sort(fileList, new ByFileName());
    	
		isRoot = !addTop;
		items = new ArrayList<String>();
		if (addTop) {
			items.add(getString(R.string.fs_to_top));
		}
		for (File file : fileList) 
			items.add(file.getPath());
		ArrayAdapter<String> filePathList = new ArrayAdapter<String>(this, R.layout.fs_file_row, items);
		setListAdapter(filePathList);
	}
}