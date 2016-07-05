package com.example.randroidbrowser;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyActiveTabsArrayAdapter extends ArrayAdapter<String> {

	private Activity mActivity;
	private int layoutResourceId;
	private ArrayList<String> activeTabsTitleArray, activeTabsUrlArray;
	
	public MyActiveTabsArrayAdapter(Activity mActivity, int layoutResourceId, ArrayList<String> activeTabsTitleArray, ArrayList<String> activeTabsUrlArray) {
		super(mActivity, layoutResourceId, activeTabsTitleArray);
		
		this.mActivity = mActivity;
		this.layoutResourceId = layoutResourceId;
		this.activeTabsUrlArray = activeTabsUrlArray;
		this.activeTabsTitleArray = activeTabsTitleArray;
	}
	
	public View getView(final int position, View view, ViewGroup parent) {
		
		if(view == null) {
			view = mActivity.getLayoutInflater().inflate(layoutResourceId, null);	}
		if(mActivity.getActionBar().getSelectedNavigationIndex() == position) {
			view.findViewById(R.id.active_tab_layout_select).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.active_tab_layout_select).setVisibility(View.INVISIBLE);	
		}
		((TextView) view.findViewById(R.id.active_tab_url_text)).setText(activeTabsUrlArray.get(position));
		((TextView) view.findViewById(R.id.active_tab_title_text)).setText(activeTabsTitleArray.get(position));
		((ImageView) view.findViewById(R.id.active_tab_close_image)).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				MainActivity.closeCurrentTab(mActivity, mActivity.getActionBar().getTabAt(position));
				if(MainActivity.popupWindowActiveTabs.isShowing()) {
					MainActivity.popupWindowActiveTabs.dismiss();
					MainActivity.showActiveTabs(mActivity);
				}
			}
		});
		return view;
	}	

}
