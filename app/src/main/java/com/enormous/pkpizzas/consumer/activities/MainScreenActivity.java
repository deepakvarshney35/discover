package com.enormous.pkpizzas.consumer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.common.SlidingTabsMainScreen;
import com.enormous.pkpizzas.consumer.fragments.MainScreenArchiveFragment;
import com.enormous.pkpizzas.consumer.fragments.MainScreenChatsFragment;
import com.enormous.pkpizzas.consumer.fragments.MainScreenHomeFragment;
import com.enormous.pkpizzas.consumer.fragments.MissingUserInfoDialogFragment;
import com.enormous.pkpizzas.consumer.services.BeaconRangingService;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * MainScreenActivity
 * View Pager with 3 Fragments 
 * 1. Archive Fragment
 * 2. Home Fragment
 * 3. Profile Fragment
 * 
 * Home Fragment Shows Detected Beacons which are assigned to different Brands. Once Beacons are in Bluetooth range,
 * Brands will be shown on Home Fragment. If there are more then 1 , list of all Brands will be shown.
 * Also Nearby Shops within 50kms will be shown on the Home Fragment. List of shops will be downloaded from Parse. 
 * And also from Google Places Api will give remaining shops. The one from Parse or one created using Discover Publisher app 
 * will be shown first. and google ones which are read only will be shown later. Brands shown here are maintained and updated using the
 * pkpizzas Publisher app.
 * 
 * Archive Fragment will show list of all shops which were found earlier using bluetooth based on their categories.
 * 
 * Profile Fragment has all details about the user/consumer. Option for Invite friends and Notification preferences will be shown here.
 * Invite friend button will open up Invite Friend Activity. and Preferences button will open up ChooseCategoriesActivity.
 * 
 * Also Main Screen Activity has Frame layout with Buttons above the View Pager on the bottom right.On clicking this button
 * we have either Search Option or History option depending upon which page is opened.
 * 
 * If we have Home Fragment open then clicking on this button opens up History Activity.
 * 
 * If we have ArchiveFragment open then clicking on this floating button will take us to Search Activity.
 * 
 * */
public class MainScreenActivity extends FragmentActivity implements OnPageChangeListener, OnClickListener {

	private SlidingTabsMainScreen slidingTabStrip;
	private ViewPager pager;
	private int currentPage = 1;
	public static ImageView bottomCircleImageView;
	public static ParseUser CURRENT_USER;
	public static NumberFormat CURRENCY_FORMATTER;
    public static Animation animIn,animOut;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_main_screen);
		findViews();

        animIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        animOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);

		CURRENT_USER = ParseUser.getCurrentUser();
		CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

		//prompt the user to fill in missing information
		if (CURRENT_USER.getString("phoneNumber") == null || CURRENT_USER.getString("firstName") == null) {
			new MissingUserInfoDialogFragment().show(getSupportFragmentManager(), "missingInfoDialog");
		}
		else {
			setUpPagerAdpter();
			ParseInstallation installation = ParseInstallation.getCurrentInstallation();
			installation.put("deviceId", ParseUser.getCurrentUser().getObjectId());
			installation.saveInBackground();
		}
		ParsePush.subscribeInBackground("Consumer");
		//set onClick listeners
		bottomCircleImageView.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		BeaconRangingService.isMainScreenActivityVisible = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		BeaconRangingService.isMainScreenActivityVisible = false;
	}

	public void setUpPagerAdpter() {
		CustomPagerAdapter adapter = new CustomPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		pager.setCurrentItem(1);
		pager.setOffscreenPageLimit(2);
		slidingTabStrip.setDelegatePageListener(this);
		slidingTabStrip.setViewPager(pager);
	}

	private class CustomPagerAdapter extends FragmentPagerAdapter {

		ArrayList<Fragment> fragments;

		public CustomPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new ArrayList<Fragment>();
			fragments.add(new MainScreenArchiveFragment());
			fragments.add(new MainScreenHomeFragment());
			fragments.add(new MainScreenChatsFragment());
		}

		@Override
		public Fragment getItem(int pos) {
			return fragments.get(pos);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

	}

	private void findViews() {
		slidingTabStrip = (SlidingTabsMainScreen) findViewById(R.id.slidingTabStripMainScreen);
		pager = (ViewPager) findViewById(R.id.pager);
		bottomCircleImageView = (ImageView) findViewById(R.id.bottomCircleImageView);
	}


	//----------------------OnPageChangedListener Methods---------------------------

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int pos) {
		currentPage = pos;
		selectPage(currentPage);
	}

	private void selectPage(int pos) {
		switch(pos) {
		case 0:
			bottomCircleImageView.setVisibility(View.VISIBLE);
			bottomCircleImageView.setImageResource(R.drawable.ic_action_search);
			break;
		case 1:
			bottomCircleImageView.setVisibility(View.VISIBLE);
			bottomCircleImageView.setImageResource(R.drawable.ic_action_history);
			break;
		case 2:
			bottomCircleImageView.setVisibility(View.INVISIBLE);
			break;
		}
	}


	//-----------------------onClickListener method----------------------------------

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.searchButton:
			pager.setCurrentItem(0);
			break;
		case R.id.homeButton:
			pager.setCurrentItem(1);
			break;
		case R.id.profileButton:
			pager.setCurrentItem(2);
			break;
		case R.id.bottomCircleImageView:
			if (currentPage == 0) {
				Intent goToSearch = new Intent(this, SearchActivity.class);
				startActivity(goToSearch);
				overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
			}
			if (currentPage == 1) {
				Intent goToHistory = new Intent(this, HistoryActivity.class);
				startActivity(goToHistory);
				overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
			}
			break;
		}
	}

    //-----------------------Toggle Buttons----------------------------------
    public static void hideFAB(){
        if (bottomCircleImageView.getVisibility()==View.GONE);
        else {
            bottomCircleImageView.setVisibility(View.GONE);
            //bottomCircleImageView.startAnimation(animOut);
        }
    }
    public static void showFAB(){
        if (bottomCircleImageView.getVisibility()==View.VISIBLE);
        else {
            bottomCircleImageView.setVisibility(View.VISIBLE);
            //bottomCircleImageView.startAnimation(animIn);
        }
    }
}
