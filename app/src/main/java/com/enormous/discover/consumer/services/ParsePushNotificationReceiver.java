package com.enormous.discover.consumer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.ChatMessageActivity;
import com.enormous.discover.consumer.common.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ParsePushNotificationReceiver extends BroadcastReceiver {

	private ParseUser user;
    public String merchantEmail,merchantPhone;
	public static String openedConfession = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		user = ParseUser.getCurrentUser();
		if(user!=null){
			try {
				//String action = intent.getAction();
				//String channel = intent.getExtras().getString("com.parse.Channel");
				JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

				String message = "New Chat!";
				if (json.has("header"))
					message = json.getString("header");

				String merchantUserId = user.getObjectId();
				if (json.has("merchantUserId"))
					merchantUserId = json.getString("merchantUserId");

				String merchantCategory = "bookmarks";
				if (json.has("merchantCategory"))
					merchantCategory = json.getString("merchantCategory");
				
				String myObjectId = user.getObjectId();
				if (json.has("myObjectId"))
					myObjectId = json.getString("myObjectId");

				String customerObjectId = "";
				if (json.has("customerObjectId"))
					customerObjectId = json.getString("customerObjectId");

				String brandName = "";
				if (json.has("brandName"))
					brandName = json.getString("brandName");

				if(myObjectId.equals(user.getObjectId())){
					Log.d("tag", "same user notification");
					//will be commented after testing
					//generateCustomNotification(context,channel, message ,Confession,attachedPhotoURL,confessionObjectId,voteCountNum,userObjectId);
				} else {
					if("".equals(openedConfession)){
						Log.d("tag", "notification when confessions are closed");
						//
						generateCustomNotification(context, message ,merchantUserId,customerObjectId,brandName,merchantCategory);
					} else if((customerObjectId+merchantUserId).equals(openedConfession)){
						Log.d("tag", "confession opened but is real time - no notification");
					}else{
						Log.d("tag", "confession opened but notification in other confession");
						generateCustomNotification(context, message ,merchantUserId,customerObjectId,brandName,merchantCategory);

					}
				}
			} catch (JSONException e) {
				Log.d("ParsePushNotificationReceiver", "JSONException: " + e.getMessage());
			}}
	}


	public void generateCustomNotification(Context context,String message, String merchantUserId, String customerObjectId, String brandName,String merchantCategory) {
		//Number of notifications
		SharedPreferences sharedpreferences = context.getSharedPreferences("MyPrefs", Service.MODE_PRIVATE);
		int notifications = sharedpreferences.getInt("key"+merchantUserId, 0);
		notifications= notifications +1;

        //Get Customer Data
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", merchantUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                merchantEmail = parseUsers.get(0).getEmail();
                merchantPhone = parseUsers.get(0).getString("phoneNumber");
                Log.d("Sanat",""+merchantEmail+" "+merchantPhone);
            }
        });



		final Resources res = context.getResources();
		
		Editor editor = sharedpreferences.edit();
		editor.putInt("key" + merchantUserId, notifications);
		editor.commit();
		// Show the notification
		if(message.contains("attachmentPicture.jpg")){
			new sendNotification(context).execute(message,brandName,merchantUserId,merchantCategory);
		}else{
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			builder.setLargeIcon(Bitmap.createScaledBitmap(
					Utils.decodeImageResource(res, res.getIdentifier(merchantCategory, "drawable", context.getPackageName()), 250, 250)
					, (int) res.getDimension(android.R.dimen.notification_large_icon_width)
					, (int) res.getDimension(android.R.dimen.notification_large_icon_height)
					, false))
					.setSmallIcon(R.drawable.ic_notification_icon)
					.setContentTitle(brandName)
					.setContentText(message)
					.setNumber(notifications)
					//		.addAction(android.R.drawable.ic_menu_share, "Share", intent1)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
			//.setContentInfo(NOTIFICATION_ID)
			// .setPriority(Notification.PRIORITY_MAX);
			Intent notificationIntent = new Intent(context, ChatMessageActivity.class);
			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			notificationIntent.putExtra("merchantUserId", merchantUserId);
			notificationIntent.putExtra("merchantName", brandName);
            notificationIntent.putExtra("merchantEmail", merchantEmail);
            notificationIntent.putExtra("merchantPhone", merchantPhone);
			int NOTIFICATION_ID = merchantUserId.hashCode();
			PendingIntent intent = PendingIntent.getActivity(context, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(intent);
			Notification notification = builder.build();
			//notification.bigContentView = bigView;
			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(NOTIFICATION_ID, notification);	
		}
	}
	private class sendNotification extends AsyncTask<String, Void, Bitmap> {

		Context context;
		String message,merchantUserId,merchantName,merchantCategory;

		public sendNotification(Context context) {
			super();
			this.context = context;
		}

		@Override
		protected Bitmap doInBackground(String... params) {

			InputStream in;
			merchantName = params[1] ;
			merchantUserId = params[2] ;
			merchantCategory = params[3];
			try {

				URL url = new URL(params[0]);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.connect();
				in = connection.getInputStream();
				Bitmap myBitmap = BitmapFactory.decodeStream(in);
				return myBitmap;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			final Resources res = context.getResources();
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			builder	.setLargeIcon(Bitmap.createScaledBitmap(
					Utils.decodeImageResource(res, res.getIdentifier(merchantCategory, "drawable", context.getPackageName()), 250, 250)
					, (int) res.getDimension(android.R.dimen.notification_large_icon_width)
					, (int) res.getDimension(android.R.dimen.notification_large_icon_height)
					, false))
			.setSmallIcon(R.drawable.ic_launcher_light_grey)
			.setContentTitle(merchantName)
			.setContentText(message)
			//		.addAction(android.R.drawable.ic_menu_share, "Share", intent1)
			.setStyle(new NotificationCompat.BigPictureStyle()
			.bigPicture(result)
			.setBigContentTitle(merchantName));
			//.setContentInfo(NOTIFICATION_ID)
			// .setPriority(Notification.PRIORITY_MAX);
			Intent notificationIntent = new Intent(context, ChatMessageActivity.class);
			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			notificationIntent.putExtra("merchantUserId", merchantUserId);
			notificationIntent.putExtra("merchantName", merchantName);
            notificationIntent.putExtra("merchantEmail", merchantEmail);
            notificationIntent.putExtra("merchantPhone", merchantPhone);
			//String not_ID = merchantUserId.replaceAll("[^0-9]", "");
			int NOTIFICATION_ID = merchantUserId.hashCode();
			PendingIntent intent = PendingIntent.getActivity(context, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(intent);
			Notification notification = builder.build();
			//notification.bigContentView = bigView;
			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}
}

