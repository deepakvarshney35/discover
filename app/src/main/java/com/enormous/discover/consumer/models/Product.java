package com.enormous.discover.consumer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;
public class Product implements Parcelable {

	private String objectId;
	private String productId;
	private String brandObjectId;
	private String brandName;
	private String name;
	private String category;
	private String pictureURL;
	private int cost;
	private int shippingCost;
	private int tax;
	private String description;
	private int numberOfItemsInStock;
	private ArrayList<String> options;
	private ArrayList<String> optionsCost;
	private int selectedQuantity;
	private String selectedOption;
	private boolean checkOut;

	public Product(String brandObjectId, String objectId, String brandName, String name, String category, String pictureURL, int cost, int shippingCost, int tax,  String description, int numberOfItemsInStock, ArrayList<String> options, ArrayList<Integer> optionsCost) {
		this.brandObjectId = brandObjectId;
		this.objectId = objectId;
		this.brandName = brandName;
		this.name = name;
		this.category = category;
		this.pictureURL = pictureURL;
		this.cost = cost;
		this.shippingCost = shippingCost;
		this.tax = tax;
		this.description = description;
		this.numberOfItemsInStock = numberOfItemsInStock;
		this.options = options;
		ArrayList<String> newList = new ArrayList<String>(optionsCost.size()); 
		for (Integer myInt : optionsCost) { 
			newList.add(String.valueOf(myInt)); 
		}
		this.optionsCost = newList;
	}


	//Constructor for ShoppingCart
	public Product(String brandObjectId,String objectId, String productId, String brandName, String name, String category, String pictureURL, int cost, int shippingCost, int tax,  String description, int numberOfItemsInStock, ArrayList<String> options, int selectedQuantity, String selectedOption ,boolean checkOut, ArrayList<Integer> optionsCost) {
		this.brandObjectId=brandObjectId;
		this.objectId = objectId;
		this.productId = productId;
		this.brandName = brandName;
		this.name = name;
		this.category = category;
		this.pictureURL = pictureURL;
		this.cost = cost;
		this.shippingCost = shippingCost;
		this.tax = tax;
		this.description = description;
		this.numberOfItemsInStock = numberOfItemsInStock;
		this.options = options;
		this.selectedQuantity = selectedQuantity;
		this.selectedOption = selectedOption;
		this.checkOut = checkOut;
		ArrayList<String> newList = new ArrayList<String>(optionsCost.size()); 
		for (Integer myInt : optionsCost) { 
			newList.add(String.valueOf(myInt)); 
		}
		this.optionsCost = newList;
	}
	public String getBrandObjectId(){
		return brandObjectId;
	}
	
	public String getProductObjectId(){
		return objectId;
	}
	
	public ArrayList<String> getOptionsCost(){
		return optionsCost;
	}

	public boolean getCheckOutStatus(){
		return checkOut;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getProductId() { return productId; }

	public String getBrandName() {
		return brandName;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getPictureURL() {
		return pictureURL;
	}

	public int getCost() {
		return cost;
	}

	public int getShippingCost() {
		return shippingCost;
	}

	public int getTax() {
		return tax;
	}

	public String getDescription() {
		return description;
	}

	public int getNumberOfItemsInStock() {
		return numberOfItemsInStock;
	}

	public ArrayList<String> getOptions() {
		return options;
	}

	public int getSelectedQuantity() {
		return selectedQuantity;
	}

	public String getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedQuantity(int selectedQuantity) {
		this.selectedQuantity = selectedQuantity;
	}

	public void setSelectedOption(String selectedOption) {
		this.selectedOption = selectedOption;
	}

	public void setSelectedCost(int selectedCost) {
		this.cost = selectedCost;
	}
	//Parcelling
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(brandObjectId);
		dest.writeString(objectId);
		dest.writeString(productId);
		dest.writeString(brandName);
		dest.writeString(name);
		dest.writeString(category);
		dest.writeString(pictureURL);
		dest.writeString(description);
		dest.writeInt(cost);
		dest.writeInt(shippingCost);
		dest.writeInt(tax);
		dest.writeInt(numberOfItemsInStock);
		dest.writeStringList(options);
		dest.writeInt(selectedQuantity);
		dest.writeString(selectedOption);
		dest.writeStringList(optionsCost);
	}

	public Product(Parcel in) {
		this.brandObjectId = in.readString();
		this.objectId = in.readString();
		this.productId = in.readString();
		this.brandName = in.readString();
		this.name = in.readString();
		this.category = in.readString();
		this.pictureURL = in.readString();
		this.description = in.readString();
		this.cost = in.readInt();
		this.shippingCost = in.readInt();
		this.tax = in.readInt();
		this.numberOfItemsInStock = in.readInt();
		this.options = new ArrayList<String>();
		in.readStringList(this.options);
		this.selectedQuantity = in.readInt();
		this.selectedOption = in.readString();
		this.optionsCost = new ArrayList<String>();
		in.readStringList(this.optionsCost);
	}
	public static int[] convertIntegers(ArrayList<Integer> integers)
	{
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}
	public static final Creator<Product> CREATOR = new Creator<Product>() {
		@Override
		public Product createFromParcel(Parcel source) {
			return new Product(source);
		}
		@Override
		public Product[] newArray(int size) {
			return new Product[size];
		}
	};

}
