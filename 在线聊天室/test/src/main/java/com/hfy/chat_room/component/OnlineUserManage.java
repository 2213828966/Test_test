package com.hfy.chat_room.component;

import com.hfy.chat_room.Controller.WebSocketAPI;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

// 通过这个类来记录用户登录状态，同时也维护了 userId 和 WebSocketSession 的映射关系
@Component
public class OnlineUserManage {
    // 此处用这个哈希表是为了考虑线程安全问题
    private ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<Integer,WebSocketSession>();

    // 1) 用户上线，给 哈希表 插入键值对
    public void online(int userId, WebSocketSession session) {
        if(sessions.get(userId) != null){
            // 表示用户已经登录过了，不会重复添加键值对
            System.out.println("[" + userId +"] 正在登录，此次登录失败!" );
            return;
        }
        sessions.put(userId, session);
        System.out.println("[" + userId +"] 登录成功!" );
    }

    // 2) 用户下线
    public void offline(int userId, WebSocketSession session) {
        WebSocketSession existSession = sessions.get(userId);
        if(existSession == session){
            // 如果这俩 session 是同一个，才进行下线操作
            sessions.remove(userId);
            System.out.println("[" + userId +"] 下线!" );
        }
    }

    // 根据 userId 获取到 WebSocketSession
    public WebSocketSession getSession(int userId){
        return sessions.get(userId);
    }
}
