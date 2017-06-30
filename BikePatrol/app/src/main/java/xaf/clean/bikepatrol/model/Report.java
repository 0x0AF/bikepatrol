package xaf.clean.bikepatrol.model;

import io.realm.RealmObject;

public class Report extends RealmObject {

    public static final String TYPE_PICTURE = "TYPE_PICTURE";
    public static final String TYPE_VIDEO = "TYPE_VIDEO";

    private long timestamp;
    private String type;
    private String mediaAbsolutePath;
    private double latitude;
    private double longitude;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaAbsolutePath() {
        return mediaAbsolutePath;
    }

    public void setMediaAbsolutePath(String mediaAbsolutePath) {
        this.mediaAbsolutePath = mediaAbsolutePath;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
