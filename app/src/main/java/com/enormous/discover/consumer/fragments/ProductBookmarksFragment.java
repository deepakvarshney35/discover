package com.enormous.discover.consumer.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.adapters.ProductsListViewAdapter;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.models.Product;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manas on 8/11/2014.
 */
public class ProductBookmarksFragment extends Fragment {

    private ListView listView;
    private LinearLayout progressLinearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout nothingFoundLinearLayout;
    private LinearLayout noInternetLinearLayout;
    private ArrayList<Product> products;
    private ProductsListViewAdapter adapter;
    private ArrayList<String> productBookmarks;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        findViews(view);
        setHasOptionsMenu(true);

        //add ability to pull to refresh;
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshProducts();
            }
        });

        //set up products listview
        productBookmarks = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("productBookmarks");
        products = new ArrayList<Product>();
        adapter = new ProductsListViewAdapter(getActivity(), products);
        listView.setAdapter(adapter);
        if (productBookmarks == null) {
            productBookmarks = new ArrayList<String>();
            nothingFoundLinearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
        else if (productBookmarks.size() == 0) {
            nothingFoundLinearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
        else {
            refreshProducts();
        }

        noInternetLinearLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                refreshProducts();
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
                refreshProducts();
                break;
        }
        return true;
    }

    private void refreshProducts() {
        noInternetLinearLayout.setVisibility(View.GONE);
        if (Utils.isConnectedToInternet(getActivity())) {
            RefreshProductBookmarksTask refreshTask = new RefreshProductBookmarksTask();
            refreshTask.execute();
        }
        else {
            swipeRefreshLayout.setRefreshing(false);
            listView.setVisibility(View.INVISIBLE);
            noInternetLinearLayout.setVisibility(View.VISIBLE);
            nothingFoundLinearLayout.setVisibility(View.GONE);
        }
    }

    //RefreshProductsTask from ProductCatalogueCategoryFragment cannot be used here as it works in a very different way (works closely with its parent activity)
    private class RefreshProductBookmarksTask extends AsyncTask<Void, Void, ArrayList<Product>> {

        ArrayList<Product> newProducts;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            if (products.size() == 0) {
                progressLinearLayout.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
                nothingFoundLinearLayout.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected ArrayList<Product> doInBackground(Void... voids) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Product");
            List<ParseObject> objects;
            query.whereContainedIn("objectId", productBookmarks);
            try {
                objects = query.find();
                newProducts = new ArrayList<Product>();
                for (ParseObject object : objects) {
                    newProducts.add(new Product(object.getString("brandObjectId"),object.getObjectId(), object.getString("brandName"), object.getString("productName"), object.getString("productType"), object.getParseFile("productPicture").getUrl(), object.getInt("productCost"), object.getInt("productShippingCost"), object.getInt("productTax"), object.getString("productDescription"), object.getInt("productStock"), (ArrayList<String>) object.get("productOptions"),(ArrayList<Integer>) object.get("productOptionsCost")));
                }

                return newProducts;
            }
            catch (Exception e) {
//                Log.e("TEST", e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Product> result) {
            super.onPostExecute(result);
            if (result != null) {
                if (result.size() == 0) {
                    nothingFoundLinearLayout.setVisibility(View.VISIBLE);
                }
                if (!products.equals(newProducts)) {
                    products.clear();
                    products.addAll(newProducts);
                    adapter.notifyDataSetChanged();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
            progressLinearLayout.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
    }
}
