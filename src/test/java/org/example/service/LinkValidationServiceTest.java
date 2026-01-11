package org.example.service;

import org.example.exception.LinkExceptions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LinkValidationServiceTest {

    @Test
    void testValidLink() {
        assertDoesNotThrow(() -> LinkValidationService.validateLongLink("https://example.com"));
        assertDoesNotThrow(() -> LinkValidationService.validateLongLink("http://example.com"));
    }

    @Test
    void testEmptyLink() {
        assertThrows(LinkExceptions.EmptyLongLinkException.class,
                () -> LinkValidationService.validateLongLink(""));
        assertThrows(LinkExceptions.EmptyLongLinkException.class,
                () -> LinkValidationService.validateLongLink(null));
    }

    @Test
    void testInvalidLink() {
        assertThrows(LinkExceptions.InvalidUrlException.class,
                () -> LinkValidationService.validateLongLink("https:/example.com"));
        assertThrows(LinkExceptions.InvalidUrlException.class,
                () -> LinkValidationService.validateLongLink("https://"));
    }

}
