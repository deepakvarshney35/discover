package com.enormous.discover.consumer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.BrandInfoActivity;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.asynctasks.GetBrandsTask;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.models.Brand;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

/**
 * Beacon Ranging Service
 * We have used Alt Beacon Library with Parser to act as iBeacon Detector. Each Beacon is associated with 1 Brand by the 
 * publisher and whenever beacon is detected, App will query User Class in Parse to find the correct Brand and will show 
 * which Brand was detected. 
 * 
 * When App Detects beacon for the first time, Notification is generated. After that notifications from the same shop wont be seen for 24Hrs.
 * Detected SHops will Be shown in MainScreenActivity- Home Fragment.
 * 
 * Service will run in background as long as app is running. And service will be stopped when the app is killed(back Button on MainScreen/ Removing from recent app)
 * 
 **/
public class BeaconRangingService extends Service implements BeaconConsumer {

	private final String TAG = "BeaconRangingService";
	/**
	 * This field is used to decide whether to send notification or not. If MainScreenActivity is visible, then no notification would be sent.
	 */
	public static boolean isMainScreenActivityVisible = false;
	private BeaconManager beaconManager;
	private ArrayList<String> scannedUUIDS;
	private ArrayList<String> newUUIDS;
	private UUIDRequester uuidRequester;
	private boolean isFirst = true;
	private NotificationManager notificationManager;
	public static final String ACTION_SEND_UUIDS = "ACTION_SEND_UUIDS";
	public static final String ACTION_REQUEST_UUIDS = "ACTION_REQUEST_UUIDS";
	public static final int NOTIFICATION_ID = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "service started");

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		//initialize and bind beaconManager
		beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
		beaconManager.setForegroundBetweenScanPeriod(5000);
		beaconManager.setForegroundScanPeriod(1100);
		//ad ability to detect iBeacons
		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
		beaconManager.bind(this);

		//initialize and register UUIDRequester broadcastreceiver
		uuidRequester = new UUIDRequester();
		IntentFilter filter = new IntentFilter(ACTION_REQUEST_UUIDS);
		registerReceiver(uuidRequester, filter);

		//initialize new UUIDS list
		newUUIDS = new ArrayList<String>();

		return Service.START_NOT_STICKY;
	}

	public void updateUUIDS() {
		if (newUUIDS != null && scannedUUIDS != null ) {
			Log.d("beacons", scannedUUIDS.toString());
			if (!newUUIDS.containsAll(scannedUUIDS) || !scannedUUIDS.containsAll(newUUIDS)) {
				newUUIDS = new ArrayList<String>(scannedUUIDS);
				if (!isMainScreenActivityVisible) {
					sendNotifications(newUUIDS);
				} 
				addToHistory();
			} 
		}
	}

	//Brand are added to History after detecting.
	public void addToHistory() {
        if(ParseUser.getCurrentUser()!=null) {
            ArrayList<String> copyUUIDS = new ArrayList<String>(newUUIDS);
            ArrayList<String> userHistory = (ArrayList<String>) ParseUser.getCurrentUser().get("userHistory");
            ArrayList<String> copyUserHistory;
            if (userHistory == null) {
                copyUserHistory = new ArrayList<String>();
                Collections.reverse(copyUUIDS);
                userHistory = new ArrayList<String>(copyUUIDS);
            } else {
                copyUserHistory = userHistory;
                for (String UUID : copyUUIDS) {
                    if (userHistory.contains(UUID)) {
                        userHistory.remove(UUID);
                    }
                    userHistory.add(0, UUID);
                }
            }
            //finally, upload list to parse
            if (!userHistory.equals(copyUserHistory)) {
                MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
                MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d(TAG, "user history updated successfully");
                        } else {
                            Log.d(TAG, "user history update error: " + e.getMessage());
                        }
                    }
                });
            }
        }
	}

	public void sendUUIDS() {
		Intent intent = new Intent(ACTION_SEND_UUIDS);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("UUIDS", newUUIDS);
		intent.putExtras(bundle);
		sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//unbind beaconManager
		beaconManager.unbind(this);

		//register UUIDREquester broadcastreceiver
		unregisterReceiver(uuidRequester);

		Log.d(TAG, "service stopped");

	}

	//---------------------BeaconConsumer method------------------------------------

	@Override
	public void onBeaconServiceConnect() {
		beaconManager.setRangeNotifier(new RangeNotifier() {

			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> ibeacons, Region region) {
				Iterator<Beacon> iterator = ibeacons.iterator();
				scannedUUIDS = new ArrayList<String>();
				while (iterator.hasNext()) {
					Beacon beacon = iterator.next();
					String UUID = beacon.getId1().toString();
					if (!UUID.equals("00000000-0000-0000-0000-000000000000")) {
						scannedUUIDS.add(UUID);
					}
				}

				updateUUIDS();
				//send UUIDS automatically when the service first starts
				if (isFirst) {
					sendUUIDS();
					isFirst = false;
					Log.i(TAG, "SENT FIRST");
				}
			}
		});
		try {
			beaconManager.startRangingBeaconsInRegion(new Region("myRegion", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	class UUIDRequester extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			sendUUIDS();
		}
	}

	private void sendNotifications(ArrayList<String> uuids) {
        if (ParseUser.getCurrentUser() != null) {
            if (uuids.size() > 0) {
                ArrayList<String> uuIDS = new ArrayList<String>();
                final Resources res = getResources();
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                ArrayList<String> selectedCategories = (ArrayList<String>) ParseUser.getCurrentUser().get("selectedCategories");
                final SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences("MyPrefs", Service.MODE_PRIVATE);
                final Date date = new Date(System.currentTimeMillis());
                for (String uuid : uuids) {
                    Long savedTime = sharedpreferences.getLong("uuidKey" + uuid, date.getTime());
                    if (savedTime <= date.getTime()) {
                        uuIDS.add(uuid);
                    }
                }
                if (uuIDS.size() > 0) {
                    new GetBrandsTask(uuIDS, selectedCategories, null, null, null, null, null, null, new GetBrandsTask.GetBrandsCallback() {

                        @Override
                        public void done(ArrayList<Brand> brands) {
                            if (brands != null) {
                                Log.d(TAG, "sending notifications: " + brands.size());
                                for (Brand brand : brands) {
                                    builder.setLargeIcon(Bitmap.createScaledBitmap(
                                            Utils.decodeImageResource(res, res.getIdentifier(brand.getCategoryName().toLowerCase(Locale.ENGLISH), "drawable", getPackageName()), 250, 250)
                                            , (int) res.getDimension(android.R.dimen.notification_large_icon_width)
                                            , (int) res.getDimension(android.R.dimen.notification_large_icon_height)
                                            , false))
                                            .setSmallIcon(R.drawable.ic_notification_icon)
                                            .setContentTitle(brand.getName())
                                            .setContentText(brand.getLocation())
                                            .setContentInfo(brand.getCategoryName())
                                            .setPriority(Notification.PRIORITY_MAX);
                                    Intent resultIntent = new Intent(BeaconRangingService.this, BrandInfoActivity.class);
                                    resultIntent.putExtra("selectedBrand", brand);
                                    resultIntent.setAction(System.currentTimeMillis() + "");  //add time to intent to make it unique (so that brand extras dont get updated)
                                    PendingIntent pendingIntent = PendingIntent.getActivity(BeaconRangingService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    builder.setContentIntent(pendingIntent);
                                    Notification notification = builder.build();
                                    notification.defaults = Notification.DEFAULT_ALL;
                                    notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
                                    notificationManager.notify(brand.getUUID(), NOTIFICATION_ID, notification);
                                    // No more notifications for 24 hours for selected UUID
                                    Editor editor = sharedpreferences.edit();
                                    editor.putLong("uuidKey" + brand.getUUID(), date.getTime() + (60000 * 60 * 24));
                                    editor.commit();
                                }
                            }
                        }
                    }).execute();
                }
            }
        }
    }

}
