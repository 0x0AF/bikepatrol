package xaf.clean.bikepatrol;

import android.os.Vibrator;
import android.util.Log;

import com.flurgle.camerakit.CameraView;
import com.github.nisrulz.sensey.TouchTypeDetector;

class ActionTouchListener implements TouchTypeDetector.TouchTypListener {
    private final CameraView cameraView;
    private final Vibrator vibrator;
    private State state = State.IDLE;

    ActionTouchListener(CameraView cameraView, Vibrator vibrator) {
        this.cameraView = cameraView;
        this.vibrator = vibrator;
    }

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
            vibrator.vibrate(10);
    }

    @Override
    public void onSingleTap() {
    }

    @Override
    public void onSwipe(int swipeDirection) {
        Log.d(getClass().getName(), "onSwipe");
        if (state == State.IDLE) {
            state = State.VIDEO;
            cameraView.startRecordingVideo();
            cameraView.postDelayed(() -> {
                state = State.IDLE;
                cameraView.stopRecordingVideo();
            }, 10000);
        }
    }

    @Override
    public void onLongPress() {
        if (state == State.IDLE)
            cameraView.toggleFacing();
    }

    private enum State {
        IDLE, PHOTO, VIDEO
    }
}
