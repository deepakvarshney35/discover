package com.enormous.pkpizzas.consumer.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.activities.MainScreenActivity;
import com.enormous.pkpizzas.consumer.activities.ProductInfoActivity;
import com.enormous.pkpizzas.consumer.activities.ProductPhotoViewerActivity;
import com.enormous.pkpizzas.consumer.common.ImageLoader;
import com.enormous.pkpizzas.consumer.common.NotifyingScrollView;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.enormous.pkpizzas.consumer.models.Product;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays Product Information.
 * Photos, Price , Extra Photos..
 * Has option to add product to cart.
 * It opens up a dialog box to select quantity and options.
 */
public class ProductInfoFragment extends android.app.Fragment implements View.OnClickListener {

    private final String TAG = "ProductInfoFragment";
    private ProductInfoActivity activity;
    private Product product;
    public NotifyingScrollView scrollView;
    private ImageView productImageView;
    private FrameLayout productImageViewContainer;
    private TextView productNameTextView;
    private TextView productBrandTextView;
    private TextView productCostTextView;
    private LinearLayout bookmarkLinearLayout;
    private ImageView bookmarkImageView;
    private LinearLayout addToCartLinearLayout;
    private TextView productDescriptionTextView;
    private LinearLayout photosContainer;
    private ProgressBar photoProgress;
    private TextView nothingFoundTextView;
    private Resources res;

    //vars related to product bookmarks
    private ArrayList<String> productBookmarks;
    private String objectId;
    boolean isBookmarked;


