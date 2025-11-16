package com.miDiario.blog.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String createSession(long userId) {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, userId);
        return token;
    }

    public Optional<Long> getUserId(String token) {
        if (token == null) return Optional.empty();
        return Optional.ofNullable(sessions.get(token));
    }

    public void destroy(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
