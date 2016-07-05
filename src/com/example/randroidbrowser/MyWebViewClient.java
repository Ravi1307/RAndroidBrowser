package com.example.randroidbrowser;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyWebViewClient extends WebViewClient {

	private AutoCompleteTextView textUrl;
	private ImageView goCancel, addOrRemoveBookmark;
	private Activity mActivity;
	private GridView gridView;
	private ProgressBar progressBar;
	private TextView textViewTitle, textViewUrl;
	protected static boolean isDownloading;
	
	public MyWebViewClient(Activity mActivity, ImageView goCancel, ProgressBar progressBar, AutoCompleteTextView textUrl, 
			ImageView addOrRemoveBookmark, GridView gridView) {
		
		this.gridView = gridView;
		this.textUrl = textUrl;
		this.goCancel = goCancel;
		this.mActivity = mActivity;
		MyWebViewClient.isDownloading = false;
		this.progressBar = progressBar;
		this.addOrRemoveBookmark = addOrRemoveBookmark;
		this.textViewTitle = (TextView) mActivity.getActionBar().getSelectedTab().getCustomView().findViewById(R.id.active_tab_title_text);
		this.textViewUrl = (TextView) mActivity.getActionBar().getSelectedTab().getCustomView().findViewById(R.id.active_tab_url_text);
	}
	
	public void onPageStarted(WebView webView, String url, Bitmap favicon) {

		super.onPageStarted(webView, url, favicon);
		textUrl.dismissDropDown();
		gridView.setVisibility(View.GONE);
		webView.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		goCancel.setImageDrawable(TabFragment.CANCEL_IMAGE);
		goCancel.setContentDescription(TabFragment.TEXT_CANCEL);
		textViewTitle.setText("Connecting...");
		
		if(url == null) {
			textViewUrl.setText(textUrl.getText().toString());
		} else {
			textViewUrl.setText(url);	}
	}

	public void onPageFinished(final WebView webView, final String url) {

		super.onPageFinished(webView, url);
		progressBar.setMax(100);
		progressBar.setVisibility(View.GONE);
		addOrRemoveBookmark.setEnabled(true);
		goCancel.setImageDrawable(TabFragment.GO_IMAGE);
		goCancel.setContentDescription(TabFragment.TEXT_GO);
		String tempUrl = webView.getUrl();
		
		if(tempUrl != null && !tempUrl.contentEquals("")) {
			textUrl.setText(url);	}
		textUrl.setSelection(textUrl.getText().toString().length());
		textUrl.dismissDropDown();
		
		setTitleAndUrl(webView, url);
		TabFragment.addOrRemoveBookmark(mActivity, false, textUrl.getText().toString());
		addHistory();
		checkURL(url);
		
	}

	public boolean shouldOverrideUrlLoading(WebView webView, String url) {
		
		if(!url.contentEquals("about:blank")) {
			webView.loadUrl(url);	}
		return super.shouldOverrideUrlLoading(webView, url);
	}
	
	private void checkURL(String url) {
		
		try {
			
			URL fileUrl = new URL(url);
			
			if(fileUrl.getPath().indexOf(".") != -1) {

				isDownloading = true;
				download(fileUrl);
			} else {
				
				throw (new MalformedURLException());
			}	
			
		} catch (MalformedURLException e) {}
		
	}

	private void setTitleAndUrl(WebView webView, String url) {
		
		if(webView.getTitle() == null) {
			if(url.contentEquals("about:blank")) {
				textViewTitle.setText("New Tab");
				return;	}
			
			int dot1, dot2, numberOrLetter = (url.indexOf('/') + 2);
			if(Character.isDigit(url.charAt(numberOrLetter))) {
				dot1 = numberOrLetter;
				dot2 = url.indexOf('/', dot1);	
			} else {
				if(url.startsWith("www.", numberOrLetter) || url.startsWith("m.", numberOrLetter)) {
					dot1 = (url.indexOf('.') + 1);	
				} else {
					dot1 = numberOrLetter;	}
				dot2 = url.indexOf('.', dot1);
			}
			StringBuffer buffer = new StringBuffer(url.substring(dot1, dot2));
			buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
			textViewTitle.setText(buffer.toString());
		} else {
			
			int queryStartIndex;
			if((queryStartIndex = url.indexOf("#q=")) != -1) {
				String query = ((url.substring(queryStartIndex + 3)).replace('+', ' ') + (" - Google Search"));
				textViewTitle.setText(query);
			} else {
				textViewTitle.setText(webView.getTitle());	}
		}
		textViewUrl.setText(url);
		if(MainActivity.popupWindowActiveTabs.isShowing()) {
			MainActivity.popupWindowActiveTabs.dismiss();
			MainActivity.showActiveTabs(mActivity);
		}
	}
	
	private void addHistory() {
		
		new Thread() {
			public void run() {
				synchronized (this) {
				
				String title = textViewTitle.getText().toString();
				String url = textViewUrl.getText().toString();
				
				if(!url.contentEquals("about:blank") && !title.contentEquals("Connecting...") && !title.contentEquals("Webpage not available")) {
					
					String urls = MainActivity.readFromFile(mActivity, "HistoryUrl");
					String titles = MainActivity.readFromFile(mActivity, "HistoryTitle");
					if(!urls.contentEquals("")) {

						int urlsIndex = urls.indexOf(url);
						if(urlsIndex != -1) {

							int urlsNewLineIndex = 0;
							int startUrlIndex = 0;
							while((startUrlIndex = urls.indexOf('\n', startUrlIndex) + 1) <= urlsIndex ) {
								urlsNewLineIndex++;	}
							
							int titleStartIndex = 0;
							while(urlsNewLineIndex-- > 0) {
								titleStartIndex = (titles.indexOf('\n', titleStartIndex) + 1);	}
							int titleEndIndex = (titles.indexOf('\n', titleStartIndex) + 1);
							
							title = title + "\n" + ((new StringBuffer(titles)).delete(titleStartIndex, titleEndIndex)).toString();
							url = url + "\n" + urls.replace(url + "\n", "");

							MainActivity.emptyFile(mActivity, "HistoryTitle");
							MainActivity.emptyFile(mActivity, "HistoryUrl");
						}
					}
					MainActivity.writeToFile(mActivity, "HistoryTitle", title);
					MainActivity.writeToFile(mActivity, "HistoryUrl", url);
				}
				}
			}
		}.start();	
		
	}

	private void download(URL url) {
		
		final Dialog dialog = new Dialog(mActivity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(R.layout.activity_download_task);
		ProgressBar fileProgressBar = (ProgressBar) dialog.findViewById(R.id.dialog_fileProgressBar);
		TextView fileTitle = (TextView) dialog.findViewById(R.id.dialog_fileTitle);
		TextView fileSize = (TextView) dialog.findViewById(R.id.dialog_fileSize);
		TextView filePercent = (TextView) dialog.findViewById(R.id.dialog_filePercent);
		Button fileDownloadCancel = (Button) dialog.findViewById(R.id.dialog_buttonCancel);
		final Button fileDownloadStart = (Button) dialog.findViewById(R.id.dialog_buttonStart);
		final Button fileDownloadHide = (Button) dialog.findViewById(R.id.dialog_buttonHide);
		fileProgressBar.setVisibility(View.GONE);
		fileDownloadHide.setVisibility(View.GONE);
		
		String filePath = url.getPath();
		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
		
		fileTitle.setText(fileName);
		
		final DownloadTask downloadTask = new DownloadTask(mActivity, dialog, fileProgressBar, fileSize, filePercent, fileName, url);
		
		fileDownloadCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if(downloadTask.getStatus() == AsyncTask.Status.RUNNING || downloadTask.getStatus() == AsyncTask.Status.PENDING) {
					downloadTask.cancel(true);	}
				isDownloading = false;
				dialog.dismiss();
			}
		});
		fileDownloadStart.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				downloadTask.execute();
				fileDownloadStart.setVisibility(View.GONE);
				fileDownloadHide.setVisibility(View.VISIBLE);
			}
		});
		fileDownloadHide.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				isDownloading = false;
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
}
