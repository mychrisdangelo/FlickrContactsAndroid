package com.chrisdangelo.flickrcontacts;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by chrisdangelo on 2/18/14.
 *
 * Helfpul Resource:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 27<--NOTE
 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 *
 * NOTE: This class comes from Ch. 27. It is perhaps simpler to implement Asynchronous loading of
 * images using AsyncTask alone but Ch. 27 advocates this level of indirection
 * because calling AsyncTask repeatedly for this kind of task is not robust.
 *
 * I have utilized this implementation directly because it is a more challenging learning experience
 * and a more elegant solution. While the code is directly from Ch. 27, I have provided comments
 * to support my complete understanding of the implementation.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    /*
     * In the case of this application the Token is always the ImageView
     * String is the url. When an ImageView is presented to the map the return value
     * will be a url. This map is created using .synchronizedMap. A Java threadsafe version.
     */
    Handler mHandler;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    /*
     * Variable hold a Handler passed from the main thread.
     */
    Handler mResponseHandler;
    Listener<Token> mListener;

    public interface Listener<Token> {
        /*
         * interface to function that will be implemented in
         * FlickrListFragment. on the main UI thread.
         */
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG); // for debugging
        mResponseHandler = responseHandler;
    }

    /*
     * Here, Android Lint will warn you about subclassing Handler. The Handler will be
     * kept alive by its Looper. So if the Handler is an anonymous inner class, it
     * is easy to leak memory accidentally through an implicit object reference. Here,
     * though, everything is tied to the HandlerThread, so there is no danger of leaking anything.
     *
     * OnLooperPepared() is called before the Looper checks the queue for the first
     * time. This makes it a good place to create the Handler implementation
     */
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        /*
         * A Handler is attached to exactly one Looper. A message is attached
         * to exactly one target handler called its target
         * A Looper has a whole queue of Messages
         *
         * By default, the Handler will attach itself to the
         * Looper for the current thread
         *
         * A handler always has reference to its looper
         */
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /*
                 * first check the message type. could be one of many kinds
                 */
                if (msg.what == MESSAGE_DOWNLOAD) {
                    /*
                     * @SuppressWarnings("unchecked") is necessary because Token is a
                     * generic class argument, but msg.obj is an Object. Due
                     * to type erasure, it is not possible to actually make this
                     * cast. (type erasure)
                     */
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj; // pull out the obj from the msg
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token, String url) {
        Log.i(TAG, "Got an URL: " + url);
        /*
         * store the ImageView and the url for that ImageView
         */
        requestMap.put(token, url);

        /*
         * Handler.obtainMessage() pulls from a common recycling pool to avoid creating
         * new Message objects, so it is also more efficient than creating new instances.
         *
         * Each message contains a "what", "obj" and "target"
         *
         * When we obtain this message we set the "what" to MESSAGE_DOWNLOAD
         * an integer that decribes the message
         *
         * We set the obj to the token (in this case the ImageView)
         *
         * And we've set the target of mHandler already when onLooperPrepared
         * that was called by the HandlerThread (the mother class here).
         *
         * Once we've obtained the message we call sendToTarget to send the message
         * to the handler. The handler will then put the Message at the end of the
         * Looper's message queue
         */
        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }

    private void handleRequest(final Token token) {
        try {
            /*
             * pull the url back out of the map
             */
            final String url = requestMap.get(token);
            if (url == null) return;

            // actually download the bytes
            byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);

            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            /*
             * Handler.post(Runnable) is a convenience method for posting Messages
             * that look like this:
             *
             *  Runnable myRunnable = new Runnable() {
             *      public void run() {
             *          << code here >>
             *      }
             *  }
             *  Message m = mHandler.obatinMessage();
             *  m.callback = myRunnable;
             *
             * When a Message has its callback field set, instead of being run by its Handler
             * target, the Runnable in callback is run instead.
             *
             * (The alternative is sending a custom Message, which would require subclassing
             * Handler with an override of handleMessage() the way we had for queueThumbnail.
             */
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    /*
                     * this check is necessary because ListView recycles its views. By the time
                     * ThumbnailDownloader finishes downloading the Bitmap, ListView
                     * may have recycled the ImageView * and requested a different URL for it.
                     */
                    if (requestMap.get(token) != url) return;

                    requestMap.remove(token);
                    /*
                     * mListener will set our thumbnail image finally
                     */
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image" + ioe);
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}

