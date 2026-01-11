package org.example.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.example.model.NotificationType;
import org.example.model.ShortLink;
import org.example.storage.Config;

/**
 *  Сервис для обработки уведомлений
 */
public class NotificationService {


    public static void notify(NotificationType type, String link) {
        System.out.printf("Уведомление для короткой ссылки %s: %s\n",
                link, type.getDescription());
    }

    public static void checkAndSendLinkNotifications(ShortLink link) {
        if (link.getCountOfRequest() <= Integer.parseInt(Config.get("links.count_notification", "3"))) {
            notify(NotificationType.LOW_LIMIT_WARNING, link.getShortLink());
        }
        if (Duration.between(LocalDateTime.now(), link.getRemoveTime()).toSeconds() <= Integer.parseInt(Config.get("links.time_notification", "3600"))) {
            notify(NotificationType.DATE_WARNING, link.getShortLink());
        }
    }

    public static void sendDeleteLinkNotificationByUse(ShortLink link) {
        notify(NotificationType.LIMIT_EXCEEDED, link.getShortLink());
        notify(NotificationType.LINK_DELETED, link.getShortLink());
    }

}
