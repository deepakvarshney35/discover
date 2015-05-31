package com.enormous.pkpizzas.consumer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.common.ImageLoader;
import com.enormous.pkpizzas.consumer.models.Category;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Locale;

/**
 * After successfull signup this activity is called. 
 * This makes user to select interested categories. And user will get SHOP notification(Detected Beacons) only from the Brands with
 * Selected category.
 * */
public class ChooseCategoriesActivity extends Activity {

    private ActionBar actionBar;
    private GridView gridView;
    private ProgressDialog progressDialog;
    private SharedPreferences spf;
    private int totalCategories;
    private ParseUser currentUser;
    private String callingActivity;


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_choose_categories);
		findViews();

		//set actionBar properties
		actionBar.setDisplayHomeAsUpEnabled(false);		//default value, later changed depending on the calling activity
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));

		//get currentUser
		currentUser = ParseUser.getCurrentUser();

		//set up gridview adapter
		CustomGridViewAdapter adapter = new CustomGridViewAdapter();
		gridView.setAdapter(adapter);

		//get name of calling activity
		if (getIntent().getExtras() != null) {
			callingActivity = getIntent().getExtras().getString("callingActivity");
			if (callingActivity != null) {
				if (callingActivity.equals("MainScreenActivity")) {
					//get previously selected categories from Parse
					ArrayList<String> previouslySelectedCategories = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("selectedCategories");
					setGridViewSelections(gridView, previouslySelectedCategories);

					//enable home button to got back 
					actionBar.setDisplayHomeAsUpEnabled(true);
				}
			}

		}

		//get total number of categories
		totalCategories = adapter.getCount();

	}

	@Override
	protected void onResume() {
		super.onResume();
		//set actionBar title
		actionBar.setTitle("What Interests You?");
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (callingActivity != null) {
			if (callingActivity.equals("MainScreenActivity")) {
				overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
			}
		}
	}

    private ArrayList<Integer> getArrayList(SparseBooleanArray checkedItems) {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int i=0;i<checkedItems.size();i++) {
			if (checkedItems.valueAt(i)) {
				arrayList.add(checkedItems.keyAt(i));
			}
		}
		return arrayList;
	}

    private class CustomGridViewAdapter extends BaseAdapter implements OnItemClickListener {

        ArrayList<Category> categories;

		public CustomGridViewAdapter() {
            categories = new ArrayList<Category>();
            categories.add(new Category("Accessories", 0));
            categories.add(new Category("Apparels", 0));
            categories.add(new Category("Billboards", 0));
            categories.add(new Category("Bookstore", 0));
            categories.add(new Category("Computers", 0));
            categories.add(new Category("Food", 0));
            categories.add(new Category("Grocery", 0));
            categories.add(new Category("Movies", 0));
            categories.add(new Category("Offers", 0));
            categories.add(new Category("Amusement Park", 0));
            categories.add(new Category("Bakery", 0));
            categories.add(new Category("Bank", 0));
            categories.add(new Category("Bar", 0));
            categories.add(new Category("Beauty Salon", 0));
            categories.add(new Category("Cafe", 0));
            categories.add(new Category("Convenience Store", 0));
            categories.add(new Category("Department Store", 0));
            categories.add(new Category("Electronics Store", 0));
            categories.add(new Category("Doctor", 0));
            categories.add(new Category("Electrician", 0));
            categories.add(new Category("Dentist", 0));
            
			//set item click listener for gridView
			gridView.setOnItemClickListener(this);
		}

		@Override
		public int getCount() {
			return categories.size();
		}

		@Override
		public Object getItem(int pos) {
			return categories.get(pos);
		}


		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		class GridViewHolder {
			ImageView categoryImageView;
			ImageView selectedImageView;
            TextView categoryNameTextView;

			public GridViewHolder(View row) {
				categoryImageView = (ImageView) row.findViewById(R.id.categoryImageView);
				selectedImageView = (ImageView) row.findViewById(R.id.selectedImageView);
                categoryNameTextView = (TextView) row.findViewById(R.id.categoryNameTextView);
			}
		}

		@Override	
		public View getView(int pos, View convertView, ViewGroup parent) {
			View row = convertView;
			GridViewHolder holder = null;
			if (row == null) {
				LayoutInflater inflater = ChooseCategoriesActivity.this.getLayoutInflater();
				row = inflater.inflate(R.layout.gridview_item, parent, false);
				holder = new GridViewHolder(row);
				row.setTag(holder);
			}
			else {
				holder = (GridViewHolder) row.getTag();
			}

            ImageLoader.getInstance().displayImage(ChooseCategoriesActivity.this,
                    getResources().getIdentifier(categories.get(pos).getName().toLowerCase().replace(" ", ""), "drawable", getPackageName())+"",
                    holder.categoryImageView, true, 500, 500, 0);
            holder.categoryNameTextView.setText(categories.get(pos).getName());

			//select/deselect items according to the checked item positions returned by gridView
			if (gridView.getCheckedItemPositions().get(pos)) {
				holder.selectedImageView.setVisibility(View.VISIBLE);
			}
			else {
				holder.selectedImageView.setVisibility(View.INVISIBLE);
			}

			return row;
		}

		@Override
		public void onItemClick(AdapterView<?> adView, View view, int pos,
				long arg3) {
			notifyDataSetChanged();
		}
	}

    private void setGridViewSelections(GridView gridView, ArrayList<String> selectedCategories) {
		for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
            if (selectedCategories.contains(((Category) gridView.getAdapter().getItem(i)).getName().toLowerCase())) {
                gridView.setItemChecked(i, true);
            }
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (callingActivity != null) {
			if (callingActivity.equals("MainScreenActivity")) {
				getMenuInflater().inflate(R.menu.menu_save, menu);
			}
		}
		else {
			getMenuInflater().inflate(R.menu.menu_next, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		if (item.getItemId() == R.id.action_next || item.getItemId() == R.id.action_save) {
			//get selected items in an arrayList
			ArrayList<Integer> selectedCategoriesPositions = getArrayList(gridView.getCheckedItemPositions());
            ArrayList<String> selectedCategories = new ArrayList<String>();
            for (Integer pos : selectedCategoriesPositions) {
                selectedCategories.add(((Category) gridView.getAdapter().getItem(pos)).getName().toLowerCase(Locale.ENGLISH));
            }

			//ensure at least one category is chosen
			if (selectedCategories.size() == 0) {
				Toast.makeText(ChooseCategoriesActivity.this, "You need to choose at least 1 category.", Toast.LENGTH_SHORT).show();
			}
			else {
				//set up progressDialog
				progressDialog = new ProgressDialog(ChooseCategoriesActivity.this);
				progressDialog.setCancelable(false);
				if (callingActivity != null) {
					if (callingActivity.equals("MainScreenActivity")) {
						progressDialog.setMessage("Saving Preferences...");
					}
				}
				else {
					progressDialog.setMessage("Setting things up...");
				}
				progressDialog.show();

				//upload selected categories for currentUser
				currentUser = ParseUser.getCurrentUser();
				currentUser.put("selectedCategories", selectedCategories);
				currentUser.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
//							Log.d("TEST", "categories uploaded successfully");
							if (callingActivity != null) {
								if (callingActivity.equals("MainScreenActivity")) {
									//back to main screen
                                    finish();
                                    overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
								}
							}
							else {
								//go to MainScreen and finish() this activity
								Intent goToMainScreen = new Intent(ChooseCategoriesActivity.this, MainScreenActivity.class);
								startActivity(goToMainScreen);
								//override entry transition animation
								overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
								finish();
							}
						}
						else {
//							Log.d("TEST", "categories FAILED to upload: " + e.getMessage());
							Toast.makeText(ChooseCategoriesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
						progressDialog.dismiss();
					}
				});
			}
		}
		return true;
	}


    private void findViews() {
		actionBar = getActionBar();
		gridView = (GridView) findViewById(R.id.gridView1);
	}

}
