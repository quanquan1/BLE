package com.controller.android.ui.setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.controller.android.R;
import com.controller.android.object.User;
import com.controller.android.ui.common.BaseSwipListAdapter;

import java.util.List;

public class AccountAdapter extends BaseSwipListAdapter {

    private List<User> mUserList;

    public AccountAdapter(List<User> userList) {
        mUserList = userList;
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
            convertView = mInflater.inflate(R.layout.user_list_item, parent, false);
            viewHolder.userName = (TextView) convertView.findViewById(R.id.user_name);
            viewHolder.password = (TextView) convertView.findViewById(R.id.password);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        User user = (User) getItem(position);
        viewHolder.userName.setText(user.getUserName());
        viewHolder.password.setText(user.getPassword());
        return convertView;
    }

    class ViewHolder {
        TextView userName;
        TextView password;
    }

    @Override
    public boolean getSwipEnableByPosition(int position) {
        if (position % 2 == 0) {
            return false;
        }
        return true;
    }
}
