package com.controller.android.object;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("username")
    private String mUserName;

    @SerializedName("password")
    private String mPassword;

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public User(String userName, String password) {
        mUserName = userName;
        mPassword = password;
    }
}
