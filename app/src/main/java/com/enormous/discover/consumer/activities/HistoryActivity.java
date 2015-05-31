package com.enormous.discover.consumer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.adapters.BrandsListViewAdapter;
import com.enormous.discover.consumer.asynctasks.GetBrandsTask;
import com.enormous.discover.consumer.common.BrandsEndlessScrollListener;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.fragments.MainScreenHomeFragment;
import com.enormous.discover.consumer.models.Brand;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * Saves arraylist in parse of the brands which has been detected by mobile and are saved fo future reference with this activity.
 * Listing of all Brands is similar to MainScreenActivity.
 * */
public class HistoryActivity extends Activity {

	private ActionBar actionBar;
	private SwipeRefreshLayout swipeRefreshLayout;
	private ListView brandsListView;
	private BrandsListViewAdapter brandsListViewAdapter;
	private LinearLayout progressLinearLayout;
	private ArrayList<String> categoryConstraintList;
	private Handler handler;
	private ArrayList<Brand> brands,localBrands;
	private ArrayList<String> userHistory,userHistoryTemp;
	private LinearLayout nothingFoundLinearLayout;
	private LinearLayout endlessScrollLinearLayout;
	private LinearLayout noInternetLinearLayout;
	LocalBrandsViewAdapter localBrandsViewAdapter;
	private ListView listViewLocalBrands;

	ProgressBar progressBarLocalBrands;
	TextView headerTitle;

