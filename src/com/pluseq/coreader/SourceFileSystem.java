package com.pluseq.coreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class SourceFileSystem implements SourceInterface, OnClickListener {
	public static final int fileSelectorCode = 6;
	public static final int recentFilesCode = 7;
	
	protected String fileName = "";
	protected int bytesRead = 0;
	protected int fileSize = 0;
	protected Activity caller;
	protected EditText selectFileName;
	protected Spinner encoding = null;
	protected String selectedEncoding = "auto";
	
	public SourceFileSystem() {
		this(CoConfig.currentActivity);
	}
	public SourceFileSystem(Activity caller) {
		this.caller = caller;
	}
	
	@Override
	public View getSourceSelectorView(Context context) {
		  
		LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 0);
		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);

		OnClickListener browseListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startFileSelector = new Intent();
				startFileSelector.setClassName(caller, "com.pluseq.android.FileSelector");
				startFileSelector.putExtra("jailPath", CoConfig.getJailDirectory());
 				caller.startActivityForResult(startFileSelector, SourceFileSystem.fileSelectorCode );
			}
		};
		
		OnClickListener recentListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startRecentFiles = new Intent();
				startRecentFiles.setClassName(caller, "com.pluseq.coreader.android.RecentFilesActivity");
 				caller.startActivityForResult(startRecentFiles, SourceFileSystem.recentFilesCode);
			}
		};
		
		Button recent = new Button(ll.getContext());
		recent.setText(R.string.recentFiles);
		recent.setOnClickListener(recentListener);
		ll.addView(recent, ltp);
		
		
		selectFileName = new EditText(ll.getContext());
		selectFileName.setText(fileName);
		ll.addView(selectFileName, ltp);
		
		Button browse = new Button(ll.getContext());
		browse.setText(R.string.browse);
		browse.setOnClickListener(browseListener);
		ll.addView(browse, ltp);
		
		String[] encodingArray = {"UTF-8", "KOI8-R", "windows-1251", "windows-1252", "iso-8859-1"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ll.getContext(), R.layout.spinner_item, encodingArray);	
		encoding = new Spinner(ll.getContext());
		encoding.setAdapter(adapter);
		ll.addView(encoding, ltp);
		
		Button b = new Button(ll.getContext());
		b.setText(R.string.load);
		b.setOnClickListener(this);
		ll.addView(b, ltp);		
		
		return ll;
	}
	
	public String getFileId() {
		return "file://" + fileName + ";charset=" + selectedEncoding;
	}
	
	protected static String getMd5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1,messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32)
                md5 = "0" + md5;

            return md5;

        } catch(NoSuchAlgorithmException e) {
            Log.e("MD5", e.getMessage());
            return null;
        }
    }
	
	public void openBook(Book book) {
		this.fileName = book.fileName;
		this.selectedEncoding = book.encoding;
		openBook();
	}
	
	protected ScrapeManagerInterface readFile() {
		ScrapeManagerInMemory smim = new ScrapeManagerInMemory();
		try {
			smim.setHash(getBookHash());
			FileInputStream fIn = new FileInputStream(fileName);
			
			// Read file with UTF-8
			//InputStreamReader isr = new InputStreamReader(fIn,"UTF-8");          
			String useEncoding = selectedEncoding.toString();
	
			if (useEncoding.equals("auto")) {
				useEncoding = "UTF-8";
			}
			
			InputStreamReader isr = new InputStreamReader(fIn, useEncoding);
			
			BufferedReader in = new BufferedReader(isr, Constants.charsPerScrape);
			Scrape scrape;
			
			CharBuffer cb = CharBuffer.allocate(Constants.charsPerScrape);
				
			int read;
			int scrapeCounter = 0;

			do {
				read = in.read(cb);
				
				if (read > 0) {
					scrape= new Scrape(cb, read);
					smim.add(scrape);
					cb = CharBuffer.allocate(Constants.charsPerScrape);
					scrapeCounter++;
				}
			} while (read > 0 && scrapeCounter < 100);			
			
			in.close();
			isr.close();
			fIn.close();
			
			smim.initIterator();

		} catch (Exception e) {
			Log.e("SourceFileSystem", "error reading file ", e);
			return null;
		}
		
		return smim;
	}
	
	@Override
	public void setFilePath(String filePath) {
		selectFileName.setText(filePath);
	}
	
	@Override
	public void setEncoding(String encodingParam) {
		for (int i=0; i<encoding.getCount(); i++) {
			if (((String)encoding.getItemAtPosition(i)).equals(encodingParam)) {
				encoding.setSelection(i);
				return;
			}
		}
	}
	
	/**
	 * starts reader thread (see below)
	 */
	public void openBook() {
		progress = ProgressDialog.show(caller, caller.getString(R.string.loading), fileName);
		Thread thread =  new Thread(null, LoadBookInBG, "loadBook");
        thread.start();
	}
	
	@Override
	public void onClick(View v) {
		fileName = selectFileName.getText().toString();
		selectedEncoding = (String) encoding.getSelectedItem();
		openBook();
	}
	
	private ProgressDialog progress;
	private ScrapeManagerInterface loadedSM;
	private Handler handler = new Handler(); 
	
	/*
	 * This thread loads book while progress bar is shown.
	 * No views can be touched during the process
	 */
    private Runnable LoadBookInBG = new Runnable(){
        public void run(){
        	loadedSM = readFile();
			handler.post(DoReturnRes); 
	    }
    }; 
	
	/**
	 * Handles data returned by LoadBookInBG
	 */
	private Runnable DoReturnRes = new Runnable(){
        public void run(){
        	if (null != loadedSM) {
        		caller = null;
        		
        		CoStorage storage = CoConfig.getStorage();
        		String fileId = getFileId();
        		String hash = getBookHash();
        		
        		BookManager bm = new BookManager();
        		Book book = bm.loadByHash(hash);
        		if (null == book) {
        			book = bm.create(fileId, 0, 0);
        			storage.saveBook(fileId, getBookHash(), new FrameMapping());
        			Log.i("SourceFileSystem", "book saved with hash=" + getBookHash() + " and fileId=" + fileId);
        		}
        		
        		CoConfig.currentActivity.onBookSelected(loadedSM, book);        		
        		progress.dismiss();
        	} else {
        		progress.dismiss();
        		new AlertDialog.Builder(caller).setMessage("Can't read the file " + String.valueOf(fileName)).show();
        	}
        }
    };

	@Override
	public String getBookHash() {
		File f = new File(fileName);
		
		String input = getFileId() + ";constants=" + Constants.charsPerScrape + "," +Constants.minCharsPerPage + ";time=" + f.lastModified();
		return getMd5Hash(input);
	} 
}