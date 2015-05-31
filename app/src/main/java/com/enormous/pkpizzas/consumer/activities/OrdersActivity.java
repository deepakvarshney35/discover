package com.enormous.pkpizzas.consumer.activities;// Created by Sanat Dutta on 1/15/2015.

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.common.ImageLoader;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.enormous.pkpizzas.consumer.models.pastOrder;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends Activity {
    private ActionBar mActionBar;
    private ListView ordersListView;
    private ArrayList<pastOrder> pastOrders = new ArrayList<pastOrder>();
    private LinearLayout nothingFoundLinearLayout,noInternetLinearLayout;
    private ProgressBar mprogressBar;
    private DateFormat mDateFormat = new SimpleDateFormat("dd-MM-yy");
    private boolean isSearchOrder=false;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        if(!getIntent().getExtras().getString("orderId").equals("NA")){
            isSearchOrder=true;
            orderId = getIntent().getExtras().getString("orderId");
        }
        else isSearchOrder=false;

        findViews();

        //set actionBar properties
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));

        setupOrdersList();

        noInternetLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupOrdersList();
            }
        });
    }

    private void setupOrdersList() {

        pastOrders.clear();
        noInternetLinearLayout.setVisibility(View.GONE);
        if (Utils.isConnectedToInternet(this)) {
            ordersListView.setVisibility(View.GONE);
            nothingFoundLinearLayout.setVisibility(View.GONE);
            mprogressBar.setVisibility(View.VISIBLE);

            //get orders from Parse
            ParseQuery<ParseObject> ordersQuery = ParseQuery.getQuery("PastOrders");
            ordersQuery.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
            ordersQuery.orderByDescending("createdAt");
            if (isSearchOrder) ordersQuery.whereEqualTo("objectId", orderId);
            ordersQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        if(parseObjects.size()>0) {
                            for (ParseObject object : parseObjects) {
                                pastOrders.add(new pastOrder(
                                        object.getObjectId(),
                                        object.getString("brandObjectId"),
                                        object.getString("brandName"),
                                        object.getString("brandProfilePictureUrl"),
                                        object.getString("userObjectId"),
                                        object.getString("products"),
                                        "₹ "+object.getString("totalCost"),
                                        mDateFormat.format(object.getCreatedAt())));
                            }
                            mprogressBar.setVisibility(View.GONE);

                            //now set cartAdapter and update order details
                            OrderAdapter mAdapter = new OrderAdapter();
                            ordersListView.setAdapter(mAdapter);
                            ordersListView.setVisibility(View.VISIBLE);
                            nothingFoundLinearLayout.setVisibility(View.GONE);
                        } else{
                            ordersListView.setVisibility(View.GONE);
                            mprogressBar.setVisibility(View.GONE);
                            nothingFoundLinearLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        e.printStackTrace();
                        Toast.makeText(OrdersActivity.this, "An error occurred while fetching your orders info.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            noInternetLinearLayout.setVisibility(View.VISIBLE);
            ordersListView.setVisibility(View.GONE);
            mprogressBar.setVisibility(View.GONE);
            nothingFoundLinearLayout.setVisibility(View.GONE);
        }
    }

    private void findViews() {
        mActionBar = getActionBar();
        ordersListView = (ListView) findViewById(R.id.ordersListView);
        nothingFoundLinearLayout = (LinearLayout) findViewById(R.id.nothingFoundLinearLayout);
        noInternetLinearLayout = (LinearLayout) findViewById(R.id.noInternetLinearLayout);
        mprogressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //set actionBar title
        mActionBar.setTitle("Orders");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
    }


    private class OrderAdapter extends BaseAdapter{
        @Override
        public int getCount() { return pastOrders.size(); }

        @Override
        public Object getItem(int position) { return pastOrders.get(position); }

        @Override
        public long getItemId(int position) { return 0; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;
            if (row == null) {
                row = getLayoutInflater().inflate(R.layout.listview_order_item, parent, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            final pastOrder order = pastOrders.get(position);
            holder.brandNameTextView.setText(order.getBrandName());
            holder.dateOrderedTextView.setText(order.getOrderDate());
            holder.productsTextView.setText(order.getProducts());
            holder.totalCostTextView.setText(order.getTotalCost());
            if (order.getBrandProfilePictureUrl()!=null) ImageLoader.getInstance().displayImage(OrdersActivity.this, order.getBrandProfilePictureUrl(), holder.brandProfilePictureImageView, false, 200, 200, 0);

            return row;
        }
    }

    private static class ViewHolder {

        TextView brandNameTextView;
        TextView dateOrderedTextView;
        ImageView brandProfilePictureImageView;
        TextView productsTextView;
        TextView totalCostTextView;

        public ViewHolder(View row) {
            brandNameTextView = (TextView)row.findViewById(R.id.brandName);
            dateOrderedTextView = (TextView)row.findViewById(R.id.dateOrdered);
            brandProfilePictureImageView = (ImageView)row.findViewById(R.id.brandProfilePicture);
            productsTextView = (TextView)row.findViewById(R.id.products);
            totalCostTextView = (TextView)row.findViewById(R.id.totalCost);
        }
    }
}
