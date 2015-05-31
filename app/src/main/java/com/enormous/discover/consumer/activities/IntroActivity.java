package com.enormous.discover.consumer.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.common.CirclePagerIndicator;
import com.enormous.discover.consumer.fragments.IntroSlideFragmentImage;
import com.enormous.discover.consumer.fragments.IntroSlideFragmentWelcome;
import com.enormous.discover.consumer.fragments.LogInFragmentSignIn;
import com.enormous.discover.consumer.fragments.LogInFragmentSignUp;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Manas on 8/5/2014.
 * 
 * Intro come sign in , sign up activity. 
 * ALso supports google 1 click sign in and facebook login.
 * 
 * Default password is used after successfull "google sign in" or successfull "fb login" to sign in/up into parse.
 */
public class IntroActivity extends FragmentActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public final String TAG = "IntroActivity";
    private ViewPager pager;
    CirclePagerIndicator circlePagerIndicator;
    private int prevPage = 0;
    private int currentPage = 0;
    private Drawable[] drawables;

    //buttons
    private LinearLayout signInWithGooglePlusButton;
    private LoginButton signInWithFacebookButton;
    private Button signInWithEmailButton;
    private Button signUpWithEmailButton;

    //list of all unique user accounts
    public ArrayList<String> allEmails;

    //google plus login vars
    private static final int RC_SIGN_IN = 0;
    private static final String DEFAULT_PASSWORD = "over123";
    private GoogleApiClient mGoogleApiClient;
    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;
    private ProgressDialog gplusConnectionProgressDialog;

    //facebook login vars
    private ProgressDialog facebookProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpIntro();

        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        //set callbacks for facebook login
        signInWithFacebookButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        signInWithFacebookButton.setSessionStatusCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session != null && state.isOpened()) {
                    Request.newMeRequest(session, new Request.GraphUserCallback() {

                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (response != null && user != null) {
                                new ParseLoginTask(user).execute();
                            }
                            facebookProgressDialog.dismiss();
                        }
                    }).executeAsync();
                } else {
                    facebookProgressDialog.dismiss();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        signOutFromGplus();
        signOutFromFacebook();
    }

    private void setUpIntro() {
        setContentView(R.layout.activity_intro);
        findViews();

        //set the pager with an adapter and update indicator
        IntroPagerAdapter adapter = new IntroPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
//        pager.setOffscreenPageLimit(4);
        circlePagerIndicator.setViewPager(pager);

        //get all user accounts
        AccountManager accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccounts();
        HashSet<String> emailSet = new HashSet<String>();
        for (Account account : accounts) {
            emailSet.add(account.name);
        }
        allEmails = new ArrayList<String>(emailSet);

        //set click listeners for buttons
        signInWithGooglePlusButton.setOnClickListener(this);
        signInWithFacebookButton.setOnClickListener(this);
        signInWithEmailButton.setOnClickListener(this);
        signUpWithEmailButton.setOnClickListener(this);
    }

    private void findViews() {
        pager = (ViewPager) findViewById(R.id.pager);
        circlePagerIndicator = (CirclePagerIndicator) findViewById(R.id.circlePagerIndicator);
        signInWithGooglePlusButton = (LinearLayout) findViewById(R.id.signInWithGooglePlusButton);
        signInWithFacebookButton = (LoginButton) findViewById(R.id.signInWithFacebookButton);
        signInWithEmailButton = (Button) findViewById(R.id.signInWithEmailButton);
        signUpWithEmailButton = (Button) findViewById(R.id.signUpWithEmailButton);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInWithFacebookButton:
                facebookProgressDialog = new ProgressDialog(this);
                facebookProgressDialog.setMessage("Loading...");
                facebookProgressDialog.setCancelable(false);
                facebookProgressDialog.show();
                break;
            case R.id.signInWithGooglePlusButton:
                gplusConnectionProgressDialog = new ProgressDialog(IntroActivity.this);
                gplusConnectionProgressDialog.setMessage("Loading...");
                gplusConnectionProgressDialog.setCancelable(false);
                gplusConnectionProgressDialog.show();
                mGoogleApiClient.connect();
                break;
            case R.id.signUpWithEmailButton:
                DialogFragment signUpDialog = new LogInFragmentSignUp();
                signUpDialog.show(getSupportFragmentManager(), "signUpDialog");
                break;
            case R.id.signInWithEmailButton:
                DialogFragment signInDialog = new LogInFragmentSignIn();
                signInDialog.show(getSupportFragmentManager(), "signInDialog");
                break;
        }

    }

    private class IntroPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments;

        public IntroPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<Fragment>();
            fragments.add(new IntroSlideFragmentWelcome());
            fragments.add(IntroSlideFragmentImage.newInstance(1));
            fragments.add(IntroSlideFragmentImage.newInstance(2));
            fragments.add(IntroSlideFragmentImage.newInstance(3));
            fragments.add(IntroSlideFragmentImage.newInstance(4));
            fragments.add(IntroSlideFragmentImage.newInstance(5));
        }

        @Override
        public Fragment getItem(int pos) {
            return fragments.get(pos);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    //google plus connection callbacks
    /**
     *Called when we've resolved any connection errors.  mGoogleApiClient can be used to
     *access Google APIs on behalf of the user.
     **/
    @Override
    public void onConnected(Bundle bundle) {
//        Log.d(TAG, "successfully connected to googlePlus");
        if (gplusConnectionProgressDialog.isShowing()) {
            gplusConnectionProgressDialog.dismiss();
        }
        new ParseLoginTask(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient)).execute();
    }

    @Override
    public void onConnectionSuspended(int i) {
//        Log.d(TAG, "googlePlus connection suspended");
    }

    /**When the GoogleApiClient object is unable to establish a connection, your implementation is notified
     *of the failure in the onConnectionFailed callback, where you are passed a ConnectionResult that can be
     *used to resolve the error. You can call ConnectionResult.getResolution() to retrieve a PendingIntent which,
     *when sent, will allow Google Play services to solicit any user interaction needed to resolve sign in errors
     *(for example by asking the user to select an account, consent to permissions, enable networking, etc).
     **/
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            }
            catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**Because the resolution for the connection failure was started with startIntentSenderForResult and
     *the code RC_SIGN_IN, we can capture the result inside Activity.onActivityResult.
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_CANCELED) {
            mIntentInProgress = false;
        }

        if (gplusConnectionProgressDialog != null) {
            gplusConnectionProgressDialog.dismiss();
        }

        Session activeSession;
        if ((activeSession = Session.getActiveSession()) != null) {
            activeSession.onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private class ParseLoginTask extends AsyncTask<Void, Void, Boolean> {

        Person gplusPerson;
        GraphUser facebookUser;
        ProgressDialog progressDialog;
        ParseUser user;


        public ParseLoginTask(Person gplusPerson) {
            this.gplusPerson = gplusPerson;
        }

        public ParseLoginTask(GraphUser facebookUser) {
            this.facebookUser = facebookUser;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(IntroActivity.this);
            progressDialog.setMessage("Signing in...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //get information about current user
            String firstName = null;
            String lastName = null;
            String photoUrl = null;
            String about = null;
            String email = null;

            if (gplusPerson != null) {
                String fullName = gplusPerson.getDisplayName();
                String[] nameSplit = fullName.split(" ");
                firstName = nameSplit[0];
                lastName = nameSplit[nameSplit.length - 1];
                photoUrl = gplusPerson.getImage().getUrl();
                about = gplusPerson.getAboutMe();
                email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            }
            else if (facebookUser != null) {
                firstName = facebookUser.getFirstName();
                lastName = facebookUser.getLastName();
                photoUrl = "https://graph.facebook.com/" + facebookUser.getId() +"/picture";
                email = (String) facebookUser.getProperty("email");
            }
            else {
                return false;
            }

            //now check if the user with the same email already exists
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", email);
            try {
                List<ParseUser> users = query.find();
                //user already exists, so we can simply sign in
                if (users.size() > 0) {
                    ParseUser.logInInBackground(email, DEFAULT_PASSWORD,new LogInCallback() {
						
						@Override
						public void done(ParseUser userd, ParseException e) {
							// TODO Auto-generated method stub
							if(e==null){
								user = userd;
								startAppropriateActivity(user);
							} else{
								DialogFragment signInDialog = new LogInFragmentSignIn();
				                signInDialog.show(getSupportFragmentManager(), "signInDialog");
								 Toast.makeText(IntroActivity.this, "You already have an account. Sign in with your password.", Toast.LENGTH_SHORT).show();
							}
						}
					});
                }
                //user does not exist, so we sign up and add a few more details about him/her
                else {
                    user = new ParseUser();
                    user.setUsername(email);
                    user.setEmail(email);
                    user.setPassword(DEFAULT_PASSWORD);
                    user.signUp();
                    if (firstName != null) {
                        user.put("firstName", firstName);
                    }
                    if (lastName != null) {
                        user.put("lastName", lastName);
                    }
                    if (about != null) {
                        user.put("about", about);
                    }
                    if (photoUrl != null) {
                        user.put("profilePictureUrl", photoUrl);
                    }
                    user.save();
                    startAppropriateActivity(user);
                }

                return true;
            }
            catch (ParseException e) {
                e.printStackTrace();
//                Log.e(TAG, "Sign in with gplus/facebook failed: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            progressDialog.dismiss();
            if (!success) {
                Toast.makeText(IntroActivity.this, "An error occurred while signing in.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Used to sign out user from google plus
     **/
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
//            Log.d(TAG, "signed out from google plus");
        }
    }

    /**
     * Used to revoke user access from google plus
     **/
    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
            mGoogleApiClient.disconnect();
//            Log.d(TAG, "revoked access from google plus");
        }
    }


    public void startAppropriateActivity(ParseUser user) {
        //check if user logging in for the first time
        if (((ArrayList<Integer>) user.get("selectedCategories")) == null) {
            //start chooseCategories activity
            Intent goToChooseCategories = new Intent(IntroActivity.this, ChooseCategoriesActivity.class);
            startActivity(goToChooseCategories);
        }
        else {
            //start mainScreen activity
            Intent goToMainScreen = new Intent(IntroActivity.this, MainScreenActivity.class);
            startActivity(goToMainScreen);
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
        finish();
    }

    private void signOutFromFacebook() {
        Session activeSession;
        if ((activeSession = Session.getActiveSession()) != null) {
            activeSession.closeAndClearTokenInformation();
        }
    }

}
