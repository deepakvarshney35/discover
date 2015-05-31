package com.enormous.discover.consumer.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.common.Utils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class LoginFragmentForgotPassword extends DialogFragment implements OnClickListener {

    private LinearLayout progressLinearLayout;
    private AutoCompleteTextView emailEditText;
    private Button cancelButton;
    private Button resetButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login_forgot_password, container, false);
		findViews(view);
		setCancelable(false);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //set onClick listeners
		cancelButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);

		return view;
	}

    private void findViews(View view) {
        progressLinearLayout = (LinearLayout) view.findViewById(R.id.progressLinearLayout);
		emailEditText = (AutoCompleteTextView) view.findViewById(R.id.emailEditText);
		cancelButton = (Button) view.findViewById(R.id.cancelButton);
		resetButton = (Button) view.findViewById(R.id.resetButton);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancelButton:
			dismiss();
			break;
		case R.id.resetButton:
            Utils.hideKeyboard(getActivity(), emailEditText.getWindowToken());
            String email = emailEditText.getText().toString().trim();
			if (email.length() == 0) {
				Toast.makeText(getActivity(), "Email cannot be left blank", Toast.LENGTH_SHORT).show();
			}
			else {
				//set up progress dialog
				progressLinearLayout.setVisibility(View.VISIBLE);

				ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
                            LoginFragmentForgotPassword.this.dismiss();
                            Toast.makeText(getActivity(), "An email was successfully sent with reset instructions", Toast.LENGTH_LONG).show();
						}
						else {
							Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
						}
                        progressLinearLayout.setVisibility(View.GONE);
					}
				});
			}
			break;
		}
	}
}

