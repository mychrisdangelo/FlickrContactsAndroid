package com.chrisdangelo.flickrcontacts;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful resources:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 26
 * http://developer.android.com/training/basics/network-ops/connecting.html
 * http://developer.android.com/training/basics/network-ops/xml.html
 */
public class FlickrFetcher {
    private static final String TAG = "FlickrFetchrLogTag";

    /*
     * Assignment described using .search REST call. Response from flickr:
     * Parameterless searches have been disabled. Please use flickr.photos.getRecent instead
     */
    private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
    private static final String API_KEY = "0e5ce78ee1c6a238b80667055f891480";
    private static final String METHOD = "flickr.photos.getRecent";
    private static final String EXTRAS = "date_taken,owner_name,description";

    private static final String XML_PHOTO = "photo";

    public String getUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();


        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return new String(out.toByteArray());
        } finally {
            connection.disconnect();
        }
    }

    public ArrayList<FlickrPhoto> fetchPhotos(String searchTerm) {
        ArrayList<FlickrPhoto> photos = new ArrayList<FlickrPhoto>();

        try {
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("extras", EXTRAS)
                    .build().toString();
            Log.i(TAG, "Url being sent: " + url);
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: " + xmlString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items" + ioe);
        }

        return photos;
    }

}
