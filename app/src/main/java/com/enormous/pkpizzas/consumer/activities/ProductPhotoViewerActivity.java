package com.enormous.pkpizzas.consumer.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.fragments.ProductPhotoViewerFragment;

import java.util.ArrayList;

/**
 * Created by Manas on 8/21/2014.
 * 
 * Photo Viewer Activity
 * -array of URL's is supplied with the intent extras.
 * -Has zoom option. 
 * 
 */
public class ProductPhotoViewerActivity extends Activity {

    private ViewPager pager;
    private TextView imagePositionIndicatorTextView;
    private ArrayList<String> urls;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_photo_viewer);
        findViews();

        //get list of image urls from intent
        urls = getIntent().getStringArrayListExtra("imageUrls");

        //get position of selected image
        pos = getIntent().getIntExtra("imagePos", 0);

        //set up pager adapter
        pager.setAdapter(new ImagePagerAdapter(getFragmentManager()));
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                imagePositionIndicatorTextView.setText(i + 1 + " of " + urls.size());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        pager.setCurrentItem(pos);
        pager.setPageTransformer(true, new DepthPageTransformer());
        imagePositionIndicatorTextView.setText(pager.getCurrentItem()+1 + " of " + urls.size());
    }

    private void findViews() {
        pager = (ViewPager) findViewById(R.id.pager);
        imagePositionIndicatorTextView = (TextView) findViewById(R.id.imagePositionIndicatorTextView);
    }

    private class ImagePagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments;

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<Fragment>();
            for (String url : urls) {
                fragments.add(ProductPhotoViewerFragment.newInstance(url));
            }
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
