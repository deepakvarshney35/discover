package com.enormous.pkpizzas.consumer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.common.Chat;
import com.enormous.pkpizzas.consumer.models.Brand;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.firebase.client.Firebase;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Share Brand Activity
 * - Makes user share Brand on Facebook, for availing the offer.
 * - Defaults Method available are used. On completeing the task Class "SharedOffer" is updated.
 * - Data from the SharedOffer Class are shown in Publisher app.(MainScreenGraphFragment)
 * 
 * */
public class ShareBrandActivity extends Activity{

    private static final String FIREBASE_URL = "https://pkpizzas-merchant.firebaseio.com";
    private Firebase ref,pRef;
	private UiLifecycleHelper uiHelper;
	private Brand selectedBrand;
	private String selectedItemName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		selectedBrand = getIntent().getParcelableExtra("selectedBrand");
		selectedItemName = getIntent().getExtras().getString("selectedItemName");

		uiHelper = new UiLifecycleHelper(this, null);
		uiHelper.onCreate(savedInstanceState);
		if (FacebookDialog.canPresentShareDialog(getApplicationContext(), 
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			// Publish the post using the Share Dialog
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
			.setLink(selectedBrand.getWebsite())
			.setDescription(selectedBrand.getAbout())
			.setPlace(selectedBrand.getLocation())
			.setCaption(selectedBrand.getName())
			.setApplicationName("Discover")
			.setPicture(selectedBrand.getCoverPictureUrl())
			.build();
			uiHelper.trackPendingDialogCall(shareDialog.present());

		} else {
			// Fallback. For example, publish the post using the Feed Dialog
			publishFeedDialog();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				Log.e("Activity", String.format("Error: %s", error.toString()));
				finish();
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				Log.i("Activity", "Success!");
				final Date date = new Date();
				//Feedback to Publisher app HERE
				ParseObject shareFb = new ParseObject("SharedOffer");
				shareFb.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
				shareFb.put("username", ParseUser.getCurrentUser().getUsername());
				shareFb.put("brandObjectId", selectedBrand.getObjectId());
				shareFb.put("firstName", ParseUser.getCurrentUser().get("firstName"));
				shareFb.put("lastName",ParseUser.getCurrentUser().get("lastName"));
				shareFb.put("date", date);
				shareFb.put("itemName", selectedItemName);
				shareFb.saveInBackground();
				
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
				query.whereEqualTo("brandObjectId", selectedBrand.getObjectId());
				query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
				query.findInBackground(new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> checkins, ParseException e) {
						if(e==null){
							for(ParseObject checkin : checkins){
								int num;
								num = checkin.getInt("SharedOffer");
								num  = num+1;
								checkin.put("SharedOffer", num);
								checkin.put("date", date);
								checkin.saveInBackground();
							}
						}
					}
				});
                chatShareNotify();
				finish();
			}
		});
	}

    private void chatShareNotify() {

            ref = new Firebase(FIREBASE_URL).child(ParseUser.getCurrentUser().getObjectId()+selectedBrand.getObjectId());
            Date date = new Date();
            pRef=  ref.push();
            Chat chat = new Chat(pRef.getName(),"::NewShare:: "+ParseUser.getCurrentUser().get("firstName")+" "+selectedItemName, ParseUser.getCurrentUser().getObjectId() , date.getTime());
            // Create a new, auto-generated child of that chat location, and save our chat data there
            pRef.setValue(chat);
            //Sends Parse Notification to channel named after Unique ID. (customer + merchant ID)

            // Find users near a given location
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            userQuery.whereEqualTo("objectId", selectedBrand.getObjectId());

            // Find devices associated with these users
            ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
            //pushQuery.whereMatchesQuery("user", userQuery);
            pushQuery.whereEqualTo("channels", "Publisher");
            pushQuery.whereEqualTo("deviceId", selectedBrand.getObjectId());
            try {
                JSONObject data = new JSONObject("{\"header\": \"" + ParseUser.getCurrentUser().get("firstName")+" has shared your offer "+selectedItemName + "\","
                        + "\"action\": \"com.enormous.pkpizzas.publisher.UPDATE_STATUS\","
                        + "\"myObjectId\": \""+ ParseUser.getCurrentUser().getObjectId() + "\","
                        + "\"merchantUserId\": \""+ selectedBrand.getObjectId() + "\","
                        + "\"customerObjectId\": \""+ ParseUser.getCurrentUser().getObjectId() + "\","
                        + "\"profilePictureUrl\": \""+ ParseUser.getCurrentUser().getString("profilePictureUrl") + "\","
                        + "\"customerName\": \""+ ParseUser.getCurrentUser().getString("firstName")+" "+ParseUser.getCurrentUser().getString("lastName") + "\"}");
                //Send Push notification
                ParsePush push = new ParsePush();
                push.setQuery(pushQuery);
                //push.setChannel(customerObjectId+merchantUserId);
                push.setData(data);
                push.sendInBackground(new SendCallback() {

                    @Override
                    public void done(ParseException e) {
                        // TODO Auto-generated method stub
                        if(e==null){
                            //Toast.makeText(ChatMessageActivity.this, "DONE", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(ShareBrandActivity.this, ""+e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
    }

    @Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}
	private void publishFeedDialog() {
		Bundle params = new Bundle();
		params.putString("name", "Discover");
		params.putString("caption", selectedBrand.getName());
		params.putString("description", selectedBrand.getAbout());
		params.putString("link", selectedBrand.getWebsite());
		params.putString("picture", selectedBrand.getCoverPictureUrl());
		params.putString("place", selectedBrand.getLocation());

		WebDialog feedDialog = (
				new WebDialog.FeedDialogBuilder(ShareBrandActivity.this,
						Session.getActiveSession(),
						params))
						.setOnCompleteListener(new OnCompleteListener() {

							@Override
							public void onComplete(Bundle values,
									FacebookException error) {
								if (error == null) {
									// When the story is posted, echo the success
									// and the post Id.
									final String postId = values.getString("post_id");
									if (postId != null) {
										Log.i("tag", "Success");

										//Feedback to Publisher app HERE

										//Feedback to Publisher app HERE
										ParseObject shareFb = new ParseObject("SharedOffer");
										shareFb.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
										shareFb.put("username", ParseUser.getCurrentUser().getUsername());
										shareFb.put("brandObjectId", selectedBrand.getObjectId());
										shareFb.put("firstName", ParseUser.getCurrentUser().get("firstName"));
										shareFb.put("lastName",ParseUser.getCurrentUser().get("lastName"));
										shareFb.put("itemName", selectedItemName);
										shareFb.saveInBackground();
										finish();

									} else {
										// User clicked the Cancel button
										Log.i("tag", "User Cancelled");
										finish();
									}
								} else if (error instanceof FacebookOperationCanceledException) {
									// User clicked the "x" button
									Log.i("tag", "Cancelled");
									finish();
								} else {
									// Generic, ex: network error
									Log.i("tag", "Error posting story");
									finish();
								}
							}

						})
						.build();
		feedDialog.show();
	}
}
