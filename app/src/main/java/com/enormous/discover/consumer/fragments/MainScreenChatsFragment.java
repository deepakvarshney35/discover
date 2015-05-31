package com.enormous.discover.consumer.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.discover.consumer.DiscoverApp;
import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.ChatMessageActivity;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.activities.ProfileActivity;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.models.Brand;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainScreenChatsFragment extends Fragment implements OnClickListener {

	private final String TAG = "MainScreenProfileFragment";
	private ImageView userPictureImageView;
	private FrameLayout userPictureImageViewContainer;
	private TextView fullNameUserText;
    //private TextView emailUserText;
    //private TextView phoneUserText;
	//private ImageButton overflowButton;
	private PopupMenu popupMenu;
    private ImageView profileButton;
	
	private ProgressDialog imageUploadProgress;
    private boolean mVisible;



	//requestCodes for image chooser intents
	private int CHOOSE_CAMERA = 1;
	private int CHOOSE_GALLERY = 2;

	private String email;
	private String firstName;
	private String lastName;
    private String phone;
	
	private String userDomain;

	Handler handler;
	private ArrayList<Brand> customers ;
	CustomersListViewAdapter adapter;
	ListView clientProfileList;
	LayoutInflater inflater;
	ProgressBar progressBarLocalBrands;
	TextView noChatsPlaceholder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mainscreen_profile_2, container, false);
		findViews(view);

		//set onClick listeners
		userPictureImageViewContainer.setOnClickListener(this);
		//overflowButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);
		
		clientProfileList = (ListView)view.findViewById(android.R.id.list);
		progressBarLocalBrands = (ProgressBar)view.findViewById(R.id.customerRefreshBar);
		noChatsPlaceholder = (TextView)view.findViewById(R.id.noChatsPlaceholder);
		noChatsPlaceholder.setVisibility(View.GONE);
		//get user details
		email = MainScreenActivity.CURRENT_USER.getEmail();
		firstName = MainScreenActivity.CURRENT_USER.getString("firstName");
		lastName = MainScreenActivity.CURRENT_USER.getString("lastName");
        phone = MainScreenActivity.CURRENT_USER.getString("phoneNumber");

		if (email != null) {
			String[] split = email.split("@");
			userDomain = split[1];
		}
		else {
			userDomain = "Twitter";
			email = "Not available";
		}
		fullNameUserText.setText(firstName + " " + lastName);
        //emailUserText.setText(email);
        //phoneUserText.setText(phone);

		//set user's profile picture
		setProfilePicture();

		customers = new ArrayList<Brand>();

		//add header to categoriesListView
		//View header = inflater.inflate(R.layout.listview_graph_header, null);
		//clientProfileList.addHeaderView(header);

		adapter = new CustomersListViewAdapter();
		clientProfileList.setAdapter(adapter);



		updateCustomers();


		//setUpPopupMenu();

		return view;
	}

    private void updateCustomers() {
        customers.clear();
        adapter.notifyDataSetChanged();
        progressBarLocalBrands.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatBrandList");
        query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
        //query.whereEqualTo("email",ParseUser.getCurrentUser().getString("email"));
        query.addDescendingOrder("updatedAt");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> results, ParseException e) {
                if(e==null){
                    ArrayList<String> dummy = new ArrayList<String>();
                    for (ParseObject result : results) {
                        if(!"GOOGLE".equals(result.getString("brandObjectId"))){
                            customers.add(new Brand(result.getString("brandObjectId"),"", result.getString("brandName"), result.getString("brandEmail"), result.getString("brandPhone"),"", dummy,  result.getString("brandPic"), "", "", ""));
                            //customers.add(new Brand(result.getString("brandObjectId"),"", result.getString("brandName"), result.getString("email"), "","", dummy, "", "", "", ""));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBarLocalBrands.setVisibility(View.GONE);
                    if(customers.size()==0){
                        clientProfileList.setVisibility(View.GONE);
                        noChatsPlaceholder.setVisibility(View.VISIBLE);
                    }else{
                        noChatsPlaceholder.setVisibility(View.GONE);
                    }
                }else{
                    clientProfileList.setVisibility(View.VISIBLE);
                    noChatsPlaceholder.setVisibility(View.GONE);
                    progressBarLocalBrands.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Error - Try Again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (!visible) {
            mVisible=false;
        }else{
            if (mVisible==false) {
                mVisible=true;
                updateCustomers();
            }

        }
    }

    private void findViews(View view) {
		userPictureImageViewContainer = (FrameLayout) view.findViewById(R.id.userPictureImageViewContainer);
		userPictureImageView = (ImageView) view.findViewById(R.id.userPictureImageView);
		fullNameUserText = (TextView) view.findViewById(R.id.fullNameUserText);
        //emailUserText = (TextView) view.findViewById(R.id.emailUserText);
        //phoneUserText = (TextView) view.findViewById(R.id.phoneUserText);
		//overflowButton = (ImageButton) view.findViewById(R.id.overflowButton);
        profileButton = (ImageView) view.findViewById(R.id.profileButton);
	}

	/*private void setUpPopupMenu() {
		popupMenu = new PopupMenu(getActivity(), overflowButton);
		popupMenu.inflate(R.menu.menu_profile);
		popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.ic_action_profile:
					//go to chats activity
					Intent goToChats = new Intent(getActivity(), ProfileActivity.class);
					startActivity(goToChats);
					break;
				case R.id.ic_action_logout:
					//clear whole cache and logout
					DownloadAndSaveDocumentTask.clearDocumentCache();
					ImageLoader.getInstance().clearImageCache();
					ParseUser.logOut();

					//go to intro activity
					Intent goToIntro = new Intent(getActivity(), IntroActivity.class);
					startActivity(goToIntro);
					getActivity().finish();
					break;
				case R.id.ic_action_clear_cache:
					ImageLoader.getInstance().clearImageCache();
					DownloadAndSaveDocumentTask.clearDocumentCache();
					Toast.makeText(getActivity(), "Cache successfully cleared", Toast.LENGTH_SHORT).show();
					break;
				case R.id.ic_action_deactivate_account:
					Builder builder = new Builder(getActivity());
					View customDialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_deactivate_account, null);
					Button yesButton = (Button) customDialog.findViewById(R.id.yesButton);
					Button noButton = (Button) customDialog.findViewById(R.id.noButton);
					final LinearLayout progressLinearLayout = (LinearLayout) customDialog.findViewById(R.id.progressLinearLayout);
					builder.setView(customDialog);
					final Dialog dialog = builder.create();
					dialog.show();

					OnClickListener buttonListener = new OnClickListener() {

						@Override
						public void onClick(View v) {
							switch(v.getId()) {
							case R.id.yesButton:
								progressLinearLayout.setVisibility(View.VISIBLE);
								MainScreenActivity.CURRENT_USER.deleteInBackground(new DeleteCallback() {

									@Override
									public void done(ParseException e) {
										if (e == null) {
											dialog.dismiss();
											//clear whole cache and logout
											DownloadAndSaveDocumentTask.clearDocumentCache();
											ImageLoader.getInstance().clearImageCache();
											ParseUser.logOut();

											//go to IntroActivity
											Intent goToIntro = new Intent(getActivity(), IntroActivity.class);
											startActivity(goToIntro);
											getActivity().overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
											getActivity().finish();
										}
										else {
											Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
											//                                                    Log.d("TEST", "Account deactivation error: " + e.getMessage());
										}
										progressLinearLayout.setVisibility(View.GONE);
									}
								});
								break;
							case R.id.noButton:
								dialog.dismiss();
								break;
							}
						}
					};
					yesButton.setOnClickListener(buttonListener);
					noButton.setOnClickListener(buttonListener);
					break;
				}

				return true;
			}
		});
	}*/

	private void setProfilePicture() {
		Resources res = getResources();
		float reqWidth = res.getDimension(R.dimen.profilepic_thumbnail_width);
		float reqHeight = res.getDimension(R.dimen.profilepic_thumbnail_height);

		String profilePictureUrl = MainScreenActivity.CURRENT_USER.getString("profilePictureUrl");
		if (profilePictureUrl != null) {
			ImageLoader.getInstance().displayImage(getActivity(), profilePictureUrl, userPictureImageView, false, reqWidth, reqHeight, R.drawable.default_profilepic);
		}
		else {
			userPictureImageView.setImageBitmap(Utils.decodeImageResource(res, R.drawable.default_profilepic, reqWidth, reqHeight));
		}
	}

	@Override
	public void onClick(View v) {

		/*if (v.getId() == R.id.overflowButton) {
			popupMenu.show();
		}*/

		if (v.getId() == R.id.userPictureImageViewContainer) {
			showPictureChooser();
		}
        if (v.getId() == R.id.profileButton) {
            //go to profile activity
            Intent goToProfile = new Intent(getActivity(), ProfileActivity.class);
            startActivity(goToProfile);
        }
    }

	private void showPictureChooser() {
		Builder builder = new Builder(getActivity());
		View customView = getActivity().getLayoutInflater().inflate(R.layout.dialog_picture_chooser, null);
		ListView optionsListView = (ListView) customView.findViewById(R.id.dialogListView);
		DialogListViewAdapter adapter = new DialogListViewAdapter(getActivity());
		optionsListView.setAdapter(adapter);
		builder.setView(customView);

		//create and show dialog
		final AlertDialog dialog = builder.create();
		dialog.show();

		//set onItemClick listener on options listview
		optionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adv, View v,
					int pos, long arg3) {
				if (pos == 0) {
					captureImageUsingCamera();
				}
				if (pos == 1) {
					//fire image picker intent to choose existing picture
					Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
					pictureIntent.setType("image/*");
					startActivityForResult(pictureIntent, CHOOSE_GALLERY);
				}

				dialog.dismiss();
			}
		});
	}

	private void captureImageUsingCamera() {
		Intent pictureIntent = null;
		Uri profilePicUri = null;
		try {
			//fire camera intent to take picture
			pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			//save image to external cache dir
			File imagesFolder = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/images");
			if (!imagesFolder.exists()) {
				imagesFolder.mkdirs();
			}
			File tempFile = new File(imagesFolder.getAbsolutePath() + "/" + "profile_picture.jpg");
			if (tempFile.exists()) {
				tempFile.delete();
			}
			profilePicUri = Uri.fromFile(tempFile);
			//tell the intent to save the image in order to get full resoulution photo
			pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, profilePicUri);
			startActivityForResult(pictureIntent, CHOOSE_CAMERA);
		}
		catch (Exception e) {
			//            Log.e("TEST", "taking picture using camera failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	//retrieve images chosen/taken by user
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			//set up progress dialog
			imageUploadProgress = new ProgressDialog(getActivity());
			imageUploadProgress.setCancelable(true);
			imageUploadProgress.setMessage("Uploading your profile picture...");
			imageUploadProgress.show();

			Bitmap bitmap = null;
			double reqWidth = 300.0;
			double reqHeight = 300.0;

			File imagesFolder = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/images");
			if (!imagesFolder.exists()) {
				imagesFolder.mkdirs();
			}
			File tempFile = new File(imagesFolder.getAbsolutePath() + "/" + "profile_picture.jpg");

			if (requestCode == CHOOSE_CAMERA) {
				bitmap = Utils.decodeImageFile(tempFile, reqWidth, reqHeight);
			}
			if (requestCode == CHOOSE_GALLERY) {
				Uri imageUri = data.getData();
				InputStream is = null;
				OutputStream os = null;
				try {
					is = getActivity().getContentResolver().openInputStream(imageUri);
					os = new FileOutputStream(tempFile);
					Utils.copyStream(is, os);
					bitmap = Utils.decodeImageFile(tempFile, reqWidth, reqHeight);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if (is != null) {
						try {
							is.close();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (os != null) {
						try {
							os.close();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			userPictureImageView.setImageBitmap(bitmap);

			//compress image
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, 70, baos);

			//upload image to parse
			final ParseFile parseFile = new ParseFile("profile_picture.jpg", baos.toByteArray());
			MainScreenActivity.CURRENT_USER.put("profilePicture", parseFile);
			parseFile.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						MainScreenActivity.CURRENT_USER.put("profilePictureUrl", parseFile.getUrl());
						MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null) {
									//                                    Log.d("TEST", "Profile pic uploaded successfully");
								}
								else {
									//                                    Log.d("TEST", "Profile pic upload FAILED");
									e.printStackTrace();
									Toast.makeText(getActivity(), "Upload Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
								}
								imageUploadProgress.dismiss();
							}
						});
					}
				}
			});
		}
	}


	private class DialogListViewAdapter extends BaseAdapter {
		String[] options;
		Bitmap[] bitmaps;

		public DialogListViewAdapter(Context c) {
			options= new String[]{"Take a new picture", "Choose an existing picture"};
			bitmaps = new Bitmap[]{BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_camera), BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_gallery)};
		}

		@Override
		public int getCount() {
			return options.length;
		}

		@Override
		public Object getItem(int pos) {
			return options[pos];
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup container) {
			View row = getActivity().getLayoutInflater().inflate(R.layout.listview_picture_choose_dialog_item, container, false);
			TextView itemName = (TextView) row.findViewById(R.id.itemNameTextView);
			ImageView itemIcon = (ImageView) row.findViewById(R.id.itemIconImageView);
			itemName.setText(options[pos]);
			itemIcon.setImageBitmap(bitmaps[pos]);

			return row;
		}
	}

