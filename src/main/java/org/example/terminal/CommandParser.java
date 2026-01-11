package org.example.terminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Класс для обработки параметов команды
 */
public class CommandParser {

    private final static List<String> errors = new ArrayList<>();
    private Scanner scanner;


    public CommandParser(Scanner scanner) {
        this.scanner = scanner;
    }

    public Map<String, String> parseCommand(String[] args, CommandInfo command) {
        errors.clear();

        // Получаем ожидаемые аргументы для команды
        List<CommandInfo.FlagInfo> flags = command.getFlags();

        // Индекс начала аргументов (после имени команды)
        int indexOfBeginArguments = command.getName().split(" ").length;

        Map<String, String> params = new HashMap<>();


        for (int i = indexOfBeginArguments; i < args.length; i++) {
            String currentArg = args[i];

            if (currentArg.isEmpty()) continue;

            if (currentArg.startsWith("-")) {
                // Проверяем, что флаг ожидаемый

                if (flags.stream().noneMatch(flag -> flag.getName().equals(currentArg))) {
                    errors.add("Неизвестный флаг: " + currentArg);
                    continue;
                }
                while (i + 1 < args.length && args[i + 1].isEmpty()) {
                    i++;
                }

                // Проверяем, что есть значение для флага
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    errors.add("Флаг " + currentArg + " требует значения");
                    continue;
                }

                // Обработка значений в кавычках
                String value = args[i + 1];
                if (value.startsWith("\"") && (!value.endsWith("\"") || value.length() == 1)) {
                    // Многословное значение в кавычках
                    StringBuilder quotedValue = new StringBuilder();
                    quotedValue.append(value.substring(1)); // убираем открывающую кавычку

                    int j = 2;
                    while (i + j < args.length && !args[i + j].endsWith("\"")) {
                        quotedValue.append(" ").append(args[i + j]);
                        j++;
                    }

                    if (i + j < args.length) {
                        quotedValue.append(" ").append(args[i + j], 0, args[i + j].length() - 1);
                        value = quotedValue.toString();
                        i += j; // пропускаем все слова кавычек
                    } else {
                        errors.add("Незакрытая кавычка для флага: " + currentArg);
                        value = quotedValue.toString();
                    }
                } else {
                    // Обычное значение
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    i++; // пропускаем значение
                }

                params.put(currentArg, value);
            } else {
                errors.add("Неожиданный аргумент: " + currentArg + ". Ожидался флаг.");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }

        for (int i = 0; i < flags.size(); i++) {
            if (!params.containsKey(flags.get(i).getName())) {
                String input = getInput(flags.get(i).getDescription());
                if ("q".equalsIgnoreCase(input)) {
                    throw new IllegalArgumentException("Завершение операции...");
                }
                params.put(flags.get(i).getName(), input);
                if (input == null && flags.get(i).isRequired()) {
                    System.out.println("Поле \"" + flags.get(i).getDescription() + "\" не может быть пустым.");
                    return null;
                } else if (input == null && flags.get(i).getDefaultValue() != null) {
                    System.out.println("Для поля будет использовано значение " + flags.get(i).getDefaultValue() + ".");
                    params.put(flags.get(i).getName(), flags.get(i).getDefaultValue());
                }
            }
        }
        return params;
    }

    /**
     * Получить значение от пользователя
     */
    private String getInput(String prompt) {
        System.out.print("Введите поле \"" + prompt + "\" (q - отмена): ");
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }

    public String getUserUUID(String uuid) {
        if (uuid != null) return uuid;
        System.out.println("Если Вы не имеете UUID пользователя пропустите следующий шаг.");
        return getInput("UUID пользователя");
    }

}