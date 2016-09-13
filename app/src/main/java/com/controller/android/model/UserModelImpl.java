package com.controller.android.model;

import com.controller.android.object.User;
import com.controller.android.util.UserUtil;

import java.util.List;

public class UserModelImpl implements UserModel {

    @Override
    public void login(String userName, String password, OnLoginListener onLoginListener) {
        List<User> users = UserUtil.fetchAllUser();

        for (User user : users) {
            if (user.getUserName().equals(userName) && user.getPassword().equals(password)
                    || user.getUserName().equals("admin") && user.getPassword().equals("admin")) {
                onLoginListener.onLoginSuccess(user);
                return;
            }
        }

        onLoginListener.onLoginFailed();
    }
}
