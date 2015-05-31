package com.enormous.pkpizzas.consumer.models;

import android.net.Uri;

public class Contact {
	public String id;
	public String name;
	public String email;
	public String phone;
	public Uri thumbnail;
	
	public Contact(String id, String name, String email, String phone, Uri thumbnail) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.thumbnail = thumbnail;
	}

}
