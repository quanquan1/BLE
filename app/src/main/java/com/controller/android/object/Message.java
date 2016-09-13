package com.controller.android.object;

public class Message {
    String message;
    MessageType mMessageType;

    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return mMessageType;
    }

    public enum MessageType {
        SEND, RECEIVE
    }

    public Message(String message, MessageType messageType) {
        this.message = message;
        mMessageType = messageType;
    }
}