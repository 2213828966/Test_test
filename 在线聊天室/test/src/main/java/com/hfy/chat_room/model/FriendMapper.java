package com.hfy.chat_room.model;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<Friend> selectFriendList(int userId);

    // 添加好友关系
    int addFriend(@Param("userId") int userId, @Param("friendId") int friendId);

    // 根据用户名查找用户
    User searchUserByName( String username);

    // 检查是否已经是好友
    int checkFriendship(@Param("userId") int userId, @Param("friendId") int friendId);

    // 删除好友关系
    int deleteFriend(@Param("userId") int userId, @Param("friendId") int friendId);
}
