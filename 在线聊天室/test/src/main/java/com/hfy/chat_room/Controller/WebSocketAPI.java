package com.hfy.chat_room.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hfy.chat_room.component.OnlineUserManage;
import com.hfy.chat_room.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketAPI extends TextWebSocketHandler {
    @Autowired
    private OnlineUserManage onlineUserManage;

    @Autowired
    private MessageSessionMapper messageSessionMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FriendMapper friendMapper;  // 添加 FriendMapper

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("[WebSocketAPI] 连接成功!");
        // getAttributes() 返回的是一个 Map 对象，通过 get("user") 拿到对应 key: user 的 value
        User user = (User)session.getAttributes().get("user");
        if(user == null){
            return;
        }
        //System.out.println("获取到的 userId: " + user.getUserId());
        // 把获取到的键值对存起来
        onlineUserManage.online(user.getUserId(),session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[WebSocketAPI] 收到消息!" + message.toString());
        // TODO 后续处理消息的 接收 转发 保存...
        // 1. 先获取当前用户的信息，后续进行消息转发...
        User user = (User)session.getAttributes().get("user");
        if(user == null){
            System.out.println("[WebSocketAPI] user == null 未登录用户，无法进行消息转发");
            return;
        }

        // 针对请求进行解析，把 json 格式字符串，转化成一个Java中的对象
        JSONObject jsonObject = JSON.parseObject(message.getPayload());

        String type = jsonObject.getString("type");
        switch(type) {
            case "message":
                // 处理普通消息
                MessageRequest req = objectMapper.readValue(message.getPayload(), MessageRequest.class);
                transferMessage(user, req);
                break;
            case "friendRequest":
                // 处理好友请求
                handleFriendRequest(jsonObject, session);
                break;
            case "friendResponse":
                // 处理好友请求响应
                handleFriendResponse(jsonObject, session);
                break;
        }
    }
    private void handleFriendRequest(JSONObject payload, WebSocketSession session) throws IOException {
        User fromUser = (User)session.getAttributes().get("user");
        Integer toUserId = payload.getInteger("toUserId");

        // 构造响应消息
        JSONObject resp = new JSONObject();
        resp.put("type", "friendRequest");
        resp.put("fromUserId", fromUser.getUserId());
        resp.put("fromName", fromUser.getUsername());
        resp.put("content", payload.getString("content"));

        // 发送给目标用户
        WebSocketSession toSession = onlineUserManage.getSession(toUserId);
        if(toSession != null) {
            toSession.sendMessage(new TextMessage(resp.toJSONString()));
        }
    }

    private void handleFriendResponse(JSONObject payload, WebSocketSession session) throws IOException {
        User fromUser = (User)session.getAttributes().get("user");
        Integer toUserId = payload.getInteger("toUserId");
        boolean accepted = payload.getBoolean("accepted");

        if(accepted) {
            // 同意好友请求，添加好友关系
            friendMapper.addFriend(fromUser.getUserId(), toUserId);
            friendMapper.addFriend(toUserId, fromUser.getUserId());
        }

        // 构造响应消息
        JSONObject resp = new JSONObject();
        resp.put("type", "friendResponse");
        resp.put("accepted", accepted);
        resp.put("content", payload.getString("content"));

        // 发送响应给请求方
        WebSocketSession toSession = onlineUserManage.getSession(toUserId);
        if(toSession != null) {
            toSession.sendMessage(new TextMessage(resp.toJSONString()));
        }
    }
    

    private void transferMessage(User fromUser, MessageRequest req) throws IOException {
        // 1. 先构造一个待转发的响应对象 MessageResponse
        MessageResponse resp = new MessageResponse();
        resp.setType("message");
        resp.setFromId(fromUser.getUserId());
        resp.setFromName(fromUser.getUsername());
        resp.setSessionId(req.getSessionId());
        resp.setContent(req.getContent());
        // 把这个 Java对象 转化成 json 格式字符串
        String respJson = objectMapper.writeValueAsString(resp);
        System.out.println("[transferMessage] respJson = " + respJson);

        // 2. 通过请求中的 sessionId 获取到 MessageSession 里都有哪些用户
        List<Friend> friends = messageSessionMapper.getFriendsBySessionId(req.getSessionId(),fromUser.getUserId());
        // 之前定义的方法会把用户自己刨除,此处需要再手动添加一份自己的信息
        Friend myself = new Friend();
        myself.setFriendId(fromUser.getUserId());
        myself.setFriendName(fromUser.getUsername());
        friends.add(myself);

        // 3. 循环遍历上述的这个列表 给列表中的每个用户都发一份响应消息
        // 知道了每个用户的 userId ,进一步查询 OnlineUserManage ,就知道对应的 WebSocketSession
        // 从而进行发送消息
        // PS： 这里除了给用户发消息,也要给自己发一个,方便实现自己在客户端上也能显示自己发送的消息
        for(Friend friend : friends){
            WebSocketSession webSocketSession = onlineUserManage.getSession(friend.getFriendId());
            if(webSocketSession == null){
                // 如果用户未在线,则不发送
                continue;
            }
            webSocketSession.sendMessage(new TextMessage(respJson));
        }
        // 4. 转发的消息,还需要放到数据库中,方便用户下线后,重新上线时可以通过历史消息的方式拿到之前的消息
        // 需要往 Message 表中写入一条消息
        Message message = new Message();
        message.setFromId(fromUser.getUserId());
        message.setSessionId(req.getSessionId());
        message.setContent(req.getContent());
        // 自增主键、时间都可以用 SQL 在 数据库中生成
        messageMapper.add(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("[WebSocketAPI] 连接正常关闭!" + exception.toString());

        User user = (User)session.getAttributes().get("user");
        if(user == null){
            return;
        }
        onlineUserManage.offline(user.getUserId(),session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("[WebSocketAPI] 连接异常!" + status.toString());

        User user = (User)session.getAttributes().get("user");
        if(user == null){
            return;
        }
        onlineUserManage.offline(user.getUserId(),session);
    }


}
