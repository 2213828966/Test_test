package com.hfy.chat_room.model;

// 使用这个类的对象来表示 message_session_user 表里的一个记录
public class MessageSessionUserItem {
    private int sessionId;
    private int userId;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
