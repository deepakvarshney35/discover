package com.enormous.pkpizzas.consumer.asynctasks;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.enormous.pkpizzas.consumer.adapters.BrandsListViewAdapter;
import com.enormous.pkpizzas.consumer.models.Brand;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GetBrandsTask extends AsyncTask<Void, Void, ArrayList<Brand>> {

    private ArrayList<String> UUIDS;
    private ArrayList<String> categoryConstraintList;
    private ArrayList<Brand> brands;
    private BrandsListViewAdapter brandsListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout progressLinearLayout;
    private LinearLayout nothingFoundHereLinearLayout;
    private ListView brandsListView;
    private ArrayList<Brand> newBrands;
    private GetBrandsCallback callback;
	
	public GetBrandsTask(ArrayList<String> UUIDS, ArrayList<String> categoryConstraintList, ArrayList<Brand> brands, BrandsListViewAdapter brandsListViewAdapter,SwipeRefreshLayout swipeRefreshLayout, LinearLayout progressLinearLayout, LinearLayout nothingFoundHereLinearLayout, ListView brandsListView, GetBrandsCallback callback) {
		this.UUIDS = UUIDS;
        this.categoryConstraintList = categoryConstraintList;
		this.brands = brands;
		this.brandsListViewAdapter = brandsListViewAdapter;
		this.swipeRefreshLayout = swipeRefreshLayout;
		this.progressLinearLayout = progressLinearLayout;
        this.nothingFoundHereLinearLayout = nothingFoundHereLinearLayout;
		this.brandsListView = brandsListView;
        this.callback = callback;
		
		newBrands = new ArrayList<Brand>();
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//null check as some activities/fragments do not use swipeRefreshLayout
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setRefreshing(true);
		}
		if (progressLinearLayout != null) {			
			progressLinearLayout.setVisibility(View.VISIBLE);
		}
        if (nothingFoundHereLinearLayout != null) {
            nothingFoundHereLinearLayout.setVisibility(View.GONE);
        }
	}
	
	@Override
	protected ArrayList<Brand> doInBackground(Void... params) {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereContainedIn("UUID", UUIDS);
        if (categoryConstraintList != null) {
            query.whereContainedIn("brandCategory", categoryConstraintList);
        }
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

		return newBrands;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Brand> result) {
		super.onPostExecute(result);
        if (brands != null) {
            brands.clear();
            brands.addAll(result);
        }
        if (callback != null) {
            callback.done(result);
        }
        if (brandsListViewAdapter != null) {
            brandsListViewAdapter.notifyDataSetChanged();
        }
		if (progressLinearLayout != null) {			
			progressLinearLayout.setVisibility(View.GONE);
		}
        if (brandsListView != null) {
            brandsListView.setVisibility(View.VISIBLE);
            if(brands.size()>0)
            	setListViewHeightBasedOnChildren(brandsListView);
        }
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setRefreshing(false);
		}
        if (result.size() == 0) {
            if (nothingFoundHereLinearLayout != null) {
                nothingFoundHereLinearLayout.setVisibility(View.VISIBLE);
            }
        }
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

    public static interface GetBrandsCallback {
        public void done(ArrayList<Brand> brands);
    }
    
	/**** Method for Setting the Height of the ListView dynamically.
	 **** Hack to fix the issue of not showing all the items of the ListView
	 **** when placed inside a ScrollView  ****/
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
			return;

		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
		int totalHeight = 0;
		View view = null;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			view = listAdapter.getView(i, view, listView);
			if (i == 0)
				view.setLayoutParams(new LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

			view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += view.getMeasuredHeight();
		}
		LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() ));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}
}
