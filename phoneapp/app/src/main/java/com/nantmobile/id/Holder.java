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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.att.m2x.android.main.M2XAPI;

import idsdk.api.Camera;
import idsdk.api.Engine;

public class Holder extends AppCompatActivity implements Engine.OnCreateListener, Engine.OnErrorListener {
    private static final String CLIENT_ID = "HHUjj4FFCstG3TEBlytkhA";
    private static final String CLIENT_SECRET = "mQ2eKfCeO44BKriJ-ufZ0Q";

    private FragmentManager manager;
    private ActionBar actionBar;
    private ProgressBar progressBar;

    private Camera camera;
    private Engine engine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        M2XAPI.initialize(getApplicationContext(), "7620741335388aa83bcdacab007e2391");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = manager.findFragmentById(R.id.content_frame);
                if (fragment != null) {
                    fragment.onResume();
                    actionBar.setDisplayHomeAsUpEnabled(manager.getBackStackEntryCount() >= 1);
                }
            }
        });

        actionBar = getSupportActionBar();

        progressBar.setVisibility(View.VISIBLE);
        Engine.create(this, CLIENT_ID, CLIENT_SECRET, this, this);
    }

    @Override
    public void onStart() {
        this.camera = Camera.open(this);
        super.onStart();
    }

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;

        manager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content_frame, new Features())
                .commit();

        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onError(idsdk.api.Error error) {
        progressBar.setVisibility(View.INVISIBLE);
    }

    Camera getCamera() {
        return this.camera;
    }

    Engine getEngine() {
        return this.engine;
    }

    @Override
    public void onStop() {
        super.onStop();
        camera.release();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (manager.getBackStackEntryCount() >= 1) {
                    manager.popBackStack();
                }
                return true;
            default:
                return false;
        }
    }

    void showProgress(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
