package com.controller.android.ui.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.controller.android.Config;
import com.controller.android.R;
import com.controller.android.event.BusProvider;
import com.controller.android.event.FinishActivityEvent;
import com.controller.android.ui.BaseActivity;
import com.controller.android.ui.login.LoginActivity;

public class AccountActivity extends BaseActivity {

    private AccountFragment mAccountFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.account);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        mAccountFragment = (AccountFragment) fragmentManager.findFragmentById(R.id.account_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                mAccountFragment.showEditDialog(null, R.string.add_account);
                break;
            case R.id.menu_logout:
                confirmUnregisterAccount();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmUnregisterAccount() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.unregister_account_confirm);
        alert.setCancelable(true);
        alert.setPositiveButton(R.string.global_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Config.getInstance().clear();
                startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                startActivityAnimation(AccountActivity.this);

                BusProvider.getInstance().post(new FinishActivityEvent());
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }
}
