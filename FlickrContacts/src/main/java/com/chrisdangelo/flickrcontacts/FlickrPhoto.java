package com.chrisdangelo.flickrcontacts;

import android.graphics.Bitmap;

/**
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful resources:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 26
 */
public class FlickrPhoto {
    private String mOwnerName;
    private String mDateTaken;
    private String mTitle;
    private String mDescription;
    private String mFarm;
    private String mServer;
    private String mId;
    private String mSecret;

    public String getPhotoUrlSmall() {
        return getPhotoPartialUrl() + "_m.jpg";
    }

    public String getPhotoUrlMedium() {
        return getPhotoPartialUrl() + "_c.jpg";
    }

    // assumes all attributes in place.
    private String getPhotoPartialUrl() {
        return "http://farm" + mFarm + ".staticflickr.com/" + mServer + "/" + mId + "_" + mSecret;
    }

    public String getOwnerName() {
        return mOwnerName;
    }

    public void setOwnerName(String ownerName) {
        mOwnerName = ownerName;
    }

    public String getDateTaken() {
        return mDateTaken;
    }

    public void setDateTaken(String dateTaken) {
        mDateTaken = dateTaken;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getFarm() {
        return mFarm;
    }

    public void setFarm(String farm) {
        mFarm = farm;
    }

    public String getServer() {
        return mServer;
    }

    public void setServer(String server) {
        mServer = server;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getSecret() {
        return mSecret;
    }

    public void setSecret(String secret) {
        mSecret = secret;
    }
}
