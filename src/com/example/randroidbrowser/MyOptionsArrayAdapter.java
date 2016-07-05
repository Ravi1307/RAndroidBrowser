package com.example.randroidbrowser;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyOptionsArrayAdapter extends ArrayAdapter<String> {

	private Activity mActivity;
	private int layoutResourceId;
	private String[] title, url;
	
	public MyOptionsArrayAdapter(Activity mActivity, int layoutResourceId, String[] title, String[] url) {
		super(mActivity, layoutResourceId, url);
		
		this.mActivity = mActivity;
		this.layoutResourceId = layoutResourceId;
		this.url = url;
		this.title = title;
	}
	
	public View getView(final int position, View view, ViewGroup parent) {
		
		if(view == null) {
			view = mActivity.getLayoutInflater().inflate(layoutResourceId, null);	
		}

		view.findViewById(R.id.active_tab_layout_select).setVisibility(View.GONE);	
		TextView textViewUrl = ((TextView) view.findViewById(R.id.active_tab_url_text));
		textViewUrl.setSingleLine(false);
		textViewUrl.setText(url[position]);
		
		((TextView) view.findViewById(R.id.active_tab_title_text)).setText(title[position]);
		((ImageView) view.findViewById(R.id.active_tab_close_image)).setVisibility(View.GONE);
		((LinearLayout) view.findViewById(R.id.layout_list_view)).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				MainActivity.urlSelected = url[position];
				mActivity.finish();
			}
		});
		
		return view;
	}
	
}
