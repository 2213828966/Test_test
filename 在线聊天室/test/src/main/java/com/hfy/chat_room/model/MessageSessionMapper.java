package com.hfy.chat_room.model;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageSessionMapper {
    // 1. 根据 userId 获取到该用户都在哪些会话中存在 ，返回结果是一组 sessionId
    List<Integer> getSessionIdsByUserId(int userId);

    // 2. 根据 sessionId 再来查询这个会话都包含了哪些用户(刨除最初的自己)
    List<Friend> getFriendsBySessionId(int sessionId,int selfUserId);

    // 3. 新增一个会话记录 ，返回会话的 id
    // 这样的方法返回值 int 表示的是插入操作影响到几行, 此处获取sessionId 是通过参数的 messageSession 的 sessionId 属性获取的
    int addMessageSession(MessageSession messageSession);

    // 4. 给 message_session_user 表页新增对应的记录
    void addMessageSessionUser(MessageSessionUserItem messageSessionUserItem);

    // 删除会话
    int deleteSession(@Param("sessionId") int sessionId);

    // 删除会话相关的所有消息
    int deleteSessionMessages(@Param("sessionId") int sessionId);

    // 删除 会话ID
    void deleteSessionId(@Param("sessionId") int sessionId);

    // 获取两个用户之间的会话ID
    Integer getSessionId(@Param("userId1") int userId1, @Param("userId2") int userId2);
}
