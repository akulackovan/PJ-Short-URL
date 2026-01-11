package org.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *  Модель для хранения пользователей, уведомлений для них при удалении ссылок, ссокращенных ссылок
 */
public class StorageData {

    private List<ShortLink> links;
    private Map<UUID, List<Notification>> users;

    public StorageData() {
        this.links = new ArrayList<>();
        this.users = new HashMap<>();
    }

    public StorageData(List<ShortLink> links, Map<UUID, List<Notification>> users) {
        this.links = links;
        this.users = users;
    }

    public Map<UUID, List<Notification>> getUsers() {
        return users;
    }

    public void setUsers(Map<UUID, List<Notification>> users) {
        this.users = users;
    }

    public List<ShortLink> getLinks() {
        return links;
    }

    public void setLinks(List<ShortLink> links) {
        this.links = links;
    }



}
