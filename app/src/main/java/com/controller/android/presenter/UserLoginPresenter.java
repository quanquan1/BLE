package com.controller.android.presenter;

import com.controller.android.model.UserModelImpl;
import com.controller.android.object.User;
import com.controller.android.model.OnLoginListener;
import com.controller.android.ui.login.UserLoginView;

public class UserLoginPresenter {

    private UserLoginView mUserLoginView;
    private UserModelImpl mUserModelImpl;

    public UserLoginPresenter(UserLoginView userLoginView) {
        mUserLoginView = userLoginView;
        mUserModelImpl = new UserModelImpl();
    }

    public void login() {

        mUserModelImpl.login(mUserLoginView.getUserName(), mUserLoginView.getPassword(), new OnLoginListener() {
            @Override
            public void onLoginSuccess(User user) {
                mUserLoginView.toMainActivity(user);
            }

            @Override
            public void onLoginFailed() {
                mUserLoginView.showFailedError();
            }
        });
    }


}
