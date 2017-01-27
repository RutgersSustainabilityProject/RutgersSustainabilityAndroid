package com.rutgerssustainability.android.rutgerssustainability.pojos;

/**
 * Created by shreyashirday on 1/23/17.
 */
public class Trash {

    private String userId;
    private String picture;
    private double latitude;
    private double longitude;
    private long epoch;
    private String tags;

    public String getDeviceId() {
        return this.userId;
    }

    public String getPictureUrl() {
        return this.picture;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public long getEpoch() {
        return this.epoch;
    }

    public String getTags() {
        return this.tags;
    }

    public String getUniqueId() {
        return this.userId + this.epoch;
    }

}
