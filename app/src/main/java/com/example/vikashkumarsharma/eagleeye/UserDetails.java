package com.example.vikashkumarsharma.eagleeye;

/**
 * Created by Vikash Kumar Sharma on 30-10-2017.
 */

public class UserDetails {
    private String mDeviceId;
    private String mUsername;

    public UserDetails() {
    }

    private String mEmail;
    private LocationDetails mLocation;

    public UserDetails(String mDeviceId, String mUsername, String mEmail, LocationDetails mLocation) {
        this.mDeviceId = mDeviceId;
        this.mUsername = mUsername;
        this.mEmail = mEmail;
        this.mLocation = mLocation;
    }

    public String getmDeviceId() {
        return mDeviceId;
    }

    public void setmDeviceId(String mDeviceId) {
        this.mDeviceId = mDeviceId;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public LocationDetails getmLocation() {
        return mLocation;
    }

    public void setmLocation(LocationDetails mLocation) {
        this.mLocation = mLocation;
    }
}
