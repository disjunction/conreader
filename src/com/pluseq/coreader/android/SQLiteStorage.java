package com.pluseq.coreader.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcelable.Creator;
import android.util.Log;

import com.pluseq.coreader.CoReaderStateInterface;
import com.pluseq.coreader.CoStorage;
import com.pluseq.coreader.FrameMapping;
import com.pluseq.coreader.R;

public class SQLiteStorage implements CoStorage {
	protected static final String databaseName = "coreader";
	protected Context context;
	public void setContext(Context context) {
		this.context = context;
	}
	
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "SQLiteStorage";
    private DatabaseHelper dbHelper;

    private static final int DATABASE_VERSION = 8;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
    	public SQLiteDatabase sourceDB;
    	
    	public void execSQL(String sql, SQLiteDatabase db) throws SQLException {
        	Log.i("SQLiteSource", "execSQL: " + sql);
        	db.execSQL(sql);
        }
        
    	DatabaseHelper(Context context) {
            super(context, databaseName, null, DATABASE_VERSION);
            sourceDB = getWritableDatabase();
            Log.i("SQLiteSource", "started...");            
        }

    	protected void createSettingTable(SQLiteDatabase db) {
            execSQL("DROP TABLE IF EXISTS setting", db);
        	execSQL("create table setting (name varchar(32), value varchar(32), profileIndex int default 0);", db);
            execSQL("insert into state(currentWindow,bookHash) values(" + R.layout.main + ",'');", db);
    	}

    	protected void createStateTable(SQLiteDatabase db) {
            execSQL("DROP TABLE IF EXISTS state", db);
        	execSQL("create table state (currentWindow int, bookHash char(32), themeName varchar(30) default 'BlackOnWhite', fontFactor int default 3, fullscreen int default 0, keepScreenOn int(1) default 1, autoLoadBook int(1) default 1);", db);
            execSQL("insert into state(currentWindow,bookHash) values(" + R.layout.main + ",'');", db);
    	}
    	
    	protected void createBookTables(SQLiteDatabase db) {
            execSQL("DROP TABLE IF EXISTS book", db);
            execSQL("DROP TABLE IF EXISTS page", db);
        	execSQL("create table book (hash char(32) primary key, "
        	        + "accessTime int, fileId varchar(255), pageIndex int default 0, cobitIndex int default 0, lastPageIndex int null default null);", db);
            execSQL("create table page (bookHash char(32), pageIndex int, scrapeIndex int, scrapeOffset int, finishScrapeIndex int, finishScrapeOffset);", db);
    	}
    	
