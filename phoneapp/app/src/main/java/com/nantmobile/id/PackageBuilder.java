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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import idsdk.api.ar.ARPackage;
import idsdk.api.ar.Overlay;

public final class PackageBuilder extends Fragment {
    static final String PARAMETER_ITEMS = "items";

    static class Item implements Parcelable {
        private final Uri markerUri;
        private final Uri overlayUri;
        private final Overlay.Type type;

        private Item(Uri markerUri, Uri overlayUri, Overlay.Type type) {
            this.markerUri = markerUri;
            this.overlayUri = overlayUri;
            this.type = type;
        }

        public Item(Parcel in) {
            String[] data = new String[3];
            in.readStringArray(data);
            this.markerUri = Uri.parse(data[0]);
            this.overlayUri = Uri.parse(data[1]);
            this.type = Overlay.Type.from(data[2]);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringArray(new String[]{
                    this.markerUri.toString(),
                    this.overlayUri.toString(),
                    this.type.toString()
            });
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public Item createFromParcel(Parcel in) {
                return new Item(in);
            }

            public Item[] newArray(int size) {
                return new Item[size];
            }
        };

        Uri getMarkerUri() {
            return this.markerUri;
        }

        Uri getOverlayUri() {
            return this.overlayUri;
        }

        Overlay.Type getType() {
            return this.type;
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

    private final ArrayList<Item> markers = new ArrayList<>();

    private class MarkersAdapter extends ArrayAdapter<Object> {
        public MarkersAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = markers.get(position);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            convertView = inflater.inflate(R.layout.marker_item, parent, false);

            TextView title = (TextView) convertView.findViewById(R.id.id);
            title.setText(item.getMarkerUri().toString());

            TextView description = (TextView) convertView.findViewById(R.id.path);
            description.setText(ARPackage.Marker.Type.Marker2D.toString());

            TextView overlayType = (TextView) convertView.findViewById(R.id.overlay_type);
            overlayType.setText(item.getType().toString());

            TextView overlayPath = (TextView) convertView.findViewById(R.id.overlay_path);
            overlayPath.setText(item.getOverlayUri().toString());

            ImageView preview = (ImageView) convertView.findViewById(R.id.preview);
            Bitmap bitmap = BitmapFactory.decodeFile(item.getMarkerUri().getEncodedPath(), null);
            preview.setImageBitmap(bitmap);

            return convertView;
        }

        @Override
        public int getCount() {
            return markers.size();
        }

        @Override
        public String getItem(int position) {
            return "";
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

    }

    private MarkersAdapter markersAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.package_builder, null);

        ListView list = (ListView) view.findViewById(R.id.markers);
        markersAdapter = new MarkersAdapter(getActivity());
        list.setAdapter(markersAdapter);

        TextView hint = (TextView) view.findViewById(R.id.hint);
        hint.setText(R.string.add_marker_hint);

        Button addMarker = (Button) view.findViewById(R.id.add_marker);
        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iDScreen fragment = new iDScreen().setMode(iDScreen.Mode.Camera);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content_frame, fragment)
                        .addToBackStack(fragment.getTag())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });

        Button startRecognition = (Button) view.findViewById(R.id.start_recognition);
        startRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iDScreen fragment = new iDScreen().setMode(iDScreen.Mode.AugmentedReality);
                Bundle arguments = new Bundle();
                arguments.putParcelableArrayList(PARAMETER_ITEMS, markers);
                fragment.setArguments(arguments);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content_frame, fragment)
                        .addToBackStack(fragment.getTag())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });

        return view;
    }

    void add(String path, String type, String uri) {
        markers.add(new Item(Uri.parse(path), Uri.parse(uri), Overlay.Type.from(type)));
        markersAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setTitle(R.string.local_image_recognizer);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(null);
        }
    }
}