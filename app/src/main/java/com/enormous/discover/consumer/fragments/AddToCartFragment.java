package com.enormous.discover.consumer.fragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.activities.CartActivity;
import com.enormous.discover.consumer.activities.MainScreenActivity;
import com.enormous.discover.consumer.models.Product;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Manas on 8/2/2014.
 */
public class AddToCartFragment extends DialogFragment {


    private AddToCartDialogListener listener = null;
    private boolean isAddToCartDialog = true;
    private String dialogTitle;
    private String positiveButtonTitle;
    private TextView titleTextView;
    private LinearLayout progressLinearLayout;
    private Spinner optionSpinner;
    private Spinner quantitySpinner;
    private Button cancelButton;
    private Button addToCartButton;
    private Product selectedProduct;
    private ArrayList<Integer> quantities;
    private ArrayList<String> options;
    private ArrayList<String> optionsCost;
    private ArrayList<Integer> optionsCosts;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_add_to_cart, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        findViews(view);

        //get dialog, button titles and selectedProduct from arguments
        selectedProduct = getArguments().getParcelable("selectedProduct");
        dialogTitle = getArguments().getString("dialogTitle");
        positiveButtonTitle = getArguments().getString("positiveButtonTitle");

        titleTextView.setText(dialogTitle);
        addToCartButton.setText(positiveButtonTitle);

        //check what kind of a dialog is this (edit item or add item to cart)
        if (dialogTitle.equals("Edit item")) {
            isAddToCartDialog = false;
        }

        setUpSpinners();
        setUpButtons();

