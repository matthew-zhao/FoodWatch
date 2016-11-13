package com.foodwatch;

public class TagToFood {
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("tag")
    private String mTag;

    @com.google.gson.annotations.SerializedName("foods")
    private String mFoods;

    public TagToFood(String tag, String foods) {
        this.setTag(tag);
        this.setFoods(foods);
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public String getTag() {
        return mTag;
    }

    public void setFoods(String foods) {
        mFoods = foods;
    }

    public String getFoods() {
        return mFoods;
    }
}
