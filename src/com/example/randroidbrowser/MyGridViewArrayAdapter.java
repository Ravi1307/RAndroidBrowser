package com.example.randroidbrowser;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyGridViewArrayAdapter extends ArrayAdapter<String> {
	
	private Activity mActivity;
	private int layoutResourceId;
	private String[] gridViewImageName, gridViewImageText;
	private PopupWindow popupWindowGridView;
	
	public MyGridViewArrayAdapter(Activity mActivity, int layoutResourceId, PopupWindow popupWindowGridView, String[] gridViewImageText, String[] gridViewImageName) {
		super(mActivity, layoutResourceId, gridViewImageText);
		this.mActivity = mActivity;
		this.layoutResourceId = layoutResourceId;
		this.gridViewImageText = gridViewImageText;
		this.gridViewImageName = gridViewImageName;
		this.popupWindowGridView = popupWindowGridView;
	}

	public View getView(final int position, View view, ViewGroup parent) {
		
		if(view == null) {
			view = mActivity.getLayoutInflater().inflate(layoutResourceId, null);
		}
		
		((ImageView) view.findViewById(R.id.image)).setImageResource(mActivity.getResources().getIdentifier(gridViewImageName[position], "drawable", mActivity.getPackageName()));
		((TextView) view.findViewById(R.id.text)).setText(gridViewImageText[position]);
        ((RelativeLayout) view.findViewById(R.id.grid_view_item)).setOnClickListener(new OnClickListener() {
        	public void onClick(View view) {
        		onGridItemClickListener(position);
        	}
        });;
		return view;
	}
	
	private void onGridItemClickListener(int position) {
		
		popupWindowGridView.dismiss();
		switch(position) {
		
		case 0: //New Tab
			MainActivity.addNewTab(mActivity);
			break;
			
		case 1: //Close Tab
			MainActivity.closeCurrentTab(mActivity, mActivity.getActionBar().getSelectedTab());
			break;
			
		case 2: //Refresh
			String tag = mActivity.getActionBar().getSelectedTab().getText().toString();
			((WebView) mActivity.getFragmentManager().findFragmentByTag(tag).getView().findViewById(R.id.webView)).reload();
			break;

		case 5: //Settings
			mActivity.startActivity(new Intent(mActivity, Settings.class));
			break;
			
		case 3: //Bookmarks
		case 4: //History
		case 6: //About
			MainActivity.urlSelected = null;
			Intent intent = new Intent(mActivity, OptionsActivity.class);
			intent.putExtra("titleSelected", gridViewImageText[position]);
			mActivity.startActivity(intent);
			break;
			
		case 7: //Exit
			MainActivity.exitConfirmation(mActivity);
			break;
		}
	}

}
