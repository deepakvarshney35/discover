package com.enormous.pkpizzas.consumer.models;

import com.parse.ParseFile;

public class Item {
	
	private String name;
	private String type;
	private String description;
	private ParseFile document;
    private String itemUrl;
    private String itemMap;
	
	public Item(String name,String type,  String description, ParseFile document, String itemUrl, String itemMap) {
		this.name = name;
		this.type = type;
		this.description = description;
		this.document = document;
        this.itemUrl = itemUrl;
        this.itemMap = itemMap;
	}
	public String getAddress(){
		return itemMap;
	}
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public ParseFile getDocument() {
		return document;
	}

    public String getItemUrl() {
        if (itemUrl.contains("http://") || itemUrl.contains("https://")) {
            return itemUrl;
        }
        else {
            return "http://" + itemUrl;
        }
    }
	
}
