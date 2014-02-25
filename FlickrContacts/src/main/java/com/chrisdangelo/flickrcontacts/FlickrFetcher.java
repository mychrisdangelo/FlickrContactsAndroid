package com.chrisdangelo.flickrcontacts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

/*
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful resources:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 26 <--NOTE
 * http://developer.android.com/training/basics/network-ops/connecting.html
 * http://developer.android.com/training/basics/network-ops/xml.html
 * http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
 * http://www.flickr.com/services/api/flickr.photos.search.html
 *
 * NOTE: Essential design of this class comes from Ch. 26. While the design here is fairly
 * straightforward and very similar to the standard developer.android/training examples
 * I have provided verbose comments to demonstrate my understanding of the techniques employed.
 */
public class FlickrFetcher {
    private static final String TAG = "FlickrFetchrLogTag";

    /*
     * query parameters
     */
    private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
    private static final String API_KEY = "0e5ce78ee1c6a238b80667055f891480";
    private static final String METHOD = "flickr.photos.search";
    private static final String EXTRAS = "date_taken,owner_name,description";
    private static final String PER_PAGE = "25";

    /*
     * photo is the parent tag. description is another tag within photo
     * the rest are attributes of photo
     */
    private static final String XML_PHOTO = "photo";
    private static final String XML_OWNERNAME = "ownername";
    private static final String XML_DATETAKEN = "datetaken";
    private static final String XML_TITLE = "title";
    private static final String XML_DESCRIPTION = "description";
    private static final String XML_FARM = "farm";
    private static final String XML_SERVER = "server";
    private static final String XML_ID = "id";
    private static final String XML_SECRET = "secret";

    /*
     * getURLBytes will be used to pull out the bytes for creating an image
     * and also to pull the original search request
     */
    public byte[] getUrlBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /*
     * getURL as a string will be used only to deliver the XML return
     * result from Flickr
     */
    public String getUrl(String urlString) throws IOException {
        return new String(getUrlBytes(urlString));
    }

    /*
     * fetchPhotos will be the main entry point for the AsyncList loading.
     */
    public ArrayList<FlickrPhoto> fetchPhotos(String searchTerm, String pageNumber) {
        ArrayList<FlickrPhoto> photos = new ArrayList<FlickrPhoto>();

        try {
            /*
             * Uri.Builder is a convenience class for creating properly escaped
             * parameterized URLs. "escaped" character meaning that a space may be
             * translated to %20
             */
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("tags", searchTerm)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("per_page", PER_PAGE)
                    .appendQueryParameter("page", pageNumber)
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

    /*
     * parseItems will parse a return value of the following type:
     *
     * Example Photo object from flickr:
     *     <photo id="12619305265" owner="104681977@N06" secret="eefedcde47" server="7372"
     *     farm="8" title="" ispublic="1" isfriend="0" isfamily="0" datetaken="2014-02-18 20:13:35"
     *     datetakengranularity="0" ownername="TheRsport"> <description />
     *
     */
    void parseItems(ArrayList<FlickrPhoto> photos, XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.next(); // will skip: <?xml version="1.0" encoding="utf-8" ?>
        FlickrPhoto photo = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())) {
                String ownerName = parser.getAttributeValue(null, XML_OWNERNAME);
                String dateTaken = parser.getAttributeValue(null, XML_DATETAKEN);
                String title = parser.getAttributeValue(null, XML_TITLE);
                String farm = parser.getAttributeValue(null, XML_FARM);
                String server = parser.getAttributeValue(null, XML_SERVER);
                String id = parser.getAttributeValue(null, XML_ID);
                String secret = parser.getAttributeValue(null, XML_SECRET);

                photo = new FlickrPhoto();
                photo.setOwnerName(ownerName);
                photo.setDateTaken(dateTaken);
                photo.setTitle(title);

                photo.setFarm(farm);
                photo.setServer(server);
                photo.setId(id);
                photo.setSecret(secret);

            } else if (eventType == XmlPullParser.START_TAG && XML_DESCRIPTION.equals(parser.getName())) {
                /*
                 * XML_DESCRIPTION is a separate tag so we must pull out the value a bit differently
                 * If there is no description we give it an empty string. If there is data it's filled.
                 * This is always at the end of the photo object so we can finally finish this photo
                 * and add it to the list.
                 */
                eventType = parser.next();
                if (eventType == XmlPullParser.TEXT) {
                    String description = parser.getText();
                    photo.setDescription(description);
                }
                photos.add(photo);
            }

            /*
             * Will skip amongst other things:
             *      <rsp stat="ok">
             *      <photos page="1" pages="16738" perpage="25" total="418438">
             */
            eventType = parser.next();
        }
    }


}
