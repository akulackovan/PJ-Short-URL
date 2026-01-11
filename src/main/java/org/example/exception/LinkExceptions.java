package org.example.exception;

public class LinkExceptions {
    /**
     * Исключение при передаче невалидной полной ссылки
     */
    public static class InvalidUrlException extends Exception {
        public InvalidUrlException() {
            super("Ошибка: ссылка для сокращения невалидна");
        }
    }

    /**
     * Исключение при передаче пустой полной ссылки
     */
    public static class EmptyLongLinkException extends Exception {
        public EmptyLongLinkException() {
            super("Ошибка: ссылка для сокращения для пуста");
        }
    }

    /**
     * Исключение при передаче пустой сокращенной ссылки
     */
    public static class EmptyShortCodeException extends Exception {
        public EmptyShortCodeException() {
            super("Ошибка: не введена сокращенная ссылка");
        }
    }


    /**
     * Исключение при неверном времени
     */
    public static class InvalidExpirationTimeException extends Exception {
        public InvalidExpirationTimeException() {
            super("Ошибка: указано время удаления короткой ссылки меньше времени ее создания");
        }
    }

    /**
     * Исключение при отсутсвии ссылки
     */
    public static class ShortLinkNotFoundException extends Exception {
        public ShortLinkNotFoundException() {
            super("Ошибка: короткая ссылка не существует");
        }
    }

    /**
     * Исключение при масимальном кол-ве попыток
     */
    public static class MaxGenerationAttemptsException extends Exception {
        public MaxGenerationAttemptsException() {
            super("Ошибка: достигнуто максимальное число попыток для генерации ссылки");
        }
    }

    /**
     * Исключение при попытке редактирования ссылки другим пользователем
     */
    public static class UnauthorizedAccessException extends Exception {
        public UnauthorizedAccessException() {
            super("Ошибка: ссылка не принадлежит пользователю");
        }
    }
}
