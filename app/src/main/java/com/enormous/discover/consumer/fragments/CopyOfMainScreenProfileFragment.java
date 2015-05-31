package com.enormous.discover.consumer.fragments;/*package com.enormous.discover.consumer.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.discover.consumer.DiscoverApp;
import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.ChooseCategoriesActivity;
import com.enormous.discover.consumer.activities.IntroActivity;
import com.enormous.discover.consumer.activities.InviteFriendsActivity;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.activities.ShowChatsActivity;
import com.enormous.discover.consumer.asynctasks.DownloadAndSaveDocumentTask;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.common.Utils;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyOfMainScreenProfileFragment extends Fragment implements OnClickListener {

	private final String TAG = "MainScreenProfileFragment";
	private ImageView userPictureImageView;
	private FrameLayout userPictureImageViewContainer;
	private TextView emailTextView;
	private ImageButton overflowButton;
	private PopupMenu popupMenu;
	private String userDomain;
	private ProgressDialog imageUploadProgress;

	private RelativeLayout preferencesLinearLayout;
	private RelativeLayout inviteFriendsLinearLayout;

	//requestCodes for image chooser intents
	private int CHOOSE_CAMERA = 1;
	private int CHOOSE_GALLERY = 2;

	private String email;
	private String firstName;
	private String lastName;
	private String phone;
	private String phoneCode;
	private String about;

	//settings views
	private TextView fullNameTextView;
	private TextView emailTextView2;
	private TextView phoneTextView;
	private TextView aboutTextView;
	private RelativeLayout resetPasswordLinearLayout;
	private TextView editProfileTextView;
	private TextView editAboutTextView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mainscreen_profile_2, container, false);
		findViews(view);

		//set onClick listeners
		userPictureImageViewContainer.setOnClickListener(this);
		overflowButton.setOnClickListener(this);
		preferencesLinearLayout.setOnClickListener(this);
		inviteFriendsLinearLayout.setOnClickListener(this);
		resetPasswordLinearLayout.setOnClickListener(this);
		editProfileTextView.setOnClickListener(this);
		editAboutTextView.setOnClickListener(this);

		//get user details
		email = MainScreenActivity.CURRENT_USER.getEmail();
		firstName = MainScreenActivity.CURRENT_USER.getString("firstName");
		lastName = MainScreenActivity.CURRENT_USER.getString("lastName");
		phone = MainScreenActivity.CURRENT_USER.getString("phoneNumber");
		phoneCode = MainScreenActivity.CURRENT_USER.getString("phoneCode");
		about = MainScreenActivity.CURRENT_USER.getString("about");
		if (email != null) {
			String[] split = email.split("@");
			userDomain = split[1];
		}
		else {
			userDomain = "Twitter";
			email = "Not available";
		}
		emailTextView.setText(firstName + " " + lastName);

		//set user's profile picture
		setProfilePicture();

		//set up Profile settings
		fullNameTextView.setText(firstName + " " + lastName);
		emailTextView2.setText(email);
		phoneTextView.setText(phoneCode + " " + phone);
		if (about != null) {
			aboutTextView.setText(about);
		}

		setUpPopupMenu();

		return view;
	}

	private void findViews(View view) {
		userPictureImageViewContainer = (FrameLayout) view.findViewById(R.id.userPictureImageViewContainer);
		userPictureImageView = (ImageView) view.findViewById(R.id.userPictureImageView);
		emailTextView = (TextView) view.findViewById(R.id.emailTextView);
		overflowButton = (ImageButton) view.findViewById(R.id.overflowButton);
		preferencesLinearLayout = (RelativeLayout) view.findViewById(R.id.preferencesRelativeLayout);
		inviteFriendsLinearLayout = (RelativeLayout) view.findViewById(R.id.inviteFriendsRelativeLayout);
		fullNameTextView = (TextView) view.findViewById(R.id.fullNameTextView);
		emailTextView2 = (TextView) view.findViewById(R.id.emailTextView2);
		phoneTextView = (TextView) view.findViewById(R.id.phoneTextView);
		resetPasswordLinearLayout = (RelativeLayout) view.findViewById(R.id.resetPasswordRelativeLayout);
		aboutTextView = (TextView) view.findViewById(R.id.aboutTextView);
		editProfileTextView = (TextView) view.findViewById(R.id.editProfileTextView);
		editAboutTextView = (TextView) view.findViewById(R.id.editAboutTextView);
	}

	private void setUpPopupMenu() {
		popupMenu = new PopupMenu(getActivity(), overflowButton);
		popupMenu.inflate(R.menu.menu_profile);
		popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.ic_action_all_chats:
					//go to chats activity
					Intent goToChats = new Intent(getActivity(), ShowChatsActivity.class);
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
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					View customDialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_deactivate_account, null);
					Button yesButton = (Button) customDialog.findViewById(R.id.yesButton);
					Button noButton = (Button) customDialog.findViewById(R.id.noButton);
					final LinearLayout progressLinearLayout = (LinearLayout) customDialog.findViewById(R.id.progressLinearLayout);
					builder.setView(customDialog);
					final Dialog dialog = builder.create();
					dialog.show();

					View.OnClickListener buttonListener = new View.OnClickListener() {

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
	}

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
		if (v.getId() == R.id.overflowButton) {
			popupMenu.show();
		}

		if (v.getId() == R.id.userPictureImageViewContainer) {
			showPictureChooser();
		}

		if (v.getId() == R.id.preferencesRelativeLayout) {
			//start categories chooser activity
			Intent goToChooseCategories = new Intent(getActivity(), ChooseCategoriesActivity.class);
			goToChooseCategories.putExtra("callingActivity", "MainScreenActivity");
			startActivity(goToChooseCategories);
			getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
		}

		if (v.getId() == R.id.inviteFriendsRelativeLayout) {
			//start invite friends activity
			Intent goToInviteFriends = new Intent(getActivity(), InviteFriendsActivity.class);
			startActivity(goToInviteFriends);
			getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
		}

		if (v.getId() == R.id.resetPasswordRelativeLayout) {
			DialogFragment resetDialog = new LoginFragmentForgotPassword();
			resetDialog.show(getActivity().getSupportFragmentManager(), "resetDialog");
		}

		if (v.getId() == R.id.editProfileTextView) {
			Builder builder = new Builder(getActivity());
			View customView = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
			builder.setView(customView);
			final EditText firstNameEditText = (EditText) customView.findViewById(R.id.firstNameEditText);
			final EditText lastNameEditText = (EditText) customView.findViewById(R.id.lastNameEditText);
			final EditText phoneEditText = (EditText) customView.findViewById(R.id.phoneEditText);
			final LinearLayout progressLinearLayout = (LinearLayout) customView.findViewById(R.id.progressLinearLayout);
			String firstName;
			if ((firstName = MainScreenActivity.CURRENT_USER.getString("firstName")) != null) {
				firstNameEditText.setText(firstName);
			}
			String lastName;
			if ((lastName = MainScreenActivity.CURRENT_USER.getString("lastName")) != null) {
				lastNameEditText.setText(lastName);
			}
			String phone;
			if ((phone = MainScreenActivity.CURRENT_USER.getString("phoneNumber")) != null) {
				phoneEditText.setText(phone);
			}
			Button saveButton = (Button) customView.findViewById(R.id.saveButton);
			Button cancelButton = (Button) customView.findViewById(R.id.cancelButton);
			final Dialog editProfileDialog = builder.create();
			editProfileDialog.show();

			View.OnClickListener clickListener = new OnClickListener() {
				@Override
				public void onClick(View view) {
					Utils.hideKeyboard(getActivity(), phoneEditText.getWindowToken());
					switch (view.getId()) {
					case R.id.saveButton:
						final String newFirstName = firstNameEditText.getText().toString().trim();
						final String newLastName = lastNameEditText.getText().toString().trim();
						final String newPhone = phoneEditText.getText().toString().trim();
						if (newFirstName.length() == 0) {
							Toast.makeText(getActivity(), "First name cannot be left blank", Toast.LENGTH_SHORT).show();
						}
						else if (newLastName.length() == 0) {
							Toast.makeText(getActivity(), "Last name cannot be left blank", Toast.LENGTH_SHORT).show();
						}
						else if (newPhone.length() < 10) {
							Toast.makeText(getActivity(), "Phone number must be at least 10 characters", Toast.LENGTH_SHORT).show();
						}
						else {
							progressLinearLayout.setVisibility(View.VISIBLE);
							MainScreenActivity.CURRENT_USER.put("firstName", newFirstName);
							MainScreenActivity.CURRENT_USER.put("lastName", newLastName);
							MainScreenActivity.CURRENT_USER.put("phoneNumber", newPhone);
							MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e) {
									progressLinearLayout.setVisibility(View.GONE);
									if (e == null) {
										CopyOfMainScreenProfileFragment.this.firstName = newFirstName;
										CopyOfMainScreenProfileFragment.this.lastName = newLastName;
										CopyOfMainScreenProfileFragment.this.phone = newPhone;
										emailTextView.setText(CopyOfMainScreenProfileFragment.this.firstName + " " + CopyOfMainScreenProfileFragment.this.lastName);
										fullNameTextView.setText(CopyOfMainScreenProfileFragment.this.firstName + " " + CopyOfMainScreenProfileFragment.this.lastName);
										phoneTextView.setText(CopyOfMainScreenProfileFragment.this.phoneCode + " " + CopyOfMainScreenProfileFragment.this.phone);
										editProfileDialog.dismiss();
									}
									else {
										e.printStackTrace();
										Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
						break;
					case R.id.cancelButton:
						editProfileDialog.dismiss();
						break;
					}
				}
			};
			saveButton.setOnClickListener(clickListener);
			cancelButton.setOnClickListener(clickListener);
		}

		if (v.getId() == R.id.editAboutTextView) {
			Builder builder = new Builder(getActivity());
			View customDialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_about, null);
			Button saveButton = (Button) customDialog.findViewById(R.id.saveButton);
			Button cancelButton = (Button) customDialog.findViewById(R.id.cancelButton);
			final EditText aboutEditText= (EditText) customDialog.findViewById(R.id.aboutEditText);
			final LinearLayout progressLinearLayout = (LinearLayout) customDialog.findViewById(R.id.progressLinearLayout);
			if (about != null) {
				aboutEditText.setText(about);
			}
			builder.setView(customDialog);
			final Dialog dialog = builder.create();
			dialog.show();

			OnClickListener buttonListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch(v.getId()) {
					case R.id.saveButton:
						Utils.hideKeyboard(getActivity(), aboutEditText.getWindowToken());
						final String newAbout = aboutEditText.getText().toString().trim();
						progressLinearLayout.setVisibility(View.VISIBLE);
						MainScreenActivity.CURRENT_USER.put("about", newAbout);
						MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null) {
									about = newAbout;
									aboutTextView.setText(about);
									dialog.dismiss();
								}
								else {
									Toast.makeText(getActivity(), "Updating user details error: " + e.getMessage(), Toast.LENGTH_LONG).show();
									//                                        Log.d("TEST", "Updating user details error: " + e.getMessage());
								}
								progressLinearLayout.setVisibility(View.GONE);
							}
						});
						break;
					case R.id.cancelButton:
						dialog.dismiss();
						break;
					}
				}
			};
			saveButton.setOnClickListener(buttonListener);
			cancelButton.setOnClickListener(buttonListener);
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
*/