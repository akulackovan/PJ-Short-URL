package org.example.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.example.exception.LinkExceptions;
import org.example.model.ShortLink;
import org.example.model.StorageData;
import org.example.storage.Config;
import org.example.storage.JsonStorage;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShortLinkServiceTest {
    @Mock
    private JsonStorage storage;

    private ShortLinkService service;
    private List<ShortLink> testLinks;

    private static MockedStatic<Config> mockedConfig;
    private final String prefixLinks1 = "clck.ru/";
    private final String prefixLinks2 = "click.ru/";
    private final UUID firstUser = UUID.randomUUID();
    private final UUID secondUser = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testLinks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime removeTime = now.plusMinutes(10);

        testLinks.add(new ShortLink("123456", prefixLinks1, "https://example.com", 1, now, removeTime, firstUser));
        testLinks.add(new ShortLink("123456", prefixLinks2, "https://example.com", 1, now, removeTime, firstUser));
        testLinks.add(new ShortLink("abcdef", prefixLinks1, "https://example.com", 1, now, removeTime, firstUser));
        testLinks.add(new ShortLink("abcdef", prefixLinks2, "https://example.com", 1, now, removeTime, secondUser));
        testLinks.add(new ShortLink("789456", prefixLinks1, "https://example.com", 1, now, removeTime, secondUser));
        testLinks.add(new ShortLink("852963", prefixLinks2, "https://example.com", 1, now, removeTime, secondUser));

        mockedConfig = Mockito.mockStatic(Config.class);

        mockedConfig.when(() -> Config.get(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(1);
                });

        mockedConfig.when(() -> Config.get(anyString()))
                .thenReturn(null);

        mockedConfig.when(() -> Config.get("links.link")).thenReturn("clck.ru/");

        mockedConfig.when(() -> Config.get("links.check_by_code", "true"))
                .thenReturn("true");

        storage = mock(JsonStorage.class);
        StorageData storageData = mock(StorageData.class);
        when(storageData.getLinks()).thenReturn(testLinks);
        when(storageData.getUsers()).thenReturn(new HashMap<>());
        when(storage.loadData()).thenReturn(storageData);
    }

    @AfterEach
    void tearDown() {
        if (mockedConfig != null) {
            mockedConfig.close();
        }
    }

    @Test
    void testCheckLinkByTrueModeCheckByCode() {
        mockedConfig.when(() -> Config.get("links.check_by_code", "true"))
                .thenReturn("true");

        service = new ShortLinkService(storage);

        Set<String> activeCodes = service.getActiveCode();

        assertEquals(4, activeCodes.size());
        assertTrue(activeCodes.contains("123456"));
        assertTrue(activeCodes.contains("abcdef"));
        assertTrue(activeCodes.contains("789456"));
        assertTrue(activeCodes.contains("852963"));
    }

    @Test
    void testLinkByFalseModeCheckByCode() {
        mockedConfig.when(() -> Config.get("links.check_by_code", "true"))
                .thenReturn("false");
        service = new ShortLinkService(storage);
        Set<String> activeCodes = service.getActiveCode();
        assertEquals(3, activeCodes.size());
        assertTrue(activeCodes.contains("123456"));
        assertTrue(activeCodes.contains("abcdef"));
        assertTrue(activeCodes.contains("789456"));
        assertFalse(activeCodes.contains("852963"));
    }

    @Test
    /**
     * Уникальность ссылки
     */
    void testGenerateCode() throws LinkExceptions.MaxGenerationAttemptsException {
        mockedConfig.when(() -> Config.get("links.check_by_code", "true"))
                .thenReturn("false");
        mockedConfig.when(() -> Config.get("links.alphabet", "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz"))
                .thenReturn("01");
        mockedConfig.when(() -> Config.get("links.size", "6"))
                .thenReturn("1");
        mockedConfig.when(() -> Config.get("links.max_try", "1000"))
                .thenReturn("1000");

        service = new ShortLinkService(storage);
        testLinks.clear();
        String code = service.generateShortLink();
        assertEquals(1, code.length());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime removeTime = now.plusMinutes(10);
        testLinks.add(new ShortLink(code, prefixLinks1, "https://example.com", 1, now, removeTime, firstUser));

        code = service.generateShortLink();
        assertEquals(1, code.length());
        testLinks.add(new ShortLink(code, prefixLinks1, "https://example.com", 1, now, removeTime, firstUser));

        assertThrows(
                LinkExceptions.MaxGenerationAttemptsException.class,
                () -> service.generateShortLink()
        );
    }


}
