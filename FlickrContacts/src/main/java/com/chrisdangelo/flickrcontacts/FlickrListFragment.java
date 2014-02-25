package com.chrisdangelo.flickrcontacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful sources:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 9
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 10
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 26
 * http://developer.android.com/training/basics/network-ops/connecting.html
 * http://www.deitel.com/articles/java_tutorials/20060106/VariableLengthArgumentLists.html
 */
public class FlickrListFragment extends ListFragment {
    private static final String TAG = "FlickrListFragmentLogTag";
    private String mSearchString;
    private int mCurrentPage = 0;
    private ArrayList<FlickrPhoto> mPhotos;
    public static final String EXTRA_SEARCH_STRING = "com.chrisdangelo.flickrcontacts.search_string";
    ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchString = (String)getActivity().getIntent().getSerializableExtra(EXTRA_SEARCH_STRING);
        Log.i(TAG, "List received " + mSearchString);

        new FetchItemsTask().execute(mSearchString);

        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");

        setupAdapter();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FlickrPhoto p = ((PhotoAdapter)getListAdapter()).getItem(position);

        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(p.getPhotoUrlMedium()));
        startActivity(i);
    }

    private void setupAdapter() {
        if (mPhotos != null) {
            setListAdapter(new PhotoAdapter(mPhotos));
        }
    }

    public static FlickrListFragment newInstance(String searchString) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SEARCH_STRING, searchString);

        FlickrListFragment fragment = new FlickrListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private class FetchItemsTask extends AsyncTask<String, Void, ArrayList<FlickrPhoto>> {
        @Override
        protected ArrayList<FlickrPhoto> doInBackground(String... params) {
            return new FlickrFetcher().fetchPhotos(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<FlickrPhoto> photos) {
            mPhotos = photos;
            setupAdapter();
        }
    }

    private class PhotoAdapter extends ArrayAdapter<FlickrPhoto> {
        public PhotoAdapter(ArrayList<FlickrPhoto> photos) {
            super(getActivity(), 0, photos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_photo, null);
            }

            // Set all the lightweight items. The photos we will queue
            // for another thread to pick up
            FlickrPhoto p = getItem(position);

            TextView titleTextView = (TextView)convertView
                    .findViewById(R.id.photo_item_title);
            titleTextView.setText(p.getTitle());

            TextView subtitleTextView = (TextView)convertView
                    .findViewById(R.id.photo_item_subtitle);
            subtitleTextView.setText(p.getDescription());

            TextView ownerTextView = (TextView)convertView
                    .findViewById(R.id.photo_item_ownersname);
            ownerTextView.setText("Owner: " + p.getOwnerName());

            TextView dateTakenTextView = (TextView)convertView
                    .findViewById(R.id.photo_item_datetaken);
            dateTakenTextView.setText("Date: " + p.getDateTaken());

            ImageView imageView = (ImageView)convertView
                    .findViewById(R.id.photo_item_image_view);
            imageView.setImageResource(R.drawable.placeholder);

            mThumbnailThread.queueThumbnail(imageView, p.getPhotoUrlSmall());

            return convertView;
        }
    }


}
