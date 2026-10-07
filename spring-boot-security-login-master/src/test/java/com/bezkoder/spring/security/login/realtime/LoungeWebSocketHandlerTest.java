package com.bezkoder.spring.security.login.realtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.bezkoder.spring.security.login.security.jwt.JwtUtils;
import com.bezkoder.spring.security.login.security.services.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class LoungeWebSocketHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtils jwtUtils = mock(JwtUtils.class);
    private final UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
    private final LoungeWebSocketHandler handler = new LoungeWebSocketHandler(objectMapper, jwtUtils,
            userDetailsService);

    @Test
    void shouldRejectJoinWithInvalidToken() throws Exception {
        WebSocketSession session = mockSession("session-1");
        List<TextMessage> sentMessages = captureMessages(session);
        when(jwtUtils.validateJwtToken("invalid-token")).thenReturn(false);

        handler.handleTextMessage(session,
                new TextMessage("{\"type\":\"join\",\"token\":\"invalid-token\",\"avatar\":\"🐱\"}"));

        assertEquals("error", objectMapper.readTree(sentMessages.get(0).getPayload()).get("type").asText());
        org.mockito.Mockito.verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    @Test
    void shouldJoinWithValidTokenAndBroadcastOnlyTemporaryRoomState() throws Exception {
        WebSocketSession session = mockSession("session-2");
        List<TextMessage> sentMessages = captureMessages(session);
        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid-token")).thenReturn("coworker");

        handler.handleTextMessage(session,
                new TextMessage("{\"type\":\"join\",\"token\":\"valid-token\",\"avatar\":\"🐱\"}"));

        JsonNode snapshot = objectMapper.readTree(sentMessages.get(0).getPayload());
        assertEquals("snapshot", snapshot.get("type").asText());
        assertEquals("coworker", snapshot.get("participants").get(0).get("username").asText());
        assertTrue(snapshot.get("participants").get(0).has("x"));
        assertEquals("participants", objectMapper.readTree(sentMessages.get(1).getPayload()).get("type").asText());
    }

    private WebSocketSession mockSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    private List<TextMessage> captureMessages(WebSocketSession session) throws Exception {
        List<TextMessage> sentMessages = new ArrayList<>();
        doAnswer(invocation -> {
            sentMessages.add(invocation.getArgument(0));
            return null;
        }).when(session).sendMessage(any(TextMessage.class));
        return sentMessages;
    }
}
