package com.controller.android.model;

public interface UserModel {
    void login(String userName, String password, OnLoginListener onLoginListener);
}
