package com.pluseq.coreader.android;

import java.util.ArrayList;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.CoTheme;
import com.pluseq.coreader.R;

public class ProfileSettingsActivity extends Activity implements OnClickListener{
    protected Spinner fontSpinner = null;
    protected Spinner colorThemeSpinner = null;
    
	protected void setupHandlers() {
		fontSpinner = (Spinner) findViewById(R.id.SettingsFontSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(fontSpinner.getContext(), R.layout.spinner_item, FontSettings.getFactorList());	
		fontSpinner.setAdapter(adapter);
		fontSpinner.setSelection(CoConfig.getFontSettings().currentFactor - 10);
		
		colorThemeSpinner = (Spinner) findViewById(R.id.ColorThemeSpinner);
		ArrayList<String> themeList = CoTheme.getThemeList();
		ArrayAdapter<String> themeAdapter = new ArrayAdapter<String>(colorThemeSpinner.getContext(), R.layout.spinner_item, themeList);
		colorThemeSpinner.setAdapter(themeAdapter);
		colorThemeSpinner.setSelection(CoTheme.getPositionByName(CoConfig.getCoTheme().getName()));
				
		Button button = (Button) findViewById(R.id.SettingsSave);
    	button.setOnClickListener(this);
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.profile_settings);
    	setupHandlers();
	}
    
	/**
	 * @return selected theme name
	 */
	protected String setCoTheme() {
		int themeIndex = ((Spinner)findViewById(R.id.ColorThemeSpinner)).getSelectedItemPosition();
		CoTheme theme = CoTheme.getThemeByIndex(themeIndex);
		CoConfig.setCoTheme(theme);
		return theme.getName();
	}
	
	/**
	 * @return font factor
	 */
	protected int setFont() {
		int fontFactor = ((Spinner)findViewById(R.id.SettingsFontSpinner)).getSelectedItemPosition() + 10;
		
		FontSettings fs = CoConfig.getFontSettings();
		fs.setFontFactor(fontFactor);
		return fontFactor;
	}
	
	@Override
	public void onClick(View v) {
		CoConfig.getStorage().saveSetting("fontFactor", String.valueOf(setFont()), CoConfig.getProfileIndex());
		CoConfig.getStorage().saveSetting("theme", setCoTheme(), CoConfig.getProfileIndex());
		
		//CoConfig.getStorage().saveStateSettings(setFont(), setCoTheme());
		if (null != CoConfig.currentActivity.bv) {
			CoConfig.currentActivity.bv.reloadFrame();
		}
		finish();
	}
}