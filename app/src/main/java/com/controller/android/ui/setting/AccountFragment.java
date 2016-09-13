package com.controller.android.ui.setting;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.controller.android.Application;
import com.controller.android.Constants;
import com.controller.android.R;
import com.controller.android.object.User;
import com.controller.android.util.UserUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {

    private SwipeMenuListView mListView;
    private AccountAdapter mAdapter;
    private List<User> mUserList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.account_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (SwipeMenuListView) view.findViewById(R.id.listView);
        mUserList = new ArrayList<>(UserUtil.fetchAllUser());
        mAdapter = new AccountAdapter(mUserList);
        mListView.setAdapter(mAdapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(Application.getInstance());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle(getResources().getString(R.string.edit));
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(Application.getInstance());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.mipmap.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        mListView.setMenuCreator(creator);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                User user = mUserList.get(position);
                switch (index) {
                    case 0:
                        showEditDialog(user, R.string.edit_account);
                        break;
                    case 1:
                        showDeleteDialog(user);
                        break;
                }
                return false;
            }
        });
    }

    private void showDeleteDialog(final User user) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        String message = getString(R.string.delete_user, user.getUserName());
        alert.setMessage(message);
        alert.setCancelable(true);
        alert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mUserList.remove(user);
                mAdapter.notifyDataSetChanged();
                writeToFile();
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

    private void writeToFile() {
        Context context = Application.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.ACCOUNT_LIST, new Gson().toJson(mUserList));
        editor.apply();
    }

    public void showEditDialog(final User user, int titleId) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_account, null);

        final EditText userNameEdit = (EditText) view.findViewById(R.id.user_name);
        final EditText passwordEdit = (EditText) view.findViewById(R.id.password);

        if (user != null) {
            String userName = user.getUserName();
            String password = user.getPassword();
            userNameEdit.setText(userName);
            userNameEdit.setSelection(userName.length());
            passwordEdit.setText(password);
            passwordEdit.setSelection(password.length());
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(titleId);
        alert.setView(view);
        alert.setCancelable(true);
        alert.setPositiveButton(R.string.global_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                String nameName = userNameEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                if (TextUtils.isEmpty(nameName)) {
                    Toast.makeText(getActivity(), R.string.user_name_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getActivity(), R.string.password_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (user != null) {
                    user.setUserName(nameName);
                    user.setPassword(password);
                } else {
                    mUserList.add(new User(nameName, password));
                }

                mAdapter.notifyDataSetChanged();
                writeToFile();
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

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