	String categoryConstraint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_history);
		findViews();

		localBrands = new ArrayList<Brand>();

		localBrandsViewAdapter = new LocalBrandsViewAdapter();
		listViewLocalBrands.setAdapter(localBrandsViewAdapter);

		//set actionBar properties
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);

		//get category constraint from intent
		categoryConstraint = getIntent().getStringExtra("categoryConstraint");
		if (categoryConstraint != null) {
			actionBar.setTitle(categoryConstraint);
			categoryConstraintList = new ArrayList<String>();
			categoryConstraintList.add(categoryConstraint.toLowerCase(Locale.ENGLISH));
			if(MainScreenHomeFragment.mCurrentLocation!=null){
				new GetPlaces(HistoryActivity.this,"",MainScreenHomeFragment.mCurrentLocation.getLatitude(), MainScreenHomeFragment.mCurrentLocation.getLongitude() , categoryConstraint).execute();
			}
		} else{
			headerTitle.setVisibility(View.GONE);
			progressBarLocalBrands.setVisibility(View.GONE);
		}
		//add ability to pull to refresh
		swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {
				if (categoryConstraint != null) {
					refreshBrandsCat();
				}else{
					refreshBrandsHistory();
				}
			}
		});

		//initalize brands and set brandsListView adapter
		brands = new ArrayList<Brand>();
		//add footer to brandsListView
		View footer = getLayoutInflater().inflate(R.layout.listview_brands_footer_endless_scroll, null);
		endlessScrollLinearLayout = (LinearLayout) footer.findViewById(R.id.endlessScrollLinearLayout);
		brandsListView.addFooterView(footer);

		brandsListViewAdapter = new BrandsListViewAdapter(this, brands);
		brandsListView.setAdapter(brandsListViewAdapter);

		//set custom onScrollListener to enable endlessScrolling
		brandsListView.setOnScrollListener(new BrandsEndlessScrollListener(userHistory, brandsListView, brands, brandsListViewAdapter, swipeRefreshLayout, endlessScrollLinearLayout));

		//set click listener to retry after internet connection has been extablished
		noInternetLinearLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (categoryConstraint != null) {
					refreshBrandsCat();
				}else{
					refreshBrandsHistory();
				}
			}
		});

		//get user's history (past UUIDS)
		userHistory = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("userHistory");

		Log.d("history", userHistory+"");
		if (userHistory == null) {
			//nothingFoundLinearLayout.setVisibility(View.VISIBLE);
			//progressLinearLayout.setVisibility(View.GONE);
			userHistory = new ArrayList<String>();
			MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
			MainScreenActivity.CURRENT_USER.saveInBackground();
		}
		else if (userHistory.size() == 0) {
			//nothingFoundLinearLayout.setVisibility(View.VISIBLE);
			//progressLinearLayout.setVisibility(View.GONE);
		}
		else {
            //reverse the UUIDs
            int userHistorySize=userHistory.size();
            userHistoryTemp = new ArrayList<String>();
            userHistoryTemp.clear();
            for(int i=0; i<userHistorySize; i++) {
                userHistoryTemp.add(userHistory.get(userHistorySize-(i+1)));
            }
            userHistory = (ArrayList<String>)userHistoryTemp.clone();

			//fetch brands for past UUIDS
			if (categoryConstraint != null) {
				refreshBrandsCat();
			}else{
				refreshBrandsHistory();
			}
		}
	}

	private void findViews() {
		actionBar = getActionBar();
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
		brandsListView = (ListView) findViewById(R.id.listViewBrands);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		nothingFoundLinearLayout = (LinearLayout) findViewById(R.id.nothingFoundLinearLayout);
		noInternetLinearLayout = (LinearLayout) findViewById(R.id.noInternetLinearLayout);
		listViewLocalBrands = (ListView)findViewById(R.id.listViewLocalBrands);
		progressBarLocalBrands = (ProgressBar)findViewById(R.id.progressBarLocalBrands);
		headerTitle = (TextView)findViewById(R.id.headerTitle);
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_overflow_refresh, menu);
		return true;
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.ic_action_refresh:
			if (categoryConstraint != null) {
				refreshBrandsCat();
			}else{
				refreshBrandsHistory();
			}
			break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
	}

	private void refreshBrandsCat() {
		noInternetLinearLayout.setVisibility(View.GONE);
		if (Utils.isConnectedToInternet(this)) {
			//only pass progressLinearLayout if the listview doesn't contain any items/brands
			swipeRefreshLayout.setRefreshing(true);
			if (brands.size() == 0) {
				GetBrandsTask getBrandsTask = new GetBrandsTask(getUUIDSToLoad(userHistory), categoryConstraintList, brands, brandsListViewAdapter, swipeRefreshLayout, null, null, brandsListView, null);
				getBrandsTask.execute();
			}
			else {
				GetBrandsTask getBrandsTask = new GetBrandsTask(getUUIDSToLoad(userHistory), categoryConstraintList, brands, brandsListViewAdapter, swipeRefreshLayout, null, null, brandsListView, null);
				getBrandsTask.execute();
			}
		}
		else {
			swipeRefreshLayout.setRefreshing(false);
			brandsListView.setVisibility(View.INVISIBLE);
			noInternetLinearLayout.setVisibility(View.VISIBLE);
			//nothingFoundLinearLayout.setVisibility(View.GONE);
		}
	}

	private void refreshBrandsHistory() {
		noInternetLinearLayout.setVisibility(View.GONE);
		if (Utils.isConnectedToInternet(this)) {
			//only pass progressLinearLayout if the listview doesn't contain any items/brands
			swipeRefreshLayout.setRefreshing(true);
			if (brands.size() == 0) {
				GetBrandsTask getBrandsTask = new GetBrandsTask(getUUIDSToLoad(userHistory), categoryConstraintList, brands, brandsListViewAdapter, swipeRefreshLayout, progressLinearLayout, nothingFoundLinearLayout, brandsListView, null);
				getBrandsTask.execute();
			}
			else {
				GetBrandsTask getBrandsTask = new GetBrandsTask(getUUIDSToLoad(userHistory), categoryConstraintList, brands, brandsListViewAdapter, swipeRefreshLayout, null, nothingFoundLinearLayout, brandsListView, null);
				getBrandsTask.execute();
			}
		}
		else {
			swipeRefreshLayout.setRefreshing(false);
			brandsListView.setVisibility(View.INVISIBLE);
			noInternetLinearLayout.setVisibility(View.VISIBLE);
			//nothingFoundLinearLayout.setVisibility(View.GONE);
		}
	}

	private ArrayList<String> getUUIDSToLoad(ArrayList<String> userHistory) {
		//retrieve only the required UUIDS from userHistory
		Log.d("history history", userHistory+"");
		ArrayList<String> uuidsToLoad = new ArrayList<String>();
		if (userHistory.size() > BrandsEndlessScrollListener.NUMBER_OF_ITEMS_TO_LOAD) {			
			for (int i=0;i<BrandsEndlessScrollListener.NUMBER_OF_ITEMS_TO_LOAD;i++) {
				uuidsToLoad.add(userHistory.get(i));
			}	
		}
		else {
			for (int i=0;i<userHistory.size();i++) {
				uuidsToLoad.add(userHistory.get(i));
			}
		}
		return uuidsToLoad;
	}
	private class LocalBrandsViewAdapter extends BaseAdapter implements OnItemClickListener {

		public LocalBrandsViewAdapter() {
			listViewLocalBrands.setOnItemClickListener(this);
		}

		@Override
		public int getCount() {
			return localBrands.size();
		}

		@Override
		public Object getItem(int pos) {
			return localBrands.get(pos);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewHolder holder = null;
			if (row == null) {
				row = getLayoutInflater().inflate(R.layout.listview_search_item, parent, false);
				holder = new ViewHolder(row);
				row.setTag(holder);
			}
			else {
				holder = (ViewHolder) row.getTag();
			}
			Brand brand = localBrands.get(pos);
			Resources res = getResources();
			//set card background according to position
			//Log.i("TEST", pos + " pos");
			if (pos == 0) {
				row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_top));
			}
			else if (pos == localBrands.size()-1) {
				row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_bottom));
			}
			else {
				row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey));
			}
			ImageLoader.getInstance().displayImage(getApplicationContext(), res.getIdentifier(brand.getCategoryName(), "drawable", getPackageName())+"", holder.brandCategoryImageView, true, 300, 300, 0);
			holder.brandLocationTextView.setText(brand.getLocation());
			holder.brandNameTextView.setText(brand.getName());
			addTags(res, brand.getTags(), holder.tagsLinearLayout);

			return row;
		}

		@Override
		public void onItemClick(AdapterView<?> adv, View v, final int pos,
				long arg3) {
			final Date date = new Date();
			//Save Check in
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
			query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
			query.whereEqualTo("brandObjectId", localBrands.get(pos).getObjectId());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> checkinList, ParseException e) {
					// TODO Auto-generated method stub
					if(e==null){
						if(checkinList.size()==0){
							ParseObject checkin = new ParseObject("Checkin");
							checkin.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
							checkin.put("brandObjectId",  localBrands.get(pos).getObjectId());
							checkin.put("brandName",  localBrands.get(pos).getName());
							checkin.put("archive", false);
							checkin.put("type","remote");
							checkin.put("date", date);
							checkin.put("fullName", ParseUser.getCurrentUser().get("firstName")+" "+ParseUser.getCurrentUser().get("lastName"));
							checkin.put("email",ParseUser.getCurrentUser().get("email"));
							checkin.put("phoneNumber", ParseUser.getCurrentUser().get("phoneNumber"));
							checkin.put("SharedOffer", 0);
							checkin.put("CartItems", 0);
							if(ParseUser.getCurrentUser().get("profilePictureUrl")!=null){
								checkin.put("profilePictureUrl", ParseUser.getCurrentUser().get("profilePictureUrl"));
							}
							checkin.saveInBackground();
						} else {
							for(ParseObject chkin : checkinList){
								chkin.put("archive", false);
								chkin.put("date", date);
								chkin.saveInBackground();
							}
						}
					}
				}
			});

			Intent goToBrandInfo = new Intent(HistoryActivity.this, BrandInfoActivity.class);
			goToBrandInfo.putExtra("selectedBrand", localBrands.get(pos));
			startActivity(goToBrandInfo);
			overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
		}

		private void addTags(Resources res, ArrayList<String> tags, LinearLayout container) {
			container.removeAllViews();
			float marginRight = Utils.convertDpToPixel(7);
			if (tags != null) {
				LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lParams.setMargins(0, 0, (int) marginRight, 0);
				for (String tag : tags) {
					TextView textView = new TextView(HistoryActivity.this);
					textView.setText(tag);
					textView.setTextColor(res.getColor(R.color.textPrimary));
					textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
					textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
					textView.setLayoutParams(lParams);
					container.addView(textView);
				}
			}
		}
	}

	private static class ViewHolder {
		ImageView brandCategoryImageView;
		TextView brandNameTextView;
		TextView brandLocationTextView;
		LinearLayout tagsLinearLayout;

		public ViewHolder(View row) {
			brandCategoryImageView = (ImageView) row.findViewById(R.id.brandCategoryImageView);
			brandNameTextView = (TextView) row.findViewById(R.id.brandNameTextView);
			brandLocationTextView = (TextView) row.findViewById(R.id.brandLocationTextView);
			tagsLinearLayout = (LinearLayout) row.findViewById(R.id.tagsLinearLayout);
		}
	}
	class GetPlaces extends AsyncTask<Brand, Void, ArrayList<Brand>>
	{
		Context mContext;
		//String mToken;
		double latitude=0,longitude=0;
		String category;

		public GetPlaces(Context aContext,String aToken,double lat,double lon,String categoryConstraint) {
			super();
			mContext = aContext;
			//mToken = aToken;
			latitude =lat;
			longitude =lon;
			category = categoryConstraint;
		}

		@Override
		// three dots is java for an array of double
		protected ArrayList<Brand> doInBackground(Brand... args)
		{

			Log.d("gottaGo", "doInBackground HISTORY");

			ArrayList<Brand> predictionsArr = new ArrayList<Brand>();

			double radius=50000;
			//String key="AIzaSyBzKW9Pp5qHb7vV50iRkCBp6ZDFST0ERT8";
			String key="AIzaSyB3iHBPT4rstXdka5dITdJO2fKs9we5mQ8";
			//mToken = "";
			category = category.toLowerCase().replace(" ", "");
			if(category.equals("movies")){
				category="movie_theater";
			}else if (category.equals("bookstore")) {
				category="book_store";
			}else if (category.equals("apparels")) {
				category="clothing_store";
			}else if (category.equals("food")) {
				category="food"; //restaurant,meal_delivery,meal_takeaway
			}else if (category.equals("cafe")) {
				category="cafe";
			}else if (category.equals("food")) {
				category="restaurant";
			}else if (category.equals("conveniencestore")) {
				category="convenience_store";
			}else if (category.equals("dentist")) {
				category="dentist";
			}else if (category.equals("doctor")) {
				category="doctor";
			}else if (category.equals("departmentstore")) {
				category="department_store";
			}else if (category.equals("electronicsstore")) {
				category="electronics_store";
			}else if (category.equals("beautysalon")) {
				category="beauty_salon";
			}else if (category.equals("electrician")) {
				category="electrician";
			}else if (category.equals("grocery")) {
				category="grocery_or_supermarket";
			}else{
				//return predictionsArr;
			}
			Log.d("gottaGo", "doInBackground HISTORY category="+category);
			String PlaceName="";
			double Lat=0 , Long=0;
			try
			{
				URL googlePlaces = new URL(
						// URLEncoder.encode(url,"UTF-8");
				"https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&rankby=distance&types="+"country"
						+ "%7C"+category
						+ "&sensor=true&key="+key);
				URLConnection tc = googlePlaces.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						tc.getInputStream()));

				String line="";
				String placeid="";
				StringBuffer sb = new StringBuffer();
				//take Google's legible JSON and turn it into one big string.
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
				//turn that string into a JSON object
				for(int i=0;i<20;i++){
					JSONObject results = new JSONObject(sb.toString());	
					if((JSONArray)results.get("results") !=null){
						Long = ((JSONArray)results.get("results")).getJSONObject(i) .getJSONObject("geometry").getJSONObject("location") .getDouble("lng");
						Lat = ((JSONArray)results.get("results")).getJSONObject(i) .getJSONObject("geometry").getJSONObject("location") .getDouble("lat");
						Log.d("TAG", Lat+" "+Long);
						PlaceName = ((JSONArray)results.get("results")).getJSONObject(i).getString("name");
						placeid =  ((JSONArray)results.get("results")).getJSONObject(i).getString("place_id");
						JSONArray categoryArray =  ((JSONArray)results.get("results")).getJSONObject(i).getJSONArray("types");
					}
					//mToken = results.getString("next_page_token");
					String line1="";
					URL googlePlacesInfo = new URL(
							"https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeid+"&key="+key);
					URLConnection tc1 = googlePlacesInfo.openConnection();
					BufferedReader in1 = new BufferedReader(new InputStreamReader(
							tc1.getInputStream()));
					StringBuffer sb1 = new StringBuffer();
					//take Google's legible JSON and turn it into one big string.
					while ((line1 = in1.readLine()) != null) {
						sb1.append(line1);
					}
					//turn that string into a JSON object
					JSONObject results1 = new JSONObject(sb1.toString());
					//String Phone = ((JSONObject)results1.get("result")).getString("formatted_phone_number");
					String Address = "";
					String icon  = "";
					if(!results1.getString("status").equals("OVER_QUERY_LIMIT")){
						if(((JSONObject)results1.get("result")).getString("formatted_address")!=null){
							Address = ((JSONObject)results1.get("result")).getString("formatted_address");
						}
						if( ((JSONObject)results1.get("result")).getString("icon")!=null){
							icon = ((JSONObject)results1.get("result")).getString("icon");
						}
					}
					ArrayList<String> tags = new ArrayList<String>();
					predictionsArr.add(new Brand("GOOGLE", "GOOGLE", PlaceName, "", "", Address, tags, icon, "", category, ""));
				}
			} catch (IOException e)
			{

				Log.e("YourApp", "GetPlaces : doInBackground HISTORY", e);

			} catch (JSONException e)
			{

				Log.e("YourApp", "GetPlaces : doInBackground HISTORY", e);

			}

			return predictionsArr;

		}

		//then our post

		@Override
		protected void onPostExecute(ArrayList<Brand> result)
		{
			if(result.size()>0){
				localBrands.addAll(result);
				localBrandsViewAdapter.notifyDataSetChanged();
				setListViewHeightBasedOnChildren(listViewLocalBrands,600);
				headerTitle.setVisibility(View.VISIBLE);
				progressBarLocalBrands.setVisibility(View.GONE);
			}
			progressBarLocalBrands.setVisibility(View.GONE);
			if(localBrands.size()==0){
				headerTitle.setVisibility(View.GONE);
			}
			if(brands.size()==0 && localBrands.size()==0){
				nothingFoundLinearLayout.setVisibility(View.VISIBLE);
			}else{
				nothingFoundLinearLayout.setVisibility(View.GONE);
			}
		}
	}
	/**** Method for Setting the Height of the ListView dynamically.
	 **** Hack to fix the issue of not showing all the items of the ListView
	 **** when placed inside a ScrollView  ****/
	public void setListViewHeightBasedOnChildren(ListView listView,int threshold) {
		//ListAdapter listAdapter = listView.getAdapter();
		if (localBrandsViewAdapter == null)
			return;

		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
		int totalHeight = 0;
		View view = null;
		for (int i = 0; i < localBrandsViewAdapter.getCount(); i++) {
			view = localBrandsViewAdapter.getView(i, view, listView);
			if (i == 0)
				view.setLayoutParams(new LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

			view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += view.getMeasuredHeight();
		}
		LayoutParams params = listView.getLayoutParams();
		//Threshold is used as it was not calculating height of list returned by google places correctly. 
		params.height = threshold + totalHeight + (listView.getDividerHeight() * (localBrandsViewAdapter.getCount() +1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}
}
