package com.hfy.chat_room.config;

import com.hfy.chat_room.Controller.TestWebSocketAPI;
import com.hfy.chat_room.Controller.WebSocketAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private TestWebSocketAPI testWebSocketAPI;

    @Autowired
    private WebSocketAPI webSocketAPI;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 通过这个方法 把刚刚创建好的 handle 类给注册到具体的路径上
        // 此时浏览器请求路径是 "/test" 的时候，就会调用 TestWebSocketAPI 里的方法
        //registry.addHandler(testWebSocketAPI, "/test");

        registry.addHandler(webSocketAPI, "/WebSocketMessage")
                // 通过注册这个特定的 HttpSession拦截器，就可以把HttpSession 中添加的 Attribute 键值对
                // 往 WebSocketSession 中也添加一份
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
