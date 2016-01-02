/*
 * Copyright 2015 Nantmobile, LLC. All rights reserved.
 *
 * This software is provided "as is", without warranty of any kind, express
 * or implied, including but not limited to the warranties of
 * merchantability, fitness for a particular purpose and noninfringement. In
 * no event shall the authors be liable for any claim, damages or other
 * liability, whether in an action of contract, tort or otherwise, arising
 * from, out of or in connection with the software or the use or other
 * dealings in the software.
 */

package com.nantmobile.id;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import idsdk.api.Engine;

public final class Features extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.activity_feature, container, false);
        ListView featureListView = (ListView) view.findViewById(R.id.feature);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item, R.id.list_item_tv, new String[]{
                getString(R.string.image_recognizer),
                getString(R.string.barcode_recognizer),
                getString(R.string.local_image_recognizer)
        });

        featureListView.setAdapter(adapter);
        featureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = new iDScreen().setMode(iDScreen.Mode.ImageQuery);
                        break;
                    case 1:
                        fragment = new iDScreen().setMode(iDScreen.Mode.BarcodeScanner);
                        break;
                    case 2:
                        fragment = new PackageBuilder();
                        break;
                }

                if (fragment != null) {
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.content_frame, fragment)
                            .addToBackStack(fragment.getTag())
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setTitle(R.string.app_name);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(Engine.SDK.VERSION);
        }
    }
}