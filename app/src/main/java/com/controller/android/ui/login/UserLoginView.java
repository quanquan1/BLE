package com.controller.android.ui.login;

import com.controller.android.object.User;

public interface UserLoginView {

    String getUserName();

    String getPassword();

    void toMainActivity(User user);

    void showFailedError();

}
