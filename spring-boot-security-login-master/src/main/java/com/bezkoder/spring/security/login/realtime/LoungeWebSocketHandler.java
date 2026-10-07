package com.bezkoder.spring.security.login.realtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.bezkoder.spring.security.login.security.jwt.JwtUtils;
import com.bezkoder.spring.security.login.security.services.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 處理午休小屋中未持久化的角色移動與即時泡泡。
 */
@Component
public class LoungeWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoungeWebSocketHandler.class);
    private static final Set<String> ALLOWED_AVATARS = Set.of("🐱", "🐻", "🐰", "🐼", "🐸", "🐥");
    private static final Set<String> ALLOWED_EMOJIS = Set.of("👋", "😊", "🎉", "❤️");
    private static final int MAX_MESSAGE_CODE_POINTS = 60;
    private static final int JOIN_TIMEOUT_SECONDS = 10;

    private final ObjectMapper objectMapper;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final Map<String, Participant> participants = new ConcurrentHashMap<>();

    /**
     * 建立訊息處理器。
     *
     * @param objectMapper JSON 讀寫器
     * @param jwtUtils     JWT 驗證服務
     */
    public LoungeWebSocketHandler(
            ObjectMapper objectMapper,
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsService) {
        this.objectMapper = objectMapper;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 握手時不接受身份；必須先以 join 訊息提交有效 JWT。
        CompletableFuture.delayedExecutor(JOIN_TIMEOUT_SECONDS, TimeUnit.SECONDS).execute(() -> {
            if (session.isOpen() && !participants.containsKey(session.getId())) {
                try {
                    session.close(CloseStatus.POLICY_VIOLATION);
                } catch (IOException exception) {
                    logger.debug("關閉未驗證的聊天室連線失敗");
                }
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            JsonNode payload = objectMapper.readTree(message.getPayload());
            String type = textValue(payload, "type");

            if (!participants.containsKey(session.getId())) {
                joinRoom(session, payload, type);
                return;
            }

            switch (type) {
                case "move":
                    moveParticipant(session, payload);
                    break;
                case "say":
                    broadcastBubble(session, textValue(payload, "text"), false);
                    break;
                case "emoji":
                    broadcastBubble(session, textValue(payload, "text"), true);
                    break;
                case "ping":
                    send(session, Map.of("type", "pong"));
                    break;
                default:
                    sendError(session, "不支援的聊天室操作");
            }
        } catch (IllegalArgumentException exception) {
            sendError(session, exception.getMessage());
        } catch (Exception exception) {
            logger.warn("處理聊天室訊息失敗，關閉連線");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    private void joinRoom(WebSocketSession session, JsonNode payload, String type) throws IOException {
        if (!"join".equals(type)) {
            reject(session, "請先登入聊天室");
            return;
        }

        String token = textValue(payload, "token");
        String avatar = textValue(payload, "avatar");
        if (token.isBlank() || token.length() > 4096 || !jwtUtils.validateJwtToken(token)) {
            reject(session, "登入已失效，請重新登入");
            return;
        }
        if (!ALLOWED_AVATARS.contains(avatar)) {
            reject(session, "請選擇有效角色");
            return;
        }

        String username = jwtUtils.getUserNameFromJwtToken(token);
        try {
            userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException exception) {
            reject(session, "登入已失效，請重新登入");
            return;
        }
        Participant participant = new Participant(
                session.getId(), username, avatar,
                ThreadLocalRandom.current().nextDouble(12, 88),
                ThreadLocalRandom.current().nextDouble(18, 82));
        sessions.put(session.getId(), session);
        participants.put(session.getId(), participant);

        send(session, Map.of(
                "type", "snapshot",
                "selfId", session.getId(),
                "participants", participantSnapshots()));
        broadcastParticipants();
    }

    private void moveParticipant(WebSocketSession session, JsonNode payload) {
        double x = numberValue(payload, "x");
        double y = numberValue(payload, "y");
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("移動位置無效");
        }

        Participant participant = participants.get(session.getId());
        if (participant == null) {
            throw new IllegalArgumentException("聊天室連線已失效");
        }
        participant.setPosition(clamp(x), clamp(y));
        broadcastParticipants();
    }

    private void broadcastBubble(WebSocketSession session, String text, boolean emoji) {
        String normalized = text == null ? "" : text.trim();
        if (normalized.isEmpty() || normalized.codePointCount(0, normalized.length()) > MAX_MESSAGE_CODE_POINTS
                || normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("請輸入 1 到 60 個字的單行內容");
        }
        if (emoji && !ALLOWED_EMOJIS.contains(normalized)) {
            throw new IllegalArgumentException("表情符號不在允許清單中");
        }

        Map<String, Object> bubble = Map.of(
                "type", "bubble",
                "participantId", session.getId(),
                "text", normalized,
                "expiresInMs", 5000);
        broadcast(bubble);
    }

    private void broadcastParticipants() {
        broadcast(Map.of("type", "participants", "participants", participantSnapshots()));
    }

    private List<Map<String, Object>> participantSnapshots() {
        List<Map<String, Object>> snapshots = new ArrayList<>();
        for (Participant participant : participants.values()) {
            snapshots.add(Map.of(
                    "id", participant.id,
                    "username", participant.username,
                    "avatar", participant.avatar,
                    "x", participant.x,
                    "y", participant.y));
        }
        return snapshots;
    }

    private void broadcast(Map<String, Object> payload) {
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(payload);
        } catch (IOException exception) {
            logger.warn("無法序列化聊天室事件");
            return;
        }

        for (String sessionId : participants.keySet()) {
            WebSocketSession session = findSession(sessionId);
            if (session != null) {
                sendSerialized(session, serialized);
            }
        }
    }

    private WebSocketSession findSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        if (participants.remove(session.getId()) != null) {
            broadcastParticipants();
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        send(session, Map.of("type", "error", "message", message));
    }

    private void reject(WebSocketSession session, String message) throws IOException {
        sendError(session, message);
        session.close(CloseStatus.POLICY_VIOLATION);
    }

    private void send(WebSocketSession session, Map<String, Object> payload) throws IOException {
        sendSerialized(session, objectMapper.writeValueAsString(payload));
    }

    private void sendSerialized(WebSocketSession session, String payload) {
        if (!session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException exception) {
            logger.debug("聊天室連線傳送失敗");
        }
    }

    private String textValue(JsonNode payload, String key) {
        JsonNode value = payload.get(key);
        return value != null && value.isTextual() ? value.asText() : "";
    }

    private double numberValue(JsonNode payload, String key) {
        JsonNode value = payload.get(key);
        if (value == null || !value.isNumber()) {
            throw new IllegalArgumentException("移動位置無效");
        }
        return value.asDouble();
    }

    private double clamp(double value) {
        return Math.max(6, Math.min(94, value));
    }

    private static final class Participant {
        private final String id;
        private final String username;
        private final String avatar;
        private volatile double x;
        private volatile double y;

        private Participant(String id, String username, String avatar, double x, double y) {
            this.id = id;
            this.username = username;
            this.avatar = avatar;
            this.x = x;
            this.y = y;
        }

        private void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}