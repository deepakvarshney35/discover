package com.enormous.pkpizzas.consumer.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.activities.BrandInfoActivity;
import com.enormous.pkpizzas.consumer.activities.ChatMessageActivity;
import com.enormous.pkpizzas.consumer.activities.MainScreenActivity;
import com.enormous.pkpizzas.consumer.common.ImageLoader;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.enormous.pkpizzas.consumer.models.Brand;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Brand List View Adapter.
 * 
 **/
public class BrandsListViewAdapter extends BaseAdapter {

    private final String TAG = "BrandListViewAdapter";
    private ArrayList<String> userHistory = new ArrayList<String>();
	private Context c;
	private ArrayList<Brand> brands;
	private Resources res;
	private ArrayList<String> brandBookmarks = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("brandBookmarks");

	public BrandsListViewAdapter(Context c, ArrayList<Brand> brands) {
		this.c = c;
		this.brands = brands;
		res = c.getResources();
	}

	@Override
	public int getCount() {
		return brands.size();
	}

	@Override
	public Object getItem(int pos) {
		return brands.get(pos);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int pos, View convertView, ViewGroup container) {
		View row = convertView;
		ViewHolder holder = null;
		if (row == null) {
			row = ((Activity) c).getLayoutInflater().inflate(R.layout.listview_brands_item_2, container, false);
			holder = new ViewHolder(row);
			row.setTag(holder);
		}
		else {
			holder = (ViewHolder) row.getTag();
		}

		final Brand brand = brands.get(pos);
		holder.brandNameTextView.setText(brand.getName());
		holder.brandLocationTextView.setText(brand.getLocation());
		addTags(res, brand.getTags(), holder.tagsLinearLayout);
		ImageLoader.getInstance().displayImage(c, brand.getCoverPictureUrl(), holder.brandCoverImageView, false, 640, 640, 0);
		ImageLoader.getInstance().displayImage(c, res.getIdentifier(brand.getCategoryName().toLowerCase(), "drawable", c.getPackageName())+"", holder.brandCategoryImageView, true, 300, 300, 0);

		//check if this brand has already been bookmarked in order to set the bookmark button image
		boolean isBookmarked;
		if (brandBookmarks == null) {
			brandBookmarks = new ArrayList<String>();
		}
		final String UUID = brand.getUUID();
		if (brandBookmarks.contains(UUID)) {
			isBookmarked = true;
			ImageLoader.getInstance().displayImage(c, res.getIdentifier("ic_action_bookmark_selected", "drawable", c.getPackageName())+"", holder.bookmarkImageView, true, 100, 100, 0);
		}
		else {
			isBookmarked = false;
			ImageLoader.getInstance().displayImage(c, res.getIdentifier("ic_action_bookmark", "drawable", c.getPackageName())+"", holder.bookmarkImageView, true, 100, 100, 0);
		}
		holder.bookmarkLinearLayout.setTag(isBookmarked);

		//set onClick listener for brand image and bookmark button
		View.OnClickListener onClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.brandCategoryImageViewContainer:
                    final Date date = new Date();
                    //Save Check in upon clicking the Brand
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
                    query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
                    query.whereEqualTo("brandObjectId", brand.getObjectId());
                    query.findInBackground(new FindCallback<ParseObject>() {

                        @Override
                        public void done(List<ParseObject> checkinList, ParseException e) {
                            // TODO Auto-generated method stub
                            if(e==null){
                                if(checkinList.size()==0){
                                    ParseObject checkin = new ParseObject("Checkin");
                                    checkin.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
                                    checkin.put("brandObjectId", brand.getObjectId());
                                    checkin.put("brandName", brand.getName());
                                    checkin.put("archive", false);
                                    checkin.put("date", date);
                                    checkin.put("type","");
                                    checkin.put("fullName", ParseUser.getCurrentUser().get("firstName")+" "+ParseUser.getCurrentUser().get("lastName"));
                                    checkin.put("email",ParseUser.getCurrentUser().get("email"));
                                    checkin.put("phoneNumber", ParseUser.getCurrentUser().get("phoneNumber"));
                                    checkin.put("SharedOffer", 0);
                                    checkin.put("CartItems", 0);
                                    if(ParseUser.getCurrentUser().get("profilePictureUrl")!=null){
                                        checkin.put("profilePictureUrl", ParseUser.getCurrentUser().get("profilePictureUrl"));
                                    }
                                    checkin.saveInBackground();

                                    //Add UUID to userHistory
                                    userHistory = (ArrayList<String>) ParseUser.getCurrentUser().get("userHistory");
                                    if(userHistory.contains(brands.get(pos).getUUID())){
                                        //update to new location
                                        userHistory.remove(brands.get(pos).getUUID());
                                        userHistory.add(brands.get(pos).getUUID());

                                        MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
                                        MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.d(TAG, "user history updated successfully");
                                                }
                                                else {
                                                    Log.d(TAG, "user history update error: " + e.getMessage());
                                                }
                                            }
                                        });
                                    }

                                } else {
                                    for(ParseObject chkin : checkinList){
                                        chkin.put("archive", false);
                                        chkin.put("date", date);
                                        chkin.saveInBackground();
                                    }

                                    //Add UUID to userHistory
                                    userHistory = (ArrayList<String>) ParseUser.getCurrentUser().get("userHistory");
                                    if(userHistory.contains(brands.get(pos).getUUID())){
                                        //update to new location
                                        userHistory.remove(brands.get(pos).getUUID());
                                        userHistory.add(brands.get(pos).getUUID());

                                        MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
                                        MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.d(TAG, "user history updated successfully");
                                                }
                                                else {
                                                    Log.d(TAG, "user history update error: " + e.getMessage());
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });

                    //go to BrandsInfo activity
                    Intent goToBrandInfo = new Intent(c, BrandInfoActivity.class);
                    Bundle bundle = new Bundle();
                    goToBrandInfo.putExtra("selectedBrand", brand);
                    c.startActivity(goToBrandInfo);
                    ((Activity) c).overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                    break;
					/*
                        //go to BrandsInfo activity
                        Intent goToBrandInfo1 = new Intent(c, ShareBrandActivity.class);
                        Bundle bundle1 = new Bundle();
                        goToBrandInfo1.putExtra("selectedBrand", brand);
                        c.startActivity(goToBrandInfo1);
                        ((Activity) c).overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                        break;*/
				case R.id.moreInformationLinearLayout:
                    if(!brands.get(pos).getObjectId().equals("GOOGLE")){
                        Intent goToChat = new Intent(c, ChatMessageActivity.class);
                        goToChat.putExtra("merchantUserId", brands.get(pos).getObjectId());
                        goToChat.putExtra("merchantName", brands.get(pos).getName());
                        goToChat.putExtra("merchantPic", brands.get(pos).getCoverPictureUrl());
                        goToChat.putExtra("merchantPhone", brands.get(pos).getPhone());
                        goToChat.putExtra("merchantEmail", brands.get(pos).getEmail());
                        c.startActivity(goToChat);
                    } else{
                        Toast.makeText(c, "Publisher not on Discover yet.", Toast.LENGTH_LONG).show();
                    }
                    break;
				case R.id.bookmarkLinearLayout:
					if ((Boolean) view.getTag()) {
						brandBookmarks.remove(UUID);
					}
					else {
						brandBookmarks.add(UUID);
						Toast.makeText(c, "'" + brand.getName() + "' added to bookmarks.", Toast.LENGTH_SHORT).show();
					}
					notifyDataSetChanged();
					//now, upload brandBookmarks to Parse
					MainScreenActivity.CURRENT_USER.put("brandBookmarks", brandBookmarks);
					MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e != null) {
								//                                    Log.e("TEST", "BrandsListViewAdapter bookmarkButton onClick: " + e.getMessage());
								e.printStackTrace();
							}
						}
					});
					break;
				}
			}
		};
		holder.brandCoverImageViewContainer.setOnClickListener(onClickListener);
		holder.bookmarkLinearLayout.setOnClickListener(onClickListener);
		holder.moreInformationLinearLayout.setOnClickListener(onClickListener);

		return row;
	}

	private void addTags(Resources res, ArrayList<String> tags, LinearLayout container) {
		container.removeAllViews();
		float marginRight = Utils.convertDpToPixel(7);
		if (tags != null) {
			LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lParams.setMargins(0, 0, (int) marginRight, 0);
			for (String tag : tags) {
				TextView textView = new TextView(c);
				textView.setText(tag);
				textView.setTextColor(res.getColor(R.color.textPrimary));
				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				//                textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
				textView.setLayoutParams(lParams);
				container.addView(textView);
			}
		}
	}

	private static class ViewHolder {
		ImageView brandCategoryImageView;
		TextView brandNameTextView;
		TextView brandLocationTextView;
		ImageView brandCoverImageView;
		FrameLayout brandCoverImageViewContainer;
		LinearLayout tagsLinearLayout;
		LinearLayout bookmarkLinearLayout;
		ImageView bookmarkImageView;
		LinearLayout moreInformationLinearLayout;

		public ViewHolder(View row) {
			brandCategoryImageView = (ImageView) row.findViewById(R.id.brandCategoryImageView);
			brandNameTextView = (TextView) row.findViewById(R.id.brandNameTextView);
			brandLocationTextView = (TextView) row.findViewById(R.id.brandLocationTextView);
			brandCoverImageView = (ImageView) row.findViewById(R.id.brandCoverImageView);
			brandCoverImageViewContainer = (FrameLayout) row.findViewById(R.id.brandCategoryImageViewContainer);
			tagsLinearLayout = (LinearLayout) row.findViewById(R.id.tagsLinearLayout);
			bookmarkLinearLayout = (LinearLayout) row.findViewById(R.id.bookmarkLinearLayout);
			bookmarkImageView = (ImageView) bookmarkLinearLayout.findViewById(R.id.bookmarkImageView);
			moreInformationLinearLayout = (LinearLayout) row.findViewById(R.id.moreInformationLinearLayout);
		}
	}
}


