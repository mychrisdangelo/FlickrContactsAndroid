package com.chrisdangelo.flickrcontacts;

/**
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful resources:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 26
 */
public class FlickrPhoto {
    private String mId;
    private String mCaption;
    private String mUrl;

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String toString() {
        return mCaption;
    }
}
