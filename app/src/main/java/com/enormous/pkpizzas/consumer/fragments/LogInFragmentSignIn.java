package com.enormous.pkpizzas.consumer.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.activities.IntroActivity;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LogInFragmentSignIn extends DialogFragment implements OnClickListener{

	private  ProgressDialog signInProgress;
	private AutoCompleteTextView emailEditText;
	private EditText passwordEditText;
	private Button signInButton;
	private TextView forgotPasswordTextView;
	private IntroActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.fragment_login_signin, container, false);
		findViews(view);

		activity = (IntroActivity) getActivity();

		//set arrayAdapter for emailEditText
		emailEditText.setAdapter(new ArrayAdapter<String>(activity, R.layout.spinner_item, activity.allEmails));

		//set up onClick listener
		forgotPasswordTextView.setOnClickListener(this);
		signInButton.setOnClickListener(this);

		return view;
	}

	private void findViews(View view) {
		emailEditText = (AutoCompleteTextView) view.findViewById(R.id.emailEditText);
		passwordEditText = (EditText) view.findViewById(R.id.passwordEditText);
		forgotPasswordTextView = (TextView) view.findViewById(R.id.forgotPasswordTextView);
		signInButton = (Button) view.findViewById(R.id.signInButton);
	}

	private void attemptToLogin() {
		final String username = emailEditText.getText().toString().trim();
		String password = passwordEditText.getText().toString().trim();

		if (username.length() == 0) {
			Toast.makeText(activity, "Email cannot be left blank", Toast.LENGTH_SHORT).show();
		}
		else if (password.length() == 0) {
			Toast.makeText(activity, "Password cannot be left blank", Toast.LENGTH_SHORT).show();
		}
		else if (password.length() < 8) {
			Toast.makeText(activity, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
		}
		else {
			signInProgress = new ProgressDialog(activity);
			signInProgress.setMessage("Signing in...");
			signInProgress.setCancelable(false);
			signInProgress.show();
			LogInFragmentSignIn.this.getDialog().hide();

			//finally, log in
			ParseUser.logInInBackground(username, password, new LogInCallback() {

				@Override
				public void done(ParseUser user, ParseException e) {
					if (e == null) {
						//Log.d(activity.TAG, "Sign in with email successful");
						signInProgress.dismiss();

						//check if user verified his/her email
						//if (user.getBoolean("emailVerified")) {

						LogInFragmentSignIn.this.getDialog().dismiss();
						((IntroActivity) getActivity()).startAppropriateActivity(user);
						//}
						//else {
						//	Toast.makeText(activity, "Please verify your email to sign in", Toast.LENGTH_LONG).show();
						//  LogInFragmentSignIn.this.getDialog().show();
						//   ParseUser.logOut();
						//}
					}
					else {
						//						Log.e(activity.TAG, "Sign in with email failed: " + e.getMessage());
						e.printStackTrace();
						signInProgress.dismiss();
						LogInFragmentSignIn.this.getDialog().show();
						Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			});
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.forgotPasswordTextView:
			this.dismiss();
			DialogFragment forgotPasswordDialog = new LoginFragmentForgotPassword();
			forgotPasswordDialog.show(activity.getSupportFragmentManager(), "forgotPasswordDialog");
			break;
		case R.id.signInButton:
			Utils.hideKeyboard(activity, emailEditText.getWindowToken());
			attemptToLogin();
			break;
		case R.id.cancelButton:
			activity.onBackPressed();
			break;
		}
	}
}
