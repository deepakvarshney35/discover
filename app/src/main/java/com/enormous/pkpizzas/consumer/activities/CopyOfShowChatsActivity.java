package com.enormous.pkpizzas.consumer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.enormous.pkpizzas.consumer.models.Brand;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CopyOfShowChatsActivity extends Activity {

	Handler handler;
	private ArrayList<Brand> customers ;
	CustomersListViewAdapter adapter;
	ListView clientProfileList;
	LayoutInflater inflater;
	ProgressBar progressBarLocalBrands;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_show_chats);

		clientProfileList = (ListView) findViewById(android.R.id.list);
		progressBarLocalBrands = (ProgressBar) findViewById(R.id.progressBarLocalBrands);

		inflater = getLayoutInflater();

		//set actionBar properties
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle("Chats");

		handler = new Handler();
		//db = new DatabaseHandler(getActivity());
		customers = new ArrayList<Brand>();

		//add header to categoriesListView
		View header = inflater.inflate(R.layout.listview_graph_header, null);
		clientProfileList.addHeaderView(header);

		adapter = new CustomersListViewAdapter();
		clientProfileList.setAdapter(adapter);

		progressBarLocalBrands.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
		query.whereEqualTo("email",ParseUser.getCurrentUser().getString("email"));
		query.addDescendingOrder("updatedAt");
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> results, ParseException e) {
				if(e==null){
					ArrayList<String> dummy = new ArrayList<String>();
					for (ParseObject result : results) {
						if(!"GOOGLE".equals(result.getString("brandObjectId"))){
							customers.add(new Brand(result.getString("brandObjectId"),"", result.getString("brandName"), result.getString("email"), "","", dummy, "", "", "", ""));
						}
					}
					adapter.notifyDataSetChanged();
					progressBarLocalBrands.setVisibility(View.GONE);
					if(customers.size()==0){
						clientProfileList.setVisibility(View.GONE);
					}
				}else{
					clientProfileList.setVisibility(View.GONE);
					progressBarLocalBrands.setVisibility(View.GONE);
					Toast.makeText(CopyOfShowChatsActivity.this, "Error - Try Again.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return true;
	}
	
	public class GetCustomersTask extends AsyncTask<Void, Void, ArrayList<Brand>> {

		ArrayList<Brand> customers;
		public GetCustomersTask() {
			customers = new ArrayList<Brand>();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}

		@Override
		protected ArrayList<Brand> doInBackground(Void... params) {
			ArrayList<String> dummy = new ArrayList<String>();
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
			query.whereEqualTo("email",ParseUser.getCurrentUser().getString("email"));
			query.addDescendingOrder("updatedAt");
			try {				
				List<ParseObject> results = query.find();
				for (ParseObject result : results) {
					if(!"GOOGLE".equals(result.getString("brandObjectId"))){
						customers.add(new Brand(result.getString("brandObjectId"),"", result.getString("brandName"), result.getString("email"), "","", dummy, "", "", "", ""));
					}
				}
			}
			catch (Exception e) {
				Log.e("TEST", "Error retrieving brand items: " + e.getMessage());
			}
			return customers;
		}

		@Override
		protected void onPostExecute(ArrayList<Brand> result) {
			super.onPostExecute(result);
			
		}
	}

	private class CustomersListViewAdapter extends BaseAdapter {

		public CustomersListViewAdapter() {
		}

		@Override
		public int getCount() {
			return customers.size();
		}

		@Override
		public Object getItem(int pos) {
			return customers.get(pos);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int pos, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater infalInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				row = infalInflater.inflate(R.layout.listview_customers, parent, false);
			}

			SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Service.MODE_PRIVATE);
			int notifications = sharedpreferences.getInt("key"+customers.get(pos).getObjectId(), 0);
			
			TextView customerName = (TextView) row.findViewById(R.id.customerName);
			TextView customerInformation = (TextView) row.findViewById(R.id.customerInformation);
			ImageView customerProfilePicture = (ImageView) row.findViewById(R.id.customerProfilePicture);
			TextView categoryCountTextView = (TextView) row.findViewById(R.id.categoryCountTextView);
			LinearLayout select =(LinearLayout)  row.findViewById(R.id.select);

			if(notifications>0){
				categoryCountTextView.setVisibility(View.VISIBLE);
				categoryCountTextView.setText(""+notifications);
			}else{
				categoryCountTextView.setVisibility(View.GONE);
			}
			
			customerName.setText(customers.get(pos).getName());
			//customerInformation.setText("Email: "+customers.get(pos).getEmail()+" \nPhone: "+customers.get(pos).getPhone());
			//if(customers.get(pos).getCoverPictureUrl()!=null){
			///	ImageLoader.getInstance().displayImage(getBaseContext(), customers.get(pos).getCoverPictureUrl(), customerProfilePicture, false, 50, 50, 0);
			//}
			select.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent goToClientInfo2 = new Intent(CopyOfShowChatsActivity.this, ChatMessageActivity.class);
					goToClientInfo2.putExtra("merchantUserId",customers.get(pos).getObjectId());
					goToClientInfo2.putExtra("merchantName",customers.get(pos).getName());
					startActivity(goToClientInfo2);
					overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
				}

			});

			//customerProfilePicture.setImageBitmap();

			return row;
		}
	}
}
