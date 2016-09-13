package com.controller.android.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.controller.android.Config;
import com.controller.android.R;
import com.controller.android.object.User;
import com.controller.android.presenter.UserLoginPresenter;
import com.controller.android.ui.BaseActivity;
import com.controller.android.ui.home.MainActivity;

public class LoginActivity extends BaseActivity implements UserLoginView {

    private final UserLoginPresenter mUserLoginPresenter = new UserLoginPresenter(this);

    private EditText mUserNameEdit;
    private EditText mPasswordEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User currentUser = Config.getInstance().getCurrentUser();
        if (!TextUtils.isEmpty(currentUser.getUserName())
                && !TextUtils.isEmpty(currentUser.getPassword())) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.login);

        mUserNameEdit = (EditText) findViewById(R.id.user_name);
        mPasswordEdit = (EditText) findViewById(R.id.password);

        Button loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserLoginPresenter.login();
            }
        });
    }

    @Override
    public String getUserName() {
        return mUserNameEdit.getText().toString();
    }

    @Override
    public String getPassword() {
        return mPasswordEdit.getText().toString();
    }

    @Override
    public void toMainActivity(User user) {
        // login success
        Config.getInstance().saveUser(user);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showFailedError() {
        Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show();
    }
}
