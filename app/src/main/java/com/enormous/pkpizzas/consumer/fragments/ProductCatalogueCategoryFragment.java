package com.enormous.pkpizzas.consumer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.activities.ProductCatalogueActivity;
import com.enormous.pkpizzas.consumer.adapters.ProductsListViewAdapter;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.enormous.pkpizzas.consumer.models.Product;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ProductCatalogueCategoryFragment extends android.app.Fragment {

    private final String TAG = "ProductCatalogueCategoryFragment";
    private ProductCatalogueActivity activity;
    private ListView productsListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Product> products;
    private ProductsListViewAdapter adapter;
    private String productCategory;
    private String brandObjectId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_catalogue_category, container, false);
        findViews(view);

        //get productCategory and brandName from arguments and then filter the products according to this category
        productCategory = getArguments().getString("productCategory");
        brandObjectId = getArguments().getString("brandObjectId");
        products = new ArrayList<Product>();
        if (getActivity() instanceof ProductCatalogueActivity) {
            activity = (ProductCatalogueActivity) getActivity();
            for (Product product : activity.products) {
                if (product.getCategory().toLowerCase().equals(productCategory.toLowerCase())) {
                    products.add(product);
                }
            }
        }

        //set swipeToRefresh colorScheme and listener
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshProducts();
            }
        });

        //set up productsListView adapter
        adapter = new ProductsListViewAdapter(getActivity(), products);
        productsListView.setAdapter(adapter);


        return view;
    }

    private void findViews(View view) {
        productsListView = (ListView) view.findViewById(R.id.productsListView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
    }

    public void refreshProducts() {
        if (Utils.isConnectedToInternet(activity)) {
            new RefreshProductsTask().execute();
        }
        else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public static ProductCatalogueCategoryFragment newInstance(String productCategory, String brandObjectId) {
        ProductCatalogueCategoryFragment categoryFragment = new ProductCatalogueCategoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("productCategory", productCategory);
        bundle.putString("brandObjectId", brandObjectId);
        categoryFragment.setArguments(bundle);
        return categoryFragment;
    }

    private class RefreshProductsTask extends AsyncTask<Void, Void, ArrayList<Product>> {
        ArrayList<Product> newProducts;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<Product> doInBackground(Void... params) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Product");
            List<ParseObject> objects;
            query.whereEqualTo("brandObjectId", brandObjectId);
            query.whereEqualTo("productType", productCategory);
            if (activity.sortOrder.equals("ascending")) {
                query.addAscendingOrder(activity.sortKey);
            }
            else {
                query.addDescendingOrder(activity.sortKey);
            }
            try {
                objects = query.find();
                newProducts = new ArrayList<Product>();
                for (ParseObject object : objects) {
                    newProducts.add(new Product(object.getString("brandObjectId"),object.getObjectId(), object.getString("brandName"), object.getString("productName"), object.getString("productType"), object.getParseFile("productPicture").getUrl(), object.getInt("productCost"), object.getInt("productShippingCost"), object.getInt("productTax"), object.getString("productDescription"), object.getInt("productStock"), (ArrayList<String>) object.get("productOptions"),(ArrayList<Integer>) object.get("productOptionsCost")));
                }

                return newProducts;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Product> result) {
            super.onPostExecute(result);
            if (result != null) {
                if (result.size() > 0) {
                    if (!products.equals(newProducts)) {
                        products.clear();
                        products.addAll(newProducts);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
