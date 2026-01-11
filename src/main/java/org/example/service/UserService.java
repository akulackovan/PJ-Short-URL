package org.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.example.exception.UserExceptions;
import org.example.model.Notification;
import org.example.storage.JsonStorage;

/*
 * Сервис управления списом пользователей и уведомлений
 */
public class UserService {

    private Map<UUID, List<Notification>> users;
    private JsonStorage storage;

    public UserService(JsonStorage storage) {
        this.storage = storage;
        this.users = storage.loadData().getUsers();
    }

    public UUID createAnonymousUser() {
        UUID userId = UUID.randomUUID();
        users.put(userId, new ArrayList<>());
        storage.saveUser(users);
        return userId;
    }

    public boolean userExists(UUID userId) {
        return users.containsKey(userId);
    }

    public void addNotification(UUID userId, Notification notification) {
        users.computeIfAbsent(userId, k -> new ArrayList<>()).add(notification);
        storage.saveUser(users);
    }

    public UUID validateUser(String userUUID) throws UserExceptions.InvalidUuidException, UserExceptions.UserNotFoundException {
        if (userUUID == null) {
            return null;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(userUUID);
        } catch (IllegalArgumentException e) {
            throw new UserExceptions.InvalidUuidException();
        }
        if (!users.containsKey(uuid)) {
            throw new UserExceptions.UserNotFoundException(userUUID);
        }
        return uuid;
    }

    public void sendNotificationsToUser(UUID userId) {
        List<Notification> userNotifications = users.getOrDefault(userId, new ArrayList<>());
        for (Notification notification : userNotifications) {
            NotificationService.notify(notification.getNotificationType(), notification.getLink());
        }
        users.put(userId, new ArrayList<>());
        storage.saveUser(users);
    }

    public List<Notification> getNotifications(UUID userId) {
        return users.getOrDefault(userId, new ArrayList<>());
    }

}
