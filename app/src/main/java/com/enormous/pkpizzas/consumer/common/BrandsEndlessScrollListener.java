package com.enormous.pkpizzas.consumer.common;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.enormous.pkpizzas.consumer.adapters.BrandsListViewAdapter;
import com.enormous.pkpizzas.consumer.asynctasks.GetMoreBrandsTask;
import com.enormous.pkpizzas.consumer.models.Brand;

import java.util.ArrayList;

public class BrandsEndlessScrollListener implements OnScrollListener {

	private ArrayList<String> UUIDS;
	private ListView listView;
	private ArrayList<Brand> brands;
	private BrandsListViewAdapter brandsListViewAdapter;
	private SwipeRefreshLayout swipeRefreshLayout;
	private LinearLayout endlessScrollLinearLayout;
	private int counter = 1;

	public static final int THRESHOLD = 1;
	public static final int NUMBER_OF_ITEMS_TO_LOAD = 5;
	public static boolean IS_LOADING_ITEMS = false;

	public BrandsEndlessScrollListener(ArrayList<String> UUIDS, ListView listView, ArrayList<Brand> brands, BrandsListViewAdapter brandsListViewAdapter , SwipeRefreshLayout swipeRefreshLayout, LinearLayout endlessScrollLinearLayout) {
		this.UUIDS = UUIDS;
		this.listView = listView;
		this.brands = brands;
		this.brandsListViewAdapter = brandsListViewAdapter;
		this.swipeRefreshLayout = swipeRefreshLayout;
		this.endlessScrollLinearLayout = endlessScrollLinearLayout;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		totalItemCount = totalItemCount - 2;	//subtract 2 from totalItems due to header + footer

		if (!IS_LOADING_ITEMS) {			
			int lastVisiblePosition = listView.getLastVisiblePosition();
			if (lastVisiblePosition > (totalItemCount - THRESHOLD) && totalItemCount > 0) {
				if(UUIDS!=null){
					ArrayList<String> uuidsToLoad = getNextUUIDSToLoad(UUIDS);
					if (uuidsToLoad.size() != 0) {					
						//					Log.d("TEST", "LOADING MORE ITEMS");
						GetMoreBrandsTask getMoreBrandsTask = new GetMoreBrandsTask(uuidsToLoad, brands, brandsListViewAdapter, swipeRefreshLayout, listView, endlessScrollLinearLayout);
						getMoreBrandsTask.execute();
						counter++;
					}
				}
			}			
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private ArrayList<String> getNextUUIDSToLoad(ArrayList<String> UUIDS) {
		ArrayList<String> uuidsToLoad = new ArrayList<String>();
		int helper = counter * NUMBER_OF_ITEMS_TO_LOAD;
		Log.d("history", UUIDS+"");
		if (UUIDS.size() > helper + NUMBER_OF_ITEMS_TO_LOAD) {			
			for (int i=helper;i<helper+NUMBER_OF_ITEMS_TO_LOAD;i++) {
				uuidsToLoad.add(UUIDS.get(i));
			}
		}
		else {
			for (int i=helper;i<UUIDS.size();i++) {
				uuidsToLoad.add(UUIDS.get(i));
			}
		}
		return uuidsToLoad;
	}

}
