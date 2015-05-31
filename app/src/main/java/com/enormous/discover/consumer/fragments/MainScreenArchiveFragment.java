package com.enormous.discover.consumer.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.BookmarksActivity;
import com.enormous.discover.consumer.activities.HistoryActivity;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.models.Category;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainScreenArchiveFragment extends Fragment {

    private static final String TAG = "MainScreenArchiveFragment";
    private ListView categoriesListView;
    private ArrayList<Category> categories;
    private CategoriesListViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_screen_search, container, false);
        findViews(view);

        //add header to categoriesListView
        View header = inflater.inflate(R.layout.listview_categories_header, null);
        categoriesListView.addHeaderView(header);

        //set categoriesListView adapter
        adapter = new CategoriesListViewAdapter();
        categoriesListView.setAdapter(adapter);

        getCategoryCounts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBookmarksCount();
    }

    private void findViews(View view) {
        categoriesListView = (ListView) view.findViewById(R.id.categoriesListView);
    }

    /**
     * this method fetches the number of items in each category and then notifies the adapter when done
     */
    private void getCategoryCounts() {
        if (Utils.isConnectedToInternet(getActivity())) {
            ArrayList<String> userHistory = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("userHistory");
            if (userHistory == null) {
                return;
            }
            else {
                ParseQuery<ParseUser> query = MainScreenActivity.CURRENT_USER.getQuery();
                query.whereContainedIn("UUID", userHistory);
                query.findInBackground(new FindCallback<ParseUser>() {

                    @Override
                    public void done(List<ParseUser> parseUsers, ParseException e) {
                        if (e == null) {
                            resetCategoryCounts();

                            for (ParseUser parseUser : parseUsers) {
                                for (int i = 1; i < categories.size(); i++) {
                                    Category category = categories.get(i);
                                    if (category.getName().toLowerCase(Locale.ENGLISH).equals(parseUser.getString("brandCategory"))) {
                                        category.setCount(category.getCount() + 1);
                                    }
                                }
                            }

                            adapter.notifyDataSetChanged();
//                            Log.d(TAG, "categoryCounts completed");
                        }
                        else {
//                            Log.e(TAG, "getCategoryCounts error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * This method fetches the number of bookmarks a user has in total (product + brand bookmarks) and then notifies the adapter when done
     */
    private void getBookmarksCount() {
        ArrayList<String> brandBookmarks = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("brandBookmarks");
        ArrayList<String> productBookmarks = (ArrayList<String>) MainScreenActivity.CURRENT_USER.get("productBookmarks");
        int brandBookmarksSize = (brandBookmarks == null) ? 0 : brandBookmarks.size();
        int productBookmarksSize = (productBookmarks == null) ? 0 : productBookmarks.size();
        categories.get(0).setCount(brandBookmarksSize + productBookmarksSize);
        adapter.notifyDataSetChanged();
    }

    private void resetCategoryCounts() {
        for (int i = 1; i < categories.size(); i++) {
            categories.get(i).setCount(0);
        }
    }

    private class CategoriesListViewAdapter extends BaseAdapter implements OnItemClickListener {

        public CategoriesListViewAdapter() {
            //initialize and add categories
            categories = new ArrayList<Category>();
            categories.add(new Category("Bookmarks", 0));
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
             
            //set onItemClickListener on categoriesListView
            categoriesListView.setOnItemClickListener(this);
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

        @Override
        public View getView(int pos, View convertView, ViewGroup container) {
            View row = convertView;
            ViewHolder holder = null;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.listview_categories_item, container, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            else {
                holder = (ViewHolder) row.getTag();
            }

            //set bold text style for bookmarks
            if (pos == 0) {
                holder.categoryNameTextView.setTypeface(Typeface.create("sans-serif-regular", Typeface.BOLD));
            }
            else {
                holder.categoryNameTextView.setTypeface(Typeface.create("sans-serif-regular", Typeface.NORMAL));
            }

            //set card background for last item in list
            Resources res = getResources();
            if (pos == categories.size()-1) {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_bottom));
            }
            else {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey));
            }

            Category category = categories.get(pos);
            holder.categoryNameTextView.setText(category.getName());
            ImageLoader.getInstance().displayImage(getActivity(), getResources().getIdentifier(category.getName().toLowerCase().replace(" ", ""), "drawable", getActivity().getPackageName()) + "", holder.categoryThumbnailImageView, true, 300, 300, 0);
            if (category.getCount() > 0) {
                holder.categoryCountTextView.setText(category.getCount()+"");
                holder.categoryCountTextView.setVisibility(View.VISIBLE);
            }
            else {
                holder.categoryCountTextView.setVisibility(View.INVISIBLE);
            }

            return row;
        }

        @Override
        public void onItemClick(AdapterView<?> adv, View v, int pos,
                                long id) {
            if (pos != 0) {
                if (pos == 1) {
                    //start BookmarksActivity
                    Intent goToBookmarks = new Intent(getActivity(), BookmarksActivity.class);
                    startActivity(goToBookmarks);
                    getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                }
                else {
                    //start HistoryActivity
                    Intent goToHistoryActivity = new Intent(getActivity(), HistoryActivity.class);
                    goToHistoryActivity.putExtra("categoryConstraint", categories.get(pos-1).getName());
                    startActivity(goToHistoryActivity);
                    getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                }
            }
        }
    }


    private static class ViewHolder {
        TextView categoryNameTextView;
        ImageView categoryThumbnailImageView;
        TextView categoryCountTextView;

        public ViewHolder(View row) {
            categoryNameTextView = (TextView) row.findViewById(R.id.categoryNameTextView);
            categoryThumbnailImageView = (ImageView) row.findViewById(R.id.categoryThumbnailImageView);
            categoryCountTextView = (TextView) row.findViewById(R.id.categoryCountTextView);
        }
    }

}
