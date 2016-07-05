package com.example.randroidbrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class TabFragment extends Fragment {

	private WebView webView;
	private GridView gridView;
	private AutoCompleteTextView textUrl;
	private ProgressBar progressBar;
	protected static String TEXT_GO, TEXT_CANCEL;
	protected static Drawable GO_IMAGE, CANCEL_IMAGE, IMPORTANT, NOT_IMPORTANT;
	private ImageView addNewTab, goBack, goForward, goCancel, addOrRemoveBookmark;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.activity_fragment, container, false);
		webView = (WebView) view.findViewById(R.id.webView);
		gridView = (GridView) view.findViewById(R.id.home_page);
		goBack = (ImageView) view.findViewById(R.id.goBack);
		textUrl = (AutoCompleteTextView) view.findViewById(R.id.textUrl);
		goCancel = (ImageView) view.findViewById(R.id.goCancel);
		goForward = (ImageView) view.findViewById(R.id.goForward);
		addNewTab = (ImageView) view.findViewById(R.id.addNewTab);
		TEXT_GO = getResources().getString(R.string.textGo);
		TEXT_CANCEL = getResources().getString(R.string.textCancel);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		GO_IMAGE = getResources().getDrawable(R.drawable.ic_action_forward);
		CANCEL_IMAGE = getResources().getDrawable(R.drawable.ic_action_cancel_orignal);
		IMPORTANT = getResources().getDrawable(R.drawable.ic_action_important);
		NOT_IMPORTANT = getResources().getDrawable(R.drawable.ic_action_not_important);
		addOrRemoveBookmark = (ImageView) view.findViewById(R.id.addOrRemoveBookmark);
		addOrRemoveBookmark.setEnabled(false);
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient(getActivity(), goCancel,progressBar, textUrl, 
				addOrRemoveBookmark, gridView));
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int newProgress) {
				progressBar.setProgress(newProgress);
				super.onProgressChanged(view, newProgress);
			}
		});
		textUrl.setOnItemClickListener(new MyListener(getActivity()));
		textUrl.setOnEditorActionListener(new MyListener(getActivity()));
		textUrl.setOnKeyListener(new MyListener(getActivity()));
		goBack.setOnClickListener(new MyListener(getActivity()));
		goCancel.setOnClickListener(new MyListener(getActivity()));
		goForward.setOnClickListener(new MyListener(getActivity()));
		addNewTab.setOnClickListener(new MyListener(getActivity()));
		addOrRemoveBookmark.setOnClickListener(new MyListener(getActivity()));
		return view;
	}
	
	public void onResume() {
		
		super.onResume();
		pinchZoomView();
		desktopMode();
	}
	
	private void desktopMode() {
		
		boolean enableDesktopView = MainActivity.sharedPreferences.getBoolean("Desktop View", false);
		if(!enableDesktopView) {
			webView.getSettings().setUserAgentString("");
			webView.getSettings().setUseWideViewPort(false);
			return;
		}
		
		String userAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/20 Safari/537.31";
		webView.getSettings().setUserAgentString(userAgent);
		webView.getSettings().setUseWideViewPort(true);
		
	}

	private void pinchZoomView() {
		
		boolean pinchZoom = MainActivity.sharedPreferences.getBoolean("Pinch Zoom", false);
		if(!pinchZoom) {
			webView.getSettings().setBuiltInZoomControls(false);
			return;
		}
		
		String temp = textUrl.getText().toString();
		if(temp.contentEquals("about:blank")) {
			return;
		}
		
		webView.getSettings().setBuiltInZoomControls(true);
		new ScaleGestureDetector(getActivity(), new SimpleOnScaleGestureListener() {
        	public boolean onScale(ScaleGestureDetector detector) {
        		
        		float scale = 1f;
        		scale *= detector.getScaleFactor();
        		scale = Math.max(0.1f, Math.min(scale, 5.0f));
        		webView.setScaleX(scale);
        		webView.setScaleY(scale);	
        		return true;
        	}
    	});
	}
	
	protected static synchronized void addOrRemoveBookmark(final Activity mActivity, final boolean isPressed, final String url) {
		
		new Thread() {
			public void run() {
				
				String fileBookmarksUrl, bookmarksUrl;
				fileBookmarksUrl = "BookmarksUrl";
				bookmarksUrl = MainActivity.readFromFile(mActivity, fileBookmarksUrl);
				
				if(!isPressed) {	
					if(!url.contentEquals("") && !bookmarksUrl.contentEquals("") && bookmarksUrl.contains(url + "\n")) {
						markImportantOrNot(mActivity, url, true);
					} else {
						markImportantOrNot(mActivity, url, false);
					}
					return;	
				}
				
				String fileBookmarksTitle, bookmarksTitle, title, writeTitle, writeUrl;
				fileBookmarksTitle = "BookmarksTitle";
				bookmarksTitle = MainActivity.readFromFile(mActivity, fileBookmarksTitle);
				title = ((TextView) mActivity.getActionBar().getSelectedTab().getCustomView()
						.findViewById(R.id.active_tab_title_text)).getText().toString();
				
				if(bookmarksUrl.contentEquals("") || !bookmarksUrl.contains(url + "\n")) {
					
					markImportantOrNot(mActivity, url, true);
					writeTitle = title;
					writeUrl = url;
				} else {

					markImportantOrNot(mActivity, url, false);
					int urlsIndex = bookmarksUrl.indexOf(url);
					int urlsNewLineIndex = 0;
					int startIndex = 0;
					while((startIndex = bookmarksUrl.indexOf('\n', startIndex) + 1) <= urlsIndex ) {
						urlsNewLineIndex++;	}
					
					startIndex = 0;
					while(urlsNewLineIndex-- > 0) {
						startIndex = (bookmarksTitle.indexOf('\n', startIndex) + 1);	}
					
					bookmarksTitle = ((new StringBuffer(bookmarksTitle)).delete(startIndex, (bookmarksTitle.indexOf('\n', startIndex) + 1))).toString();
					bookmarksUrl = bookmarksUrl.replace(url + "\n", "");
					
					MainActivity.emptyFile(mActivity, fileBookmarksUrl);
					MainActivity.emptyFile(mActivity, fileBookmarksTitle);
					writeTitle = bookmarksTitle;
					writeUrl = bookmarksUrl;
				}
				MainActivity.writeToFile(mActivity, fileBookmarksUrl, writeUrl);
				MainActivity.writeToFile(mActivity, fileBookmarksTitle, writeTitle);
			}
		}.start();
	}

	private static void markImportantOrNot(final Activity mActivity, final String url, final boolean isImportant) {
		
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				
				int tabsCount = mActivity.getActionBar().getTabCount();
				while(tabsCount-- > 0) {
					
					View view = mActivity.getFragmentManager().findFragmentByTag(
							mActivity.getActionBar().getTabAt(tabsCount).getText().toString()).getView();
					String urlAllTabs = ((AutoCompleteTextView) view.findViewById(R.id.textUrl)).getText().toString();
					
					if(url.contentEquals(urlAllTabs)) {
						if(isImportant) {
							((ImageView) view.findViewById(R.id.addOrRemoveBookmark)).setImageDrawable(IMPORTANT);
						} else {
							((ImageView) view.findViewById(R.id.addOrRemoveBookmark)).setImageDrawable(NOT_IMPORTANT);	}	
					}
				}
			}
		});
	}
	
	private static void hideKeypad(Activity mActivity) {

		InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private static void setWifiOn(Activity mActivity) {
		
		((WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
	}
	
	private static void setMobileDataOn(Activity mActivity) {
		
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
			(connectivityManager.getClass().getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE)).invoke(connectivityManager, true);
		} catch (Exception e) {} 
	}
	
	protected static void checkNetworkConnection(final Activity mActivity) {

		hideKeypad(mActivity);
		NetworkInfo networkInfo = ((ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		
		if (networkInfo != null && networkInfo.isConnected()) {
			buttonUrlClick(mActivity);
		} else {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setCancelable(false);
			builder.setMessage("You Are Not Connected To Any Network, Please Turn On");
			builder.setPositiveButton("Mobile Data", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					setMobileDataOn(mActivity);	}
			});
			builder.setNeutralButton("Wi-Fi", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					setWifiOn(mActivity);	}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});
			builder.create().show();
		}
	}

	private static void buttonUrlClick(Activity mActivity) {


		WebView webView = ((WebView) mActivity.getFragmentManager().findFragmentByTag(
				mActivity.getActionBar().getSelectedTab().getText().toString()).getView().findViewById(R.id.webView));
		ImageView goCancel = ((ImageView) mActivity.getFragmentManager().findFragmentByTag(
				mActivity.getActionBar().getSelectedTab().getText().toString()).getView().findViewById(R.id.goCancel));
		AutoCompleteTextView textUrl = ((AutoCompleteTextView) mActivity.getFragmentManager().findFragmentByTag(mActivity.getActionBar()
				.getSelectedTab().getText().toString()).getView().findViewById(R.id.textUrl));
		String enteredUrl = textUrl.getText().toString().trim();
		
		if (enteredUrl.contentEquals("")) {
			Toast.makeText(mActivity, "Please Enter Url", Toast.LENGTH_SHORT).show();
			return;
		}
		if (goCancel.getContentDescription().toString().contentEquals(TEXT_GO)) {
			
			String url = enteredUrl;
			if(url.startsWith(".") || url.indexOf(' ') != -1 || url.indexOf('.') == -1 || ((url.indexOf('.') + 3) >  url.length())) {
				
				url = ("http://www.google.com/#q=" + url.replaceAll(" ", "+"));
			} else if (!(url.startsWith("http://") || url.startsWith("https://"))) {
				
				url = "http://" + url;
			}
			textUrl.setText(url);
			webView.loadUrl(url);
		} else {
			webView.stopLoading();
		}
	}
		
}
