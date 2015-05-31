package com.enormous.discover.consumer.asynctasks;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.enormous.discover.consumer.adapters.BrandsListViewAdapter;
import com.enormous.discover.consumer.common.BrandsEndlessScrollListener;
import com.enormous.discover.consumer.models.Brand;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GetMoreBrandsTask extends AsyncTask<Void, Void, ArrayList<Brand>> {

    private ArrayList<String> UUIDS;
    private ArrayList<Brand> brands;
    private BrandsListViewAdapter brandsListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView brandsListView;
    private LinearLayout endlessScrollLinearLayout;
    private ArrayList<Brand> newBrands;
	
	public GetMoreBrandsTask(ArrayList<String> UUIDS, ArrayList<Brand> brands, BrandsListViewAdapter brandsListViewAdapter, SwipeRefreshLayout swipeRefreshLayout, ListView brandsListView, LinearLayout endlessScrollLinearLayout) {
		this.UUIDS = UUIDS;
		this.brands = brands;
		this.brandsListViewAdapter = brandsListViewAdapter;
		this.swipeRefreshLayout = swipeRefreshLayout;
		this.brandsListView = brandsListView;
		this.endlessScrollLinearLayout = endlessScrollLinearLayout;
		
		newBrands = new ArrayList<Brand>();
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//null check as some activities/fragments do not use swipeRefreshLayout
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setRefreshing(true);
		}
	
		endlessScrollLinearLayout.setVisibility(View.VISIBLE);
		
		BrandsEndlessScrollListener.IS_LOADING_ITEMS = true;
	}
	
	@Override
	protected ArrayList<Brand> doInBackground(Void... params) {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereContainedIn("UUID", UUIDS);
		try {
			List<ParseUser> users = query.find();
			if (users.size() > 0) {						
				for (ParseUser user : users) {
					newBrands.add(new Brand(user.getObjectId(), user.getString("UUID"), user.getString("brandName"), user.getString("brandEmail"), user.getString("brandPhone"), user.getString("brandLocation"),  (ArrayList<String>) user.get("brandTags"), user.getParseFile("brandCoverPicture").getUrl(), user.getString("brandWebsite"), user.getString("brandCategory"), user.getString("brandAbout")));
				}
				newBrands = sortBrandsAccordingToUUIDS();
			}
		}
		catch (ParseException e) {
//			Log.e("TEST", "error while fetching brand info: " + e.getMessage());
            e.printStackTrace();
		}

		return brands;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Brand> result) {
		super.onPostExecute(result);
		brands.addAll(newBrands);
		brandsListViewAdapter.notifyDataSetChanged();
		
		brandsListView.setVisibility(View.VISIBLE);
		
		if (swipeRefreshLayout != null) {			
			swipeRefreshLayout.setRefreshing(false);
		}
		
		endlessScrollLinearLayout.setVisibility(View.GONE);
		
		BrandsEndlessScrollListener.IS_LOADING_ITEMS = false;
	}

    private ArrayList<Brand> sortBrandsAccordingToUUIDS() {
		ArrayList<Brand> newBrandsSorted = new ArrayList<Brand>();
		for (String UUID : UUIDS) {
			for (Brand brand : newBrands) {
				if (brand.getUUID().equals(UUID)) {
					newBrandsSorted.add(brand);
				}
			}
		}
		return newBrandsSorted;
	}
}
