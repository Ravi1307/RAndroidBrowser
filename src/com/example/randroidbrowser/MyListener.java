package com.example.randroidbrowser;

import java.util.ArrayList;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class MyListener implements View.OnClickListener, View.OnKeyListener, TextView.OnEditorActionListener, AdapterView.OnItemClickListener {

	private Activity mActivity;
	private View mView;
	
	public MyListener(Activity mActivity) {
		
		this.mActivity = mActivity;
		
	}
	
	public void onClick(View view) {
		
		mView = mActivity.getFragmentManager().findFragmentByTag(
				mActivity.getActionBar().getSelectedTab().getText().toString()).getView();
		
		switch(view.getId()) {
			
		case R.id.addNewTab:
			MainActivity.addNewTab(mActivity);
			break;
			
		case R.id.addOrRemoveBookmark:
			TabFragment.addOrRemoveBookmark(mActivity, true, ((AutoCompleteTextView) mView.findViewById(R.id.textUrl)).getText().toString());
			break;
			
		case R.id.goCancel:
			TabFragment.checkNetworkConnection(mActivity);
			break;
			
		case R.id.goBack:
			((WebView) mView.findViewById(R.id.webView)).goBack();
			break;
			
		case R.id.goForward:
			((WebView) mView.findViewById(R.id.webView)).goForward();
			break;
			
		case R.id.image_action_full_screen:
			MainActivity.fullScreen(mActivity);
			break;
			
		case R.id.image_action_active_tabs:
			MainActivity.showActiveTabs(mActivity);
			break;
			
		case R.id.image_action_view_as_grid:
	        MainActivity.popupWindowGridView.showAsDropDown(MainActivity.imageActionViewAsGrid, 3, 3);
	        break;
	        
		}
	}
	
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
		
		if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {
			TabFragment.checkNetworkConnection(mActivity);	
		}
		return true;
	}
	
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long itemId) {
		
		TabFragment.checkNetworkConnection(mActivity);	
	}
	
	public boolean onKey(View view, int arg1, KeyEvent keyEvent) {
		
		if(keyEvent.getAction() == KeyEvent.ACTION_UP) {
			
			ArrayAdapter<String> textUrlAdapter;
			ArrayList<String> urlList = new ArrayList<String>();
			AutoCompleteTextView textUrl = ((AutoCompleteTextView) view);
			String enteredUrl = textUrl.getText().toString();
			
			if(enteredUrl.contentEquals("")) {
				
				while(urlList.size() > 0) {
					urlList.remove(0);		
				}
				textUrlAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, urlList);
				textUrl.setAdapter(textUrlAdapter);
			} else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				
				TabFragment.checkNetworkConnection(mActivity);
			} else if ((keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL || 
					keyEvent.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) || 
					(keyEvent.getKeyCode() != KeyEvent.KEYCODE_DPAD_DOWN && 
					keyEvent.getKeyCode() != KeyEvent.KEYCODE_DPAD_UP)){
				
				String historyUrl = MainActivity.readFromFile(mActivity, "HistoryUrl");
				String bookmarksUrl = MainActivity.readFromFile(mActivity, "BookmarksUrl");
				for(String s : bookmarksUrl.split("\n")) {
					if(!historyUrl.contains(s)) {
						historyUrl += s + "\n";
					}
				}
				while(urlList.size() > 0) {
					urlList.remove(0);		
				}
				for(String s : historyUrl.split("\n")) {
					if(s.contains(enteredUrl)) {
						urlList.add(s);
					}
				}
				textUrlAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, urlList);
				textUrl.setAdapter(textUrlAdapter);
				textUrl.showDropDown();
				return true;	
			}
		}
		return false;
	}
}
