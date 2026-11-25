package com.example.shortener.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {
    private final Map<String, List<String>> notifications = new ConcurrentHashMap<>();

    public void notifyUser(String userId, String message) {
        notifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    public List<String> getAndClearNotifications(String userId) {
        List<String> msgs = notifications.remove(userId);
        return msgs != null ? msgs : new ArrayList<>();
    }
}
