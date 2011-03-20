package com.pluseq.coreader.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.pluseq.coreader.*;

public class RecentFilesActivity extends ListActivity {
	protected ArrayList<String> items;
	protected ArrayList<Book> books;
	public int MAX_LINES = 20;
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,  
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        setContentView(R.layout.fs_directory_list);
        fill();
    }

	private void fill() {
		items = new ArrayList<String>();
		books = new ArrayList<Book>();
		
		Cursor c = CoConfig.getStorage().getRecentBooks();
		if (c == null) {
			items.add(getString(R.string.noRecentBooksFound));
		} else {
			if (c.getCount() <=0 ) {
				items.add(getString(R.string.noRecentBooksFound));
			} else {
				c.moveToFirst();
				BookManager bm = new BookManager();
				while (!c.isAfterLast() && (items.size() < MAX_LINES))
				{
					Book b = bm.create(c.getString(0), 0, 0);
					items.add(b.getSimpleFileName() + " (" + b.encoding + ")");
					books.add(b);
					c.moveToNext();
				}
				c.close();
			}
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.fs_file_row, items);
		setListAdapter(adapter);
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	Intent resultIntent = new Intent();
    	if (books != null && books.size() > 0) {
			resultIntent.putExtra("book", books.get(position));
			setResult(Activity.RESULT_OK, resultIntent);
    	} else {
    		setResult(Activity.RESULT_CANCELED);
    	}
    	finish();
	}
}
