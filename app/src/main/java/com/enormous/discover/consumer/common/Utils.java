package com.enormous.discover.consumer.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

import com.enormous.discover.consumer.DiscoverApp;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;

		try {
			byte[] buffer = new byte[buffer_size];
			int read = 0;
			while ((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap decodeImageFile(File f, double reqWidth, double reqHeight) {
		Bitmap bitmap = null;
		if (f != null) {
			if (f.exists()) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(f.getAbsolutePath(), options);
				int bitmapWidth = options.outWidth;
				int bitmapHeight = options.outHeight;
				int scaleFactor = Math.max((int) Math.round(bitmapWidth/reqWidth), (int) Math.round(bitmapHeight/reqHeight));
				options.inSampleSize = scaleFactor;
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
			}
		}
		return bitmap;
	}

    public static Bitmap decodeImageResource(Resources res, int resId, double reqWidth, double reqHeight) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;
        int scaleFactor = Math.max((int) Math.round(bitmapWidth/reqWidth), (int) Math.round(bitmapHeight/reqHeight));
        options.inSampleSize = scaleFactor;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeResource(res, resId, options);
        return bitmap;
    }
	
	public static void hideKeyboard(Context c, IBinder windowToken) {
		InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(windowToken, 0);
	}

    public static float convertDpToPixel(float dp){
        float px = dp * (DiscoverApp.DISPLAY_DPI / 160f);
        return px;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
    }

}
