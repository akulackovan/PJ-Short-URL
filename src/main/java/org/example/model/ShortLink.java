package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *  Модель для хранения сокращенной ссылки
 */
public class ShortLink {

    private String shortCode; // короткая ссылка
    private String shortPrefix; // префикс ссылка
    private String realURL; // настоящая ссылка
    private int countOfRequest; // количество доступных обращений
    private LocalDateTime removeTime; // время удаления
    private LocalDateTime createTime; // время создания
    private UUID uuid; // UUID пользователя
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // формат даты

    public ShortLink() {
    }

    public ShortLink(String shortCode, String shortPrefix, String realURL, int countOfRequest, LocalDateTime createTime, LocalDateTime removeTime, UUID uuid) {
        this.shortCode = shortCode;
        this.shortPrefix = shortPrefix;
        this.realURL = realURL;
        this.countOfRequest = countOfRequest;
        this.createTime = createTime;
        this.removeTime = removeTime;
        this.uuid = uuid;
    }

    /**
     * @return Строковое представление в формате:
     * "короткая ссылка | количество доступных обращений | время создания ссылки | время удаления ссылки | время "
     */
    @Override
    public String toString() {
        return String.format("%-20s | %-20d | %-20s | %-20s | %-200s", shortPrefix + shortCode, countOfRequest, createTime.format(formatter), removeTime.format(formatter), realURL);
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortPrefix() {
        return shortPrefix;
    }

    public void setShortPrefix(String shortPrefix) {
        this.shortPrefix = shortPrefix;
    }

    public String getRealURL() {
        return realURL;
    }

    public void setRealURL(String realURL) {
        this.realURL = realURL;
    }

    public int getCountOfRequest() {
        return countOfRequest;
    }

    public void setCountOfRequest(int countOfRequest) {
        this.countOfRequest = countOfRequest;
    }

    public LocalDateTime getRemoveTime() {
        return removeTime;
    }

    public void setRemoveTime(LocalDateTime removeTime) {
        this.removeTime = removeTime;
    }

    public void use() {
        countOfRequest--;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(removeTime);
    }

    @JsonIgnore
    public String getShortLink() {
        return shortPrefix + shortCode;
    }
}
