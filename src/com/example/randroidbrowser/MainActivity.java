package com.example.randroidbrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	private GridView gridView;
	
	private static TextView numActionActiveTabs;
	private static int numActiveTabs, numTotalTabs;
	private static int IMAGE_FULL_SCREEN, IMAGE_EXIT_FULL_SCREEN;
	
	protected static String urlSelected;
	protected static boolean exitConfirmation;
	protected static int downloadNotificationId;
	protected static SharedPreferences sharedPreferences;
	protected static PopupWindow popupWindowActiveTabs, popupWindowGridView;
	protected static ImageView fullScreen, imageActionActiveTabs, imageActionViewAsGrid;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        getActionBar().hide();
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setContentView(R.layout.activity_main);
		
		fullScreen = (ImageView) findViewById(R.id.image_action_full_screen);
		fullScreen.setContentDescription("Fullscreen");
		numActionActiveTabs = (TextView) findViewById(R.id.num_action_active_tabs);
		imageActionActiveTabs = (ImageView) findViewById(R.id.image_action_active_tabs);
		imageActionViewAsGrid = (ImageView) findViewById(R.id.image_action_view_as_grid);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		urlSelected = null;
		numTotalTabs = 0;
		numActiveTabs = 0;
		downloadNotificationId = 0;
		final Activity mActivity = this;
		IMAGE_FULL_SCREEN = R.drawable.ic_action_full_screen;
		IMAGE_EXIT_FULL_SCREEN = R.drawable.ic_action_return_from_full_screen;
		popupWindowActiveTabs = new PopupWindow(mActivity);
		popupWindowGridView = new PopupWindow(mActivity);
		gridView = new GridView(this);
		
		gridView.setNumColumns(3);
        gridView.setBackgroundColor(getResources().getColor(R.color.grey));
        gridView.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View arg0, int arg1, KeyEvent keyEvent) {
				if(keyEvent.getAction() == KeyEvent.ACTION_UP && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MENU || keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)) {
					popupWindowGridView.dismiss();	}
				return false;
			}
		});
		popupWindowGridView.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		popupWindowGridView.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindowGridView.setFocusable(true);
		popupWindowGridView.setOutsideTouchable(false);
		popupWindowGridView.setContentView(gridView);
		
		addNewTab(this);
		fullScreen.setOnClickListener(new MyListener(this));
		imageActionActiveTabs.setOnClickListener(new MyListener(this));
		imageActionViewAsGrid.setOnClickListener(new MyListener(this));
	}
	
	protected void onResume() {

		super.onResume();
		
		exitConfirmation = sharedPreferences.getBoolean("Exit Confirmation", true);
		boolean exitButton = sharedPreferences.getBoolean("Exit Button", false);
		String[] gridImage = getResources().getStringArray(R.array.gridViewImageName);
		String[] gridTitle = getResources().getStringArray(R.array.gridViewImageText);
		
		if(exitButton) {
			gridView.setAdapter(new MyGridViewArrayAdapter(this, R.layout.activity_grid_view, popupWindowGridView, gridTitle, gridImage));
		} else {
			
			int max = (gridImage.length - 1);
			String[] gImage = new String[max];
			String[] gTitle = new String[max];
			
			for(int i = 0; i < max; i++) {
				gImage[i] = gridImage[i];
				gTitle[i] = gridTitle[i];
			}
			gridView.setAdapter(new MyGridViewArrayAdapter(this, R.layout.activity_grid_view, popupWindowGridView, gTitle, gImage));
		}
		
	}

	protected void onRestart() {
		super.onRestart();
		
		if(urlSelected != null) {
			((AutoCompleteTextView) getFragmentManager().findFragmentByTag(getActionBar().getSelectedTab()
					.getText().toString()).getView().findViewById(R.id.textUrl)).setText(urlSelected);
			urlSelected = null;
			TabFragment.checkNetworkConnection(this);
		}
		TabFragment.addOrRemoveBookmark(this, false, ((AutoCompleteTextView) getFragmentManager().findFragmentByTag(
			getActionBar().getSelectedTab().getText().toString()).getView().findViewById(R.id.textUrl)).getText().toString());
	}
	
	protected void onDestroy() {
        
		super.onDestroy();
		
		boolean clearCacheOnExit = sharedPreferences.getBoolean("Clear Cache", true);
		boolean clearHistoryOnExit = sharedPreferences.getBoolean("Clear History", false);
		
		if(clearCacheOnExit) {
			clearCacheData(getCacheDir());	
		}
		if(clearHistoryOnExit) {
			emptyFile(this, "HistoryTitle");
			emptyFile(this, "HistoryUrl");
		}
        
	}
	
	protected static void clearCacheData(File cacheDir) {
		
		if(cacheDir != null && cacheDir.isDirectory()) {
			String[] cacheDirList = cacheDir.list();
			for (String filename : cacheDirList) {
				clearCacheData(new File(cacheDir, filename));
			}
		}
		cacheDir.delete();
	}

	protected static void fullScreen(Activity mActivity) {
		
		if(fullScreen.getContentDescription().toString().contentEquals("Fullscreen")) {
			
			fullScreen.setImageResource(IMAGE_EXIT_FULL_SCREEN);
            fullScreen.setContentDescription("Exit Fullscreen");
	        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			
			fullScreen.setImageResource(IMAGE_FULL_SCREEN);
            fullScreen.setContentDescription("Fullscreen");
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}
	
	protected static void showActiveTabs(final Activity mActivity) {
		
		ArrayList<String> activeTabsUrlList = new ArrayList<String>();
		ArrayList<String> activeTabsTitleList = new ArrayList<String>();
		
		int tabCount = mActivity.getActionBar().getTabCount();
		for(int i = 0; i < tabCount; i++) {
			activeTabsUrlList.add(((TextView) mActivity.getActionBar().getTabAt(i).getCustomView().findViewById(R.id.active_tab_url_text)).getText().toString());
			activeTabsTitleList.add(((TextView) mActivity.getActionBar().getTabAt(i).getCustomView().findViewById(R.id.active_tab_title_text)).getText().toString());
		}
		
		ListView listView = new ListView(mActivity);
		listView.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
		listView.setAdapter(new MyActiveTabsArrayAdapter(mActivity, R.layout.activity_active_tabs, activeTabsTitleList, activeTabsUrlList));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long itemId) {
				mActivity.getActionBar().getTabAt(position).select();
				popupWindowActiveTabs.dismiss();
			}
		});
		listView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View view, int arg1, KeyEvent keyEvent) {
				if(keyEvent.getAction() == KeyEvent.ACTION_UP && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MENU || keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)) {
					popupWindowActiveTabs.dismiss();	}
				return false;
			}
		});
		popupWindowActiveTabs.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		popupWindowActiveTabs.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindowActiveTabs.setFocusable(true);
		popupWindowActiveTabs.setContentView(listView);
		popupWindowActiveTabs.showAsDropDown(imageActionActiveTabs, 3, 3);
	}
		
	protected static void addNewTab(final Activity mActivity) {

		numActionActiveTabs.setText("" + ++numActiveTabs);
		final Tab newTab = mActivity.getActionBar().newTab();
		newTab.setCustomView(R.layout.activity_active_tabs);
		newTab.setText("" + (++numTotalTabs));
		
		newTab.getCustomView().findViewById(R.id.active_tab_layout_select).setVisibility(View.GONE);
		TextView tabTitleText = ((TextView) newTab.getCustomView().findViewById(R.id.active_tab_title_text));
		TextView tabUrlText = ((TextView) newTab.getCustomView().findViewById(R.id.active_tab_url_text));
		((ImageView) newTab.getCustomView().findViewById(R.id.active_tab_close_image)).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				closeCurrentTab(mActivity, newTab);
			}
		});
		tabTitleText.setText("New Tab");
		tabUrlText.setText("about:blank");
		tabUrlText.setVisibility(View.GONE);
		tabTitleText.setTextSize(17);
		tabTitleText.setPadding(0, 7, 0, 0);
		
		final TabFragment tabFragment = new TabFragment();
		mActivity.getFragmentManager().beginTransaction().disallowAddToBackStack().commit();
		mActivity.getFragmentManager().beginTransaction().add(R.id.frameLayout, tabFragment, ("" + numTotalTabs)).commit();
		
		newTab.setTabListener(new ActionBar.TabListener() {
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				ft.hide(tabFragment);	}
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				ft.show(tabFragment);	}
			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		});
		mActivity.getActionBar().addTab(newTab, true);
	}
	
	protected static void closeCurrentTab(Activity mActivity, Tab tab) {
		
		if(mActivity.getActionBar().getTabCount() == 1) {
			exitConfirmation(mActivity);	
			return;
		}
		
		numActionActiveTabs.setText("" + --numActiveTabs);
		((WebView) mActivity.getFragmentManager().findFragmentByTag(tab.getText().toString())
				.getView().findViewById(R.id.webView)).loadUrl("about:blank");
		mActivity.getFragmentManager().beginTransaction().remove(mActivity.getFragmentManager()
				.findFragmentByTag(tab.getText().toString())).commit();
		mActivity.getActionBar().removeTab(tab);
	}

	protected static synchronized void writeToFile(Activity mActivity, String fileName, String text) {
		
		try {
			text += ("\n" + readFromFile(mActivity, fileName));
			FileOutputStream fileOutputStream = mActivity.openFileOutput((fileName + ".txt"), MODE_PRIVATE);	
			fileOutputStream.write(text.getBytes());
			fileOutputStream.close();
		} catch(IOException e) {}
	}

	protected static synchronized void emptyFile(Activity mActivity, String fileName) {
		
		try {
			FileOutputStream fileOutputStream = mActivity.openFileOutput((fileName + ".txt"), MODE_PRIVATE);
			fileOutputStream.close();
		} catch(IOException e) {}
	}

	protected static synchronized String readFromFile(Activity mActivity, String fileName) {
		
		String item = "";
		try {
			FileInputStream fileInputStream = mActivity.openFileInput(fileName + ".txt");	
			int c;
			while((c = fileInputStream.read()) != -1) {
				item += (char) c;
			}
			fileInputStream.close();
		} catch(IOException e) {}
		return item;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		

		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
			exitConfirmation(this);
		} else if(keyCode == KeyEvent.KEYCODE_MENU) {
			
	       	popupWindowGridView.showAsDropDown(imageActionViewAsGrid, 3, 3);
		} else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			
			Toast.makeText(this, "Volume Up", Toast.LENGTH_SHORT).show();
		} else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			
			Toast.makeText(this, "Volume Down", Toast.LENGTH_SHORT).show();
		}
		
		return true;
	}
	
	protected static void exitConfirmation(final Activity mActivity) {
		
		if(!exitConfirmation) {
			exit(mActivity);
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setCancelable(false);
		builder.setMessage("Are You Sure?");
		builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				exit(mActivity);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {}
		});
		builder.create().show();
	}
	
	private static void exit(Activity mActivity) {
		
		mActivity.getActionBar().removeAllTabs();
		mActivity.finish();
		
	}
	
}
