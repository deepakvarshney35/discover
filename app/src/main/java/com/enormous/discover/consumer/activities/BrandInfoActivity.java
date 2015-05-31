package com.enormous.discover.consumer.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.discover.consumer.DiscoverApp;
import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.asynctasks.DownloadAndSaveDocumentTask;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.models.Brand;
import com.enormous.discover.consumer.models.Item;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * Activity - BrandInfoActivity
 * - Shows the information about the Brand and the Items which Discover Publisher have listed.
 * - Brand Name, Brand Address , Brand About, brand Contact Details etc.
 * 
 * Items will be shown in a list. Items are queried from "Item" class in Parse. 
 * Items are saved in Parse from Discover Pub. app.(AddItemFragment) by the publisher.
 * 
 * If there is only 1 Item thn it will be selected directly.
 * 
 * */
public class BrandInfoActivity extends FragmentActivity{

	private Brand selectedBrand;
	private ListView brandInfoListView;
	private BrandItemsListViewAdapter adapter;
	private ArrayList<Item> items;
	private LayoutInflater inflater;
	private ActionBar actionBar;
	private Drawable actionBarDrawable;
	private FrameLayout itemsListViewTitleFrameLayout;
	private LinearLayout itemProgressLinearLayout;
	public boolean itemOne = false;

