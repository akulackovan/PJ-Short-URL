package org.example.service;

import java.time.LocalDateTime;

import org.example.exception.LinkExceptions;
import org.example.storage.Config;

/**
 *  Сервис для валидации ссылок
 */
public class LinkValidationService {

    public static void validateLongLink(String link) throws LinkExceptions.InvalidUrlException, LinkExceptions.EmptyLongLinkException {
        if (link == null || link.isEmpty()) {
            throw new LinkExceptions.EmptyLongLinkException();
        }
        if (!link.matches(Config.get("links.validation", "^https?://.+"))) {
            throw new LinkExceptions.InvalidUrlException();
        }
    }

    public static void validateDate(LocalDateTime createTime, LocalDateTime removeTime) throws LinkExceptions.InvalidExpirationTimeException {
        if (removeTime != null && !removeTime.isAfter(createTime)) {
            throw new LinkExceptions.InvalidExpirationTimeException();
        }
    }

}

