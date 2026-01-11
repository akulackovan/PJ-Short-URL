package org.example.exception;

public class UserExceptions {

    /**
     * Исключение при неверном UUID
     */
    public static class InvalidUuidException  extends Exception {
        public InvalidUuidException () {
            super("Ошибка: UUID пользователя имеет невалидный тип");
        }
    }

    /**
     * Исключение при несуществующем пользователе
     */
    public static class UserNotFoundException extends Exception {
        public UserNotFoundException(String uuid) {
            super("Ошибка: пользователя " + uuid + " не существует");
        }
    }
}
