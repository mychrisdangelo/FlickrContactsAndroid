package com.chrisdangelo.flickrcontacts;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
    private static final String XML_OWNERNAME = "ownername";
    private static final String XML_DATETAKEN = "datetaken";
    private static final String XML_TITLE = "title";
    private static final String XML_DESCRIPTION = "description";
    private static final String XML_FARM = "farm";
    private static final String XML_SERVER = "server";
    private static final String XML_ID = "id";
    private static final String XML_SECRET = "secret";

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

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            parseItems(photos, parser);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items " + ioe);
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Failed to parse items " + xppe);
        }

        return photos;
    }


    void parseItems(ArrayList<FlickrPhoto> photos, XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.next();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())) {
                String ownerName = parser.getAttributeValue(null, XML_OWNERNAME);
                String dateTaken = parser.getAttributeValue(null, XML_DATETAKEN);
                String title = parser.getAttributeValue(null, XML_TITLE);
// TODO may not work
//                String description = parser.getAttributeValue(null, XML_DESCRIPTION);
                String farm = parser.getAttributeValue(null, XML_FARM);
                String server = parser.getAttributeValue(null, XML_SERVER);
                String id = parser.getAttributeValue(null, XML_ID);
                String secret = parser.getAttributeValue(null, XML_SECRET);

                FlickrPhoto photo = new FlickrPhoto();
                photo.setOwnerName(ownerName);
                photo.setDateTaken(dateTaken);
                photo.setTitle(title);
//                photo.setDescription(description);
                photo.setFarm(farm);
                photo.setServer(server);
                photo.setId(id);
                photo.setSecret(secret);
                photos.add(photo);
            }

            eventType = parser.next();
        }
    }

    /*
     * Example Photo object from twitter:
     *     <photo id="12619305265" owner="104681977@N06" secret="eefedcde47" server="7372"
     *     farm="8" title="" ispublic="1" isfriend="0" isfamily="0" datetaken="2014-02-18 20:13:35"
     *     datetakengranularity="0" ownername="TheRsport"> <description />
     */


}
