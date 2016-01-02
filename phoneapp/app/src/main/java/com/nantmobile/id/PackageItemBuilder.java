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

import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import idsdk.api.ar.Overlay;

public class PackageItemBuilder extends Fragment {
    private static final int DONE = 100001;

    private static final String HTML_OVERLAY_URL_DEFAULT = "http://www.nantmobile.com";
    private static final String VIDEO_OVERLAY_URL_DEFAULT = "http://d1l8mg14vomswk.cloudfront.net/local-recognition-sample-video.mp4";

    private EditText edit;
    private RadioGroup type;
    private String path;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.package_item_builder, null);

        Bundle arguments = getArguments();
        path = arguments.getString(iDScreen.PARAMETER_PATH);
        Bitmap bitmap = BitmapFactory.decodeFile(path, null);
        ImageView preview = (ImageView) view.findViewById(R.id.preview);
        preview.setImageBitmap(bitmap);

        edit = (EditText) view.findViewById(R.id.overlay_uri);
        edit.setText(HTML_OVERLAY_URL_DEFAULT);

        type = (RadioGroup) view.findViewById(R.id.radio_type);
        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_html:
                        edit.setText(HTML_OVERLAY_URL_DEFAULT);
                        break;
                    case R.id.radio_video:
                        edit.setText(VIDEO_OVERLAY_URL_DEFAULT);
                        break;
                    default:
                        break;
                }
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItemCompat.setShowAsAction(menu.add(Menu.NONE, DONE, Menu.NONE, R.string.done), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DONE:
                String overlayType = null;
                if (type.getCheckedRadioButtonId() == R.id.radio_html) {
                    overlayType = Overlay.Type.Html.toString();
                } else if (type.getCheckedRadioButtonId() == R.id.radio_video) {
                    overlayType = Overlay.Type.Video.toString();
                }

                FragmentManager manager = getActivity().getSupportFragmentManager();
                manager.popBackStackImmediate();
                manager.popBackStackImmediate();
                Fragment fragment = manager.findFragmentById(R.id.content_frame);
                if (fragment instanceof PackageBuilder) {
                    ((PackageBuilder) fragment).add(path, overlayType, edit.getText().toString());
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.add_overlay);
    }
}
