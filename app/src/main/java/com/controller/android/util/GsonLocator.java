package com.controller.android.util;

import com.google.gson.Gson;

public class GsonLocator {
    private static final Gson sGson = new Gson();

    public static Gson get() {
        return sGson;
    }
}
