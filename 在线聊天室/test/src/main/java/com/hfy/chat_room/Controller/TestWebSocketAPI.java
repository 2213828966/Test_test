package com.hfy.chat_room.Controller;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TestWebSocketAPI extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 这个方法 会在 websocket 连接建立成功后，被自动调用
        //super.afterConnectionEstablished(session);
        System.out.println("TestAPI 连接成功!");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // websocket 收到消息后，被自动调用
        //super.handleTextMessage(session, message);
        System.out.println("TestAPI 收到消息!" + message.toString());
        // session 是个会话，里面记录了 通信双方 是谁，(session 中就持有了 websocket 的通信连接)
        session.sendMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // websocket 连接正常关闭，被自动调用
        //super.handleTransportError(session, exception);
        System.out.println("TestAPI 连接正常关闭!");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // websocket 连接出现异常，被自动调用
        //super.afterConnectionClosed(session, status);
        System.out.println("TestAPI 连接异常!");
    }
}
