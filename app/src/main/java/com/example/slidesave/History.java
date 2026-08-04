package com.example.slidesave;

public class History {

    private String key;
    private String alert_level;
    private String alert_message;
    private String moist_value;
    private String tilt_value;
    private String acc_value;
    private String barr_status;
    private String hist_dateTime;

    public History() {
        // Required for Firebase
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAlert_level() {
        return alert_level;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public String getMoist_value() {
        return moist_value;
    }

    public String getTilt_value() {
        return tilt_value;
    }

    public String getAcc_value() {
        return acc_value;
    }

    public String getBarr_status() {
        return barr_status;
    }

    public String getHist_dateTime() {
        return hist_dateTime;
    }
}