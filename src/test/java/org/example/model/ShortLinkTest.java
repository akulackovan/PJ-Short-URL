package org.example.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ShortLinkTest {

    @Test
    void testShortLinkUse() {
        ShortLink shortLink = new ShortLink("abcdef", "click.ru/12345", "https://example.com",
                10, LocalDateTime.now(), LocalDateTime.now(), UUID.randomUUID());
        assertEquals(shortLink.getCountOfRequest(), 10);
        shortLink.use();
        assertEquals(shortLink.getCountOfRequest(), 9);
    }

}
