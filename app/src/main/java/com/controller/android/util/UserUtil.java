package com.controller.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.controller.android.Application;
import com.controller.android.Constants;
import com.controller.android.object.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;

public class UserUtil {

    private static final String USER_ASSETS = "user.json";

    public static List<User> fetchAllUser() {
        Gson gson = GsonLocator.get();

        Context context = Application.getInstance();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.ACCOUNT_LIST, "");

        if (TextUtils.isEmpty(json)) {
            json = FileUtils.getFromAssets(USER_ASSETS, context);
        }

        User[] array = gson.fromJson(json, new TypeToken<User[]>() {
        }.getType());

        return Arrays.asList(array);
    }
}
