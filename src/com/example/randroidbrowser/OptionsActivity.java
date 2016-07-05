package com.example.randroidbrowser;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;

public class OptionsActivity extends ActionBarActivity {

	private String titleSelected;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		titleSelected = intent.getStringExtra("titleSelected");
		String[] titleAll = getResources().getStringArray(R.array.gridViewImageText);
		
		getActionBar().setTitle(titleSelected);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		if (titleSelected.contentEquals(titleAll[6])) {
			setContentView(R.layout.activity_about);
		} else {
			createList();
		}
	}

	private void createList() {

		ListView listView = new ListView(this);
		setContentView(listView);
		listView.setBackgroundColor(getResources().getColor(R.color.grey));
		(new CreateList(this, listView)).execute();
	}

	private void clearBookmarksOrHistory() {
		
		MainActivity.emptyFile(this, (titleSelected + "Title"));
		MainActivity.emptyFile(this, (titleSelected + "Url"));
		createList();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_options, menu);
		if (titleSelected.contentEquals("Bookmarks") || titleSelected.contentEquals("History")) {
			MenuItem item = (MenuItem) menu.findItem(R.id.clear_bookmarks_or_history);
			item.setTitle(item.getTitle().toString() + " " + titleSelected);
			item.setVisible(true);
		}
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.clear_bookmarks_or_history:
			clearBookmarksOrHistory();
			break;

		}
		return super.onOptionsItemSelected(item);
	}
	
	private class CreateList extends AsyncTask<Void, String, Void> {

		private Dialog dialog;
		private ListView listView;
		private Activity mActivity;
		
		public CreateList(Activity mActivity, ListView listView) {
			
			this.dialog = new Dialog(mActivity);
			this.mActivity = mActivity;
			this.listView = listView;
		}
		
		protected Void doInBackground(Void... voids) {
			
			String url = MainActivity.readFromFile(mActivity, (titleSelected + "Url"));
			if (url != "") {
				String title = MainActivity.readFromFile(mActivity, (titleSelected + "Title"));
				publishProgress(url, title);
			}
			return null;
		}

		protected void onPreExecute() {
			
			super.onPreExecute();
			ProgressBar progressBar = new ProgressBar(mActivity);
			dialog.setTitle("Please Wait...");
			dialog.setCanceledOnTouchOutside(false);
			progressBar.setPadding(20, 20, 20, 20);
			dialog.setContentView(progressBar);
			dialog.show();
		}
		
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			
			dialog.setTitle("Loading...");
			listView.setAdapter(new MyOptionsArrayAdapter(mActivity, R.layout.activity_active_tabs, 
				values[1].split("\n"), values[0].split("\n")));
		}
		
		protected void onPostExecute(Void result) {
			
			super.onPostExecute(result);
			dialog.dismiss();
		}
	
	}
	
}
