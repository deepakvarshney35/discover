package com.enormous.pkpizzas.consumer.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.activities.IntroActivity;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class LogInFragmentSignUp extends DialogFragment {

    private AutoCompleteTextView emailEditText;
    private EditText passwordEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText phoneEditText;
    private Button signUpButton;
    private IntroActivity activity;

    //sign up vars
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_login_signup, container, false);
        findViews(view);

        activity = (IntroActivity) getActivity();

        //set arrayAdapter for emailEditText
        emailEditText.setAdapter(new ArrayAdapter<String>(activity, R.layout.spinner_item, activity.allEmails));

        //set onClick listener for signUpButton
        signUpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(activity, emailEditText.getWindowToken());
                signUp();
            }
        });

        return view;
    }

    private void signUp() {
        username = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        firstName = firstNameEditText.getText().toString().trim();
        lastName = lastNameEditText.getText().toString().trim();
        phone = phoneEditText.getText().toString().trim();

        if (firstName.length() == 0) {
            Toast.makeText(activity, "First name cannot be left blank", Toast.LENGTH_SHORT).show();
        }
        else if (lastName.length() == 0) {
            Toast.makeText(activity, "Last name cannot be left blank", Toast.LENGTH_SHORT).show();
        }
        else if (username.length() == 0) {
            Toast.makeText(activity, "Email cannot be left blank", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 8) {
            Toast.makeText(activity, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
        }
        else if (phone.length() < 10) {
            Toast.makeText(activity, "Phone number must be at least 10 digits", Toast.LENGTH_SHORT).show();
        }
        else {
            new SignUpTask().execute();
        }
    }

    private void findViews(View view) {
        emailEditText = (AutoCompleteTextView) view.findViewById(R.id.emailEditText);
        passwordEditText = (EditText) view.findViewById(R.id.passwordEditText);
        firstNameEditText = (EditText) view.findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) view.findViewById(R.id.lastNameEditText);
        phoneEditText = (EditText) view.findViewById(R.id.phoneEditText);
        signUpButton = (Button) view.findViewById(R.id.signUpButton);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_signup, menu);
    }

    private class SignUpTask extends AsyncTask<Void, String, Boolean> {

        ProgressDialog signUpProgress;

        @Override
        protected void onPreExecute() {
            signUpProgress = new ProgressDialog(activity);
            signUpProgress.setMessage("Signing up...");
            signUpProgress.setCancelable(false);
            signUpProgress.show();
            LogInFragmentSignUp.this.getDialog().hide();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //now check if the user with the same email already exists
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            try {
                List<ParseUser> users = query.find();
                //user already exists, so we can simply sign in
                if (users.size() > 0) {
//                    Log.d(activity.TAG, "User already exists");
                    publishProgress("That email address is already in use.");
                    return false;
                }
                else {
                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(username);
                    newUser.setEmail(username);
                    newUser.setPassword(password);
                    newUser.signUp();
                    newUser.put("phoneNumber", phone);
                    newUser.put("phoneCode", "+91");
                    newUser.put("firstName", firstName);
                    newUser.put("lastName", lastName);
                    newUser.save();
                    return true;
                }
            }
            catch (ParseException e) {
                e.printStackTrace();
                publishProgress(e.getMessage());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(activity, values[0], Toast.LENGTH_SHORT).show();
            LogInFragmentSignUp.this.getDialog().show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            signUpProgress.dismiss();
            if (success) {
            	LogInFragmentSignUp.this.dismiss();
            	signUpProgress = new ProgressDialog(activity);
            	signUpProgress.setMessage("Signing in...");
            	signUpProgress.setCancelable(false);
            	signUpProgress.show();

    			//finally, log in
    			ParseUser.logInInBackground(username, password, new LogInCallback() {

    				@Override
    				public void done(ParseUser user, ParseException e) {
    					if (e == null) {
    						signUpProgress.dismiss();
                            activity.startAppropriateActivity(user);
                        }
    					else {
    						e.printStackTrace();
    						signUpProgress.dismiss();
    						DialogFragment signInDialog = new LogInFragmentSignIn();
    		                signInDialog.show(activity.getSupportFragmentManager(), "signInDialog");
    						Toast.makeText(activity, "Sign in failed, Try Again.", Toast.LENGTH_SHORT).show();
    					}
    				}
    			});
                //DialogFragment signInDialog = new LogInFragmentSignIn();
                //signInDialog.show(activity.getSupportFragmentManager(), "signInDialog");
                //Toast.makeText(activity, "Account created. Please verify your email address and then sign in to continue.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
