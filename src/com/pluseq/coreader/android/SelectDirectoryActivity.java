package com.pluseq.coreader.android;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.R;

public class SelectDirectoryActivity extends Activity implements OnClickListener{
    protected void setupHandlers() {
    	Button button = (Button) findViewById(R.id.directoryGo);
    	button.setOnClickListener(this);
    	
    	EditText et = (EditText) findViewById(R.id.directoryPath);
    	et.setText(CoConfig.getJailDirectory());
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.select_directory);
    	setupHandlers();
	}
    
	@Override
	public void onClick(View v) {
		EditText et = (EditText) findViewById(R.id.directoryPath);
		
		File file = new File(et.getText().toString());
		if (file.isDirectory()) {
			CoConfig.setJailDirectory(file.getPath());
			CoConfig.saveState();
			finish();				
		} else {
	    	Toast toast = Toast.makeText(getApplicationContext(),
	    			                     getString(R.string.wrongDirectory), Toast.LENGTH_LONG);
	    	toast.show();
		}
		
	}
}