	//vars required for actionBar fading
	private TextView actionBarTitle;
	private ImageView brandCoverImageView;
	private int yScroll = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_brand_info);
		findViews();
		
		itemOne = true;
		
		//get selected brand from intent
		selectedBrand = getIntent().getParcelableExtra("selectedBrand");

		//Check if Shop is Local Shop retreived from google.
		if(selectedBrand.getObjectId().equals("GOOGLE")){
			Toast.makeText(BrandInfoActivity.this, "Publisher not on Discover yet.", Toast.LENGTH_LONG).show();
		}
		
		//set actionBar properties
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBarDrawable = new ColorDrawable(getResources().getColor(R.color.indigo500));
		actionBarDrawable.setAlpha(0);
		actionBar.setBackgroundDrawable(actionBarDrawable);
		actionBar.setTitle(selectedBrand.getName());

		//add header and footer to brandInfoListView
		View header = inflater.inflate(R.layout.listview_brand_info_header, null);
		itemsListViewTitleFrameLayout = (FrameLayout) header.findViewById(R.id.itemsListViewTitleLinearLayout);
		TextView brandAboutTextView = (TextView) header.findViewById(R.id.brandAboutTextView);
		brandAboutTextView.setText(selectedBrand.getAbout());
		brandInfoListView.addHeaderView(header);
		setUpHeader(header);

		View footer = inflater.inflate(R.layout.listview_brand_info_footer, null);
		itemProgressLinearLayout = (LinearLayout) footer.findViewById(R.id.itemProgressLinearLayout);
		brandInfoListView.addFooterView(footer);

		//set brandItemsListView adapter
		items = new ArrayList<Item>();
		adapter = new BrandItemsListViewAdapter();
		brandInfoListView.setAdapter(adapter);

		//fetch brand items
		refreshItems();

		//set up actionBar fading
		int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		if (actionBarTitleId > 0) {
			actionBarTitle = (TextView) findViewById(actionBarTitleId);
		}
		if (actionBarTitle != null) {
			actionBarTitle.setTextColor(Color.argb(0, 255, 255, 255));
		}

		//Scrolling effect, changes action bar color
		brandInfoListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (brandInfoListView.getChildAt(firstVisibleItem) != null) {
					if (firstVisibleItem == 0) {
						yScroll = -brandInfoListView.getChildAt(0).getTop();
						brandCoverImageView.setTranslationY((float) (yScroll/2));
						int headerHeight = brandCoverImageView.getHeight() - actionBar.getHeight();
						float ratio = 0;
						if (yScroll > 0) {
							ratio = (float) Math.min(yScroll, headerHeight) / (float) headerHeight;
						}
						int newAlpha = (int) (ratio * 255);
						actionBarDrawable.setAlpha(newAlpha);
						if (actionBarTitle != null) {
							actionBarTitle.setTextColor(Color.argb(newAlpha, 255, 255, 255));
						}
					}
				}
			}
		});
	}

	private void findViews() {
		brandInfoListView = (ListView) findViewById(R.id.brandInfoListView);
		inflater = getLayoutInflater();
		actionBar = getActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_brand_info, menu);
		return true;
	}

	private void refreshItems() {
		if (Utils.isConnectedToInternet(this)) {
			GetBrandItemsTask getBrandsItemsTask = new GetBrandItemsTask();
			getBrandsItemsTask.execute();
		}
	}

	private void setUpHeader(View header) {
		brandCoverImageView = (ImageView) header.findViewById(R.id.brandCoverImageView);
		ImageView brandCategoryImageView= (ImageView) header.findViewById(R.id.brandLogoImageView);
		TextView brandNameTextView = (TextView) header.findViewById(R.id.productNameTextView);
		TextView brandPhoneTextView = (TextView) header.findViewById(R.id.brandPhoneTextView);

		ImageLoader.getInstance().displayImage(this, selectedBrand.getCoverPictureUrl(), brandCoverImageView, false, 640, 640, 0);
		ImageLoader.getInstance().displayImage(this, getResources().getIdentifier(selectedBrand.getCategoryName(), "drawable", BrandInfoActivity.this.getPackageName()) + "", brandCategoryImageView, true, 300, 300, 0);
		brandNameTextView.setText(selectedBrand.getName());
		brandPhoneTextView.setText(selectedBrand.getLocation());
	}

	/**
	 * Getting the list of Items from Parse in Background.
	 * 
	 * */
	private class GetBrandItemsTask extends AsyncTask<Void, Void, ArrayList<Item>> {

		ArrayList<Item> items;
		TextView progressTextView;
		ProgressBar progressBar;

		public GetBrandItemsTask() {
			items = new ArrayList<Item>();
			progressTextView = (TextView) itemProgressLinearLayout.findViewById(R.id.progressTextView);
			progressBar = (ProgressBar) itemProgressLinearLayout.findViewById(R.id.progressBar);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressTextView.setText("Loading items...");
			progressBar.setVisibility(View.VISIBLE);
			itemsListViewTitleFrameLayout.setVisibility(View.GONE);
			itemProgressLinearLayout.setVisibility(View.VISIBLE);
			BrandInfoActivity.this.items.clear();
			adapter.notifyDataSetChanged();
		}

		@Override
		protected ArrayList<Item> doInBackground(Void... params) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
			query.whereEqualTo("brandObjectId", selectedBrand.getObjectId());
			try {
				List<ParseObject> results = query.find();
				for (ParseObject result : results) {
					items.add(new Item(result.getString("itemName"), result.getString("itemType"), result.getString("itemDescription"), result.getParseFile("itemDocument"), result.getString("itemURL") , result.getString("itemMap")));
				}
			}
			catch (Exception e) {
				Log.e("TEST", "Error retrieving brand items: " + e.getMessage());
			}
			return items;
		}

		@Override
		protected void onPostExecute(ArrayList<Item> result) {
			super.onPostExecute(result);
			TextView progressTextView = (TextView) itemProgressLinearLayout.findViewById(R.id.progressTextView);
			ProgressBar progressBar = (ProgressBar) itemProgressLinearLayout.findViewById(R.id.progressBar);

			if (result.size() == 0) {
				progressTextView.setText("There doesn't seem to be anything here...");
				progressBar.setVisibility(View.GONE);
				itemProgressLinearLayout.setVisibility(View.VISIBLE);
			}
			else {
				itemsListViewTitleFrameLayout.setVisibility(View.VISIBLE);
				itemProgressLinearLayout.setVisibility(View.GONE);
			}
			BrandInfoActivity.this.items.clear();
			BrandInfoActivity.this.items.addAll(result);
			adapter.notifyDataSetChanged();
		}

	}

	/**
	 * Item List Adapter. 
	 * 
	 * */
	private class BrandItemsListViewAdapter extends BaseAdapter implements OnItemClickListener {

		public BrandItemsListViewAdapter() {
			brandInfoListView.setOnItemClickListener(this);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int pos) {
			return items.get(pos);
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
				row = inflater.inflate(R.layout.listview_brand_info_item, parent, false);
				holder = new ViewHolder(row);
				row.setTag(holder);
			}
			else {
				holder = (ViewHolder) row.getTag();
			}

			//set card background for last item in list
			Resources res = getResources();
			if (pos == items.size()-1) {
				holder.itemContainer.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_bottom));
			}
			else {
				holder.itemContainer.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey));
			}
			holder.brandItemNameTextView.setText(items.get(pos).getName());
			ImageLoader.getInstance().displayImage(BrandInfoActivity.this, getResources().getIdentifier(items.get(pos).getType().toLowerCase(), "drawable", getPackageName())+"", holder.brandItemIconImageView, true, 300, 300, 0);
			Log.d("test", items.size()+"    d");
			if(items.size()==1 && itemOne ){
				Log.d("test", items.size()+"    e");
				itemOne = false;
				if (items.get(0).getType().toLowerCase().equals("products")) {
					//to to productCatalogue activity
					Intent goToProductCatalogue = new Intent(BrandInfoActivity.this, ProductCatalogueActivity.class);
					goToProductCatalogue.putExtra("brandObjectId", selectedBrand.getObjectId());
                    goToProductCatalogue.putExtra("selectedBrand", selectedBrand);
					startActivity(goToProductCatalogue);
					overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
				}
				else if (items.get(0).getType().toLowerCase().equals("url")) {
					//open url in browser
					Intent goToBrowser = new Intent();
					goToBrowser.setAction(Intent.ACTION_VIEW);
					goToBrowser.setData(Uri.parse(items.get(0).getItemUrl()));
					startActivity(goToBrowser);
				}
				else if (items.get(0).getType().toLowerCase().equals("map")){
					 try
					    {
					        ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
					        String uri = String.format(Locale.ENGLISH, "geo:0,0?q="+items.get(0).getAddress());
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
							startActivity(intent);
					    } 
					    catch(PackageManager.NameNotFoundException e)
					    {
							  	AlertDialog.Builder build = new AlertDialog.Builder(BrandInfoActivity.this);
							  	build.setTitle("Google Maps Not Installed!");
							  	AlertDialog alert = build.create();
							  	alert.show();
					    }
					
				} 
				else if(items.get(0).getType().toLowerCase().equals("offer")){
					AlertDialog.Builder builder = new AlertDialog.Builder(BrandInfoActivity.this);
					View customDialog = getLayoutInflater().inflate(R.layout.dialog_share_facebook, null);
					Button shareOnFbButton = (Button) customDialog.findViewById(R.id.shareOnFbButton);
					final TextView offerDescriptionText= (TextView) customDialog.findViewById(R.id.offerDescriptionText);
					final TextView offerNameText= (TextView) customDialog.findViewById(R.id.offerNameText);
					builder.setView(customDialog);
					offerDescriptionText.setText(items.get(0).getDescription());
					offerNameText.setText(items.get(0).getName());
					final AlertDialog dialog = builder.create();
					shareOnFbButton.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//go to Brand Share activity
							Intent goToBrandInfo1 = new Intent(BrandInfoActivity.this, ShareBrandActivity.class);
							goToBrandInfo1.putExtra("selectedBrand", selectedBrand);
							goToBrandInfo1.putExtra("selectedItemName", items.get(0).getName());
							startActivity(goToBrandInfo1);
							overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);	
							dialog.dismiss();
						}
					});

					dialog.show();
				} else
				{
                    DownloadAndSaveDocumentTask downloadAndSaveDocumentTask = new DownloadAndSaveDocumentTask(BrandInfoActivity.this);
                    ParseFile parseFile = items.get(0).getDocument();
                    //check if file already exists in cache
                    if(parseFile != null){
                    File doc = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/documents/" + parseFile.getName());
                    if (doc.exists()) {
                        downloadAndSaveDocumentTask.showDocument(BrandInfoActivity.this, doc);
                    }
                    else {
                        downloadAndSaveDocumentTask.execute(parseFile);
                    }
                    }
                }
			}
			return row;
		}

		/**
		 * Doing various function based on the type of Item.
		 * 
		 * Type of Item is saved in publisher App.
		 * */
		@Override
		public void onItemClick(AdapterView<?> adv, View v,final int pos,
				long id) {
			if (pos != 0 && pos != brandInfoListView.getCount() - 1) {
				if (items.get(pos-1).getType().toLowerCase().equals("products")) {
					//to to productCatalogue activity
					Intent goToProductCatalogue = new Intent(BrandInfoActivity.this, ProductCatalogueActivity.class);
					goToProductCatalogue.putExtra("brandObjectId", selectedBrand.getObjectId());
                    goToProductCatalogue.putExtra("selectedBrand", selectedBrand);
					startActivity(goToProductCatalogue);
					overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
				}
				else if (items.get(pos-1).getType().toLowerCase().equals("url")) {
					//open url in browser
					Intent goToBrowser = new Intent();
					goToBrowser.setAction(Intent.ACTION_VIEW);
					goToBrowser.setData(Uri.parse(items.get(pos-1).getItemUrl()));
					startActivity(goToBrowser);
				}
				else if (items.get(pos-1).getType().toLowerCase().equals("map")){
					 try
					    {
					        ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
					        String uri = String.format(Locale.ENGLISH, "geo:0,0?q="+items.get(pos-1).getAddress());
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
							startActivity(intent);
					    } 
					    catch(PackageManager.NameNotFoundException e)
					    {
							  	AlertDialog.Builder build = new AlertDialog.Builder(BrandInfoActivity.this);
							  	build.setTitle("Google Maps Not Installed!");
							  	AlertDialog alert = build.create();
							  	alert.show();
					    }
					
				} 
				else if(items.get(pos-1).getType().toLowerCase().equals("offer")){
					AlertDialog.Builder builder = new AlertDialog.Builder(BrandInfoActivity.this);
					View customDialog = getLayoutInflater().inflate(R.layout.dialog_share_facebook, null);
					Button shareOnFbButton = (Button) customDialog.findViewById(R.id.shareOnFbButton);
					final TextView offerDescriptionText= (TextView) customDialog.findViewById(R.id.offerDescriptionText);
					final TextView offerNameText= (TextView) customDialog.findViewById(R.id.offerNameText);
					builder.setView(customDialog);
					offerDescriptionText.setText(items.get(pos-1).getDescription());
					offerNameText.setText(items.get(pos-1).getName());
					final AlertDialog dialog = builder.create();
					shareOnFbButton.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//go to Brand Share activity
							Intent goToBrandInfo1 = new Intent(BrandInfoActivity.this, ShareBrandActivity.class);
							goToBrandInfo1.putExtra("selectedBrand", selectedBrand);
							goToBrandInfo1.putExtra("selectedItemName", items.get(pos-1).getName());
							startActivity(goToBrandInfo1);
							overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);	
							dialog.dismiss();

                            //Toast.makeText(BrandInfoActivity.this,""+items.get(pos-1).getName(),Toast.LENGTH_SHORT).show();
                            //sendOfferShareChat(items.get(pos-1).getName());
						}
					});

					dialog.show();
				} else
				{
                    DownloadAndSaveDocumentTask downloadAndSaveDocumentTask = new DownloadAndSaveDocumentTask(BrandInfoActivity.this);
                    ParseFile parseFile = items.get(pos-1).getDocument();
                    //check if file already exists in cache
                    if(parseFile != null){
                    File doc = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/documents/" + parseFile.getName());
                    if (doc.exists()) {
                        downloadAndSaveDocumentTask.showDocument(BrandInfoActivity.this, doc);
                    }
                    else {
                        downloadAndSaveDocumentTask.execute(parseFile);
                    }
                    }
                }
			}
		}
	}

    private static class ViewHolder {
		LinearLayout itemContainer;
		TextView brandItemNameTextView;
		ImageView brandItemIconImageView;

		public ViewHolder(View row) {
			itemContainer = (LinearLayout) row.findViewById(R.id.itemContainer);
			brandItemNameTextView = (TextView) row.findViewById(R.id.brandItemNameTextView);
			brandItemIconImageView = (ImageView) row.findViewById(R.id.brandItemIconImageView);
		}
	}

	/**
	 * Menu options on the action Bar.
	 * 
	 * Call - Opens intent to dialer
	 * Email - Opens up email app with brands email.
	 * Refresh - refreshes page
	 * Chat - Opens up chat Activity
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ic_action_call:
			Intent goToDialer = new Intent();
			goToDialer.setAction(Intent.ACTION_DIAL);
			goToDialer.setData(Uri.parse("tel:" + selectedBrand.getPhone()));
			startActivity(Intent.createChooser(goToDialer, "Call with..."));
			break;
		case R.id.ic_action_email:
			Intent goToEmail = new Intent();
			goToEmail.setAction(Intent.ACTION_SENDTO);
			goToEmail.setData(Uri.parse("mailto:" + selectedBrand.getEmail()));
			startActivity(Intent.createChooser(goToEmail, "Email with..."));
			break;
		case R.id.ic_action_refresh:
			refreshItems();
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.ic_action_chat:
			if(!selectedBrand.getUUID().equals("GOOGLE")){
				Intent goToProductCatalogue = new Intent(BrandInfoActivity.this, ChatMessageActivity.class);
				goToProductCatalogue.putExtra("merchantUserId", selectedBrand.getObjectId());
				goToProductCatalogue.putExtra("merchantName", selectedBrand.getName());
				goToProductCatalogue.putExtra("merchantPic", selectedBrand.getCoverPictureUrl());
				goToProductCatalogue.putExtra("merchantPhone", selectedBrand.getPhone());
				goToProductCatalogue.putExtra("merchantEmail", selectedBrand.getEmail());
				startActivity(goToProductCatalogue);
			} else{
				Toast.makeText(BrandInfoActivity.this, "Publisher not on Discover yet.", Toast.LENGTH_LONG).show();
			}
			break;
		}
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//itemOne = false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
	}

}
