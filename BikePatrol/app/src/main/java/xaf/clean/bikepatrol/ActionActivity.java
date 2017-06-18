package xaf.clean.bikepatrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.flurgle.camerakit.CameraView;
import com.github.nisrulz.sensey.Sensey;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import io.fabric.sdk.android.Fabric;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ActionActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = new String[]{
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
    };

    private CameraView mCameraView;
    private Vibrator mVibrator;
    private ActionTouchListener mActionTouchListener;
    private ActionCameraListener mActionCameraListener;
    private ActionLocationListener mActionLocationListener;
    private TwitterApi mTwitterApi;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private int REQUEST_CHECK_SETTINGS = 0xAF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        mTwitterApi = new TwitterApi().instantiate();

        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(uiOptions
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_action);

        mActionLocationListener = new ActionLocationListener();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mActionLocationListener)
                .addOnConnectionFailedListener(mActionLocationListener)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        mCameraView = (CameraView) findViewById(R.id.camera);
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        mActionTouchListener = new ActionTouchListener(mCameraView, mVibrator);
        mActionCameraListener = new ActionCameraListener(this);

        mCameraView.setCameraListener(mActionCameraListener);

        Sensey.getInstance().init(this);
        Sensey.getInstance().startTouchTypeDetection(this, mActionTouchListener);

        // TODO: interface
        //    new AsyncTask<Void, Void, Void>() {
        //
        //        @Override
        //        protected Void doInBackground(Void... params) {
        //            try {
        //                api.postText("test");
        //            } catch (TwitterException e) {
        //                Log.e(ActionActivity.class.getName(), "Update failed", e);
        //            }
        //            return null;
        //        }
        //    }.execute();

        tryRequestLocationUpdates();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            tryRequestLocationUpdates();
        }
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
        Sensey.getInstance().stopTouchTypeDetection();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mActionLocationListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                tryRequestLocationUpdates();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(findViewById(R.id.camera), "Permissions are required", Snackbar.LENGTH_INDEFINITE)
                        .setAction(
                                "Grant",
                                (view) -> ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS))
                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void tryRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS);
        }

        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest).setResultCallback(locationSettingsResult -> {
            final Status status = locationSettingsResult.getStatus();
            if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mActionLocationListener);
            } else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(getClass().getName(), "", e);
                }
            } else if (status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                Snackbar.make(findViewById(R.id.camera), "Permissions are required", Snackbar.LENGTH_INDEFINITE)
                        .setAction(
                                "Grant",
                                (view) -> ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS))
                        .show();
            }
        });
    }
}
