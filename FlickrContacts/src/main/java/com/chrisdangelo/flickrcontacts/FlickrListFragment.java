package com.chrisdangelo.flickrcontacts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    private static final String LOG_TAG = "FlickrListFragmentLogTag";
    private String mSearchString;
    private ArrayList<FlickrPhoto> mPhotos;
    public static final String EXTRA_SEARCH_STRING = "com.chrisdangelo.flickrcontacts.search_string";

    // TODO display error if there is no network connection

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchString = (String)getActivity().getIntent().getSerializableExtra(EXTRA_SEARCH_STRING);
        Log.i(LOG_TAG, "List received " + mSearchString);

        new FetchItemsTask().execute(mSearchString);

        setupAdapter();
    }

    private void setupAdapter() {
        if (mPhotos != null) {
            setListAdapter(new PhotoAdapter(mPhotos));
        }
    }

    /*
     * This design comes from
     */
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

            FlickrPhoto p = getItem(position);

            TextView titleTextView = (TextView)convertView.
                    findViewById(R.id.photo_item_title);
            titleTextView.setText(p.getTitle());

            // TODO fill in with extra information
            TextView subtitleTextView = (TextView)convertView.
                    findViewById(R.id.photo_item_subtitle);
            subtitleTextView.setText("Subtitle Placeholder");

            // TODO do fancy image fetching

            return convertView;
        }
    }

}
