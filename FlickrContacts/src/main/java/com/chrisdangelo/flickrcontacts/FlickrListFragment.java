package com.chrisdangelo.flickrcontacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
 * http://stackoverflow.com/questions/19351160/implements-onscrolllistener-inside-listfragment
 * http://stackoverflow.com/questions/10316743/detect-end-of-scrollview
 * http://stackoverflow.com/questions/12206259/adding-footer-to-one-of-the-listfragments-listview-automatically-adds-footer-to
 * http://stackoverflow.com/questions/2250770/how-to-refresh-android-listview
 * http://stackoverflow.com/questions/21374432/cant-set-a-custom-listview-on-listfragment
 * https://github.com/shontauro/android-pulltorefresh-and-loadmore
 */
public class FlickrListFragment extends ListFragment implements OnScrollListener {
    private static final String TAG = "FlickrListFragmentLogTag";
    private String mSearchString;
    private int mCurrentPage = 0;
    private boolean mCurrentlyLoading;
    private boolean mFooterViewExists;
    private ArrayList<FlickrPhoto> mPhotos;
    public static final String EXTRA_SEARCH_STRING = "com.chrisdangelo.flickrcontacts.search_string";
    ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchString = (String)getActivity().getIntent().getSerializableExtra(EXTRA_SEARCH_STRING);
        Log.i(TAG, "List received " + mSearchString);

        mFooterViewExists = false;
        mPhotos = new ArrayList<FlickrPhoto>();
        new FetchItemsTask().execute(mSearchString, Integer.toString(mCurrentPage));

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

    private void addLoadingFooterView() {
        ListView lv = getListView();
        View v = getActivity().getLayoutInflater().inflate(R.layout.list_footer, null);
        lv.addFooterView(v);
        lv.setOnScrollListener(this);
        mFooterViewExists = true;
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
            mCurrentlyLoading = true;
            return new FlickrFetcher().fetchPhotos(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<FlickrPhoto> photos) {
            mPhotos.addAll(photos);
            ((PhotoAdapter)getListAdapter()).notifyDataSetChanged();
            mCurrentlyLoading = false;
            if (!mFooterViewExists) {
                addLoadingFooterView();
            }
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

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // do nothing. Override required by abstract OnScrollListener
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // wait until we see the footer
        if(view.getLastVisiblePosition() == (mPhotos.size())) {
            Log.i(TAG, "Last position seen");
            if (!mCurrentlyLoading) {
                Log.i(TAG, "End of List Loading Triggered");
                new FetchItemsTask().execute(mSearchString, Integer.toString(++mCurrentPage));
            }
        }
    }

}
