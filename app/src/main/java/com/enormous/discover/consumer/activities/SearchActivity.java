package com.enormous.discover.consumer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.models.Brand;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Search Activity
 * -Publisher App saves Brand Search Key and Location Keys in Parse User class.
 * -Consumer App queries for these strings and returns the matching brand.
 * 
 * */
public class SearchActivity extends Activity {

    private ActionBar actionBar;
    private ListView searchListView;
    private EditText searchEditText;
    private ImageButton searchImageButton;
    private LinearLayout progressLinearLayout;
    private LinearLayout nothingFoundLinearLayout;
    private LinearLayout noInternetLinearLayout;
    private LinearLayout searchLinearLayout;
    private Handler handler;
    private ArrayList<Brand> brands;
    private SearchListViewAdapter searchListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();

        //set actionBar properties
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        //initalize brands list
        brands = new ArrayList<Brand>();

        //get the handler associated with the UI thread
        handler = new Handler();

        //initialize and set searchListView adapter
        searchListViewAdapter = new SearchListViewAdapter();
        searchListView.setAdapter(searchListViewAdapter);

        //set onEditorActionListener on searchEditText to detect when user presses ENTER/SEARCH
        searchEditText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchForBrands(searchEditText.getText().toString());
                    Utils.hideKeyboard(SearchActivity.this, searchEditText.getWindowToken());
                }
                return true;
            }
        });

        //set click listeners
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.searchImageButton:
                        searchEditText.setText("");
                        break;
                    case R.id.noInternetLinearLayout:
                        searchForBrands(searchEditText.getText().toString());
                        break;
                }
            }
        };
        searchImageButton.setOnClickListener(clickListener);
        noInternetLinearLayout.setOnClickListener(clickListener);

        //add TextChangeListener to searchEditText in order to change searchImageButton's icon when the user types
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence input, int arg1, int arg2, int arg3) {
                if (input.toString().trim().length() == 0) {
                    searchImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_search_edittext));
                }
                else {
                    searchImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_cancel_edittext));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    private void findViews() {
        actionBar = getActionBar();
        searchListView = (ListView) findViewById(R.id.searchListView);
        searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchImageButton = (ImageButton) findViewById(R.id.searchImageButton);
        progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
        nothingFoundLinearLayout = (LinearLayout) findViewById(R.id.nothingFoundLinearLayout);
        noInternetLinearLayout = (LinearLayout) findViewById(R.id.noInternetLinearLayout);
        searchLinearLayout = (LinearLayout) findViewById(R.id.searchLinearLayout);
    }

    private void searchForBrands(String searchTerm) {
        noInternetLinearLayout.setVisibility(View.GONE);
        if (Utils.isConnectedToInternet(this)) {
            SearchBrandsTask searchBrandsTask = new SearchBrandsTask(searchTerm);
            searchBrandsTask.execute();
        }
        else {
            searchListView.setVisibility(View.GONE);
            noInternetLinearLayout.setVisibility(View.VISIBLE);
            nothingFoundLinearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overflow_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.ic_action_refresh:
                searchForBrands(searchEditText.getText().toString());
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
    }

    private class SearchBrandsTask extends AsyncTask<Void, Void, ArrayList<Brand>> {

        String searchTerm;
        ArrayList<Brand> brands;

        public SearchBrandsTask(String searchTerm) {
            this.searchTerm = searchTerm.toLowerCase().trim().replaceAll("[^a-z0-9]+", "");
            brands = new ArrayList<Brand>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            searchLinearLayout.setVisibility(View.GONE);
            progressLinearLayout.setVisibility(View.VISIBLE);
            nothingFoundLinearLayout.setVisibility(View.GONE);
            searchListView.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<Brand> doInBackground(Void... params) {
            //create a combinedQuery that searches for BOTH name and location (searches only those UUIDS contained in user's history)
            ParseQuery<ParseUser> queryName = ParseUser.getQuery();
            queryName.whereContains("searchBrandName", searchTerm);
            ParseQuery<ParseUser> queryLocation = ParseUser.getQuery();
            queryLocation.whereContains("searchBrandLocation", searchTerm);
            ArrayList<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
            queries.add(queryName);
            queries.add(queryLocation);
            ParseQuery<ParseUser> combinedQuery = ParseQuery.or(queries);
/*            ArrayList<String> userHistory = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("userHistory");
            if (userHistory == null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        nothingFoundLinearLayout.setVisibility(View.VISIBLE);
                        progressLinearLayout.setVisibility(View.GONE);
                    }
                });
            }
            else*/ {
                //combinedQuery.whereContainedIn("UUID", userHistory);

                try {
                    List<ParseUser> users = combinedQuery.find();
                    for (ParseUser user : users) {
                    	this.brands.add(new Brand(user.getObjectId(), user.getString("UUID"), user.getString("brandName"), user.getString("brandEmail"), user.getString("brandPhone"), user.getString("brandLocation"),  (ArrayList<String>) user.get("brandTags"), user.getParseFile("brandCoverPicture").getUrl(), user.getString("brandWebsite"), user.getString("brandCategory"), user.getString("brandAbout")));
                    }
                }
                catch (Exception e) {
                    Log.d("TEST", "Error searching for brands: " + e.getMessage());
                }

            }

            return this.brands;
        }

        @Override
        protected void onPostExecute(ArrayList<Brand> result) {
            super.onPostExecute(result);
            if (result.size() == 0) {
                nothingFoundLinearLayout.setVisibility(View.VISIBLE);
            }
            SearchActivity.this.brands = new ArrayList<Brand>(this.brands);
            searchListViewAdapter.notifyDataSetChanged();

            progressLinearLayout.setVisibility(View.GONE);
            searchListView.setVisibility(View.VISIBLE);

        }

    }

    private class SearchListViewAdapter extends BaseAdapter implements OnItemClickListener {

        public SearchListViewAdapter() {
            searchListView.setOnItemClickListener(this);
        }

        @Override
        public int getCount() {
            return brands.size();
        }

        @Override
        public Object getItem(int pos) {
            return brands.get(pos);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;
            if (row == null) {
                row = SearchActivity.this.getLayoutInflater().inflate(R.layout.listview_search_item, parent, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            else {
                holder = (ViewHolder) row.getTag();
            }
            Brand brand = brands.get(pos);
            Resources res = getResources();
            //set card background according to position
            Log.i("TEST", pos + " pos");
            if (pos == 0) {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_top));
            }
            else if (pos == brands.size()-1) {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_bottom));
            }
            else {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey));
            }
            ImageLoader.getInstance().displayImage(SearchActivity.this, res.getIdentifier(brand.getCategoryName(), "drawable", SearchActivity.this.getPackageName())+"", holder.brandCategoryImageView, true, 300, 300, 0);
            holder.brandLocationTextView.setText(brand.getLocation());
            holder.brandNameTextView.setText(brand.getName());
            addTags(res, brand.getTags(), holder.tagsLinearLayout);

            return row;
        }

        @Override
        public void onItemClick(AdapterView<?> adv, View v, int pos,
                                long arg3) {
            Intent goToBrandInfo = new Intent(SearchActivity.this, BrandInfoActivity.class);
            goToBrandInfo.putExtra("selectedBrand", brands.get(pos));
            startActivity(goToBrandInfo);
            overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
        }

        private void addTags(Resources res, ArrayList<String> tags, LinearLayout container) {
            container.removeAllViews();
            float marginRight = Utils.convertDpToPixel(7);
            if (tags != null) {
                LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lParams.setMargins(0, 0, (int) marginRight, 0);
                for (String tag : tags) {
                    TextView textView = new TextView(SearchActivity.this);
                    textView.setText(tag);
                    textView.setTextColor(res.getColor(R.color.textPrimary));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                    textView.setLayoutParams(lParams);
                    container.addView(textView);
                }
            }
        }
    }

    private static class ViewHolder {
        ImageView brandCategoryImageView;
        TextView brandNameTextView;
        TextView brandLocationTextView;
        LinearLayout tagsLinearLayout;

        public ViewHolder(View row) {
            brandCategoryImageView = (ImageView) row.findViewById(R.id.brandCategoryImageView);
            brandNameTextView = (TextView) row.findViewById(R.id.brandNameTextView);
            brandLocationTextView = (TextView) row.findViewById(R.id.brandLocationTextView);
            tagsLinearLayout = (LinearLayout) row.findViewById(R.id.tagsLinearLayout);
        }
    }
}
