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

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.att.m2x.android.listeners.ResponseListener;
import com.att.m2x.android.model.Device;
import com.att.m2x.android.network.ApiV2Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import idsdk.api.Camera;
import idsdk.api.Engine;
import idsdk.api.Error;
import idsdk.api.ar.ARPackage;
import idsdk.api.ar.ARPlayer;
import idsdk.api.ar.ARView;
import idsdk.api.ar.Overlay;
import idsdk.api.model.Entity;
import idsdk.api.model.Recognition;
import idsdk.api.recognition.ContinuousRecognizer;
import idsdk.api.recognition.RecognitionResponse;
import idsdk.api.recognition.Recognizer;
import idsdk.api.recognition.Source;
import idsdk.internal.utils.Logger;
import android.provider.Settings.Secure;

public class iDScreen extends Fragment {
    private static final String EXAMPLE_LINK = "\"<a href=\\\"https://developer.theidplatform.com/sample-app\\\">https://developer.theidplatform.com/sample-app</a>\"";

    static final String PARAMETER_PATH = "path";

    private String android_id = "";
    static final String Grant = "a24dab0b6dfe2219";
    static final String Tony = "101df17a510a8cb1";
    private String user = "";


    private Button torch;
    private Button facing;
    private Button feedback;
    private Button action;

    private AppCompatActivity activity;

    private ARView preview;
    private Engine engine;
    private Camera camera;
    private ARPlayer augmentedRealityPlayer;
    private ContinuousRecognizer recognizerContinuous;

    enum Mode {
        ImageQuery,
        BarcodeScanner,
        Camera,
        AugmentedReality
    }

    private Mode mode = Mode.BarcodeScanner;

    private final Recognizer.OnRecognitionListener onRecognitionListener = new Recognizer.OnRecognitionListener() {
        @Override
        public void onRecognition(Recognizer recognizer, RecognitionResponse response) {
            android_id = Secure.getString(getContext().getContentResolver(),
                    Secure.ANDROID_ID);
            showProgress(false);
            setStatus();

            stop(mode);

            List<Recognition> results = response.getResults();
            if (results.size() == 0) {
                showToast(getString(R.string.no_match));
                return;
            }

            JSONArray array = new JSONArray();
            try {
                array = response.toJson().getJSONArray("included");
            } catch (Exception e) {
                Logger.e("", e.getMessage());
            }

            JSONObject params = new JSONObject();
            try {
                String title = array.getJSONObject(0).toString();
                title = title.replace("\"", "");
                int index = title.indexOf("title:");
                String tmp = title.substring(index);
                int end = tmp.indexOf(",");
                title = title.substring(index+6, index+end);

                if(android_id.equals(Grant)) {
                    user = "grant";
                    title += ",grant";
                } else if(android_id.equals(Tony)) {
                    user = "tony";
                    title += ",tony";
                }
                String jsonobj = "{\\\"title\\\":\\\"" +  title + "\\\"}";
                params = new JSONObject("{\"values\": {\"barcode\":\"" + jsonobj + "\"} }");
            //codes.put("barcode", barcode);
            //params.put("values", codes);
            } catch (Exception e){
                Logger.e("error", e.getMessage());
            }
            //JSONObject params = new JSONObject();

            String deviceID = "6b12c70565588ea596d9ceccdec9c108";

            Device.postDeviceUpdate(getActivity().getApplicationContext(), params, deviceID, new ResponseListener() {
                @Override
                public void onRequestCompleted(ApiV2Response apiV2Response, int i) {
                    Logger.i("PostUpdate Success!", apiV2Response.get_raw());
                }

                @Override
                public void onRequestError(ApiV2Response apiV2Response, int i) {
                    Logger.e("PostUpdate Fail", apiV2Response.get_raw());
                }
            });
            onRecognized(results.iterator().next().getEntity());
        }
    };

