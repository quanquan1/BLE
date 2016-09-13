package com.controller.android.model;

import com.controller.android.object.User;

public interface OnLoginListener {

    void onLoginSuccess(User user);

    void onLoginFailed();
}
