package com.hfy.chat_room.model;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 获取指定会话的最后一条消息
    String getLastMessageBySessionId(int sessionId);

    // 获取指定会话的历史消息列表
    // 有的会话，历史消息可能特别多
    // 默认只取最近的 100 条消息
    List<Message> getMessagesBySessionId(int sessionId);

    // 通过这个类,向数据库中插入消息
    void add(Message message);
}
