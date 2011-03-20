package com.pluseq.coreader.android;
import java.util.ArrayList;

import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.R;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class SettingsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.settings);

		Button buttonDirectory = (Button)findViewById(R.id.settingsDirectory);
		buttonDirectory.setOnClickListener(new DirectoryButtonOnClick());

		Button buttonProfile = (Button)findViewById(R.id.settingsProfile);
		buttonProfile.setOnClickListener(new ProfileButtonOnClick());

		Button buttonSwitchProfile = (Button)findViewById(R.id.settingsSwitchProfile);
		buttonSwitchProfile.setOnClickListener(new SwitchProfileOnClick());

		refreshButton();
	}
	
	protected void refreshButton() {
		Button buttonProfile = (Button)findViewById(R.id.settingsProfile);
		
		Resources r = CoConfig.currentActivity.getResources();
		String text = r.getString(R.string.setupProfile) + " " + CoConfig.getProfileIndex();
		buttonProfile.setText(text);
	}
	
	private class DirectoryButtonOnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent myIntent = new Intent();
			myIntent.setClassName(getApplicationContext(), "com.pluseq.coreader.android.SelectDirectoryActivity");
			startActivity(myIntent);   
		}
	}

	private class ProfileButtonOnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent myIntent = new Intent();
			myIntent.setClassName(getApplicationContext(), "com.pluseq.coreader.android.ProfileSettingsActivity");
			startActivity(myIntent);   
		}
	}

	private class SwitchProfileOnClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			CoConfig.switchProfile();
			if (null != CoConfig.currentActivity.bv) {
				CoConfig.currentActivity.bv.reloadFrame();
			}
			refreshButton();   
		}
	}	
}