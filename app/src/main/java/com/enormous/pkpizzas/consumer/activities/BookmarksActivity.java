package com.enormous.pkpizzas.consumer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.common.SlidingTabStrip;
import com.enormous.pkpizzas.consumer.fragments.BrandBookmarksFragment;
import com.enormous.pkpizzas.consumer.fragments.ProductBookmarksFragment;

import java.util.ArrayList;

/**
 * Created by Manas on 8/11/2014.
 * 
 * This Activity shows Bookmarks, We have two types of bookmarks 
 * 1. Product Bookmarks
 * 2. Brand/Publisher/Merchant/Shop Bookmarks
 * 
 * View Pager is used to separate both the fragments. The two fragments used are 
 *  1. BrandBookmarksFragment
 *  2. ProductBookmarksFragment
 */
public class BookmarksActivity extends Activity {

    private ActionBar actionBar;
    private ViewPager pager;
    private SlidingTabStrip slidingTabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        findViews();

        //set actionBar properties
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        //set pager adapter and tab strip
        pager.setAdapter(new BookmarksPagerAdapter(getFragmentManager()));
        slidingTabStrip.showDividers(true);
        slidingTabStrip.shouldExpand(true);
        slidingTabStrip.setViewPager(pager);
    }

    private void findViews() {
        actionBar = getActionBar();
        pager = (ViewPager) findViewById(R.id.pager);
        slidingTabStrip = (SlidingTabStrip) findViewById(R.id.slidingTabStrip);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
    }

    private static class BookmarksPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments;
        public BookmarksPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<Fragment>();
            fragments.add(new BrandBookmarksFragment());
            fragments.add(new ProductBookmarksFragment());
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Brands";
                case 1:
                    return "Products";
            }
            return null;
        }
    }
}
