package com.enormous.discover.consumer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.adapters.BrandsListViewAdapter;
import com.enormous.discover.consumer.asynctasks.GetBrandsTask;
import com.enormous.discover.consumer.common.BrandsEndlessScrollListener;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.models.Brand;

import java.util.ArrayList;

/**
 * Created by Manas on 8/11/2014.
 */
public class BrandBookmarksFragment extends Fragment {

    private final String TAG = "BrandBookmarksFragment";
    private ListView listView;
    private LinearLayout progressLinearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout nothingFoundLinearLayout;
    private LinearLayout noInternetLinearLayout;
    private ArrayList<String> brandBookmarks;
    private BrandsListViewAdapter adapter;
    private ArrayList<Brand> brands;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        findViews(view);
        setHasOptionsMenu(true);

        //add ability to pull to refresh
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshBrands();
            }
        });

        //set up brands listview
        brandBookmarks = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("brandBookmarks");
        brands = new ArrayList<Brand>();
        adapter = new BrandsListViewAdapter(getActivity(), brands);
        listView.setAdapter(adapter);
        if (brandBookmarks == null) {
            brandBookmarks = new ArrayList<String>();
            nothingFoundLinearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
        else if (brandBookmarks.size() == 0) {
            nothingFoundLinearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
        else {
            refreshBrands();
        }

        noInternetLinearLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                refreshBrands();
            }
        });

        return view;
    }

    private void findViews(View view) {
        listView = (ListView) view.findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        progressLinearLayout = (LinearLayout) view.findViewById(R.id.progressLinearLayout);
        nothingFoundLinearLayout = (LinearLayout) view.findViewById(R.id.nothingFoundLinearLayout);
        noInternetLinearLayout = (LinearLayout) view.findViewById(R.id.noInternetLinearLayout);
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_overflow_refresh, menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.ic_action_refresh:
                Log.d(TAG, "refresh clicked");
                refreshBrands();
                break;
        }
        return true;
    }

    private void refreshBrands() {
        noInternetLinearLayout.setVisibility(View.GONE);
        if (Utils.isConnectedToInternet(getActivity())) {
            //only pass progressLinearLayout if the listview doesn't contain any items/brands
            if (brands.size() == 0) {
                GetBrandsTask getBrandsTask = new GetBrandsTask(brandBookmarks, null, brands, adapter, swipeRefreshLayout, progressLinearLayout, nothingFoundLinearLayout, listView, null);
                getBrandsTask.execute();
            }
            else {
                GetBrandsTask getBrandsTask = new GetBrandsTask(brandBookmarks, null, brands, adapter, swipeRefreshLayout, null, nothingFoundLinearLayout, listView, null);
                getBrandsTask.execute();
            }
        }
        else {
            swipeRefreshLayout.setRefreshing(false);
            listView.setVisibility(View.INVISIBLE);
            noInternetLinearLayout.setVisibility(View.VISIBLE);
            nothingFoundLinearLayout.setVisibility(View.GONE);
        }
    }

    private ArrayList<String> getUUIDSToLoad(ArrayList<String> userHistory) {
        //retrieve only the required UUIDS from userHistory
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

}
