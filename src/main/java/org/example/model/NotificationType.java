package org.example.model;

/**
 * Перечисление типов уведомления
 */
public enum NotificationType {

    LINK_EXPIRED("Ссылка истекла по времени"),
    LIMIT_EXCEEDED("Лимит переходов исперпан"),
    LOW_LIMIT_WARNING("Осталось мало переходов (3 или менее)"),
    DATE_WARNING("Ссылка скоро исчезнет"),
    LINK_CREATED("Ссылка создана"),
    LINK_DELETED("Ссылка удалена");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