    @SuppressWarnings("unchecked")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_info, container, false);
        findViews(view);
        res = getResources();

        //get product using the positon passed via intent
        int pos = getArguments().getInt("pos");
        if (getActivity() instanceof ProductInfoActivity) {
            activity = (ProductInfoActivity) getActivity();
            product = activity.products.get(pos);
        }

        //get list of productBookmarks and set bookmark button image accordingly
        productBookmarks = (ArrayList<String>) ParseUser.getCurrentUser().get("productBookmarks");
        if (productBookmarks == null) {
            productBookmarks = new ArrayList<String>();
        }
        objectId = product.getObjectId();
        changeBookmarkButtonImage();

        setViews();

        //set up actionBar fading
        if (activity.actionBarTitle != null) {
            activity.actionBarTitle.setTextColor(Color.argb(0, 255, 255, 255));
        }
        final int headerHeight = (int) res.getDimension(R.dimen.brand_coverpic_height) - activity.getActionBar().getHeight();
        scrollView.setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {

            @Override
            public void onScrollChanged(ScrollView view, int l, int t, int oldl,
                                        int oldt) {
                productImageView.setTranslationY((float) t/2);
                float ratio = (float) Math.min(t, headerHeight) / (float) headerHeight;
                int newAlpha = (int) (ratio * 255);

                activity.backgroundDrawable.setAlpha(newAlpha);
                if (activity.actionBarTitle != null) {
                    activity.actionBarTitle.setTextColor(Color.argb(newAlpha, 255, 255, 255));
                }
            }
        });

        return view;
    }

    public static ProductInfoFragment newInstance(int pos) {
        ProductInfoFragment productInfoFragment = new ProductInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", pos);
        productInfoFragment.setArguments(bundle);
        return productInfoFragment;
    }

    private void changeBookmarkButtonImage() {
        if (productBookmarks.contains(objectId)) {
            isBookmarked = true;
            ImageLoader.getInstance().displayImage(activity, res.getIdentifier("ic_action_bookmark_selected", "drawable", activity.getPackageName())+"", bookmarkImageView, true, 100, 100, 0);
        }
        else {
            isBookmarked = false;
            ImageLoader.getInstance().displayImage(activity, res.getIdentifier("ic_action_bookmark", "drawable", activity.getPackageName())+"", bookmarkImageView, true, 100, 100, 0);
        }
    }

    private void findViews(View view) {
        scrollView = (NotifyingScrollView) view.findViewById(R.id.scrollView);
        productImageView = (ImageView) view.findViewById(R.id.productImageView);
        productImageViewContainer = (FrameLayout) view.findViewById(R.id.productImageViewContainer);
        productNameTextView = (TextView) view.findViewById(R.id.productNameTextView);
        productBrandTextView = (TextView) view.findViewById(R.id.productBrandTextView);
        productCostTextView = (TextView) view.findViewById(R.id.productCostTextView);
        bookmarkLinearLayout = (LinearLayout) view.findViewById(R.id.bookmarkLinearLayout);
        bookmarkImageView = (ImageView) bookmarkLinearLayout.findViewById(R.id.bookmarkImageView);
        addToCartLinearLayout = (LinearLayout) view.findViewById(R.id.addToCartLinearLayout);
        productDescriptionTextView = (TextView) view.findViewById(R.id.productDescriptionTextView);
        photosContainer = (LinearLayout) view.findViewById(R.id.photosContainer);
        photoProgress = (ProgressBar) view.findViewById(R.id.photoProgress);
        nothingFoundTextView = (TextView) view.findViewById(R.id.nothingFoundTextView);
    }

    private void setViews() {
        ImageLoader.getInstance().displayImage(getActivity(), product.getPictureURL(), productImageView, false, 640, 640, 0);
        productNameTextView.setText(product.getName());
        productBrandTextView.setText(product.getBrandName());
        productCostTextView.setText(MainScreenActivity.CURRENCY_FORMATTER.format(product.getCost()));
        productDescriptionTextView.setText(product.getDescription());
        addToCartLinearLayout.setOnClickListener(this);
        bookmarkLinearLayout.setOnClickListener(this);
        if(ProductInfoActivity.activityVisible){
        new GetPhotoUrlsTask().execute();}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private class GetPhotoUrlsTask extends AsyncTask<Void, Void, ArrayList<String>> {

        ArrayList<String> urls = new ArrayList<String>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            photoProgress.setVisibility(View.VISIBLE);
            photosContainer.setVisibility(View.GONE);
            nothingFoundTextView.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("ProductPhotos");
            String productId = product.getProductId();
            if (productId != null) {
                query.whereEqualTo("productObjectId", productId);
            }
            else {
                query.whereEqualTo("productObjectId", product.getObjectId());
            }
            try {
                List<ParseObject> objects = query.find();
                for (ParseObject object : objects) {
                    urls.add(object.getParseFile("productPicture").getUrl());
                }
                return urls;
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            photoProgress.setVisibility(View.GONE);
            if (result != null) {
                if (result.size() > 0) {
                    photosContainer.setVisibility(View.VISIBLE);
                    if(ProductInfoActivity.activityVisible){
                    setUpPhotos(result);}
                }
                else {
                    nothingFoundTextView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setUpPhotos(final ArrayList<String> urls) {
        final float photoDimension = Utils.convertDpToPixel(100);
        final float photoPadding = Utils.convertDpToPixel(6);
        final LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams((int) photoDimension, (int) photoDimension);
        final LinearLayout.LayoutParams photoLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent goToViewPhotos = new Intent(getActivity(), ProductPhotoViewerActivity.class);
                goToViewPhotos.putStringArrayListExtra("imageUrls", urls);
                goToViewPhotos.putExtra("imagePos", (Integer) view.getTag());
                startActivity(goToViewPhotos);
            }
        };
        for (int i = 0; i < urls.size(); i++) {
        	if(ProductInfoActivity.activityVisible){
            FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(frameLayoutParams);
            frameLayout.setForeground(res.getDrawable(R.drawable.gridview_selector));
            frameLayout.setPadding((int) photoPadding, (int) photoPadding, (int) photoPadding, (int) photoPadding);
            frameLayout.setClickable(true);
            ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(photoLayoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            frameLayout.addView(imageView);
            photosContainer.addView(frameLayout, i);
            ImageLoader.getInstance().displayImage(getActivity(), urls.get(i), imageView, false, 250, 250, 0);
            frameLayout.setTag(i);
            frameLayout.setOnClickListener(clickListener);
        }}

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addToCartLinearLayout:
                AddToCartFragment addToCartDialog = new AddToCartFragment();
                Bundle bundle = new Bundle();
                bundle.putString("dialogTitle", "Choose option and quantity");
                bundle.putString("positiveButtonTitle", "Add to cart");
                bundle.putParcelable("selectedProduct", product);
                addToCartDialog.setArguments(bundle);
                addToCartDialog.show(getActivity().getFragmentManager(), "addToCartDialog");
                break;
            case R.id.bookmarkLinearLayout:
                if (isBookmarked) {
                    productBookmarks.remove(objectId);
                    isBookmarked = false;
                }
                else {
                    productBookmarks.add(objectId);
                    isBookmarked = true;
                    Toast.makeText(activity, "'" + product.getName() + "' added to bookmarks.", Toast.LENGTH_SHORT).show();
                }
                changeBookmarkButtonImage();
                //now, upload productBookmarks to Parse
                MainScreenActivity.CURRENT_USER.put("productBookmarks", productBookmarks);
                MainScreenActivity.CURRENT_USER.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
//                            Log.e("TEST", "ProductCatalogueCategoryFragment bookmarkLinearLayout onClick :" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
                break;
        }
    }
}
