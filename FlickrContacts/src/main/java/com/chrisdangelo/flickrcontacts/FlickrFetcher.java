package com.chrisdangelo.flickrcontacts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful resources:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 26
 * http://developer.android.com/training/basics/network-ops/connecting.html
 */
public class FlickrFetcher {
    private static final String TAG = "FlickrFetchrLogTag";

    private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
    private static final String API_KEY = "0e5ce78ee1c6a238b80667055f891480";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";

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


}
