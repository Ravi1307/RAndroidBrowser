package com.example.randroidbrowser;

import android.os.Bundle;
import android.preference.PreferenceActivity;

@SuppressWarnings("deprecation")
public class Settings extends PreferenceActivity {

	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayShowHomeEnabled(false);
		addPreferencesFromResource(R.xml.settings);
		
	}

}
