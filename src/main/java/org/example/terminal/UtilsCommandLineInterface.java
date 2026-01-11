package org.example.terminal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Класс для утилит CLI
 */
public class UtilsCommandLineInterface {

    public static LocalDateTime parseDateTime(LocalDateTime time, String value) {
        if (value == null) return null;
        if (value.matches("^\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}$")) { // проверка через регулярные выражения
            // Формат с временем: "dd.MM.yyyy HH:mm"
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка парсинга даты с временем: " + value);
                return null;
            }
        } else if (value.matches("^\\d+:\\d{2}:\\d{2}$")) {
            String[] timeInput = value.split(":");
            if (Long.parseLong(timeInput[1]) > 60 || Long.parseLong(timeInput[2]) > 60) {
                throw new IllegalArgumentException("Неверный формат даты. Используйте: дд.мм.гггг чч:мм:cc или чч:мм:cc");
            }
            time = time.plusHours(Long.parseLong(timeInput[0]));
            time = time.plusMinutes(Long.parseLong(timeInput[1]));
            time = time.plusSeconds(Long.parseLong(timeInput[2]));
            // Формат без даты: ЧЧЧЧ:ММ:СС"
            return time;
        } else {
            throw new IllegalArgumentException("Неверный формат даты. Используйте: дд.мм.гггг чч:мм:cc или чч:мм:cc");
        }
    }

}
