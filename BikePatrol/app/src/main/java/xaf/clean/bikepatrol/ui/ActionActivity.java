package xaf.clean.bikepatrol.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
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
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import xaf.clean.bikepatrol.R;
import xaf.clean.bikepatrol.model.Report;
import xaf.clean.bikepatrol.util.ExternalStorage;
import xaf.clean.bikepatrol.util.TwitterApi;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static xaf.clean.bikepatrol.ui.HomeActivity.PERMISSIONS;
import static xaf.clean.bikepatrol.ui.HomeActivity.REQUEST_CHECK_SETTINGS;

public class ActionActivity extends AppCompatActivity {

    private CameraView mCameraView;
    private Vibrator mVibrator;
    private ActionTouchListener mActionTouchListener;
    private ActionCameraListener mActionCameraListener;
    private ActionLocationListener mActionLocationListener;
    private TwitterApi mTwitterApi;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private Realm realm;

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

        mActionTouchListener = new ActionTouchListener();
        mActionCameraListener = new ActionCameraListener();

        mCameraView.setCameraListener(mActionCameraListener);

        Sensey.getInstance().init(this);
        Sensey.getInstance().startTouchTypeDetection(this, mActionTouchListener);

        tryRequestLocationUpdates();

        realm = Realm.getDefaultInstance();
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
        realm.close();
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

    private enum State {
        IDLE, PHOTO, VIDEO
    }

    private class ActionCameraListener extends CameraListener {

        @Override
        public void onPictureTaken(byte[] jpeg) {
            super.onPictureTaken(jpeg);

            Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

            File output = ExternalStorage.getOutputMediaFile(ExternalStorage.MEDIA_TYPE_IMAGE);

            if (output != null) {
                try {
                    FileOutputStream out = new FileOutputStream(output);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    realm.executeTransaction(realm1 -> {
                        Report report = realm1.createObject(Report.class);
                        report.setType(Report.TYPE_PICTURE);

                        Location l = mActionLocationListener.getLocation();
                        if (l != null) {
                            report.setLatitude(l.getLatitude());
                            report.setLongitude(l.getLongitude());
                        }

                        report.setTimestamp(new Date().getTime());
                        report.setMediaAbsolutePath(output.getAbsolutePath());
                    });

                } catch (Exception e) {
                    Log.e(getClass().getName(), "Error writing picture", e);
                }
            }
        }

        @Override
        public void onVideoTaken(File video) {
            super.onVideoTaken(video);

            File output = ExternalStorage.getOutputMediaFile(ExternalStorage.MEDIA_TYPE_VIDEO);

            if (output != null) {
                try (FileInputStream is = new FileInputStream(video);
                     FileOutputStream os = new FileOutputStream(output)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }

                    realm.executeTransaction(realm1 -> {
                        Report report = realm1.createObject(Report.class);
                        report.setType(Report.TYPE_VIDEO);

                        Location l = mActionLocationListener.getLocation();
                        if (l != null) {
                            report.setLatitude(l.getLatitude());
                            report.setLongitude(l.getLongitude());
                        }

                        report.setTimestamp(new Date().getTime());
                        report.setMediaAbsolutePath(output.getAbsolutePath());
                    });
                } catch (Exception e) {
                    Log.e(getClass().getName(), "Error writing video", e);
                }
            }
        }
    }

    private class ActionTouchListener implements TouchTypeDetector.TouchTypListener {
        private State state = State.IDLE;

        @Override
        public void onTwoFingerSingleTap() {
        }

        @Override
        public void onThreeFingerSingleTap() {
        }

        @Override
        public void onDoubleTap() {
        }

        @Override
        public void onScroll(int scrollDirection) {
            if (state == State.IDLE)
                mVibrator.vibrate(10);
        }

        @Override
        public void onSingleTap() {
        }

        @Override
        public void onSwipe(int swipeDirection) {
            Log.d(getClass().getName(), "onSwipe");

            switch (swipeDirection) {
                case TouchTypeDetector.SWIPE_DIR_DOWN:
                    if (state == State.IDLE) {
                        state = State.PHOTO;
                        mCameraView.captureImage();
                        state = State.IDLE;
                    }
                    break;
                case TouchTypeDetector.SWIPE_DIR_UP:
                    if (state == State.IDLE) {
                        state = State.VIDEO;
                        mCameraView.startRecordingVideo();
                        mCameraView.postDelayed(() -> {
                            state = State.IDLE;
                            mCameraView.stopRecordingVideo();
                        }, 30000);
                    }
                    break;
                default:
                    finish();
                    break;
            }
        }

        @Override
        public void onLongPress() {
        }
    }
}
