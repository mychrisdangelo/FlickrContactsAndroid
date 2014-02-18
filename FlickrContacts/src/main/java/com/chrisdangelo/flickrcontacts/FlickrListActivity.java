package com.chrisdangelo.flickrcontacts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

/**
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful sources:
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 10
 */
public class FlickrListActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate activity's view
        setContentView(R.layout.flickr_list_activity);

        // inflate fragments
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.flickr_list_fragment_container);

        String searchString = getIntent().getStringExtra(FlickrListFragment.EXTRA_SEARCH_STRING);

        if (fragment == null) {
            fragment = FlickrListFragment.newInstance(searchString);
            fm.beginTransaction()
                    .add(R.id.flickr_list_fragment_container, fragment)
                    .commit();
        }
    }
}
