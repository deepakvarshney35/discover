package com.enormous.pkpizzas.consumer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Brand2 implements Parcelable{

    private String objectId;
    private String UUID;
    private String name;
    private String email;
    private String phone;
    private String location;
    private ArrayList<String> tags;
    private String pictureUrl;
    private String coverUrl;
    private String website;
    private String categoryName;
    private String about;

    public Brand2(String objectId, String UUID, String name, String email, String phone, String location, ArrayList<String> tags, String pictureUrl, String coverUrl, String website, String categoryName, String about) {
        this.objectId = objectId;
        this.UUID = UUID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.tags = tags;
        this.pictureUrl = pictureUrl;
        this.coverUrl = coverUrl;
        this.website = website;
        this.categoryName = categoryName;
        this.about = about;
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

    public String getCover() {
        return coverUrl;
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
        parcel.writeString(coverUrl);
        parcel.writeString(website);
        parcel.writeString(categoryName);
        parcel.writeString(about);
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
        this.coverUrl = parcel.readString();
        this.website = parcel.readString();
        this.categoryName = parcel.readString();
        this.about = parcel.readString();
    }

    public Brand2(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static final Creator<Brand2> CREATOR = new Creator<Brand2>() {

        @Override
        public Brand2 createFromParcel(Parcel parcel) {
            return new Brand2(parcel);
        }

        @Override
        public Brand2[] newArray(int size) {
            return new Brand2[size];
        }
    };
}
