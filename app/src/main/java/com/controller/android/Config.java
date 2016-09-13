package com.controller.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.controller.android.object.User;

public class Config {

    private static Config sConfig = new Config();

    private User mUser;

    private Config() {

    }

    public static Config getInstance() {
        return sConfig;
    }

    public void saveUser(User user) {
        mUser = user;

        Context context = Application.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.USER_NAME, user.getUserName());
        editor.putString(Constants.PASSWORD, user.getPassword());
        editor.apply();
    }

    public User getCurrentUser() {
        if (mUser != null) {
            return mUser;
        }
        Context context = Application.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userName = prefs.getString(Constants.USER_NAME, "");
        String password = prefs.getString(Constants.PASSWORD, "");
        return new User(userName, password);
    }

    public void clear() {
        mUser = null;

        Context context = Application.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.USER_NAME);
        editor.remove(Constants.PASSWORD);
        editor.apply();
    }
}
