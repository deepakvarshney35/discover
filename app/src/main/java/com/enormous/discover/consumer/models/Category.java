package com.enormous.discover.consumer.models;

/**
 * Created by Manas on 8/7/2014.
 */
public class Category {

    private String name;
    private int count;

    public Category(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
