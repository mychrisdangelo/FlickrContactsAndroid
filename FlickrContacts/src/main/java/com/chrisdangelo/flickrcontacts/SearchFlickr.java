package com.chrisdangelo.flickrcontacts;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by chrisdangelo on 2/17/14.
 *
 * Helpful sources:
 * http://developer.android.com/training/basics/intents/result.html
 * Android Programming: Big Nerd Ranch Guide (2013) Ch. 21
 */
public class SearchFlickr extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_flickr);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SearchFlickrFragment())
                    .commit();
        }
    }

    public static class SearchFlickrFragment extends Fragment {

        private static final int REQUEST_CONTACT = 0;

        private EditText mSearchTextField;
        private Button mContactsButton;
        private Button mSearchButton;

        public SearchFlickrFragment() {
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != Activity.RESULT_OK) return;

            if (requestCode == REQUEST_CONTACT) {
                Uri contactUri = data.getData();

                String[] queryFields = { ContactsContract.Contacts.DISPLAY_NAME };

                // perform the query on the contact to get the name column
                Cursor c = getActivity().getContentResolver()
                        .query(contactUri, queryFields, null, null, null);

                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    c.close();
                    return;
                }

                // Pull out the first column of the first row of data
                // that is your suspects name
                c.moveToFirst();
                String contactName = c.getString(0); // only one column returned
                mSearchTextField.setText(contactName); // artificially refresshing it
                c.close();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search_flickr, container, false);

            mSearchTextField = (EditText)rootView.findViewById(R.id.search_text_field);
            // TODO text will be lost on return this activity

            mContactsButton = (Button)rootView.findViewById(R.id.pick_contacts_button);
            mContactsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(i, REQUEST_CONTACT);
                }
            });

            mSearchButton = (Button)rootView.findViewById(R.id.search_button);
            mSearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), FlickrListActivity.class);
                    String input = mSearchTextField.getText().toString();
                    i.putExtra(FlickrListFragment.EXTRA_SEARCH_STRING, input);
                    startActivityForResult(i, 0); // 0 = not looking for result
                }
            });

            return rootView;
        }
    }

}
