package com.hfy.chat_room.Controller;

import com.alibaba.fastjson.JSONObject;
import com.hfy.chat_room.model.Friend;
import com.hfy.chat_room.model.FriendMapper;
import com.hfy.chat_room.model.MessageSessionMapper;
import com.hfy.chat_room.model.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class FriendAPI {
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private MessageSessionMapper messageSessionMapper;



    ////////////////////////////////////////////////////////////////////////////////////
// 获取登录用户的 好友列表
////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("/friendList")
    @ResponseBody
    public Object getFriendList(HttpServletRequest req) {
        // 1. 先从会话中获取到 userId
        HttpSession session = req.getSession(false);
        if(session == null) {
            // 用户未登录
            System.out.println("[getFriendList] session 不存在");
            return new ArrayList<Friend>();
        }
        User user = (User) session.getAttribute("user");
        if(user == null) {
            // 当前用户对象没在会话中
            System.out.println("[getFriendList] user 不存在");
            return new ArrayList<Friend>();
        }
        // 2. 根据userId 从数据库查询数据
        List<Friend> friendList = friendMapper.selectFriendList(user.getUserId());
        return friendList;
    }


////////////////////////////////////////////////////////////////////////////////////
// 删除登录用户的 指定好友
////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("/deleteFriend")
    @ResponseBody
    public Object deleteFriend(int friendId, HttpSession session) {
        User user = (User)session.getAttribute("user");
        JSONObject resp = new JSONObject();
        
        if (user == null) {
            resp.put("success", false);
            return resp;
        }

        try {
            // 1. 获取与该好友的会话ID
            Integer sessionId = messageSessionMapper.getSessionId(user.getUserId(), friendId);
            
            // 2. 删除会话及其消息
            if (sessionId != null) {
                // 先删除会话中的消息
                messageSessionMapper.deleteSessionMessages(sessionId);
                // 再删除会话本身
                int ret = messageSessionMapper.deleteSession(sessionId);
                // 删除会话id
                messageSessionMapper.deleteSessionId(sessionId);
            }
            
            // 3. 删除好友关系
            friendMapper.deleteFriend(user.getUserId(), friendId);
            
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            e.printStackTrace();
        }
        
        return resp;
    }
}
