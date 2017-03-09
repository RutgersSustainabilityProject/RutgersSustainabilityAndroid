package com.rutgerssustainability.android.rutgerssustainability.pojos;

/**
 * Created by shreyashirday on 3/9/17.
 */
public class Noise {

    private String userId;
    private String audio;
    private double latitude;
    private double longitude;
    private double decibels;
    private long epoch;
    private String tags;

    public String getDeviceId() {
        return this.userId;
    }

    public String getAudioUrl() {
        return this.audio;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getDecibels() {
        return this.decibels;
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
