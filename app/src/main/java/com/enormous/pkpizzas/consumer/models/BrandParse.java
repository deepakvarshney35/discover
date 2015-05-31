package com.enormous.pkpizzas.consumer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BrandParse implements Parcelable{

    private String objectId;
	private String UUID;
	private String name;
	private String email;
	private String phone;
	private String location;
	private ArrayList<String> tags;
	private String pictureUrl;
	private String website;
	private String categoryName;
	private String about;
    private double latitude;
    private double longitude;

	public BrandParse(String objectId, String UUID, String name, String email, String phone, String location, ArrayList<String> tags, String pictureUrl, String website, String categoryName, String about, double latitude, double longitude) {
		this.objectId = objectId;
        this.UUID = UUID;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.location = location;
		this.tags = tags;
		this.pictureUrl = pictureUrl;
		this.website = website;
		this.categoryName = categoryName;
		this.about = about;
        this.latitude = latitude;
        this.longitude = longitude;
	}

    public String getObjectId() {
        return objectId;
    }

    public String getUUID() {
		return UUID;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public String getLocation() {
		return location;
	}

	public String getCoverPictureUrl() {
		return pictureUrl;
	}

	public String getWebsite() {
		if (website.contains("http://") || website.contains("https://")) {
			return website;
		}
		return "http://" + website;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getAbout() {
		return about;
	}

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(objectId);
        parcel.writeString(UUID);
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(phone);
        parcel.writeString(location);
        parcel.writeStringList(tags);
        parcel.writeString(pictureUrl);
        parcel.writeString(website);
        parcel.writeString(categoryName);
        parcel.writeString(about);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }

    public void readFromParcel(Parcel parcel) {
        this.objectId = parcel.readString();
        this.UUID = parcel.readString();
        this.name = parcel.readString();
        this.email = parcel.readString();
        this.phone = parcel.readString();
        this.location = parcel.readString();
        tags = new ArrayList<String>();
        parcel.readStringList(this.tags);
        this.pictureUrl = parcel.readString();
        this.website = parcel.readString();
        this.categoryName = parcel.readString();
        this.about = parcel.readString();
        this.latitude = parcel.readDouble();
        this.longitude = parcel.readDouble();
    }

    public BrandParse(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static final Creator<BrandParse> CREATOR = new Creator<BrandParse>() {

        @Override
        public BrandParse createFromParcel(Parcel parcel) {
            return new BrandParse(parcel);
        }

        @Override
        public BrandParse[] newArray(int size) {
            return new BrandParse[size];
        }
    };
}
