package org.example.model;

/**
 * Модель хранения уведомления
 *
 */
public class Notification {
    private String link; // короткая ссылка
    private NotificationType notificationType; // тип уведомления

    public Notification() {
    }

    public Notification(String link, NotificationType notificationType) {
        this.link = link;
        this.notificationType = notificationType;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

}
