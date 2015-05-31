package com.enormous.discover.consumer;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

import java.io.File;

public class DiscoverApp extends Application {

    public static File EXTERNAL_CACHE_DIR;
    public static int DISPLAY_DPI;

	@Override
	public void onCreate() {
		super.onCreate();

        //get app's external cache directory
        EXTERNAL_CACHE_DIR = getExternalCacheDir();

        //get display dpi
        DISPLAY_DPI = getResources().getDisplayMetrics().densityDpi;

        //Enable Parse crash reports
        ParseCrashReporting.enable(this);
        
		//initialize Parse
		Parse.initialize(this, "Xq2EuNKKIiRAkTiJ2mJjrzCnHNQzdUDo89ocdFyx", "95HyKnMwzFcXtNSNU6yjNeippbgmWML0AejfnifX");

	}

}