        return view;
    }

    private void findViews(View view) {
        progressLinearLayout = (LinearLayout) view.findViewById(R.id.progressLinearLayout);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        optionSpinner = (Spinner) view.findViewById(R.id.optionSpinner);
        quantitySpinner = (Spinner) view.findViewById(R.id.quantitySpinner);
        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        addToCartButton = (Button) view.findViewById(R.id.addToCartButton);
    }

    // a listener to detect when the user clicks on 'Save'
    public static interface AddToCartDialogListener {
        void onFinishAddToCartDialog(int selectedCost, String selectedOption, int selectedQuantity);
    }

    public void setOnAddToCartDialogListener(AddToCartDialogListener listener) {
        if (this.listener == null) {
            this.listener = listener;
        }
    }

    private ArrayList<Integer> getListOfAvailableQuantities(int numberOfItemsInStock) {
        ArrayList<Integer> quantities = new ArrayList<Integer>();
        for (int i=1;i<=numberOfItemsInStock;i++) {
            quantities.add(i);
        }
        return quantities;
    }

    private void setUpSpinners() {
        //get number of items in stock and the various options for the selected product
        int numberOfItemsInStock = selectedProduct.getNumberOfItemsInStock();
        quantities = getListOfAvailableQuantities(numberOfItemsInStock);
        options = selectedProduct.getOptions();
       // optionsCost = new ArrayList<Integer>();
        optionsCost = selectedProduct.getOptionsCost();
        optionsCosts = new ArrayList<Integer>(optionsCost.size()); 
		for (String myStr : optionsCost) { 
			optionsCosts.add(Integer.parseInt(myStr)); 
		}
         
        //Log.d("TEST", optionsCost.toString());
        //set up quantity and option spinners
        ArrayAdapter<Integer> quatityAdapter = new ArrayAdapter<Integer>(getActivity(), R.layout.spinner_item, quantities);
        quantitySpinner.setAdapter(quatityAdapter);
        if (quantities.size() == 1) {
            quantitySpinner.setEnabled(false);
        }
        ArrayAdapter<String> optionAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, options);
        optionSpinner.setAdapter(optionAdapter);
        if (options != null) {
            if (options.size() == 1) {
                optionSpinner.setEnabled(false);
            }
        } else {
            optionSpinner.setEnabled(false);
        }

        //set previous selections if its an edit item dialog
        if (!isAddToCartDialog) {
            optionSpinner.setSelection(options.indexOf(getArguments().getString("selectedOption")));
            quantitySpinner.setSelection(quantities.indexOf(getArguments().getInt("selectedQuantity")));
        }
    }

    private void setUpButtons() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancelButton:
                        AddToCartFragment.this.dismiss();
                        break;
                    case R.id.addToCartButton:
                        if (isAddToCartDialog) {
                            new AddItemToCartTask().execute();
                        }
                        else {
                            if (listener != null) {
                                listener.onFinishAddToCartDialog(Integer.parseInt(optionsCost.get(optionSpinner.getSelectedItemPosition())), options.get(optionSpinner.getSelectedItemPosition()), quantities.get(quantitySpinner.getSelectedItemPosition()));
                                AddToCartFragment.this.dismiss();
                            }
                        }
                        break;
                }
            }
        };
        cancelButton.setOnClickListener(onClickListener);
        addToCartButton.setOnClickListener(onClickListener);
    }

    private class AddItemToCartTask extends AsyncTask<Void, Void, Boolean> {

        String selectedOption;
        int selectedOptionPrice;
        int selectedQuantity;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //get option and quantity selections
            selectedOption = options.get(optionSpinner.getSelectedItemPosition());
            selectedQuantity = quantities.get(quantitySpinner.getSelectedItemPosition());
            if(optionsCost.size()!=0){
            Log.d("TEST", optionsCost.toString());
            selectedOptionPrice = Integer.parseInt(optionsCost.get(optionSpinner.getSelectedItemPosition()));}
            else{
            	selectedOptionPrice = selectedProduct.getCost();
            }
            progressLinearLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //check if item already added to cart
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ShoppingCart");
            query.whereEqualTo("username", MainScreenActivity.CURRENT_USER.getUsername());
            query.whereEqualTo("productId", selectedProduct.getObjectId());
            query.whereEqualTo("selectedOption", selectedOption);
            try {
                List<ParseObject> parseObjects = query.find();
                if (parseObjects.size() > 0) {
                    //product already in cart, so we increment the quantity
                    ParseObject previousProduct = parseObjects.get(0);
                    int previousQuantity = previousProduct.getInt("selectedQuantity");
                    previousProduct.put("selectedQuantity", previousQuantity + selectedQuantity);
                    previousProduct.save();
                }
                else {
                    //product does not exist in cart, so we add it
                    ParseObject cartClass = new ParseObject("ShoppingCart");
                    cartClass.put("productId", selectedProduct.getObjectId());
                    cartClass.put("brandObjectId", selectedProduct.getBrandObjectId());
                    cartClass.put("username", MainScreenActivity.CURRENT_USER.getUsername());
                    cartClass.put("userObjectId", MainScreenActivity.CURRENT_USER.getObjectId());
                    cartClass.put("productName", selectedProduct.getName());
                    cartClass.put("brandName", selectedProduct.getBrandName());
                    cartClass.put("productDescription", selectedProduct.getDescription());
                    cartClass.put("productPicture", selectedProduct.getPictureURL());
                    cartClass.put("productType", selectedProduct.getCategory());
                    cartClass.put("productTax", selectedProduct.getTax());
                    cartClass.put("productShippingCost", selectedProduct.getShippingCost());
                    cartClass.put("productCost", selectedOptionPrice);
                    cartClass.put("productOptions", selectedProduct.getOptions());
                    cartClass.put("productStock", selectedProduct.getNumberOfItemsInStock());
                    cartClass.put("selectedQuantity", selectedQuantity);
                    cartClass.put("selectedOption", selectedOption);
                    cartClass.put("productOptionsCost", optionsCosts);
                    cartClass.put("checkOut", false);
                    cartClass.save();
                }
                return true;
            }
            catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            progressLinearLayout.setVisibility(View.GONE);

            if (success) {
    			final Date date = new Date();
    			
    			ParseQuery<ParseObject> query = ParseQuery.getQuery("Checkin");
    			query.whereEqualTo("brandObjectId", selectedProduct.getBrandObjectId());
    			query.whereEqualTo("userObjectId", MainScreenActivity.CURRENT_USER.getObjectId());
    			query.findInBackground(new FindCallback<ParseObject>() {

    				@Override
    				public void done(List<ParseObject> checkins, ParseException e) {
    					if(e==null){
    						for(ParseObject checkin : checkins){
    							int num = checkin.getInt("CartItems");
    							num = num+1;
    							checkin.put("CartItems", num);
    							checkin.put("date", date);
    							checkin.saveInBackground();
    						}
    					}
    				}
    			});

                AddToCartFragment.this.dismiss();
                Intent goToCart = new Intent(getActivity(), CartActivity.class);
                startActivity(goToCart);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
            }
            else {
                Toast.makeText(getActivity(), "An error occurred while adding item to cart.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
