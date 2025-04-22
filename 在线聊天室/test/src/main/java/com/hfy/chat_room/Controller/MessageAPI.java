package com.hfy.chat_room.Controller;

import com.hfy.chat_room.model.Message;
import com.hfy.chat_room.model.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
public class MessageAPI {
    @Resource
    private MessageMapper messageMapper;

    @GetMapping("/message")
    public Object getMessage(int sessionId){
        List<Message> messages = messageMapper.getMessagesBySessionId(sessionId);
        // 数据库中根据逆序筛选了最近100条消息，传回前端时要进行升序排列才是有逻辑的真实消息记录
        Collections.reverse(messages);
        return messages;
    }
}
