package com.enormous.pkpizzas.consumer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.models.Brand;
import com.enormous.pkpizzas.consumer.models.Brand2;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

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

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("isBrand", true);
            query.setLimit(1);
            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        ParseUser shop = objects.get(0);
                        String coverURL = shop.getParseFile("brandCoverPicture").getUrl();
                        String profileURL = shop.getParseFile("brandProfilePicture").getUrl();
                        ArrayList<String> tags = new ArrayList<String>();
                        Intent goToChooseCategories = new Intent(LoginCheckActivity.this, BrandInfoActivity.class);
                        Brand2 mBrand = new Brand2(shop.getObjectId(), shop.getString("UUID"), shop.getString("brandName"), shop.getString("brandEmail"), shop.getString("brandPhone"), shop.getString("brandLocation"), tags , profileURL, coverURL, shop.getString("brandWebsite"),shop.getString("brandCategory"), shop.getString("brandAbout"));
                        goToChooseCategories.putExtra("selectedBrand", mBrand);
                        startActivity(goToChooseCategories);
                        finish();
                    } else {
                        Toast.makeText(LoginCheckActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
//            Log.d("TEST", "no user logged in");
            //go to IntroActivity and finish() this activity
            Intent goToIntro = new Intent(this, IntroActivity.class);
            startActivity(goToIntro);
            finish();
        }
    }
}
