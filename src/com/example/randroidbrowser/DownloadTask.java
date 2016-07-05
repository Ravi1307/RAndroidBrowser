package com.example.randroidbrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<Void, Integer, String> {

	private Context mContext;
	private Dialog dialog;
	private ProgressBar fileProgressBar;
	private TextView fileSize, filePercent;
	private DownloadTaskDetails downloadTaskDetails;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder mBuilder;
	private int downloadNotificationId;
	
	public DownloadTask(Context mContext, Dialog dialog, ProgressBar fileProgressBar,
			TextView fileSize, TextView filePercent, String fileName, URL url) {

		this.downloadNotificationId = ++MainActivity.downloadNotificationId;
		this.mContext = mContext;
		this.dialog = dialog;
		this.fileProgressBar = fileProgressBar;
		this.fileSize = fileSize;
		this.filePercent = filePercent;
		this.downloadTaskDetails = new DownloadTaskDetails(fileSize, fileProgressBar, fileName, url);
		this.downloadTaskDetails.execute();
	}

	@SuppressWarnings("resource")
	protected String doInBackground(Void... values) {

		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		HttpURLConnection urlConnection = downloadTaskDetails.getHttpURLConnection();
		int totalFileSize = downloadTaskDetails.getTotalFileSize();
		
		try {
			while(downloadTaskDetails.getStatus() != AsyncTask.Status.FINISHED) {}
			
			if (downloadTaskDetails.canDownload()) {
			
				inputStream = urlConnection.getInputStream();
				fileOutputStream = new FileOutputStream(downloadTaskDetails.getFile());
				
				byte[] buffer = new byte[51200];
				int bufferLength = 0;
				int downloadFileSize = 0;
				
				while ((bufferLength = inputStream.read(buffer)) > 0) {
					if(isCancelled()) {
						stopDownloadNotification("Download Cancelled");
						terminateConnections(urlConnection, inputStream, fileOutputStream);
						return null;
					}
					fileOutputStream.write(buffer, 0, bufferLength);
					downloadFileSize += bufferLength;
					publishProgress(downloadFileSize, totalFileSize, bufferLength);
				}
			} else {
				return null;	
			}
		} catch (Exception e) {
			return null;
		} finally {
			terminateConnections(urlConnection, inputStream, fileOutputStream);
		}
		return "Download Complete";
	}

	protected void onPreExecute() {
		super.onPreExecute();

		startDownloadNotification();
		fileProgressBar.setVisibility(View.VISIBLE);
		if(downloadTaskDetails.getTotalFileSize() < 0) {
			fileProgressBar.setIndeterminate(true);	}
	}

	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

		String size, percent;
		String speed = (downloadTaskDetails.setSize(values[2]) + "/sec");
		
		if(values[1] > 0) {
			
			size = (downloadTaskDetails.setSize(values[0]) + " / " + downloadTaskDetails.setSize(values[1]));
			percent = (((int) ((values[0] * 100) / values[1])) + " % ");
			mBuilder.setProgress(values[1], values[0], false);
		} else {
			
			size = (downloadTaskDetails.setSize(values[0]));
			percent = "";
			mBuilder.setProgress(values[1], values[0], true);
		}
		
		fileSize.setText(" " + size + "\t" + speed);
		filePercent.setText(percent);
		fileProgressBar.setProgress(values[0]);
		
		mBuilder.setContentText(size + "\t\t" + speed);
		mBuilder.setContentInfo(percent);
		notificationManager.notify(downloadNotificationId, mBuilder.build());
	}

	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (result == null) {
			result = "Download Failed";	}
		
		Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
		stopDownloadNotification(result);
	}

	private void terminateConnections(HttpURLConnection urlConnection, InputStream inputStream, FileOutputStream fileOutputStream) {

		if (urlConnection != null) {
			urlConnection.disconnect();
		}
		try {
			if (inputStream != null) {
				inputStream.close();	}
		} catch (Exception e) {}
		try {
			if (fileOutputStream != null) {
				fileOutputStream.close();	}
		} catch (Exception e) {}
	}
	
	private void startDownloadNotification() {

		Intent intent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
		
		mBuilder = new NotificationCompat.Builder(mContext);
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setContentTitle(downloadTaskDetails.getFileName());
		if(downloadTaskDetails.getTotalFileSize() > 0) {
			mBuilder.setContentText(downloadTaskDetails.setSize(downloadTaskDetails.getTotalFileSize()));
			mBuilder.setContentInfo("0 % ");
			mBuilder.setProgress(downloadTaskDetails.getTotalFileSize(), 0, false);
		} else {
			mBuilder.setProgress(downloadTaskDetails.getTotalFileSize(), 0, true);
		}
		notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(downloadNotificationId, mBuilder.build());
	}
	
	private void stopDownloadNotification(String result) {
		
		MyWebViewClient.isDownloading = false;
		dialog.dismiss();
		Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mBuilder.setSound(ringtoneUri);
		mBuilder.setContentText(result);
		mBuilder.setContentInfo("");
		mBuilder.setProgress(0, 0, false);
		mBuilder.setAutoCancel(true);
		notificationManager.notify(downloadNotificationId, mBuilder.build());
	}

	private class DownloadTaskDetails extends AsyncTask<String, Void, Void> {
		
		private HttpURLConnection urlConnection;
		private File newFile;
		private URL url; 
		private String fileName;
		private int totalFileSize;
		private TextView fileSize;
		private ProgressBar fileProgressBar;
		private boolean canDownload;
	
		protected DownloadTaskDetails(TextView fileSize, ProgressBar fileProgressBar, String fileName, URL url) {
			
			this.fileName = fileName;
			this.url = url;
			this.urlConnection = null;
			this.newFile = null;
			this.totalFileSize = 0;
			this.canDownload = false;
			this.fileSize = fileSize;
			this.fileProgressBar = fileProgressBar;
		}
		
		protected HttpURLConnection getHttpURLConnection() {
			return urlConnection;	}
		
		protected File getFile() {
			return newFile;	}
		
		protected String getFileName() {
			return fileName;	}
		
		protected int getTotalFileSize() {
			return totalFileSize;	}
		
		protected boolean canDownload() {
			return canDownload;	}
		
		protected Void doInBackground(String... urls) {
			
			urlConnection = null;
			try {
				
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setReadTimeout(30000);
				urlConnection.setConnectTimeout(60000);
				urlConnection.setDoInput(true);
				urlConnection.connect();
				
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					
					File appFolder = new File(Environment.getExternalStorageDirectory() + "/RAndroid Browser/Downloads/");
					if (!appFolder.exists()) {
						appFolder.mkdirs();		}
					
					int trys = 100;
					while(totalFileSize == 0 && trys-- > 0) {
						totalFileSize = urlConnection.getContentLength();	}
					
					if(totalFileSize != 0) {
						canDownload = true;	}
				}
			} catch(Exception e) {}
			return null;
		}
		
		protected void onPostExecute(Void result) {
			
			super.onPostExecute(result);
			fileSize.setText(" " + setSize(totalFileSize));
			fileProgressBar.setMax(totalFileSize);
		}
		
		private String setSize(int length) {
			
			if(length < 0) {
				return "Unknown Size";	}
			
			String[] sizes = {" B", " KB", " MB", " GB", "TB"};
			
			float size = length;
			int i = 0;	
			
			while(((int) size > 1023) && (i++ < 5)) {
				size = (size / 1024);	}
			
			int end = 0;
			String fileSize = ("" + size);
			
			if((end = (fileSize.indexOf('.') + 3)) > fileSize.length()) {
				end = fileSize.length();	}
			
			return new String(fileSize.substring(0, end) + sizes[i]);
			
		}
		
	}
	
}
