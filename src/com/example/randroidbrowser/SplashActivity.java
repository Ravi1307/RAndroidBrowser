package com.example.randroidbrowser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;

public class SplashActivity extends ActionBarActivity {

	Thread mThread;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_splash);
		
		mThread = new Thread() {
			public void run() {
				try {
					
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					boolean quickStart = sharedPreferences.getBoolean("Quick Start", false);
					
					if(!quickStart) {
						
						ProgressBar progressBar = (ProgressBar) findViewById(R.id.splash_progress_bar);
						int progress = 0;
						while(progress < 100) {
							progress += 2;
							progressBar.setProgress(progress);
							sleep(50);
						}
					}
					startActivity(new Intent(getApplicationContext(), MainActivity.class));
					finish();
				} catch (InterruptedException e) {}
			}
		};
	}

	protected void onResume() {
		
		super.onResume();
		mThread.start();
	}

	protected void onPause() {
		
		super.onPause();
		try {
			mThread.interrupt();
		} catch(Exception e) {}
	}
	
}