    private final Recognizer.OnRecognitionErrorListener onRecognitionErrorListener = new Recognizer.OnRecognitionErrorListener() {
        @Override
        public void onRecognitionError(Recognizer recognizer, idsdk.api.Error error) {
            showProgress(false);
            setStatus();
            showToast(error.toString());
        }
    };

    iDScreen setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_id, null);

        activity = (AppCompatActivity) getActivity();
        activity.setTitle(getTitle());
        setStatus();

        torch = (Button) view.findViewById(R.id.torch);
        torch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null && camera.hasTorch()) {
                    camera.turnTorch(!camera.isTorchOn());
                    torch.setSelected(camera.isTorchOn());
                }
            }
        });

        facing = (Button) view.findViewById(R.id.facing);
        facing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    camera.setFacing(camera.getFacing() == Camera.Facing.Back ? Camera.Facing.Front : Camera.Facing.Back);
                    facing.setSelected(camera.getFacing() == Camera.Facing.Back);

                    torch.setVisibility(camera.hasTorch() ? View.VISIBLE : View.INVISIBLE);
                }
            }
        });

        action = (Button) view.findViewById(R.id.action);
        action.setSelected(false);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.setSelected(!action.isSelected());
                if (action.isSelected()) {
                    start(mode);
                } else {
                    stop(mode);
                }
            }
        });

        final Button example = (Button) view.findViewById(R.id.example);
        example.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog exampleDialog = new Dialog(activity);
                exampleDialog.setTitle(getString(R.string.example));
                exampleDialog.setContentView(R.layout.example_dialog);

                TextView exampleLink = (TextView) exampleDialog.findViewById(R.id.example_link);
                exampleLink.setMovementMethod(LinkMovementMethod.getInstance());
                exampleLink.setText(Html.fromHtml(EXAMPLE_LINK));
                exampleDialog.show();
            }
        });

        feedback = (Button) view.findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (augmentedRealityPlayer != null && augmentedRealityPlayer.isStarted()) {
                    augmentedRealityPlayer.setVisualFeedbackEnabled(!augmentedRealityPlayer.isVisualFeedbackEnabled());
                    feedback.setSelected(augmentedRealityPlayer.isVisualFeedbackEnabled());
                }
            }
        });

        preview = (ARView) view.findViewById(R.id.preview);
        engine = ((Holder) activity).getEngine();
        camera = ((Holder) activity).getCamera();

        Bundle arguments = getArguments();
        if (arguments != null) {
            ArrayList<PackageBuilder.Item> items = arguments.getParcelableArrayList(PackageBuilder.PARAMETER_ITEMS);
            ARPackage arpackage = new ARPackage();
            if (items != null && items.size() > 0) {
                for (PackageBuilder.Item item : items) {
                    ARPackage.Marker marker = new ARPackage.Marker(item.getMarkerUri(), ARPackage.Marker.Type.Marker2D);
                    Overlay overlay = new Overlay.Builder(item.getType(), item.getOverlayUri()).build();
                    arpackage.add(new ARPackage.Item(marker, overlay));
                }
            }

            startAugmentedReality(arpackage);
        }
        return view;
    }

    private void retrieveCreative(Entity entity) {
        showProgress(true);
        setStatus(R.string.retrieving_creative);

        entity.retrieveCreative(new Entity.OnCreativeResponseListener() {
            @Override
            public void onPackageResponse(Entity entity, final ARPackage arPackage) {
                showProgress(false);
                setStatus();
                startAugmentedReality(arPackage);
            }

            @Override
            public void onHtmlResponse(Entity entity, final String url) {
                showProgress(false);
                setStatus();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }

            @Override
            public void onError(Entity entity, Error error) {
                showProgress(false);
                setStatus();
                showToast(getString(R.string.creative_not_available));
            }
        }, null);
    }

    private void startAugmentedReality(ARPackage arPackage) {
        if (augmentedRealityPlayer == null) {
            augmentedRealityPlayer = ARPlayer.create(engine, camera, preview);
            augmentedRealityPlayer.start();
        }

        action.setVisibility(View.INVISIBLE);

        augmentedRealityPlayer.load(arPackage);
        setStatus(R.string.ar_active);
    }

    private void stopAugmentedReality() {
        if (augmentedRealityPlayer != null && augmentedRealityPlayer.isStarted()) {
            augmentedRealityPlayer.stop().release();
            augmentedRealityPlayer = null;
        }

        action.setVisibility(View.VISIBLE);

        setStatus();
    }

    @Override
    public void onStart() {
        super.onStart();
        camera.startPreview(preview);
        torch.setVisibility(camera.hasTorch() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
    }

    @Override
    public void onStop() {
        super.onStop();
        camera.stopPreview();
        stop(mode);
        stopAugmentedReality();
    }

    private int getTitle() {
        switch (mode) {
            case ImageQuery:
                return R.string.image_recognizer;
            case BarcodeScanner:
                return R.string.barcode_recognizer;
            case Camera:
                return R.string.capture;
            case AugmentedReality:
                return R.string.local_image_recognizer;
            default:
                return 0;
        }
    }

    private void start(Mode mode) {
        switch (mode) {
            case ImageQuery: {
                setStatus(R.string.querying);
                Recognizer recognizer = engine.recognize(Recognizer.Type.Image, new Source(camera));
                recognizer.setOnRecognitionListener(onRecognitionListener);
                recognizer.setOnRecognitionErrorListener(onRecognitionErrorListener);
                break;
            }
            case BarcodeScanner: {
                setStatus(R.string.scanning);
                recognizerContinuous = engine.recognizeContinuously(Recognizer.Type.Barcode, new Source(camera));
                recognizerContinuous.setOnRecognitionListener(onRecognitionListener);
                recognizerContinuous.setOnRecognitionErrorListener(onRecognitionErrorListener);
                recognizerContinuous.start();
                break;
            }
            case Camera: {
                action.setSelected(false);
                PackageItemBuilder fragment = new PackageItemBuilder();
                Bundle arguments = new Bundle();
                arguments.putString(PARAMETER_PATH, takePicture());
                fragment.setArguments(arguments);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content_frame, fragment)
                        .addToBackStack(fragment.getTag())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                break;
            }
            default:
                break;
        }
    }

    private void stop(Mode mode) {
        switch (mode) {
            case BarcodeScanner:
                if (recognizerContinuous != null) {
                    recognizerContinuous.stop();
                    recognizerContinuous = null;
                }
                break;
            case ImageQuery:
            case Camera:
            default:
                break;
        }

        setStatus();
    }

    private void onRecognized(final Entity entity) {
        action.setSelected(false);
        final Dialog resultDialog = new Dialog(activity);
        resultDialog.setContentView(R.layout.result_dialog);
        resultDialog.setTitle(getString(R.string.result));
        resultDialog.show();

        Button scan_again = (Button) resultDialog.findViewById(R.id.fetch_creative);
        scan_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultDialog.dismiss();
            }
        });

        Button open_stats = (Button) resultDialog.findViewById(R.id.open_stats);
        open_stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.e-cycler.xyz"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private String takePicture() {
        Bitmap bitmap = camera.getFrame();
        File file = new File(activity.getCacheDir(), String.valueOf(System.currentTimeMillis()) + "." + ARPackage.AR_MARKER_2D_EXTENSION);
        FileOutputStream output;
        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Logger.e(e);
            return null;
        }

        if (bitmap != null) {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 30, output);
            try {
                output.flush();
                output.close();
            } catch (IOException e) {
                Logger.e(e);
                return null;
            }
        }

        return file.getAbsolutePath();
    }

    private void showToast(final String message) {
        action.setSelected(false);
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showProgress(boolean show) {
        ((Holder) activity).showProgress(show);
    }

    private void setStatus() {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(null);
        }
    }

    private void setStatus(final int resId) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(resId);
        }
    }
}