/*
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
			ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatBrandList");
			query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
			query.addDescendingOrder("updatedAt");
			try {				
				List<ParseObject> results = query.find();
				for (ParseObject result : results) {
					if(!"GOOGLE".equals(result.getString("brandObjectId"))){
						customers.add(new Brand(result.getString("brandObjectId"),"", result.getString("brandName"), result.getString("brandEmail"), result.getString("brandPhone"),"", dummy,  result.getString("brandPic"), "", "", ""));
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
	*/
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
				LayoutInflater infalInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				row = infalInflater.inflate(R.layout.listview_customers, parent, false);
			}

			SharedPreferences sharedpreferences = getActivity().getSharedPreferences("MyPrefs", Service.MODE_PRIVATE);
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
			customerInformation.setText("Email: "+customers.get(pos).getEmail()+" \nPhone: "+customers.get(pos).getPhone());
			if(customers.get(pos).getCoverPictureUrl()!=null){
				ImageLoader.getInstance().displayImage(getActivity().getApplicationContext(), customers.get(pos).getCoverPictureUrl(), customerProfilePicture, false, 50, 50, 0);
			}
			select.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent goToClientInfo2 = new Intent(getActivity(), ChatMessageActivity.class);
					goToClientInfo2.putExtra("merchantUserId",customers.get(pos).getObjectId());
					goToClientInfo2.putExtra("merchantName",customers.get(pos).getName());
                    goToClientInfo2.putExtra("merchantPhone",customers.get(pos).getPhone());
                    goToClientInfo2.putExtra("merchantEmail",customers.get(pos).getEmail());
					startActivity(goToClientInfo2);
					getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
				}

			});

			//customerProfilePicture.setImageBitmap();

			return row;
		}
	}
	//	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
	//		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
	//		Canvas canvas = new Canvas(output);
	//
	//		final int color = 0xff424242;
	//		final Paint paint = new Paint();
	//		int side = 0;
	//		if (bitmap.getWidth() < bitmap.getHeight()) {
	//			side = bitmap.getWidth();
	//		} else {
	//			side = bitmap.getHeight();
	//		}
	//		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	//		new RectF(rect);
	//		paint.setAntiAlias(true);
	//		canvas.drawARGB(0, 0, 0, 0);
	//		paint.setColor(color);
	//
	//		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, side / 2, paint);
	//
	//		paint.setColor(color);
	//		paint.setStyle(Paint.Style.STROKE); // define paint style as Stroke
	//		paint.setStrokeWidth(1f);
	//
	//		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	//		canvas.drawBitmap(bitmap, rect, rect, paint);
	//
	//		return output;
	//	}

}
