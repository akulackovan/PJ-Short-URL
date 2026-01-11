package org.example.service;

import java.util.Random;
import java.util.Set;

import org.example.exception.LinkExceptions;
import org.example.storage.Config;

/**
 *  Сервис для генерации сокращенной ссылки
 *  Для каждой ссылки генерируется свой уникальный код
 */
public class LinkGeneratorService {

    public static String generateShortLink(Set<String> activeCodes) throws LinkExceptions.MaxGenerationAttemptsException {
        String shortCode;
        int numTry = 0;
        String alphabet = Config.get("links.alphabet", "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz");
        int size = Integer.parseInt(Config.get("links.size", "6"));
        int maxTry = Integer.parseInt(Config.get("links.max_try", "1000"));
        do {
            StringBuilder salt = new StringBuilder();
            Random rnd = new Random();
            while (salt.length() < size) {
                int index = (int) (rnd.nextFloat() * alphabet.length());
                salt.append(alphabet.charAt(index));
            }
            shortCode = salt.toString();
            numTry++;
            if (numTry >= maxTry) {
                throw new LinkExceptions.MaxGenerationAttemptsException();
            }
        } while (activeCodes.contains(shortCode));
        return shortCode;
    }

}
