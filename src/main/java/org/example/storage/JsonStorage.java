package org.example.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.model.Notification;
import org.example.model.ShortLink;
import org.example.model.StorageData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Сервис для работы с короткими ссылками
 * Хранит данные в JSON файле
 */
public final class JsonStorage {

    private final String FILE_PATH;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private File file = null;
    private StorageData storageData;

    public JsonStorage(String filename) {
        objectMapper.registerModule(new JavaTimeModule());
        FILE_PATH = filename == null ? Config.get("data.file") : filename;
        createFile(FILE_PATH);
        this.file = new File(FILE_PATH);
        objectMapper.findAndRegisterModules();
        storageData = new StorageData();
    }

    public StorageData loadData() {
        if (!file.exists() || file.length() == 0) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                objectMapper.writeValue(file, new HashMap<>());
            } catch (IOException e) {
                System.err.println("Ошибка при создании файла: " + e.getMessage());
            }
            return storageData;
        }
        try {
            return objectMapper.readValue(file, StorageData.class);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла пользователей: " + e.getMessage());
            return storageData;
        }
    }


    public void saveStorage(StorageData storageData) {
        try {
            this.storageData = storageData;
            objectMapper.writeValue(file, this.storageData);
        } catch (IOException e) {
            System.err.println("Ошибка записи данных в файл: " + e.getMessage());
        }
    }


    public void saveLinks(List<ShortLink> data) {
        try {
            storageData.setLinks(data);
            objectMapper.writeValue(file, storageData);
        } catch (IOException e) {
            System.err.println("Ошибка записи данных в файл: " + e.getMessage());
        }
    }

    public void saveUser(Map<UUID, List<Notification>> users) {
        try {
            storageData.setUsers(users);
            objectMapper.writeValue(file, storageData);
        } catch (IOException e) {
            System.err.println("Ошибка записи данных в файл: " + e.getMessage());
        }
    }

    /**
     * Создает файл если его нет
     *
     * @param filename - путь к файлу
     */
    public void createFile(String filename) {
        File file = new File(filename);
        if (!file.exists() || file.length() == 0) {
            try {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean dirsCreated = parentDir.mkdirs();
                    if (!dirsCreated) {
                        System.err.println("Не удалось создать директорию: " + parentDir.getAbsolutePath());
                    }
                }

                // Создаем файл
                if (!file.exists()) {
                    boolean fileCreated = file.createNewFile();
                    if (!fileCreated) {
                        System.err.println("Не удалось создать файл: " + filename);
                    }
                }

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}