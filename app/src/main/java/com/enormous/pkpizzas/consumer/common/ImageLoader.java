package com.enormous.pkpizzas.consumer.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.enormous.pkpizzas.consumer.DiscoverApp;
import com.enormous.pkpizzas.consumer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private final String TAG = "ImageLoader";
	private static ImageLoader instance;
	private LruCache<String, Bitmap> memoryCache;
	private FileCache fileCache;
	private Map<ImageView, String> imageViews;
	private ExecutorService exectorService;
	private Handler handler;
	private Drawable placeholder;

	private ImageLoader() {
		memoryCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 4));
		fileCache = new FileCache();
		imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
		exectorService = Executors.newFixedThreadPool(5);
		handler = new Handler();
	}

	public static ImageLoader getInstance() {
		if (instance == null) {
			instance = new ImageLoader();
		}
		return instance;
	}

	public void displayImage(Context c, String url, ImageView imageView, boolean isResource, double reqWidth, double reqHeight, int placeHolderResId) {
		if (placeHolderResId == 0) {
			placeholder = c.getResources().getDrawable(R.drawable.placeholder_image);
		}
        else {
            placeholder = c.getResources().getDrawable(placeHolderResId);
        }
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if(bitmap!=null) 
		{			
			imageView.setImageBitmap(bitmap);
		}
		else
		{
			if (isResource) {
				queueResourcePhoto(c, url, imageView, reqWidth, reqHeight);
			}
			else {			
				queuePhoto(url, imageView, reqWidth, reqHeight);
			}
			imageView.setImageDrawable(placeholder);
		}
	}

	private void queuePhoto(String url, ImageView imageView, double reqWidth, double reqHeight) {
		PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView, reqWidth, reqHeight);
		exectorService.submit(new PhotosLoader(photoToLoad));
	}
	
	private void queueResourcePhoto(Context c, String url, ImageView imageView, double reqWidth, double reqHeight) {
		PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView, reqWidth, reqHeight);
		exectorService.submit(new PhotosResourceLoader(photoToLoad, c));
	}

	private Bitmap getBitmap(String url, double reqWidth, double reqHeight)
	{
		File f=fileCache.getFile(url);

		//from SD cache
		Bitmap b = Utils.decodeImageFile(f, reqWidth, reqHeight);
		if(b!=null) {
			return b;
		}

		//from web
		HttpURLConnection conn = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			Bitmap bitmap=null;
			URL imageUrl = new URL(url);
			conn = (HttpURLConnection)imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			is=conn.getInputStream();
			os = new FileOutputStream(f);
			Utils.copyStream(is, os);
			bitmap = Utils.decodeImageFile(f, reqWidth, reqHeight);
			return bitmap;
		} 
		catch (Throwable ex){
			ex.printStackTrace();
			return null;
		}
		finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (is != null) {
					is.close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (os != null) {
					os.close();
				}
			}
			catch (Exception e) {
                e.printStackTrace();
			}
		}
	}

	private class PhotoToLoad
	{
		public String url;
		public ImageView imageView;
        public double reqWidth;
        public double reqHeight;

		public PhotoToLoad(String u, ImageView i, double reqWidth, double reqHeight){
			url=u; 
			imageView=i;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
		}
	}

	private class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad){
			this.photoToLoad=photoToLoad;
		}

		@Override
		public void run() {
			try{
				if(imageViewReused(photoToLoad)) {					
					return;
				}
				Bitmap bmp=getBitmap(photoToLoad.url, photoToLoad.reqWidth, photoToLoad.reqHeight);
				memoryCache.put(photoToLoad.url, bmp);
				if(imageViewReused(photoToLoad)) {
					return;
				}
				BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			}catch(Throwable th){
				th.printStackTrace();
			}
		}
	}

	private class PhotosResourceLoader implements Runnable {
		PhotoToLoad photoToLoad;
		Context c;

		public PhotosResourceLoader(PhotoToLoad photoToLoad, Context c){
			this.photoToLoad=photoToLoad;
			this.c = c;
		}

		@Override
		public void run() {
			if(imageViewReused(photoToLoad)) {					
				return;
			}
			Bitmap bmp = Utils.decodeImageResource(c.getResources(), Integer.parseInt(photoToLoad.url), photoToLoad.reqWidth, photoToLoad.reqHeight);
			memoryCache.put(photoToLoad.url, bmp);
			if(imageViewReused(photoToLoad)) {					
				return;
			}
			BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
			handler.post(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad){
		String tag=imageViews.get(photoToLoad.imageView);
		if(tag==null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	//Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable
	{
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p){
			bitmap=b;photoToLoad=p; 
		}

		public void run()
		{
			if(imageViewReused(photoToLoad)) {				
				return;
			}
			if(bitmap!=null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageDrawable(placeholder);
		}
	}

	public void clearImageCache() {
		//clear disk cache
		fileCache.clearCache();
		//clear memory cache
		memoryCache.evictAll();		
	}

	private class FileCache {
		private File imageCacheDir;

		public FileCache() {
			imageCacheDir = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/images");
			if (!imageCacheDir.exists()) {
				imageCacheDir.mkdirs();
			}
		}

		public File getFile(String url) {
			return new File(imageCacheDir.getAbsolutePath() + "/" + generateFileName(url));
		}

		public void clearCache() {
			File[] files = imageCacheDir.listFiles();
			for (File file : files) {
				file.delete();
			}
		}

		private String generateFileName(String url) {
			return url.replaceAll("[^A-Za-z0-9]", "");
		}
	}

}
