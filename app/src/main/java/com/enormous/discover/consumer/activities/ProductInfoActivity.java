package com.enormous.discover.consumer.activities;

import android.app.ActionBar;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.common.NotifyingScrollView;
import com.enormous.discover.consumer.fragments.ProductInfoFragment;
import com.enormous.discover.consumer.models.Product;

import java.util.ArrayList;

/**
 * ProductInfoActivity
 * When user opens product from Product catalogue. THis Activity will open up showing all details about the Product.
 * Products related information is stored in Parse Class "Products". 
 * 
 * Extra photos of the products are saved in class "ProductPhotos"
 * 
 * ProductInfoFragment is the fragment involved showing the details of the products. 
 * 
 * */
public class ProductInfoActivity extends FragmentActivity implements OnPageChangeListener {

    private ActionBar actionBar;
	public Drawable backgroundDrawable;
    public TextView actionBarTitle;
    private NotifyingScrollView scrollView;
    private ViewPager pager;
	public ArrayList<Product> products;
	int selectedPos;
    private ArrayList<android.app.Fragment> fragments;
    public static boolean activityVisible;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_product_info);
		findViews();
		
		//set actionBar properties
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		backgroundDrawable = new ColorDrawable(getResources().getColor(R.color.indigo500));
		backgroundDrawable.setAlpha(0);
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            actionBarTitle = (TextView) findViewById(actionBarTitleId);
        }
		actionBar.setBackgroundDrawable(backgroundDrawable);

		//get list of products and the selected position from intent
		Bundle bundle = getIntent().getExtras();
		products = bundle.getParcelableArrayList("products");
		selectedPos = bundle.getInt("selectedPos");
		Log.d("producttest1", ""+products.get(selectedPos).getBrandName()+products.get(selectedPos).getOptions()+products.get(selectedPos).getOptionsCost());
		//set up pager 
		ProductFragmentPagerAdapter adapter = new ProductFragmentPagerAdapter(getFragmentManager());
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(this);
		pager.setCurrentItem(selectedPos);

		//set actionBar title as onPageSelected isn't called when the activity first starts
		actionBar.setTitle(products.get(selectedPos).getName());
		
	}

    private void findViews() {
		actionBar = getActionBar();
		scrollView = (NotifyingScrollView) findViewById(R.id.scrollView);
		pager = (ViewPager) findViewById(R.id.pager);
	}
	
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	activityVisible = true;
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	activityVisible = false;
    }
    
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}
	
	/**
	 * Adds swipe functionality to view different products from the same category.
	 * */
	private class ProductFragmentPagerAdapter extends android.support.v13.app.FragmentPagerAdapter {
		
		public ProductFragmentPagerAdapter(android.app.FragmentManager fm) {
			super(fm);
			fragments = new ArrayList<android.app.Fragment>();
			for (int i=0;i<products.size();i++) {
				fragments.add(ProductInfoFragment.newInstance(i));
			}
		}		

		@Override
		public android.app.Fragment getItem(int pos) {
			return fragments.get(pos);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
		
	}
	
	//-------------------------onPageChangeListener methods-------------------------------

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int pos) {
		actionBar.setTitle(products.get(pos).getName());
		//reset scroll position everytime the page is changed 
		if (fragments != null) {
			NotifyingScrollView scrollView = ((ProductInfoFragment) fragments.get(pos)).scrollView;
			if (scrollView != null) {
				scrollView.scrollTo(0, 0);
				backgroundDrawable.setAlpha(0);
                if (actionBarTitle != null) {
                    actionBarTitle.setTextColor(Color.argb(0, 255, 255, 255));
                }
			}
		}
	}
	
}