    	@Override
        public void onCreate(SQLiteDatabase db) {
    		createBookTables(db);
            createStateTable(db);
            createSettingTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	if (oldVersion < 5) {
        		createBookTables(db);
        	}
        	if (oldVersion < 7) {
        		createStateTable(db);
        	}
        	if (oldVersion < 8) {
        		createSettingTable(db);
        	}
       	
        	Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        }
    }
    
	@Override
	public CoReaderStateInterface getCoReaderState() {
		return null;
	}
	
	@Override
    public CoStorage open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        //dbHelper.sourceDB = dbHelper.getWritableDatabase();
        //dbHelper.onUpgrade(dbHelper.sourceDB, 1, 2);
        return this;
    }
    
	@Override
    public void close() {
        dbHelper.close();
    }
	
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param context the Context within which to work
     */
    public SQLiteStorage(Context context) {
        this.context = context;
    }

    @Override
	public void saveState(int currentWindow, String bookHash) {
    	ContentValues values = new ContentValues();
        values.put("currentWindow", currentWindow);
        values.put("bookHash", bookHash);
		dbHelper.sourceDB.update("state", values, null, null);
    }
    
    /**
     * @deprecated - use setting table instead
     */
    @Override
	public void saveStateSettings(int fontFactor, String colorThemeName) {
    	ContentValues values = new ContentValues();
        values.put("fontFactor", fontFactor);
        values.put("themeName", colorThemeName);
		dbHelper.sourceDB.update("state", values, null, null);
    }
    
    public void clearPages() {
    	dbHelper.createBookTables(dbHelper.sourceDB);
    }
    
    @Override
	public void saveStateFullscreen(boolean fullscreen) {
    	ContentValues values = new ContentValues();
        values.put("fullScreen", (fullscreen)? 1 : 0);
		dbHelper.sourceDB.update("state", values, null, null);
    }
    
    
    @Override
    public Cursor loadState() {
        Cursor mCursor =
        dbHelper.sourceDB.query("state", new String[]{"currentWindow", "bookHash", "themeName", "fontFactor", "fullscreen"}, null, null, null, null, null);
	    if (mCursor != null) {
	        mCursor.moveToFirst();
	    }
    	return mCursor;
    }
    
	@Override
	public void saveBook(String fileId, String hash, FrameMapping position) {
		dbHelper.sourceDB.delete("book", "fileId='" + fileId + "' OR hash='" + hash + "'", null);
		
        ContentValues values = new ContentValues();
        values.put("fileId", fileId);
        values.put("hash", hash);
        values.put("pageIndex", position.pageIndex);
        values.put("cobitIndex", position.cobitIndex);
        values.put("accessTime", System.currentTimeMillis());
        try {
        	long result = dbHelper.sourceDB.insert("book", null, values);
    		if (result == -1) {
    			Log.w("SQLiteStorage", "error saving book");
    		}
        } catch (SQLiteConstraintException e) {
        	Log.i("SQLiteStorage", "Such book is already in database");
        }
	}

	@Override
	public Cursor loadBookByHash(String hash) {
		Log.i("SQLiteStorage", "quering DB - book hash=" + hash);
		Cursor mCursor =
	        dbHelper.sourceDB.query("book", new String[]{"fileId", "pageIndex", "cobitIndex", "lastPageIndex"}, "hash = '" + hash + "'", null, null, null, null);
			//dbHelper.sourceDB.query("book", new String[]{"fileId", "pageIndex", "cobitIndex", "hash"}, null, null, null, null, null);
		    if (mCursor != null) {
		        mCursor.moveToFirst();
		    }
		    return mCursor;	
	}
	
	@Override
	public Cursor getRecentBooks() {
		Log.i("SQLiteStorage", "quering DB - recent books");
		return  dbHelper.sourceDB.query("book",
				new String[]{"fileId", "accessTime"}, null, null, null, null, "accessTime desc");
		
	}
	
	@Override
	public void savePage(String bookHash, int pageIndex, int scrapeIndex, int scrapeOffset, int finishScrapeIndex, int finishScrapeOffset) {
		Log.i("SQLiteStorage", "Saving page index: " + pageIndex);
    	ContentValues values = new ContentValues();
        values.put("bookHash", bookHash);
        values.put("pageIndex", pageIndex);
        values.put("scrapeIndex", scrapeIndex);
        values.put("scrapeOffset", scrapeOffset);
        values.put("finishScrapeIndex", finishScrapeIndex);
        values.put("finishScrapeOffset", finishScrapeOffset);
		dbHelper.sourceDB.insert("page", null, values);
	}
	
	@Override
	public Cursor loadAllPages(String bookHash) {
		Log.i("SQLiteStorage", "quering DB - page hash=" + bookHash);
		Cursor mCursor = dbHelper.sourceDB.query("page", 
											     new String[]{"pageIndex", "scrapeIndex", "scrapeOffset", "finishScrapeIndex", "finishScrapeOffset"},
											     "bookHash = '" + bookHash + "'", null, null, null, null);
	    if (mCursor != null) {
	        mCursor.moveToFirst();
	    }
	    return mCursor;	
	}
	
	@Override
	public void saveBookPosition(String hash, int pageIndex, int cobitIndex) {
    	ContentValues values = new ContentValues();
        values.put("pageIndex", pageIndex);
        values.put("cobitIndex", cobitIndex);
        values.put("accessTime", System.currentTimeMillis());
		dbHelper.sourceDB.update("book", values, "hash = '" + hash + "'", null);
		flush();
	}
	
	/**
	 * makes sure the data was recorded
	 */
	@Override
	public void flush() {
		close();
		open();
	}
	
	@Override
	public void saveBookLastPageIndex(String hash, int lastPageIndex) {
    	ContentValues values = new ContentValues();
        values.put("lastPageIndex", lastPageIndex);
		dbHelper.sourceDB.update("book", values, "hash = '" + hash + "'", null);
	}

	@Override
	public String loadSetting(String name, String defaultValue, int profileIndex) {
		Cursor mCursor = dbHelper.sourceDB.query("setting", 
											     new String[]{"value"},
											     "name=? and profileIndex=?",
											     new String[]{name, String.valueOf(profileIndex)},
											     null, null, null);
	    if (mCursor == null || mCursor.isAfterLast()) {
	    	return defaultValue;
	    } else {
	        mCursor.moveToFirst();
	        return mCursor.getString(0);
	    }
	}

	@Override
	public void saveSetting(String name, String value, int profileIndex) {
		dbHelper.sourceDB.delete("setting", "name=? and profileIndex=?",
				                 new String[]{name, String.valueOf(profileIndex)});
		
		ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("value", value);
        values.put("profileIndex", profileIndex);
		dbHelper.sourceDB.insert("setting", null, values);
	}
}