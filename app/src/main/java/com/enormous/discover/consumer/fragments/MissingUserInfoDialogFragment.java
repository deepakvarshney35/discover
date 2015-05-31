package com.enormous.discover.consumer.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.common.Utils;
import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * Created by Manas on 8/14/2014.
 */
public class MissingUserInfoDialogFragment extends DialogFragment implements View.OnClickListener {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText phoneEditText;;
    private Button saveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_fragment_missing_info, container, false);
        findViews(view);
        setCancelable(false);

        //get current user's name if available
        String firstName;
        if ((firstName = MainScreenActivity.CURRENT_USER.getString("firstName")) != null) {
            firstNameEditText.setText(firstName);
        }
        String lastName;
        if ((lastName = MainScreenActivity.CURRENT_USER.getString("lastName")) != null) {
            lastNameEditText.setText(lastName);
        }

        //set click listener
        saveButton.setOnClickListener(this);

        return view;
    }

    private void findViews(View view) {
        firstNameEditText = (EditText) view.findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) view.findViewById(R.id.lastNameEditText);
        phoneEditText = (EditText) view.findViewById(R.id.phoneEditText);
        saveButton = (Button) view.findViewById(R.id.saveButton);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getActivity(), phoneEditText.getWindowToken());
        switch (view.getId()) {
            case R.id.saveButton:
                final String firstName = firstNameEditText.getText().toString().trim();
                final String lastName = lastNameEditText.getText().toString().trim();
                final String phone = phoneEditText.getText().toString().trim();
                if (firstName.length() == 0) {
                    Toast.makeText(getActivity(), "First name cannot be left blank", Toast.LENGTH_SHORT).show();
                }
                else if (lastName.length() == 0) {
                    Toast.makeText(getActivity(), "Last name cannot be left blank", Toast.LENGTH_SHORT).show();
                }
                else if (phone.length() < 10) {
                    Toast.makeText(getActivity(), "Phone number must be at least 10 characters", Toast.LENGTH_SHORT).show();
                }
                else {
                    final ProgressDialog saveProgressDialog = new ProgressDialog(getActivity());
                    saveProgressDialog.setMessage("Saving user details...");
                    saveProgressDialog.setCancelable(false);
                    saveProgressDialog.show();
                    this.getDialog().hide();
                    MainScreenActivity.CURRENT_USER.put("firstName", firstName);
                    MainScreenActivity.CURRENT_USER.put("lastName", lastName);
                    MainScreenActivity.CURRENT_USER.put("phoneNumber", phone);
                    MainScreenActivity.CURRENT_USER.put("phoneCode", "+91");
                    MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {

                        @Override
                        public void done(ParseException e) {
                            saveProgressDialog.dismiss();
                            if (e == null) {
                                ((MainScreenActivity) getActivity()).setUpPagerAdpter();
                                MissingUserInfoDialogFragment.this.dismiss();
                            }
                            else {
                                e.printStackTrace();
                                MissingUserInfoDialogFragment.this.getDialog().show();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
        }
    }
}
