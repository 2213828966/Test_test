package com.hfy.chat_room.Controller;

import com.alibaba.fastjson.JSONObject;
import com.hfy.chat_room.model.Friend;
import com.hfy.chat_room.model.FriendMapper;
import com.hfy.chat_room.model.User;
import com.hfy.chat_room.model.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class UserAPI {
    @Resource
    UserMapper userMapper;

    @Resource
    FriendMapper friendMapper;

////////////////////////////////////////////////////////////////////////////////////
// 登录
////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("/login")
    @ResponseBody
    public Object login(String username, String password, HttpServletRequest req) {
        User user = userMapper.selectByName(username);
        if(user == null || !user.getPassword().equals(password)) {
            System.out.println("登录失败！ 用户名或密码错误!"+ user);
            return new User();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        user.setPassword("");
        return user;
    }

////////////////////////////////////////////////////////////////////////////////////
// 注册
////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("/register")
    @ResponseBody
    public Object register(String username, String password) {
        System.out.println(username);
        User user = null;
        try{
            user = new User();
            user.setUsername(username);
            user.setPassword(password);
            int ret = userMapper.insert(user);
            System.out.println("注册 ret: "+ret);
            user.setPassword("");
        }catch (DuplicateKeyException e){
            // username 重复  注册失败
            System.out.println("注册失败 username = "+ username);
            user = new User();
        }
        return user;
    }

////////////////////////////////////////////////////////////////////////////////////
// 获取登录用户信息
////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("/userInfo")
    @ResponseBody
    public Object getUserInfo(HttpServletRequest req) {
        // 1.先从请求中获取会话Session
        HttpSession session = req.getSession(false);
        if(session == null) {
            // 用户尚未登陆
            System.out.println("[getUserInfo] 当前获取不到 session 对象");
            return new User();
        }
        // 2.再从会话中获取之前保存的用户对象
        User user = (User)session.getAttribute("user");
        if(user == null) {
            System.out.println("[getUserInfo] 当前获取不到 user 对象");
            return new User();
        }
        user.setPassword("");
        return user;
    }

////////////////////////////////////////////////////////////////////////////////////
// 判断好友与自己的状态
////////////////////////////////////////////////////////////////////////////////////
    @GetMapping("/searchUser")
    @ResponseBody
    public Object searchUser(String username, HttpSession session) {
        User currentUser = (User)session.getAttribute("user");
        JSONObject resp = new JSONObject();
        
        if (currentUser == null) {
            resp.put("exists", false);
            return resp;
        }


        User targetUser = friendMapper.searchUserByName(username);
        if (targetUser == null) {
            resp.put("exists", false);
            return resp;
        }

        int count = friendMapper.checkFriendship(currentUser.getUserId(), targetUser.getUserId());
        
        resp.put("exists", true);
        resp.put("userId", targetUser.getUserId());
        resp.put("username", targetUser.getUsername());
        resp.put("isFriend", count > 0);
        
        return resp;
    }


////////////////////////////////////////////////////////////////////////////////////
// 添加好友
////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("/addFriend")
    @ResponseBody
    public Object addFriend(@RequestParam int friendId, HttpServletRequest req) {
        // 1. 获取当前登录用户
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new HashMap<String, Object>(){{
                put("success", false);
                put("message", "用户未登录");
            }};
        }
        User user = (User)session.getAttribute("user");
        if (user == null) {
            return new HashMap<String, Object>(){{
                put("success", false);
                put("message", "用户未登录");
            }};
        }
        
        // 2. 执行添加好友操作
        try {
            // 把对方添加为自己好友
            friendMapper.addFriend(user.getUserId(), friendId);
            // 把自己添加为对方好友
            friendMapper.addFriend(friendId,user.getUserId());
            return new HashMap<String, Object>(){{
                put("success", true);
                put("message", "添加好友成功");
            }};
        } catch (Exception e) {
            return new HashMap<String, Object>(){{
                put("success", false);
                put("message", "添加好友失败");
            }};
        }
    }


}
