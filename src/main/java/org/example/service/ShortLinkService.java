package org.example.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.exception.LinkExceptions;
import org.example.model.ShortLink;
import org.example.storage.Config;
import org.example.storage.JsonStorage;

/**
 * Сервис управления списком ссылок
 */
public class ShortLinkService {
    private List<ShortLink> links;
    private JsonStorage storage;

    public ShortLinkService(JsonStorage storage) {
        this.storage = storage;
        this.links = storage.loadData().getLinks();
    }


    public String generateShortLink() throws LinkExceptions.MaxGenerationAttemptsException {
        synchronized (storage.LOCK) {
            Set<String> activeCodes = getActiveCode();
            return LinkGeneratorService.generateShortLink(activeCodes);
        }
    }

    public Set<String> getActiveCode() {
        synchronized (storage.LOCK) {

            if (Boolean.parseBoolean(Config.get("links.check_by_code", "true"))) { // проверка сгенерированного кода по коду
                // (если, например, links.link изменится, то не него не будет обращено внимание
                // clck.ru/123456 и click.ru/123456 - это одинаковые ссылки, при генерации кода будет сгенерирован новый)
                return links.stream()
                        .filter(link -> !link.isExpired() && link.getCountOfRequest() > 0)
                        .map(ShortLink::getShortCode)
                        .collect(Collectors.toSet());
            } else {
                // проверка сгенерированного кода по префиксу
                // (если, например, links.link изменится, то не него не будет обращено внимание
                // clck.ru/123456 и click.ru/123456 - это разные ссылки, при генерации кода не будет сгенерирован новый)
                return links.stream()
                        .filter(link -> !link.isExpired() && link.getCountOfRequest() > 0 &&
                                link.getShortPrefix().equals(Config.get("links.link", "clck.ru/")))
                        .map(ShortLink::getShortCode)
                        .collect(Collectors.toSet());
            }
        }
    }

    public void addLink(ShortLink shortLink) {
        synchronized (storage.LOCK) {
            links.add(shortLink);
            storage.saveLinks(links);
        }
    }

    public void removeLink(ShortLink shortLink) throws LinkExceptions.ShortLinkNotFoundException {
        synchronized (storage.LOCK) {
            shortLink = findLinkByShortLink(shortLink.getShortLink());
            links.remove(shortLink);
            storage.saveLinks(links);
        }
    }

    public void changeLink(ShortLink shortLink, Integer countOfRequest, LocalDateTime dateTime) throws LinkExceptions.InvalidExpirationTimeException, LinkExceptions.ShortLinkNotFoundException {
        synchronized (storage.LOCK) {
            shortLink = findLinkByShortLink(shortLink.getShortLink());
            if (countOfRequest == null && dateTime == null) return;
            if (countOfRequest != null) {
                shortLink.setCountOfRequest(countOfRequest);
            }
            if (dateTime != null) {
                LinkValidationService.validateDate(shortLink.getCreateTime(), dateTime);
                shortLink.setRemoveTime(dateTime);
            }
            storage.saveLinks(links);
        }
    }

    public void useLink(ShortLink shortURL) throws LinkExceptions.ShortLinkNotFoundException {
        synchronized (storage.LOCK) {
            shortURL = findLinkByShortLink(shortURL.getShortLink());
            shortURL.use();
            storage.saveLinks(links);
        }
    }

    public ShortLink findLinkByShortLink(String shortLink) throws LinkExceptions.ShortLinkNotFoundException {
        synchronized (storage.LOCK) {
            ShortLink link = links.stream()
                    .filter(value -> value.getShortLink().equals(shortLink))
                    .findFirst()
                    .orElse(null);
            if (link == null)
                throw new LinkExceptions.ShortLinkNotFoundException();
            return link;
        }
    }

    public List<ShortLink> getLinksForUser(UUID userId) {
        synchronized (storage.LOCK) {
            return links.stream()
                    .filter(link -> userId.equals(link.getUuid()))
                    .collect(Collectors.toList());
        }
    }

    public List<ShortLink> getExpiredLinks() {
        synchronized (storage.LOCK) {
            List<ShortLink> expiredLinks = links.stream()
                    .filter(ShortLink::isExpired)
                    .collect(Collectors.toList());
            links.removeIf(ShortLink::isExpired);
            storage.saveLinks(links);
            return expiredLinks;
        }
    }

}