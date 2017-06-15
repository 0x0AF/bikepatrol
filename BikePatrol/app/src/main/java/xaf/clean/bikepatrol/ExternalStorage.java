package xaf.clean.bikepatrol;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


@SuppressWarnings("WeakerAccess")
class ExternalStorage {

    public static final int MEDIA_DOCUMENT = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 3;

    static File getOutputMediaDirectory(int type) {
        String mediaType;

        switch (type) {
            case MEDIA_TYPE_IMAGE:
                mediaType = Environment.DIRECTORY_PICTURES;
                break;
            case MEDIA_TYPE_VIDEO:
                mediaType = Environment.DIRECTORY_MOVIES;
                break;
            case MEDIA_TYPE_AUDIO:
                mediaType = Environment.DIRECTORY_MUSIC;
                break;
            default:
                mediaType = Environment.DIRECTORY_DOWNLOADS;
                break;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(mediaType) + "/BikePatrol/");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(ExternalStorage.class.getName(), "failed to create directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    static File getOutputMediaFile(int type) {

        File mediaStorageDir = getOutputMediaDirectory(type);

        if (mediaStorageDir != null) {
            //noinspection ResultOfMethodCallIgnored
            mediaStorageDir.mkdirs();
            String timeStamp = SimpleDateFormat.getDateInstance(DateFormat.FULL).format(new Date());
            if (type == MEDIA_TYPE_IMAGE) {
                return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            } else if (type == MEDIA_TYPE_VIDEO) {
                return new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
