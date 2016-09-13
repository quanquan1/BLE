package com.controller.android.ui.setting;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.controller.android.R;
import com.controller.android.ui.BaseActivity;

public class SettingFragment extends PreferenceFragmentCompat {

    private Preference mAccountPreference;
    private Preference mAboutPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        initPreferences();
    }

    private void initPreferences() {
        final Resources resources = getResources();

        String accountKey = resources.getString(R.string.account);
        mAccountPreference = findPreference(accountKey);
        mAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BaseActivity activity = (BaseActivity) getActivity();
                activity.startActivity(new Intent(activity, AccountActivity.class));
                activity.startActivityAnimation(activity);
                return false;
            }
        });

        String aboutKey = resources.getString(R.string.about);
        mAboutPreference = findPreference(aboutKey);
        mAboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BaseActivity activity = (BaseActivity) getActivity();
                activity.startActivity(new Intent(activity, AboutActivity.class));
                activity.startActivityAnimation(activity);
                return false;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Ignore
    }
}
