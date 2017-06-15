package xaf.clean.bikepatrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.flurgle.camerakit.CameraListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

class ActionCameraListener extends CameraListener {
    private final Context context;

    ActionCameraListener(Context context) {
        this.context = context;
    }

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
            } catch (Exception e) {
                Log.e(getClass().getName(), "Error writing video", e);
            }
        }
    }
}
