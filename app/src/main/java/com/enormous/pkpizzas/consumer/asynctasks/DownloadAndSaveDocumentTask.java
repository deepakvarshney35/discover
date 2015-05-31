package com.enormous.pkpizzas.consumer.asynctasks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.DiscoverApp;
import com.enormous.pkpizzas.consumer.R;
import com.parse.ParseFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAndSaveDocumentTask extends AsyncTask<ParseFile, Integer, Boolean> {
	
	private Context c;
	private File doc;
	private File documentsFolder;
	private int contentLength;
	
	//progress dialog views
	private Dialog progressDialog;
	private ProgressBar progressBar;
	private TextView progressTextView;

	public DownloadAndSaveDocumentTask(Context c) {
		this.c = c;
		documentsFolder = new File(DiscoverApp.EXTERNAL_CACHE_DIR + "/documents");
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//set up progress dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View progressDialogView = inflater.inflate(R.layout.dialog_download_document_progress, null);
		progressBar = (ProgressBar) progressDialogView.findViewById(R.id.determinateProgressBar);
		progressTextView = (TextView) progressDialogView.findViewById(R.id.progressTextView);
		builder.setView(progressDialogView);
		progressBar.setIndeterminate(true);
		progressDialog = builder.create();
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				DownloadAndSaveDocumentTask.this.cancel(true);
				//delete partially downloaded document
				if (doc != null) {					
					if (doc.exists()) {
						doc.delete();
					}
				}
			}
		});
		progressDialog.show();
	}
	
	@Override
	protected Boolean doInBackground(ParseFile... params) {
		if (!documentsFolder.exists()) {
			documentsFolder.mkdirs();
		}
		doc = new File(documentsFolder.getAbsolutePath() + "/" + params[0].getName());
		Boolean success = false;
		if (doc.exists()) {
			success = true;
		}
		else {
			HttpURLConnection hConn = null;
			InputStream is = null;
			OutputStream os = null;
			int counter = 0;
			try {
				hConn = (HttpURLConnection) new URL(params[0].getUrl()).openConnection();
				contentLength = hConn.getContentLength();
				hConn.setReadTimeout(30000);
				hConn.setConnectTimeout(30000);
				is = hConn.getInputStream();
				os = new FileOutputStream(doc);
				int read = 0;
				byte[] buffer = new byte[1024];
				while ((read = is.read(buffer)) != -1) {
					os.write(buffer, 0, read);
					counter += read;
					publishProgress(counter);
				}
				success = true;
			}
			catch (Exception e) {
//				Log.e("TEST", "error downloading document: " + e.getMessage());
                e.printStackTrace();
			}
			finally { 
				try {
					if (hConn != null) {
						hConn.disconnect();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (is != null) {
						is.close();
					}
				}
				catch (Exception e) {
                    e.printStackTrace();
				}
				try {
					if (os != null) {
						os.close();
					}
				}
				catch (Exception e) {
                    e.printStackTrace();
				}
			}
		}
		return success;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		//make the progressBar determinate when the download starts
		if (progressBar.isIndeterminate()) {
			progressBar.setIndeterminate(false);
		}
		int currentProgress =  (int) (((double) values[0] / contentLength) * 100);
		progressBar.setProgress(currentProgress);
		progressTextView.setText(currentProgress + "%");
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result) {
//			Log.d("TEST", "DOCUMENT DOWNLOAD SUCCESSFUL");
			showDocument(c, doc);
		}
		else {
//			Log.e("TEST", "DOCUMENT DOWNLOAD FAILED");
		}
		progressDialog.dismiss();
	}

    private String getFileExtension(File file) {
		String fileName = file.getName();
		return fileName.substring(fileName.indexOf(".") + 1, fileName.length());
	}

    public void showDocument(Context c, File doc) {
		Intent viewDocument = new Intent();
		viewDocument.setAction(Intent.ACTION_VIEW);
		String MIME_TYPE = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(doc));
		if (MIME_TYPE != null) {				
			viewDocument.setDataAndType(Uri.fromFile(doc), MIME_TYPE);
			c.startActivity(Intent.createChooser(viewDocument, "Open with..."));
		}
		else {
			Toast.makeText(c, "File format is not supported.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void clearDocumentCache() {
		File documentsFolder = new File(DiscoverApp.EXTERNAL_CACHE_DIR + "/documents");
		if (documentsFolder.exists()) {
			File[] files = documentsFolder.listFiles();
			for (File file : files) {
				file.delete();
			}
		}
	}

}
