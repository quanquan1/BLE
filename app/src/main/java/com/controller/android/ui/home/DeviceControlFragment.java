package com.controller.android.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.controller.android.R;
import com.controller.android.object.Message;

import java.util.ArrayList;
import java.util.List;

public class DeviceControlFragment extends Fragment {

    private String mDeviceName;

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public interface ControlCallBack {
        void send(String character);
    }

    private EditText mInstructionEdit;
    private Button mSendButton;
    private ListView mListView;

    private ControlCallBack mControlCallBack;

    private MessageAdapter mMessageAdapter;

    private List<Message> mMessageList = new ArrayList<>();

    public void setControlCallBack(ControlCallBack controlCallBack) {
        mControlCallBack = controlCallBack;
    }

    public void addMessage(Message message) {
        mMessageList.add(message);
        mMessageAdapter.notifyDataSetChanged();

        scrollToBottom();
    }

    private void scrollToBottom() {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mListView.setSelection(mMessageAdapter.getCount() - 1);
            }
        });
    }

    public void enableSendButton(boolean enable) {
        mSendButton.setEnabled(enable);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_control, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInstructionEdit = (EditText) view.findViewById(R.id.instruction);
        mSendButton = (Button) view.findViewById(R.id.send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String instruction = mInstructionEdit.getText().toString().trim();

                if (TextUtils.isEmpty(instruction)) {
                    return;
                }

                // send instruction
                if (mControlCallBack != null) {
                    mControlCallBack.send(instruction);
                }

            }
        });

        enableSendButton(false);
        mListView = (ListView) view.findViewById(R.id.content);
        mMessageAdapter = new MessageAdapter(mMessageList);
        mListView.setAdapter(mMessageAdapter);
    }

    public void clearEditText() {
        mInstructionEdit.setText("");
    }

    class MessageAdapter extends BaseAdapter {

        private List<Message> mMessageList = new ArrayList<>();

        public MessageAdapter(List<Message> messageList) {
            mMessageList = messageList;
        }

        @Override
        public int getCount() {
            return mMessageList.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getContext(), R.layout.message_item, null);
                holder.title = (TextView) convertView.findViewById(R.id.message_type);
                holder.info = (TextView) convertView.findViewById(R.id.message_info);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Message message = (Message) getItem(position);
            String messageName = message.getMessageType() == Message.MessageType.SEND ? "Me" : mDeviceName;
            holder.title.setText(messageName + " : ");
            holder.info.setText(message.getMessage());
            return convertView;
        }

        class ViewHolder {
            public TextView title;
            public TextView info;
        }
    }


}
