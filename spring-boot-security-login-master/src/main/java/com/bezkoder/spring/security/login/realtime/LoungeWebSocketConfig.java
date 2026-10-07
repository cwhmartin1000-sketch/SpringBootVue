package com.bezkoder.spring.security.login.realtime;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 設定午休小屋的 WebSocket 端點與允許來源。
 */
@Configuration
@EnableWebSocket
public class LoungeWebSocketConfig implements WebSocketConfigurer {

    private final LoungeWebSocketHandler loungeWebSocketHandler;
    private final String[] allowedOrigins;

    /**
     * 建立 WebSocket 設定。
     *
     * @param loungeWebSocketHandler 聊天室訊息處理器
     * @param allowedOrigins         設定檔允許的前端來源
     */
    public LoungeWebSocketConfig(
            LoungeWebSocketHandler loungeWebSocketHandler,
            @Value("${app.websocket.allowed-origins}") String allowedOrigins) {
        this.loungeWebSocketHandler = loungeWebSocketHandler;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * 註冊單一聊天室端點。
     *
     * @param registry WebSocket handler registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(loungeWebSocketHandler, "/ws/lounge")
                .setAllowedOrigins(allowedOrigins);
    }
}