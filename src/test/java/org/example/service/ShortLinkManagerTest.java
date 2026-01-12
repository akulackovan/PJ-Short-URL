package org.example.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.example.exception.LinkExceptions;
import org.example.exception.UserExceptions;
import org.example.model.ShortLink;
import org.junit.jupiter.api.AfterAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShortLinkManagerTest {

    private static final String TEST_FILE = "data/test.json";
    private static final Object LOCK = new Object();
    private static MockedStatic<Desktop> mockedDesktop;
    private ShortLinkManager manager;
    private Desktop desktopMock;

    @BeforeAll
    void initAll() {
        synchronized (LOCK) {
            if (mockedDesktop == null) {
                mockedDesktop = Mockito.mockStatic(Desktop.class);
            }
        }
    }

    @AfterAll
    void tearDownAll() {
        synchronized (LOCK) {
            if (mockedDesktop != null) {
                mockedDesktop.close();
                mockedDesktop = null;
            }
        }
        cleanupTestFiles();
    }

    @BeforeEach
    void setUp() throws IOException {
        cleanupTestFiles();

        synchronized (LOCK) {
            desktopMock = Mockito.mock(Desktop.class);
            mockedDesktop.when(Desktop::getDesktop).thenReturn(desktopMock);
        }

        manager = new ShortLinkManager(TEST_FILE);
    }

    private void cleanupTestFiles() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    /**
     * Создание коротких ссылок
     * Принимает длинный URL и преобразовывет в короткую ссылку
     *
     * Лимит переходов
     */
    void testLimitOfRequestByUser() throws UserExceptions.UserNotFoundException, LinkExceptions.EmptyLongLinkException, LinkExceptions.InvalidUrlException, UserExceptions.InvalidUuidException, LinkExceptions.MaxGenerationAttemptsException, LinkExceptions.ShortLinkNotFoundException, URISyntaxException, IOException, LinkExceptions.EmptyShortCodeException {
        UUID uuid = manager.addLink("https://example.com", 3, null);

        assertNotNull(uuid);

        List<ShortLink> links = manager.getShortLinkService().getLinksForUser(uuid);

        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());

        ShortLink shortLink = links.get(0);
        assertEquals(3, shortLink.getCountOfRequest());

        manager.useLink(shortLink.getShortLink(), uuid);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertEquals(2, shortLink.getCountOfRequest());

        manager.useLink(shortLink.getShortLink(), uuid);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertEquals(1, shortLink.getCountOfRequest());

        manager.useLink(shortLink.getShortLink(), uuid);
        assertEquals(0, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertEquals(0, manager.getUserService().getNotifications(uuid).size());

        assertThrows(
                LinkExceptions.ShortLinkNotFoundException.class,
                () -> manager.useLink(shortLink.getShortLink(), null)
        );
    }

    @Test
    void testLimitOfRequestByNullUser() throws UserExceptions.UserNotFoundException, LinkExceptions.EmptyLongLinkException, LinkExceptions.InvalidUrlException, UserExceptions.InvalidUuidException, LinkExceptions.MaxGenerationAttemptsException, LinkExceptions.ShortLinkNotFoundException, URISyntaxException, IOException, LinkExceptions.EmptyShortCodeException {
        UUID uuid = manager.addLink("https://example.com", 3, null);
        assertNotNull(uuid);

        List<ShortLink> links = manager.getShortLinkService().getLinksForUser(uuid);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());

        ShortLink shortLink = links.get(0);
        assertEquals(3, shortLink.getCountOfRequest());

        manager.useLink(shortLink.getShortLink(), null);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertEquals(2, shortLink.getCountOfRequest());

        manager.useLink(shortLink.getShortLink(), null);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertEquals(1, shortLink.getCountOfRequest());

        manager.useLink(shortLink.getShortLink(), null);
        assertEquals(0, manager.getShortLinkService().getLinksForUser(uuid).size());
        System.out.println(manager.getUserService().getNotifications(uuid).get(0));
        assertFalse(manager.getUserService().getNotifications(uuid).isEmpty());
        assertThrows(
                LinkExceptions.ShortLinkNotFoundException.class,
                () -> manager.useLink(shortLink.getShortLink(), null)
        );
    }

    @Test
    /**
     * Ограничение времени жизни ссылки. Проверка времени ссылки при использовании
     */
    void testExpiredLinkInUse() throws UserExceptions.UserNotFoundException, LinkExceptions.EmptyLongLinkException, LinkExceptions.InvalidUrlException, UserExceptions.InvalidUuidException, LinkExceptions.MaxGenerationAttemptsException, LinkExceptions.ShortLinkNotFoundException, LinkExceptions.EmptyShortCodeException, LinkExceptions.InvalidExpirationTimeException, LinkExceptions.UnauthorizedAccessException, InterruptedException {
        UUID uuid = manager.addLink("https://example.com", 3, null);
        assertNotNull(uuid);

        List<ShortLink> links = manager.getShortLinkService().getLinksForUser(uuid);

        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());

        ShortLink shortLink = links.get(0);
        assertEquals(3, shortLink.getCountOfRequest());
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertFalse(shortLink.isExpired());

        manager.changeLink(shortLink.getShortLink(), uuid, 3, shortLink.getCreateTime().plusSeconds(2));
        Thread.sleep(2000);
        assertTrue(shortLink.isExpired());
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());

        assertThrows(
                LinkExceptions.ShortLinkNotFoundException.class,
                () -> manager.useLink(shortLink.getShortLink(), null)
        );
        assertEquals(0, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertFalse(manager.getUserService().getNotifications(uuid).isEmpty());
    }

    @Test
    /**
     * Ограничение времени жизни ссылки. Проверка времени ссылки при очистке
     */
    void testExpiredLinkClear() throws UserExceptions.UserNotFoundException, LinkExceptions.EmptyLongLinkException, LinkExceptions.InvalidUrlException, UserExceptions.InvalidUuidException, LinkExceptions.MaxGenerationAttemptsException, LinkExceptions.ShortLinkNotFoundException, LinkExceptions.EmptyShortCodeException, LinkExceptions.InvalidExpirationTimeException, LinkExceptions.UnauthorizedAccessException, InterruptedException {
        UUID uuid = manager.addLink("https://example.com", 3, null);
        assertNotNull(uuid);

        List<ShortLink> links = manager.getShortLinkService().getLinksForUser(uuid);

        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());

        ShortLink shortLink = links.get(0);
        assertEquals(3, shortLink.getCountOfRequest());
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertFalse(shortLink.isExpired());

        manager.changeLink(shortLink.getShortLink(), uuid, 3, shortLink.getCreateTime().plusSeconds(2));
        Thread.sleep(2000);
        assertTrue(shortLink.isExpired());
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid).size());
        manager.clearLinks();
        assertEquals(0, manager.getShortLinkService().getLinksForUser(uuid).size());
        assertFalse(manager.getUserService().getNotifications(uuid).isEmpty());
    }

    @Test
    /**
     * Разные пользователи получают разные короткие ссылки на один и тот же URL
     */
    void testDifferentShortLink() throws UserExceptions.UserNotFoundException, LinkExceptions.EmptyLongLinkException, LinkExceptions.InvalidUrlException, UserExceptions.InvalidUuidException, LinkExceptions.MaxGenerationAttemptsException {
        UUID uuid1 = manager.addLink("https://example.com", 3, null);
        assertNotNull(uuid1);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid1).size());

        UUID uuid2 = manager.addLink("https://example.com", 3, null);
        assertNotNull(uuid2);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid2).size());

        ShortLink shortLink1 = manager.getShortLinkService().getLinksForUser(uuid1).get(0);
        ShortLink shortLink2 = manager.getShortLinkService().getLinksForUser(uuid2).get(0);

        assertNotEquals(shortLink1.getShortCode(), shortLink2.getShortCode());
        assertNotEquals(shortLink1.getShortLink(), shortLink2.getShortLink());
    }

    @Test
    /**
     * Действия по редактированию или удалению ссылок доступны только создателю этой ссылки
     */
    void testChangeAndDeleteShortLink() throws UserExceptions.UserNotFoundException, LinkExceptions.EmptyLongLinkException, LinkExceptions.InvalidUrlException, UserExceptions.InvalidUuidException, LinkExceptions.MaxGenerationAttemptsException, LinkExceptions.ShortLinkNotFoundException, URISyntaxException, IOException, LinkExceptions.EmptyShortCodeException, LinkExceptions.InvalidExpirationTimeException, LinkExceptions.UnauthorizedAccessException, InterruptedException {
        UUID uuid1 = manager.addLink("https://example.com", 3, null);
        assertNotNull(uuid1);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid1).size());

        UUID uuid2 = manager.addLink("https://example.com", 3, null);
        assertNotNull(uuid2);
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid2).size());

        ShortLink shortLink1 = manager.getShortLinkService().getLinksForUser(uuid1).get(0);

        assertThrows(
                LinkExceptions.UnauthorizedAccessException.class,
                () -> manager.deleteLink(shortLink1.getShortLink(), uuid2)
        );
        assertEquals(1, manager.getShortLinkService().getLinksForUser(uuid1).size());
        assertThrows(
                LinkExceptions.UnauthorizedAccessException.class,
                () -> manager.changeLink(shortLink1.getShortLink(), uuid2, 10, null)
        );
        assertEquals(3, shortLink1.getCountOfRequest());

        manager.changeLink(shortLink1.getShortLink(), uuid1, 10, null);
        assertEquals(10, shortLink1.getCountOfRequest());
        manager.deleteLink(shortLink1.getShortLink(), uuid1);
        assertEquals(0, manager.getShortLinkService().getLinksForUser(uuid1).size());
    }

}


