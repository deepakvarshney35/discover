package com.enormous.discover.consumer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.fragments.ProductCatalogueCategoryFragment;
import com.enormous.discover.consumer.models.Brand;
import com.enormous.discover.consumer.models.Product;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Opened by clicking on Item in BrandInfoActivity.
 * If the Item type is Product then this page will be opened.
 * 
 * This will be a catalogue of the products. Which is a view pager with pages for different type of products.
 * 
 * All the products will be listed here. Also we can sort them accordingly using the menu option on the action bar.
 * 
 * **/
public class ProductCatalogueActivity extends Activity {

    public static Brand selectedBrand;
	private final String TAG = "ProductCatalogueActivity";
	private ImageView bottomCircleImageView;
	private ViewPager pager;
	private ActionBar actionBar;
	private LinearLayout progressLinearLayout;
	private LinearLayout noInternetLinearLayout;
	private String brandObjectId;
	public ArrayList<Product> products = new ArrayList<Product>();
	CataloguePagerAdapter adapter;

	//default order: Newest Arrivals
	public String actionBarSubTitle = "Newest Arrivals";
	public String sortKey = "createdAt";
	public String sortOrder = "descending";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_catalogue);
		findViews();

		//set actionBar properties
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//get brandObjectId from intent
		brandObjectId = getIntent().getStringExtra("brandObjectId");
        selectedBrand = getIntent().getParcelableExtra("selectedBrand");

		//get products belonging to the selected brand
		refreshProducts(actionBarSubTitle, sortKey, sortOrder);

		//set click listeners
		View.OnClickListener clickListener = new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.bottomCircleImageView:
					//Opens up cart Activity.
					Intent goToCart = new Intent(ProductCatalogueActivity.this, CartActivity.class);
					startActivity(goToCart);
					overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
					break;
					//refreshes the products for updats.
				case R.id.noInternetLinearLayout:
					refreshProducts(actionBarSubTitle, sortKey, sortOrder);
					break;
				}
			}
		};
		bottomCircleImageView.setOnClickListener(clickListener);
		noInternetLinearLayout.setOnClickListener(clickListener);
	}

	private void refreshProducts(String actionBarSubTitle, String sortKey, String sortOrder) {
		noInternetLinearLayout.setVisibility(View.GONE);
		if (Utils.isConnectedToInternet(this)) {
			this.actionBarSubTitle = actionBarSubTitle;
			this.sortKey = sortKey;
			this.sortOrder = sortOrder;
			if (adapter == null) {
				new GetProductsTask().execute();
			}
			else {
				for (Fragment fragment : adapter.fragments) {
					((ProductCatalogueCategoryFragment) fragment).refreshProducts();
				}
			}
		}
		else {
			noInternetLinearLayout.setVisibility(View.VISIBLE);
		}
	}

	private void findViews() {
		bottomCircleImageView = (ImageView) findViewById(R.id.bottomCircleImageView);
		pager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		noInternetLinearLayout = (LinearLayout) findViewById(R.id.noInternetLinearLayout);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_product_catalogue, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.action_newest_arrivals:
			refreshProducts(item.getTitle().toString(), "createdAt", "descending");
			break;
		case R.id.action_price_low_to_high:
			refreshProducts(item.getTitle().toString(), "productCost", "ascending");
			break;
		case R.id.action_price_high_to_low:
			refreshProducts(item.getTitle().toString(), "productCost", "descending");
			break;
		}
		return true;
	}

	private class GetProductsTask extends AsyncTask<Void, Void, ArrayList<Product>> {

		ArrayList<Product> products;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			actionBar.setSubtitle(actionBarSubTitle);
			products = new ArrayList<Product>();
			progressLinearLayout.setVisibility(View.VISIBLE);
		}

		@Override
		protected ArrayList<Product> doInBackground(Void... params) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Product");
			List<ParseObject> objects;
			query.whereEqualTo("brandObjectId", brandObjectId);
			if (sortOrder.equals("ascending")) {
				query.addAscendingOrder(sortKey);
			}
			else {
				query.addDescendingOrder(sortKey);
			}
			try {
				objects = query.find();
				for (ParseObject object : objects) {
					products.add(new Product(object.getString("brandObjectId"),object.getObjectId(), object.getString("brandName"), object.getString("productName"), object.getString("productType"), object.getParseFile("productPicture").getUrl(), object.getInt("productCost"), object.getInt("productShippingCost"), object.getInt("productTax"), object.getString("productDescription"), object.getInt("productStock"), (ArrayList<String>) object.get("productOptions"),(ArrayList<Integer>) object.get("productOptionsCost")));
				}
				return products;
			}
			catch (Exception e) {
				//				Log.e("TEST", e.getMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<Product> result) {
			super.onPostExecute(result);
			if (result != null) {
				if (result.size() > 0) {
					ProductCatalogueActivity.this.products.clear();
					ProductCatalogueActivity.this.products.addAll(result);
					progressLinearLayout.setVisibility(View.GONE);
					pager.setVisibility(View.VISIBLE);
					//set up pager
					adapter = new CataloguePagerAdapter(getFragmentManager());
					pager.setAdapter(adapter);
				}
			}
		}

	}

	private class CataloguePagerAdapter extends android.support.v13.app.FragmentPagerAdapter {

		ArrayList<String> categories;
		ArrayList<Fragment> fragments;

		public CataloguePagerAdapter(android.app.FragmentManager fm) {
			super(fm);
			//get a list of all unique categories
			categories = new ArrayList<String>();
			for (Product product : products) {
				String category = product.getCategory();
				if (!categories.contains(category)) {
					categories.add(category);
				}
			}

			fragments = new ArrayList<Fragment>();
			for (String category : categories) {
				fragments.add(ProductCatalogueCategoryFragment.newInstance(category, brandObjectId));
			}
		}

		@Override
		public Fragment getItem(int pos) {
			return fragments.get(pos);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return categories.get(position);
		}

	}
}
