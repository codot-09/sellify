package com.example.sellify.service;

import com.example.sellify.dto.ProductRequest;
import com.example.sellify.entity.UserSession;
import com.example.sellify.entity.enums.ProductStep;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public void startSession(String chatId) {
        ProductRequest request = new ProductRequest();
        request.setChatId(chatId);
        sessions.put(chatId, new UserSession(request, ProductStep.TITLE));
    }

    public UserSession getSession(String chatId) {
        return sessions.get(chatId);
    }

    public void clearSession(String chatId) {
        sessions.remove(chatId);
    }
}
