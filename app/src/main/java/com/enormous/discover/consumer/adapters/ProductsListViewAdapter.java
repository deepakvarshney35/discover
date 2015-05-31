package com.enormous.discover.consumer.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.activities.ProductInfoActivity;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.fragments.AddToCartFragment;
import com.enormous.discover.consumer.models.Product;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class ProductsListViewAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Product> products;
    private ArrayList<String> productBookmarks;
    private Resources res;

    @SuppressWarnings("unchecked")
	public ProductsListViewAdapter(Context c, ArrayList<Product> products) {
        this.activity = (Activity) c;
        this.products = products;

        productBookmarks = (ArrayList<String>) ParseUser.getCurrentUser().get("productBookmarks");
        if (productBookmarks == null) {
            productBookmarks = new ArrayList<String>();
        }
        res = c.getResources();
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            row = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_product_catalogue_item, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }
        final Product product = products.get(position);
        ImageLoader.getInstance().displayImage(activity, product.getPictureURL(), holder.productImageView, false, 640, 640, 0);
        holder.productNameTextView.setText(product.getName());
        holder.productBrandTextView.setText(product.getBrandName());
        holder.productCostTextView.setText(MainScreenActivity.CURRENCY_FORMATTER.format(product.getCost()));

        //check if this brand has already been bookmarked in order to set the bookmark button image
        boolean isBookmarked;
        if (productBookmarks.contains(product.getObjectId())) {
            isBookmarked = true;
            ImageLoader.getInstance().displayImage(activity, res.getIdentifier("ic_action_bookmark_selected", "drawable", activity.getPackageName())+"", holder.bookmarkImageView, true, 100, 100, 0);
        }
        else {
            isBookmarked = false;
            ImageLoader.getInstance().displayImage(activity, res.getIdentifier("ic_action_bookmark", "drawable", activity.getPackageName())+"", holder.bookmarkImageView, true, 100, 100, 0);
        }
        holder.bookmarkLinearLayout.setTag(isBookmarked);

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.productImageViewContainer:
                        Intent goToProductInfo = new Intent(activity, ProductInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("selectedPos", position);
                        bundle.putParcelableArrayList("products", products);
                        goToProductInfo.putExtras(bundle);
                        activity.startActivity(goToProductInfo);
                        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                        break;
                    case R.id.moreInformationLinearLayout:
                        Intent goToProductInfo2 = new Intent(activity, ProductInfoActivity.class);
                        Bundle bundle2 = new Bundle();
                        bundle2.putInt("selectedPos", position);
                        bundle2.putParcelableArrayList("products", products);
                        Log.d("producttest", ""+products.get(position).getBrandName()+products.get(position).getOptions()+products.get(position).getOptionsCost());
                		
                        goToProductInfo2.putExtras(bundle2);
                        activity.startActivity(goToProductInfo2);
                        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                        break;
                    case R.id.addToCartLinearLayout:
                        AddToCartFragment addToCartDialog = new AddToCartFragment();
                        Bundle bundle3 = new Bundle();
                        bundle3.putString("dialogTitle", "Choose option and quantity");
                        bundle3.putString("positiveButtonTitle", "Add to Cart");
                        bundle3.putParcelable("selectedProduct", products.get(position));
                        addToCartDialog.setArguments(bundle3);
                        addToCartDialog.show(activity.getFragmentManager(), "addToCartDialog");
                        break;
                    case R.id.bookmarkLinearLayout:
                        String id = product.getObjectId();
                        boolean isBookmarked = (Boolean) view.getTag();
                        if (isBookmarked) {
                            productBookmarks.remove(id);
                        }
                        else {
                            productBookmarks.add(id);
                            Toast.makeText(activity, "'" + product.getName() + "' added to bookmarks.", Toast.LENGTH_SHORT).show();
                        }
                        notifyDataSetChanged();
                        //now, upload productBookmarks to Parse
                        MainScreenActivity.CURRENT_USER.put("productBookmarks", productBookmarks);
                        MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
//                                    Log.e("TEST", "ProductCatalogueCategoryFragment bookmarkLinearLayout onClick :" + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                }
            }
        };
        holder.productImageViewContainer.setOnClickListener(onClickListener);
        holder.addToCartLinearLayout.setOnClickListener(onClickListener);
        holder.bookmarkLinearLayout.setOnClickListener(onClickListener);
        holder.moreInfoLinearLayout.setOnClickListener(onClickListener);

        return row;
    }


    private static class ViewHolder {
        ImageView productImageView;
        FrameLayout productImageViewContainer;
        TextView productNameTextView;
        TextView productBrandTextView;
        TextView productCostTextView;
        LinearLayout bookmarkLinearLayout;
        LinearLayout addToCartLinearLayout;
        LinearLayout moreInfoLinearLayout;
        ImageView bookmarkImageView;

        public ViewHolder(View row) {
            productImageView = (ImageView) row.findViewById(R.id.productImageView);
            productImageViewContainer = (FrameLayout) row.findViewById(R.id.productImageViewContainer);
            productNameTextView = (TextView) row.findViewById(R.id.productNameTextView);
            productBrandTextView = (TextView) row.findViewById(R.id.productBrandTextView);
            productCostTextView = (TextView) row.findViewById(R.id.productCostTextView);
            bookmarkLinearLayout = (LinearLayout) row.findViewById(R.id.bookmarkLinearLayout);
            bookmarkImageView = (ImageView) bookmarkLinearLayout.findViewById(R.id.bookmarkImageView);
            moreInfoLinearLayout = (LinearLayout) row.findViewById(R.id.moreInformationLinearLayout);
            addToCartLinearLayout = (LinearLayout) row.findViewById(R.id.addToCartLinearLayout);
        }
    }
}