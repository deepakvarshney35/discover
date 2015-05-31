package com.enormous.pkpizzas.consumer.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.activities.BrandInfoActivity;
import com.enormous.pkpizzas.consumer.activities.ChatMessageActivity;
import com.enormous.pkpizzas.consumer.activities.MainScreenActivity;
import com.enormous.pkpizzas.consumer.adapters.BrandsListViewAdapter;
import com.enormous.pkpizzas.consumer.asynctasks.GetBrandsTask;
import com.enormous.pkpizzas.consumer.common.ImageLoader;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.enormous.pkpizzas.consumer.models.Brand;
import com.enormous.pkpizzas.consumer.models.BrandParse;
import com.enormous.pkpizzas.consumer.models.ObservableScrollView;
import com.enormous.pkpizzas.consumer.services.BeaconRangingService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainScreenHomeFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {


    // Global variable to hold the current location
    public static Location mCurrentLocation = new Location("");
    LocationClient mLocationClient;
    private LocationManager locManager;
    private final String TAG = "MainScreenHomeFragment";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView brandsListView, listViewLocalBrands;
    public TextView headerTitle, enableBT;
    public LinearLayout btToast;
    private LinearLayout progressLinearLayout;
    private LinearLayout noBluetoothLinearLayout;
    private LinearLayout nothingFoundHereLinearLayout;
    LinearLayout noInternetLinearLayout;
    private BluetoothAdapter bAdapter;
    private BrandsListViewAdapter brandsListViewAdapter;
    private ArrayList<Brand> brands, localBrands = null;
    public static Brand SELECTED_BRAND;
    private Timer timer;
    private UUIDReceiver uuidReceiver;
    private IntentFilter filter;
    private ArrayList<String> UUIDS;
    private ArrayList<String> userHistory;
    private Handler handler;
    LocalBrandsViewAdapter localBrandsViewAdapter;

    private boolean gps_enabled, network_enabled;
    ProgressBar progressBarLocalBrands;
    private boolean localBrandsRefreshed = false;
    private int resultCounter = 0;
    private List<ParseObject> ParsePlaces = new ArrayList<>();
    private ObservableScrollView mScrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_screen_main, container, false);
        findViews(view);

        btToast.setVisibility(View.GONE);

        mLocationClient = new LocationClient(getActivity(), this, this);
        //get default bluethooth adapter
        bAdapter = BluetoothAdapter.getDefaultAdapter();

        //initialize the handler associated with this thread's message queue
        handler = new Handler();

        //initialize UUIDS list
        UUIDS = new ArrayList<String>();

        //set click listeners

        /*mScrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {


                if ((oldy >= y)||(oldy<0)) {
                    //show the buttons
                    MainScreenActivity.showFAB();
                } else {
                    //hide the buttons
                    MainScreenActivity.hideFAB();
                }

            }
        });*/

        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.noBluetoothLinearLayout:
                        if (!bAdapter.isEnabled()) {
                            bAdapter.enable();
                            final ProgressDialog dummyProgress = new ProgressDialog(getActivity());
                            dummyProgress.setMessage("Turning bluetooth on...");
                            dummyProgress.show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dummyProgress.dismiss();
                                }
                            }, 2500);
                            scheduleRequestTimer();
                        }
                        noBluetoothLinearLayout.setVisibility(View.GONE);
                        break;
                    case R.id.noInternetLinearLayout:
                        refreshBrands();
                        break;
                }
            }
        };
        noInternetLinearLayout.setOnClickListener(clickListener);

        //intialize UUIDRevceiver broadcastreceiver
        uuidReceiver = new UUIDReceiver();
        filter = new IntentFilter(BeaconRangingService.ACTION_SEND_UUIDS);

        //add ability to pull to refresh
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        });

        //initalize brands and set brandsListView adapter
        brands = new ArrayList<Brand>();
        brandsListViewAdapter = new BrandsListViewAdapter(getActivity(), brands);
        brandsListView.setAdapter(brandsListViewAdapter);

        progressLinearLayout = (LinearLayout) view.findViewById(R.id.progressLinearLayout);
        noBluetoothLinearLayout = (LinearLayout) view.findViewById(R.id.noBluetoothLinearLayout);
        nothingFoundHereLinearLayout = (LinearLayout) view.findViewById(R.id.nothingFoundLinearLayout);
        listViewLocalBrands = (ListView) view.findViewById(R.id.listViewLocalBrands);

        localBrands = new ArrayList<Brand>();
        localBrandsViewAdapter = new LocalBrandsViewAdapter();
        listViewLocalBrands.setAdapter(localBrandsViewAdapter);

        headerTitle = (TextView) view.findViewById(R.id.headerTitle);
        //headerTitle.setVisibility(View.GONE);

        Intent beaconRangingService = new Intent(getActivity(), BeaconRangingService.class);
        getActivity().startService(beaconRangingService);

        enableBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.enableBT:
                        btToast.setVisibility(View.GONE);
                        if (!bAdapter.isEnabled()) {
                            bAdapter.enable();
                            final ProgressDialog dummyProgress = new ProgressDialog(getActivity());
                            dummyProgress.setMessage("Turning bluetooth on...");
                            dummyProgress.show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dummyProgress.dismiss();
                                }
                            }, 2500);
                            scheduleRequestTimer();
                        }
                        noBluetoothLinearLayout.setVisibility(View.GONE);
                        break;
                }
            }
        });

        noBluetoothLinearLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch (v.getId()) {
                    case R.id.noBluetoothLinearLayout:
                        if (!bAdapter.isEnabled()) {
                            bAdapter.enable();
                            final ProgressDialog dummyProgress = new ProgressDialog(getActivity());
                            dummyProgress.setMessage("Turning bluetooth on...");
                            dummyProgress.show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dummyProgress.dismiss();
                                }
                            }, 2500);
                            scheduleRequestTimer();
                        }
                        noBluetoothLinearLayout.setVisibility(View.GONE);
                        break;
                }
            }
        });

        return view;
    }

    private void refreshLocalBrands() {
        if (mCurrentLocation != null) {
            if (localBrands.size() == 0) {
                localBrandsRefreshed = true;
                progressBarLocalBrands.setVisibility(View.VISIBLE);
                headerTitle.setVisibility(View.VISIBLE);
                ParseQuery<ParseUser> queryBrands = ParseUser.getQuery();
                ParseGeoPoint point = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                queryBrands.whereWithinKilometers("brandGeoPoint", point, 50.0);
                queryBrands.whereNear("brandGeoPoint", point);
                queryBrands.findInBackground(new FindCallback<ParseUser>() {

                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        // TODO Auto-generated method stub
                        if (e == null) {
                            if (objects.size() > 0) {
                                localBrands.clear();
                                localBrandsViewAdapter.notifyDataSetChanged();
                                progressBarLocalBrands.setVisibility(View.GONE);
                                boolean present = false;
                                for (ParseUser user : objects) {
                                    present = false;
                                    for (int i = 0; i < brands.size(); i++) {
                                        if (user.getObjectId().equals(brands.get(i).getObjectId())) {
                                            present = true;
                                        }
                                    }
                                    if (!present) {
                                        if (BeaconRangingService.isMainScreenActivityVisible) {
                                            localBrands.add(new Brand(user.getObjectId(), user.getString("UUID"), user.getString("brandName"), user.getString("brandEmail"), user.getString("brandPhone"), user.getString("brandLocation"), (ArrayList<String>) user.get("brandTags"), user.getParseFile("brandCoverPicture").getUrl(), user.getString("brandWebsite"), user.getString("brandCategory"), user.getString("brandAbout")));
                                            localBrandsViewAdapter.notifyDataSetChanged();
                                            setListViewHeightBasedOnChildren(listViewLocalBrands, 0);
                                            headerTitle.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                                if (BeaconRangingService.isMainScreenActivityVisible) {
                                    refreshParseBrands();
                                    new GetPlaces(getActivity().getBaseContext(), "", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()).execute();
                                }
                            } else {
                                if (BeaconRangingService.isMainScreenActivityVisible) {
                                    refreshParseBrands();
                                    new GetPlaces(getActivity().getBaseContext(), "", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()).execute();
                                }
                            }
                        } else {
                            //new GetPlaces(getActivity().getBaseContext(),"",mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude() ).execute();
                        }
                    }
                });
            }
        }
    }

    private void refreshParseBrands() {
        if (mCurrentLocation != null) {
            ParseQuery<ParseObject> queryParseBrands = ParseQuery.getQuery("ParsePlacesStore");
            ParseGeoPoint point = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            queryParseBrands.whereWithinKilometers("brandGeoPoint", point, 50.0);
            queryParseBrands.whereNear("brandGeoPoint", point);
            queryParseBrands.setLimit(100);
            queryParseBrands.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        if (parseObjects.size() > 0) {
                            if (BeaconRangingService.isMainScreenActivityVisible) {
                                for (int i = 0; i < parseObjects.size(); i++) {
                                    localBrands.add(new Brand(parseObjects.get(i).getObjectId(), parseObjects.get(i).getString("UUID"), parseObjects.get(i).getString("name"), parseObjects.get(i).getString("email"), parseObjects.get(i).getString("phone"), parseObjects.get(i).getString("location"), (ArrayList<String>) parseObjects.get(i).get("tags"), parseObjects.get(i).getString("pictureUrl"), parseObjects.get(i).getString("website"), parseObjects.get(i).getString("categoryName"), parseObjects.get(i).getString("about")));
                                }
                                localBrandsViewAdapter.notifyDataSetChanged();
                                setListViewHeightBasedOnChildren(listViewLocalBrands, 1000);
                                headerTitle.setVisibility(View.VISIBLE);
                                progressBarLocalBrands.setVisibility(View.GONE);
                            }
                        }

                    }

                }
            });

        }
    }


    private void findViews(View view) {
        enableBT = (TextView) view.findViewById(R.id.enableBT);
        btToast = (LinearLayout) view.findViewById(R.id.btToast);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        brandsListView = (ListView) view.findViewById(R.id.listViewBrands);
        noInternetLinearLayout = (LinearLayout) view.findViewById(R.id.noInternetLinearLayout);
        progressBarLocalBrands = (ProgressBar) view.findViewById(R.id.progressBarLocalBrands);
        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
    }

    private void refreshBrands() {
        noInternetLinearLayout.setVisibility(View.GONE);
        if (Utils.isConnectedToInternet(getActivity())) {
            //only pass progressLinearLayout if the listview doesn't contain any items/brands
            if (brands.size() == 0) {
                GetBrandsTask getBrandsTask = new GetBrandsTask(UUIDS, null, brands, brandsListViewAdapter, swipeRefreshLayout, progressLinearLayout, nothingFoundHereLinearLayout, brandsListView, null);
                getBrandsTask.execute();
            } else {
                GetBrandsTask getBrandsTask = new GetBrandsTask(UUIDS, null, brands, brandsListViewAdapter, swipeRefreshLayout, null, nothingFoundHereLinearLayout, brandsListView, null);
                getBrandsTask.execute();
            }
        } else {
            noInternetLinearLayout.setVisibility(View.VISIBLE);
            nothingFoundHereLinearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //check for bluetooth connection
        if (bAdapter.isEnabled()) {
            scheduleRequestTimer();
        } else {

            btToast.setVisibility(View.VISIBLE);

            final Handler handlerBT = new Handler();
            Timer timerBT = new Timer();
            TimerTask timerTaskBT = new TimerTask() {
                public void run() {
                    handlerBT.post(new Runnable() {
                        public void run() {
                            btToast.setVisibility(View.GONE);
                        }

                    });


                }
            };
            timerBT.schedule(timerTaskBT, 10000);

			/*progressLinearLayout.setVisibility(View.GONE);
            noInternetLinearLayout.setVisibility(View.GONE);
			nothingFoundHereLinearLayout.setVisibility(View.GONE);
			noBluetoothLinearLayout.setVisibility(View.VISIBLE);*/


        }
        //register UUIDReveiver broadcastreceiver
        getActivity().registerReceiver(uuidReceiver, filter);

        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setMessage("Location Services is not enabled. Please turn it on.");
            builder.setPositiveButton("Turn on now", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    //
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            Dialog dialog = builder.create();
            dialog.show();

        } else {
            mLocationClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop the timer
        if (timer != null) {
            timer.cancel();
        }

        //unregister UUIDRevceiver broadcastreceiver
        getActivity().unregisterReceiver(uuidReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent beaconRangingService = new Intent(getActivity(), BeaconRangingService.class);
        getActivity().stopService(beaconRangingService);
    }

    private void scheduleRequestTimer() {
        //initialize and schedule a Timer which requests for UUIDS every 5 seconds
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                getActivity().sendBroadcast(new Intent(BeaconRangingService.ACTION_REQUEST_UUIDS));
            }
        }, 0, 5000);

        if (brandsListView.getCount() == 0) {
            nothingFoundHereLinearLayout.setVisibility(View.VISIBLE);
        } else {
            brandsListView.setVisibility(View.VISIBLE);
        }
    }

    private class UUIDReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {
            ArrayList<String> newUUIDS = new ArrayList<String>(intent.getExtras().getStringArrayList("UUIDS"));
            if (UUIDS.containsAll(newUUIDS) && newUUIDS.containsAll(UUIDS)) {
                //do nothing as same UUIDS were detected again
            } else {
                //                Log.d(TAG, "NEW UUIDS DETECTED");
                //                Log.d(TAG, newUUIDS.toString());
                UUIDS = new ArrayList<String>(newUUIDS);
                refreshBrands();
            }
        }

    }

    private class LocalBrandsViewAdapter extends BaseAdapter {

        public LocalBrandsViewAdapter() {/*listViewLocalBrands.setOnItemClickListener(this)*/
            ;
        }

        @Override
        public int getCount() {
            return localBrands.size();
        }

        @Override
        public Object getItem(int pos) {
            return localBrands.get(pos);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;
            if (row == null) {
                row = getActivity().getLayoutInflater().inflate(R.layout.listview_search_item, parent, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            Brand brand = localBrands.get(pos);
            Resources res = getResources();
            //set card background according to position
            //Log.i("TEST", pos + " pos");
            if (pos == 0) {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_top));
            } else if (pos == localBrands.size() - 1) {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey_bottom));
            } else {
                row.setBackground(res.getDrawable(R.drawable.listview_item_selector_white_grey));
            }
            ImageLoader.getInstance().displayImage(getActivity().getApplicationContext(), res.getIdentifier(brand.getCategoryName(), "drawable", getActivity().getPackageName()) + "", holder.brandCategoryImageView, true, 300, 300, 0);
            holder.brandLocationTextView.setText(brand.getLocation());
            holder.brandNameTextView.setText(brand.getName());
            addTags(res, brand.getTags(), holder.tagsLinearLayout);

            holder.localBrandChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!localBrands.get(pos).getUUID().equals("GOOGLE")) {
                        Intent goToChat = new Intent(getActivity(), ChatMessageActivity.class);
                        goToChat.putExtra("merchantUserId", localBrands.get(pos).getObjectId());
                        goToChat.putExtra("merchantName", localBrands.get(pos).getName());
                        goToChat.putExtra("merchantPic", localBrands.get(pos).getCoverPictureUrl());
                        goToChat.putExtra("merchantPhone", localBrands.get(pos).getPhone());
                        goToChat.putExtra("merchantEmail", localBrands.get(pos).getEmail());
                        getActivity().startActivity(goToChat);
                    } else {
                        Toast.makeText(getActivity(), "Publisher not on Discover yet.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            holder.localBrandInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Date date = new Date();
                    //Save Check in
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
                    query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
                    query.whereEqualTo("brandObjectId", localBrands.get(pos).getObjectId());
                    query.findInBackground(new FindCallback<ParseObject>() {

                        @Override
                        public void done(List<ParseObject> checkinList, ParseException e) {
                            // TODO Auto-generated method stub
                            if (e == null) {
                                if (checkinList.size() == 0) {
                                    ParseObject checkin = new ParseObject("Checkin");
                                    checkin.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
                                    checkin.put("brandObjectId", localBrands.get(pos).getObjectId());
                                    checkin.put("brandName", localBrands.get(pos).getName());
                                    checkin.put("archive", false);
                                    checkin.put("type", "remote");
                                    checkin.put("date", date);
                                    checkin.put("fullName", ParseUser.getCurrentUser().get("firstName") + " " + ParseUser.getCurrentUser().get("lastName"));
                                    checkin.put("email", ParseUser.getCurrentUser().get("email"));
                                    checkin.put("phoneNumber", ParseUser.getCurrentUser().get("phoneNumber"));
                                    checkin.put("SharedOffer", 0);
                                    checkin.put("CartItems", 0);
                                    checkin.put("UUID", localBrands.get(pos).getUUID());
                                    if (ParseUser.getCurrentUser().get("profilePictureUrl") != null) {
                                        checkin.put("profilePictureUrl", ParseUser.getCurrentUser().get("profilePictureUrl"));
                                    }
                                    checkin.saveInBackground();

                                    //Add UUID to userHistory
                                    userHistory = (ArrayList<String>) ParseUser.getCurrentUser().get("userHistory");
                                    userHistory.add(localBrands.get(pos).getUUID());
                                    MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
                                    MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Log.d(TAG, "user history updated successfully");
                                            } else {
                                                Log.d(TAG, "user history update error: " + e.getMessage());
                                            }
                                        }
                                    });


                                } else {
                                    for (ParseObject chkin : checkinList) {
                                        chkin.put("archive", false);
                                        chkin.put("date", date);
                                        chkin.saveInBackground();

                                        //Add UUID to userHistory
                                        userHistory = (ArrayList<String>) ParseUser.getCurrentUser().get("userHistory");
                                        if (userHistory.contains(localBrands.get(pos).getUUID())) {
                                            //update to new location
                                            userHistory.remove(localBrands.get(pos).getUUID());
                                            userHistory.add(localBrands.get(pos).getUUID());

                                            MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
                                            MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        Log.d(TAG, "user history updated successfully");
                                                    } else {
                                                        Log.d(TAG, "user history update error: " + e.getMessage());
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    });

                    Intent goToBrandInfo = new Intent(getActivity(), BrandInfoActivity.class);
                    goToBrandInfo.putExtra("selectedBrand", localBrands.get(pos));
                    startActivity(goToBrandInfo);
                    getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                }
            });

            return row;
        }

		/*@Override
        public void onItemClick(AdapterView<?> adv, View v, final int pos,long arg3) {
            switch (v.getId()){
                case R.id.localBrandInfo:
                    final Date date = new Date();
                    //Save Check in
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
                    query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
                    query.whereEqualTo("brandObjectId", localBrands.get(pos).getObjectId());
                    query.findInBackground(new FindCallback<ParseObject>() {

                        @Override
                        public void done(List<ParseObject> checkinList, ParseException e) {
                            // TODO Auto-generated method stub
                            if(e==null){
                                if(checkinList.size()==0){
                                    ParseObject checkin = new ParseObject("Checkin");
                                    checkin.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
                                    checkin.put("brandObjectId",  localBrands.get(pos).getObjectId());
                                    checkin.put("brandName",  localBrands.get(pos).getName());
                                    checkin.put("archive", false);
                                    checkin.put("type","remote");
                                    checkin.put("date", date);
                                    checkin.put("fullName", ParseUser.getCurrentUser().get("firstName")+" "+ParseUser.getCurrentUser().get("lastName"));
                                    checkin.put("email",ParseUser.getCurrentUser().get("email"));
                                    checkin.put("phoneNumber", ParseUser.getCurrentUser().get("phoneNumber"));
                                    checkin.put("SharedOffer", 0);
                                    checkin.put("CartItems", 0);
                                    checkin.put("UUID", localBrands.get(pos).getUUID());
                                    if(ParseUser.getCurrentUser().get("profilePictureUrl")!=null){
                                        checkin.put("profilePictureUrl", ParseUser.getCurrentUser().get("profilePictureUrl"));
                                    }
                                    checkin.saveInBackground();

                                    //Add UUID to userHistory
                                    userHistory = (ArrayList<String>) ParseUser.getCurrentUser().get("userHistory");
                                    Log.d("Sanat","0 "+localBrands.get(pos).getUUID());
                                    userHistory.add(localBrands.get(pos).getUUID());
                                    MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
                                    MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Log.d(TAG, "user history updated successfully");
                                            }
                                            else {
                                                Log.d(TAG, "user history update error: " + e.getMessage());
                                            }
                                        }
                                    });


                                } else {
                                    for(ParseObject chkin : checkinList){
                                        chkin.put("archive", false);
                                        chkin.put("date", date);
                                        chkin.saveInBackground();

                                        //Add UUID to userHistory
                                        userHistory = (ArrayList<String>) ParseUser.getCurrentUser().get("userHistory");
                                        Log.d("Sanat","1 "+localBrands.get(pos).getUUID());
                                        Log.d("Sanat","3 "+userHistory);
                                        if(userHistory.contains(localBrands.get(pos).getUUID())){
                                            Log.d("Sanat","2 "+localBrands.get(pos).getUUID());
                                            //update to new location
                                            userHistory.remove(localBrands.get(pos).getUUID());
                                            userHistory.add(localBrands.get(pos).getUUID());

                                            MainScreenActivity.CURRENT_USER.put("userHistory", userHistory);
                                            MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        Log.d(TAG, "user history updated successfully");
                                                    }
                                                    else {
                                                        Log.d(TAG, "user history update error: " + e.getMessage());
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    });

                    Intent goToBrandInfo = new Intent(getActivity(), BrandInfoActivity.class);
                    goToBrandInfo.putExtra("selectedBrand", localBrands.get(pos));
                    startActivity(goToBrandInfo);
                    getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                    break;
                case R.id.localBrandChat:
                    if(!brands.get(pos).getObjectId().equals("GOOGLE")){
                        Intent goToChat = new Intent(getActivity(), ChatMessageActivity.class);
                        goToChat.putExtra("merchantUserId", brands.get(pos).getObjectId());
                        goToChat.putExtra("merchantName", brands.get(pos).getName());
                        goToChat.putExtra("merchantPic", brands.get(pos).getCoverPictureUrl());
                        goToChat.putExtra("merchantPhone", brands.get(pos).getPhone());
                        goToChat.putExtra("merchantEmail", brands.get(pos).getEmail());
                        getActivity().startActivity(goToChat);
                    } else{
                        Toast.makeText(getActivity(), "Publisher not on Discover yet.", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
		}*/

        private void addTags(Resources res, ArrayList<String> tags, LinearLayout container) {
            container.removeAllViews();
            float marginRight = Utils.convertDpToPixel(7);
            if (tags != null) {
                LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lParams.setMargins(0, 0, (int) marginRight, 0);
                for (String tag : tags) {
                    TextView textView = new TextView(getActivity());
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
        ImageView localBrandChat;
        RelativeLayout localBrandInfo;

        public ViewHolder(View row) {
            brandCategoryImageView = (ImageView) row.findViewById(R.id.brandCategoryImageView);
            brandNameTextView = (TextView) row.findViewById(R.id.brandNameTextView);
            brandLocationTextView = (TextView) row.findViewById(R.id.brandLocationTextView);
            tagsLinearLayout = (LinearLayout) row.findViewById(R.id.tagsLinearLayout);
            localBrandChat = (ImageView) row.findViewById(R.id.localBrandChat);
            localBrandInfo = (RelativeLayout) row.findViewById(R.id.localBrandInfo);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps_enabled || network_enabled) {
            mCurrentLocation = mLocationClient.getLastLocation();
            //mCurrentLocation.setLatitude(28.5105106);
            //mCurrentLocation.setLongitude(77.0501282);
            if (!localBrandsRefreshed)
                refreshLocalBrands();
        }
    }

    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub

    }

    class GetPlaces extends AsyncTask<BrandParse, Void, ArrayList<BrandParse>> {
        Context mContext;
        String mToken;
        double latitude = 0, longitude = 0;

        public GetPlaces(Context aContext, String aToken, double lat, double lon) {
            super();
            mContext = aContext;
            mToken = aToken;
            latitude = lat;
            longitude = lon;
        }

        @Override
        // three dots is java for an array of double
        protected ArrayList<BrandParse> doInBackground(BrandParse... args) {

            Log.d("gottaGo", "doInBackground");

            ArrayList<Brand> predictionsArr = new ArrayList<Brand>();
            ArrayList<BrandParse> predictionsArrNew = new ArrayList<BrandParse>();

            double radius = 50000;
            //String key="AIzaSyBzKW9Pp5qHb7vV50iRkCBp6ZDFST0ERT8";
            String key = "AIzaSyB3iHBPT4rstXdka5dITdJO2fKs9we5mQ8";
            mToken = "";

            String PlaceName = "";
            double Lat = 0, Long = 0;
            try {
                URL googlePlaces = new URL(
                        // URLEncoder.encode(url,"UTF-8");
                        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&rankby=distance&types=" + "country"
                                + "%7Cfood"
                                + "%7Crestaurant"
                                + "%7Cbar"
                                + "%7Ccafe"
                                + "%7Cclothing_store"
                                + "%7Cmeal_delivery"
                                + "%7Cgrocery_or_supermarket"
                                + "%7Cmeal_takeaway"
                                + "%7Cconvenience_store"
                                + "%7Cdentist"
                                + "%7Cdoctor"
                                + "%7Cdepartment_store"
                                + "%7Celectronics_store"
                                + "%7Celectrician"
                                + "%7Cbakery"
                                + "%7Camusement_park"
                                + "%7Cbank"
                                + "%7Cbeauty_salon"
                                + "%7Cmovie_theater"
                                + "%7Cbook_store"
                                + "&sensor=true&key=" + key + "&pagetoken=" + mToken);
                URLConnection tc = googlePlaces.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        tc.getInputStream()));

                String line = "";
                String placeid = "";
                StringBuffer sb = new StringBuffer();
                //take Google's legible JSON and turn it into one big string.
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                //turn that string into a JSON object
                for (int i = 0; i < 20; i++) {
                    JSONObject results = new JSONObject(sb.toString());
                    String category = "food";
                    if ((JSONArray) results.get("results") != null) {
                        Long = ((JSONArray) results.get("results")).getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        Lat = ((JSONArray) results.get("results")).getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        Log.d("TAG", Lat + " " + Long);
                        PlaceName = ((JSONArray) results.get("results")).getJSONObject(i).getString("name");
                        placeid = ((JSONArray) results.get("results")).getJSONObject(i).getString("place_id");
                        JSONArray categoryArray = ((JSONArray) results.get("results")).getJSONObject(i).getJSONArray("types");
                        category = categoryArray.getString(0);
                    }
                    if (category.equals("movie_theater")) {
                        category = "movies";
                    } else if (category.equals("book_store")) {
                        category = "bookstore";
                    } else if (category.equals("clothing_store")) {
                        category = "apparels";
                    } else if (category.equals("food")) {
                        category = "food";
                    } else if (category.equals("cafe")) {
                        category = "cafe";
                    } else if (category.equals("restaurant")) {
                        category = "food";
                    } else if (category.equals("meal_delivery")) {
                        category = "food";
                    } else if (category.equals("meal_takeaway")) {
                        category = "food";
                    } else if (category.equals("convenience_store")) {
                        category = "conveniencestore";
                    } else if (category.equals("dentist")) {
                        category = "dentist";
                    } else if (category.equals("doctor")) {
                        category = "doctor";
                    } else if (category.equals("department_store")) {
                        category = "departmentstore";
                    } else if (category.equals("electronics_store")) {
                        category = "electronicsstore";
                    } else if (category.equals("beauty_salon")) {
                        category = "beautysalon";
                    } else if (category.equals("electrician")) {
                        category = "electrician";
                    } else if (category.equals("grocery_or_supermarket")) {
                        category = "grocery";
                    } else {
                        category = "food";
                    }
                    mToken = results.getString("next_page_token");
                    String line1 = "";
                    URL googlePlacesInfo = new URL(
                            "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeid + "&key=" + key);
                    URLConnection tc1 = googlePlacesInfo.openConnection();
                    BufferedReader in1 = new BufferedReader(new InputStreamReader(
                            tc1.getInputStream()));
                    StringBuffer sb1 = new StringBuffer();
                    //take Google's legible JSON and turn it into one big string.
                    while ((line1 = in1.readLine()) != null) {
                        sb1.append(line1);
                    }
                    //turn that string into a JSON object
                    JSONObject results1 = new JSONObject(sb1.toString());
                    //String Phone = ((JSONObject)results1.get("result")).getString("formatted_phone_number");
                    //Log.d("Sanat",""+Phone);
                    String Address = "";
                    String icon = "";
                    if (!results1.getString("status").equals("OVER_QUERY_LIMIT")) {
                        if (((JSONObject) results1.get("result")).getString("formatted_address") != null) {
                            Address = ((JSONObject) results1.get("result")).getString("formatted_address");
                        }
                        if (((JSONObject) results1.get("result")).getString("icon") != null) {
                            icon = ((JSONObject) results1.get("result")).getString("icon");
                        }
                    }
                    ArrayList<String> tags = new ArrayList<String>();
                    //predictionsArr.add(new Brand(placeid, "GOOGLE", PlaceName, "", "", Address, tags, icon, "", category, ""));
                    predictionsArrNew.add(new BrandParse(placeid, "GOOGLE", PlaceName, "", "", Address, tags, icon, "", category, "", Lat, Long));
                }
            } catch (IOException e) {

                Log.e("YourApp", "GetPlaces : doInBackground", e);

            } catch (JSONException e) {

                Log.e("YourApp", "GetPlaces : doInBackground", e);

            }

            return predictionsArrNew;

        }

        //then our post

        @Override
        protected void onPostExecute(final ArrayList<BrandParse> result) {
            Log.d("Sanat", "Hi");

            /*if(BeaconRangingService.isMainScreenActivityVisible){
                if(result.size()>0){
                    localBrands.addAll(result);
                    localBrandsViewAdapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(listViewLocalBrands,600);
                    headerTitle.setVisibility(View.VISIBLE);
                    progressBarLocalBrands.setVisibility(View.GONE);
                }
                if(localBrands.size()==0){
                    headerTitle.setVisibility(View.GONE);
                    progressBarLocalBrands.setVisibility(View.GONE);
                }
            }
            progressBarLocalBrands.setVisibility(View.GONE);
            if(localBrands.size()==0){
                headerTitle.setVisibility(View.GONE);
            }*/

            if (result.size() > 0) {
                for (resultCounter = 0; resultCounter < result.size(); resultCounter++) {
                    ParseObject ParsePlace = new ParseObject("ParsePlaces");
                    ParsePlace.put("placeId", result.get(resultCounter).getObjectId());
                    ParsePlace.put("UUID", "GOOGLE");
                    ParsePlace.put("name", result.get(resultCounter).getName());
                    ParsePlace.put("email", result.get(resultCounter).getEmail());
                    ParsePlace.put("phone", result.get(resultCounter).getPhone());
                    ParsePlace.put("location", result.get(resultCounter).getLocation());
                    ParsePlace.put("tags", result.get(resultCounter).getTags());
                    ParsePlace.put("pictureUrl", result.get(resultCounter).getCoverPictureUrl());
                    ParsePlace.put("website", result.get(resultCounter).getWebsite());
                    ParsePlace.put("categoryName", result.get(resultCounter).getCategoryName());
                    ParsePlace.put("about", result.get(resultCounter).getAbout());
                    ParsePlace.put("provider", ParseUser.getCurrentUser().getObjectId());
                    ParseGeoPoint point = new ParseGeoPoint(result.get(resultCounter).getLatitude(), result.get(resultCounter).getLongitude());
                    ParsePlace.put("brandGeoPoint", point);
                    ParsePlaces.add(ParsePlace);
                }
                ParseObject.saveAllInBackground(ParsePlaces, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) callParseCloud();
                        Log.d("Sanat", "Cloud function called");
                    }
                });

            }
        }
    }

    private void callParseCloud() {
        ParseCloud.callFunctionInBackground("deleteDupes", new HashMap<String, Object>(), new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    //Cloud function executed
                    Log.d("Sanat", "Cloud function executed, Response " + result);
                }
            }
        });
    }

    /**
     * * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView  ***
     */
    public void setListViewHeightBasedOnChildren(ListView listView, int threshold) {
        //ListAdapter listAdapter = listView.getAdapter();
        if (localBrandsViewAdapter == null)
            return;

        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < localBrandsViewAdapter.getCount(); i++) {
            view = localBrandsViewAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        LayoutParams params = listView.getLayoutParams();
        //Threshold is used as it was not calculating height of list returned by google places correctly.
        params.height = threshold + totalHeight + (listView.getDividerHeight() * (localBrandsViewAdapter.getCount() + 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
