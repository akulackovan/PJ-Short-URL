package org.example.service;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.example.exception.LinkExceptions;
import org.example.exception.UserExceptions;
import org.example.model.Notification;
import org.example.model.NotificationType;
import org.example.model.ShortLink;
import org.example.storage.Config;
import org.example.storage.JsonStorage;

/**
 * Сервис управления ссылками
 */
public class ShortLinkManager {

    private UserService userService;
    private ShortLinkService shortLinkService;
    private JsonStorage storage;

    public ShortLinkManager(String file) {
        storage = new JsonStorage(file);
        userService = new UserService(storage);
        shortLinkService = new ShortLinkService(storage);
    }

    /*
     * Создание короткой ссылки
     */
    public UUID addLink(String longLink, Integer countOfRequest, String userUUID)
            throws LinkExceptions.InvalidUrlException,
            UserExceptions.UserNotFoundException, UserExceptions.InvalidUuidException,
            LinkExceptions.EmptyLongLinkException, LinkExceptions.MaxGenerationAttemptsException {
        LocalDateTime createTime = LocalDateTime.now();
        LinkValidationService.validateLongLink(longLink);
        UUID user = userService.validateUser(userUUID);
        if (user == null) {
            user = userService.createAnonymousUser();
        }
        String shortCode = shortLinkService.generateShortLink();
        ShortLink shortLink = new ShortLink(shortCode, Config.get("links.link", "clck.ru/"), longLink, countOfRequest, createTime,
                createTime.plusSeconds(Integer.parseInt(Config.get("links.time", "43200"))), user);
        if (userUUID == null) {
            user = shortLink.getUuid();
            System.out.println("Ваш UUID для входа: " + user.toString());
        }
        shortLinkService.addLink(shortLink);
        NotificationService.notify(NotificationType.LINK_CREATED, shortLink.getShortLink());
        return user;
    }

    public void clearLinks() {
        List<ShortLink> links = shortLinkService.getExpiredLinks();
        for (ShortLink link : links) {
            userService.addNotification(link.getUuid(), new Notification(link.getShortLink(), NotificationType.LINK_EXPIRED));
            userService.addNotification(link.getUuid(), new Notification(link.getShortLink(), NotificationType.LINK_DELETED));
        }
    }

    public void useLink(String link, UUID user)
            throws LinkExceptions.EmptyShortCodeException,
            LinkExceptions.ShortLinkNotFoundException, URISyntaxException, IOException {
        ShortLink shortLink = validateShortLink(link);
        if (shortLink == null) {
            throw new LinkExceptions.ShortLinkNotFoundException();
        }
        if (shortLink.isExpired()) {
            userService.addNotification(shortLink.getUuid(), new Notification(shortLink.getShortLink(), NotificationType.LINK_EXPIRED));
            userService.addNotification(shortLink.getUuid(), new Notification(shortLink.getShortLink(), NotificationType.LINK_DELETED));
            shortLinkService.removeLink(shortLink);
            throw new LinkExceptions.ShortLinkNotFoundException();
        }
        shortLinkService.useLink(shortLink);
        Desktop.getDesktop().browse(new URI(shortLink.getRealURL()));
        NotificationService.checkAndSendLinkNotifications(shortLink);
        if (shortLink.getCountOfRequest() == 0 && user != shortLink.getUuid()) {
            userService.addNotification(shortLink.getUuid(), new Notification(shortLink.getShortLink(),
                    NotificationType.LIMIT_EXCEEDED));
            userService.addNotification(shortLink.getUuid(), new Notification(shortLink.getShortLink(), NotificationType.LINK_DELETED));
        }
        if (shortLink.getCountOfRequest() == 0) {
            NotificationService.sendDeleteLinkNotificationByUse(shortLink);
            shortLinkService.removeLink(shortLink);
        }
    }

    public void deleteLink(String link, UUID user)
            throws LinkExceptions.EmptyShortCodeException,
            LinkExceptions.ShortLinkNotFoundException, LinkExceptions.UnauthorizedAccessException {
        ShortLink shortLink = validateShortLink(link);
        if (shortLink == null) {
            throw new LinkExceptions.ShortLinkNotFoundException();
        }
        if (!user.equals(shortLink.getUuid())) {
            throw new LinkExceptions.UnauthorizedAccessException();
        }
        shortLinkService.removeLink(shortLink);
    }

    public void changeLink(String link, UUID user, Integer countOfRequest, LocalDateTime dateTime)
            throws LinkExceptions.EmptyShortCodeException,
            LinkExceptions.ShortLinkNotFoundException, LinkExceptions.UnauthorizedAccessException,
            LinkExceptions.InvalidExpirationTimeException {
        ShortLink shortLink = validateShortLink(link);
        if (shortLink == null) {
            throw new LinkExceptions.ShortLinkNotFoundException();
        }
        if (!user.equals(shortLink.getUuid())) {
            throw new LinkExceptions.UnauthorizedAccessException();
        }
        shortLinkService.changeLink(shortLink, countOfRequest, dateTime);
    }

    public ShortLink validateShortLink(String shortLink)
            throws LinkExceptions.EmptyShortCodeException {
        if (shortLink == null || shortLink.isEmpty()) {
            throw new LinkExceptions.EmptyShortCodeException();
        }
        return shortLinkService.findLinkByShortLink(shortLink);
    }

    public ShortLinkService getShortLinkService() {
        return shortLinkService;
    }

    public UserService getUserService() {
        return userService;
    }

}
