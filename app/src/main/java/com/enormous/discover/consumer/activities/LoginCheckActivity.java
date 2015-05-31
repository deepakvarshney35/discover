package com.enormous.discover.consumer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Manas on 8/9/2014.
 */
public class LoginCheckActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if user is already logged in
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            //check if user has selected any categories or not
            if (((ArrayList<Integer>) currentUser.get("selectedCategories")) == null) {
                //start choose categories activity
                Intent goToChooseCategories = new Intent(this, ChooseCategoriesActivity.class);
                startActivity(goToChooseCategories);
            }
            else {
                //go to MainScreen and finish() this activity
                Intent goToMainScreen = new Intent(this, MainScreenActivity.class);
                startActivity(goToMainScreen);
            }
//            Log.d("TEST", "user has logged in before");
        }
        else {
//            Log.d("TEST", "no user logged in");
            //go to IntroActivity and finish() this activity
            Intent goToIntro = new Intent(this, IntroActivity.class);
            startActivity(goToIntro);
        }
        finish();
    }
}
