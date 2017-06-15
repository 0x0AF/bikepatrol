package xaf.clean.bikepatrol;

import android.content.Context;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;

import java.io.File;

import io.fabric.sdk.android.Fabric;

public class HomeActivity extends AppCompatActivity {

    private CameraView cameraView;
    private Vibrator vibrator;
    private final TouchTypeDetector.TouchTypListener touchTypListener = new TouchTypeDetector.TouchTypListener() {
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
            vibrator.vibrate(10);
        }

        @Override
        public void onSingleTap() {
            Log.d(getClass().getName(), "onSingleTap");
            vibrator.vibrate(500);
            cameraView.captureImage();
        }

        @Override
        public void onSwipe(int swipeDirection) {
            Log.e(getClass().getName(), "onSwipe");
            cameraView.startRecordingVideo();
            cameraView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cameraView.stopRecordingVideo();
                }
            }, 10000);
        }

        @Override
        public void onLongPress() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_action);

        cameraView = (CameraView) findViewById(R.id.camera);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        final TwitterApi api = new TwitterApi();
        api.instantiate();

        Sensey.getInstance().init(this);
        Sensey.getInstance().startTouchTypeDetection(this, touchTypListener);

        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(YuvImage yuv) {
                super.onPictureTaken(yuv);
                // TODO
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                // TODO
            }
        });

        // TODO: interface
        //    new AsyncTask<Void, Void, Void>() {
        //
        //        @Override
        //        protected Void doInBackground(Void... params) {
        //            try {
        //                api.postText("test");
        //            } catch (TwitterException e) {
        //                Log.e(HomeActivity.class.getName(), "Update failed", e);
        //            }
        //            return null;
        //        }
        //    }.execute();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
        Sensey.getInstance().stopTouchTypeDetection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }
}
