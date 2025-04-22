package com.hfy.chat_room.model;

// 使用一个 Friend 类 表示一个好友
public class Friend {
    private int friendId;
    private String friendName;

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
}
