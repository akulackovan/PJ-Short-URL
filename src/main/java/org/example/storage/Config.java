package org.example.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс для работы с настройками приложения
 */
public class Config {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Не найден config.properties!");
            } else {
                PROPERTIES.load(input);
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения config.properties: " + e.getMessage());
        }
    }

    /**
     * Получение значения настройки по ключу
     * @param key ключ
     * @return значение настройки или null
     */
    public static String get(String key) {
        try {
            return PROPERTIES.getProperty(key);
        } catch (Exception e) {
            System.out.println("Ошибка получения параметра конфигурации: " + e.getMessage());
        }
        return null;
    }


    /**
     * Получение значения с дефолтом
     * @param key ключ
     * @param defaultValue значение по умолчанию
     * @return значение настройки
     */
    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

}
