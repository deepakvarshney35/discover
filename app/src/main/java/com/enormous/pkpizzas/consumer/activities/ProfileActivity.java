package com.enormous.pkpizzas.consumer.activities;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.asynctasks.DownloadAndSaveDocumentTask;
import com.enormous.pkpizzas.consumer.common.ImageLoader;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.enormous.pkpizzas.consumer.fragments.LoginFragmentForgotPassword;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class ProfileActivity extends FragmentActivity implements OnClickListener {

    public ProgressDialog deleteUserDataDialog;
	public String deactiveParseUser;
    private String email;
	private String firstName;
	private String lastName;
	private String phone;
	private String phoneCode;
	private String about;

	private String userDomain;
	
	LayoutInflater inflater;
	Handler handler;

	//settings views
	private TextView fullNameTextView;
	private TextView emailTextView2;
	private TextView phoneTextView;
	private TextView aboutTextView;
	private RelativeLayout resetPasswordLinearLayout;
	private TextView editProfileTextView;
	private TextView editAboutTextView;
    private RelativeLayout preferencesLinearLayout;
    private RelativeLayout inviteFriendsLinearLayout;
    private RelativeLayout ordersLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_show_chats);
		
		//set actionBar properties
				ActionBar actionBar = getActionBar();
				actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setDisplayShowHomeEnabled(false);
				actionBar.setTitle("Profile");


		fullNameTextView = (TextView) findViewById(R.id.fullNameTextView);
		emailTextView2 = (TextView) findViewById(R.id.emailTextView2);
		phoneTextView = (TextView) findViewById(R.id.phoneTextView);
		resetPasswordLinearLayout = (RelativeLayout) findViewById(R.id.resetPasswordRelativeLayout);
		aboutTextView = (TextView) findViewById(R.id.aboutTextView);
		editProfileTextView = (TextView) findViewById(R.id.editProfileTextView);
		editAboutTextView = (TextView) findViewById(R.id.editAboutTextView);
        preferencesLinearLayout = (RelativeLayout) findViewById(R.id.preferencesRelativeLayout);
        inviteFriendsLinearLayout = (RelativeLayout) findViewById(R.id.inviteFriendsRelativeLayout);
        ordersLayout = (RelativeLayout) findViewById(R.id.OrdersRelativeLayout);

		//get user details
		email = ParseUser.getCurrentUser().getEmail();
		firstName = ParseUser.getCurrentUser().getString("firstName");
		lastName = ParseUser.getCurrentUser().getString("lastName");
		phone = ParseUser.getCurrentUser().getString("phoneNumber");
		phoneCode = ParseUser.getCurrentUser().getString("phoneCode");
		about = ParseUser.getCurrentUser().getString("about");
		if (email != null) {
			String[] split = email.split("@");
			userDomain = split[1];
		}
		else {
			userDomain = "Twitter";
			email = "Not available";
		}

		resetPasswordLinearLayout.setOnClickListener(this);
		editProfileTextView.setOnClickListener(this);
		editAboutTextView.setOnClickListener(this);
        preferencesLinearLayout.setOnClickListener(this);
        inviteFriendsLinearLayout.setOnClickListener(this);
        ordersLayout.setOnClickListener(this);

		//set up Profile settings
		fullNameTextView.setText(firstName + " " + lastName);
		emailTextView2.setText(email);
		phoneTextView.setText(phoneCode + " " + phone);
		if (about != null) {
			aboutTextView.setText(about);
		}

		inflater = getLayoutInflater();

		handler = new Handler();
			}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
            case R.id.ic_action_logout:
                //clear whole cache and logout
                DownloadAndSaveDocumentTask.clearDocumentCache();
                ImageLoader.getInstance().clearImageCache();
                ParseUser.logOut();

                //go to intro activity
                Intent goToIntro = new Intent(getApplicationContext(), IntroActivity.class);
                goToIntro.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(goToIntro);
                break;
            case R.id.ic_action_clear_cache:
                ImageLoader.getInstance().clearImageCache();
                DownloadAndSaveDocumentTask.clearDocumentCache();
                Toast.makeText(this, "Cache successfully cleared", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ic_action_deactivate_account:
                Builder builder = new Builder(this);
                View customDialog = this.getLayoutInflater().inflate(R.layout.dialog_deactivate_account, null);
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
                                deactiveParseUser=ParseUser.getCurrentUser().getObjectId();
                                progressLinearLayout.setVisibility(View.VISIBLE);
                                ParseUser.getCurrentUser().deleteInBackground(new DeleteCallback() {

									@Override
									public void done(ParseException e) {
										if (e == null) {
											dialog.dismiss();
											//clear whole cache and logout
											DownloadAndSaveDocumentTask.clearDocumentCache();
											ImageLoader.getInstance().clearImageCache();
											deleteUserData();
										} else {
											Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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

    private void deleteUserData() {
        deleteUserDataDialog = ProgressDialog.show(this, "","Deleting User Data", true);
        userDataChatBrandList();
    }

    private void userDataChatBrandList() {
        //ChatBrandList
        ParseQuery userDataChatBrandList = ParseQuery.getQuery("ChatBrandList");
        userDataChatBrandList.whereEqualTo("brandObjectId",deactiveParseUser);
        userDataChatBrandList.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> itemList, ParseException e) {
                if(e==null){
                    if(itemList.size()>0) {
                        ParseObject.deleteAllInBackground(itemList, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                deleteUserDataCheckIn();
                            }
                        });
                    }
                    else deleteUserDataCheckIn();
                }
            }
        });
    }

    private void deleteUserDataCheckIn() {
        ParseQuery userDataCheckIn = ParseQuery.getQuery("Checkin");
        userDataCheckIn.whereEqualTo("brandObjectId",deactiveParseUser);
        userDataCheckIn.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> itemList, ParseException e) {
                if(e==null){
                    if(itemList.size()>0) {
                        ParseObject.deleteAllInBackground(itemList, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                deleteUserSharedOffer();
                            }
                        });
                    }
                    else deleteUserSharedOffer();
                }
            }
        });
    }

    private void deleteUserSharedOffer() {
        ParseQuery userDataSharedOffer = ParseQuery.getQuery("SharedOffer");
        userDataSharedOffer.whereEqualTo("brandObjectId",deactiveParseUser);
        userDataSharedOffer.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> itemList, ParseException e) {
                if(e==null){
                    if(itemList.size()>0) {
                        ParseObject.deleteAllInBackground(itemList, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                deleteUserCartItems();
                            }
                        });
                    }
                    else deleteUserCartItems();
                }
            }
        });
    }

    private void deleteUserCartItems() {
        ParseQuery userDataShoppingCart = ParseQuery.getQuery("ShoppingCart");
        userDataShoppingCart.whereEqualTo("brandObjectId",deactiveParseUser);
        userDataShoppingCart.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> itemList, ParseException e) {
                if(e==null){
                    if(itemList.size()>0) {
                        ParseObject.deleteAllInBackground(itemList, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                deleteUserDataDialog.dismiss();
                                ParseUser.logOut();
                                Toast.makeText(ProfileActivity.this, "Deactivated Successfully", Toast.LENGTH_SHORT).show();
                                //go to IntroActivity
                                Intent goToIntro = new Intent(getApplicationContext(), IntroActivity.class);
                                goToIntro.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(goToIntro);
                            }
                        });
                    }
                    else{
                        deleteUserDataDialog.dismiss();
                        ParseUser.logOut();
                        Toast.makeText(ProfileActivity.this, "Deactivated Successfully", Toast.LENGTH_SHORT).show();
                        //go to IntroActivity
                        Intent goToIntro = new Intent(getApplicationContext(), IntroActivity.class);
                        goToIntro.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(goToIntro);
                    }
                }
            }
        });
    }

    @Override
	public void onClick(View v) {

        if (v.getId() == R.id.preferencesRelativeLayout) {
            //start categories chooser activity
            Intent goToChooseCategories = new Intent(this, ChooseCategoriesActivity.class);
            goToChooseCategories.putExtra("callingActivity", "MainScreenActivity");
            startActivity(goToChooseCategories);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
        }

        if (v.getId() == R.id.OrdersRelativeLayout) {
            //start invite friends activity
            Intent goToOrders = new Intent(this, OrdersActivity.class);
            goToOrders.putExtra("orderId","NA");
            startActivity(goToOrders);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
        }

        if (v.getId() == R.id.inviteFriendsRelativeLayout) {
            //start invite friends activity
            Intent goToInviteFriends = new Intent(this, InviteFriendsActivity.class);
            startActivity(goToInviteFriends);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
        }
		
		if (v.getId() == R.id.resetPasswordRelativeLayout) {
			DialogFragment resetDialog = new LoginFragmentForgotPassword();
			resetDialog.show(getSupportFragmentManager(), "resetDialog");
		}

		if (v.getId() == R.id.editProfileTextView) {
			Builder builder = new Builder(this);
			View customView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
			builder.setView(customView);
			final EditText firstNameEditText = (EditText) customView.findViewById(R.id.firstNameEditText);
			final EditText lastNameEditText = (EditText) customView.findViewById(R.id.lastNameEditText);
			final EditText phoneEditText = (EditText) customView.findViewById(R.id.phoneEditText);
			final LinearLayout progressLinearLayout = (LinearLayout) customView.findViewById(R.id.progressLinearLayout);
			String firstName;
			if ((firstName = ParseUser.getCurrentUser().getString("firstName")) != null) {
				firstNameEditText.setText(firstName);
			}
			String lastName;
			if ((lastName = ParseUser.getCurrentUser().getString("lastName")) != null) {
				lastNameEditText.setText(lastName);
			}
			String phone;
			if ((phone = ParseUser.getCurrentUser().getString("phoneNumber")) != null) {
				phoneEditText.setText(phone);
			}
			Button saveButton = (Button) customView.findViewById(R.id.saveButton);
			Button cancelButton = (Button) customView.findViewById(R.id.cancelButton);
			final Dialog editProfileDialog = builder.create();
			editProfileDialog.show();

			OnClickListener clickListener = new OnClickListener() {
				@Override
				public void onClick(View view) {
					Utils.hideKeyboard(ProfileActivity.this, phoneEditText.getWindowToken());
					switch (view.getId()) {
					case R.id.saveButton:
						final String newFirstName = firstNameEditText.getText().toString().trim();
						final String newLastName = lastNameEditText.getText().toString().trim();
						final String newPhone = phoneEditText.getText().toString().trim();
						if (newFirstName.length() == 0) {
							Toast.makeText(ProfileActivity.this, "First name cannot be left blank", Toast.LENGTH_SHORT).show();
						}
						else if (newLastName.length() == 0) {
							Toast.makeText(ProfileActivity.this, "Last name cannot be left blank", Toast.LENGTH_SHORT).show();
						}
						else if (newPhone.length() < 10) {
							Toast.makeText(ProfileActivity.this, "Phone number must be at least 10 characters", Toast.LENGTH_SHORT).show();
						}
						else {
							progressLinearLayout.setVisibility(View.VISIBLE);
							ParseUser.getCurrentUser().put("firstName", newFirstName);
							ParseUser.getCurrentUser().put("lastName", newLastName);
							ParseUser.getCurrentUser().put("phoneNumber", newPhone);
							ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e) {
									progressLinearLayout.setVisibility(View.GONE);
									if (e == null) {
										ProfileActivity.this.firstName = newFirstName;
										ProfileActivity.this.lastName = newLastName;
										ProfileActivity.this.phone = newPhone;
										//emailTextView.setText(ProfileActivity.this.firstName + " " + ProfileActivity.this.lastName);
										fullNameTextView.setText(ProfileActivity.this.firstName + " " + ProfileActivity.this.lastName);
										phoneTextView.setText(ProfileActivity.this.phoneCode + " " + ProfileActivity.this.phone);
										editProfileDialog.dismiss();
									} else {
										e.printStackTrace();
										Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
			Builder builder = new Builder(this);
			View customDialog = getLayoutInflater().inflate(R.layout.dialog_edit_about, null);
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
						Utils.hideKeyboard(ProfileActivity.this, aboutEditText.getWindowToken());
						final String newAbout = aboutEditText.getText().toString().trim();
						progressLinearLayout.setVisibility(View.VISIBLE);
						ParseUser.getCurrentUser().put("about", newAbout);
						ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null) {
									about = newAbout;
									aboutTextView.setText(about);
									dialog.dismiss();
								} else {
									Toast.makeText(ProfileActivity.this, "Updating user details error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
}
