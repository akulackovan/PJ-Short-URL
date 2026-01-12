package org.example.terminal;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.example.model.ShortLink;
import org.example.service.ShortLinkManager;
import org.example.storage.Config;

/**
 * Класс для обрабтка команд и вовода информации
 */
public class CommandLineInterface {

    private final Scanner scanner;
    private final ShortLinkManager shortLinkManager;
    private UUID loggedUser;
    private final CommandRegistry commands;
    private final CommandParser commandParser;
    private ScheduledExecutorService scheduler;

    public CommandLineInterface() {
        this.scanner = new Scanner(System.in, "CP866");
        this.shortLinkManager = new ShortLinkManager(null);
        this.loggedUser = null;
        this.commands = new CommandRegistry();
        commandParser = new CommandParser(scanner);
    }

    /**
     * Точка входа
     */
    public void run() {
        System.out.println("Сервис коротких ссылок");
        showMainMenu();
    }

    /**
     * Главное меню
     */
    private void showMainMenu() {
        System.out.println("\n--- Вход в главное меню ---");


        scheduler = Executors.newScheduledThreadPool(1);


        int time = Integer.parseInt(Config.get("links.clear_time", "1"));
        if (time > 0) {
            scheduler.scheduleAtFixedRate(() -> {
                shortLinkManager.clearLinks();
            }, 0, time, TimeUnit.SECONDS);
        }

        while (true) {
            shortLinkManager.clearLinks();
            System.out.println("\nВведите операцию:");
            String[] args = scanner.nextLine().trim().split(" ");
            CommandsName commandEnum = CommandsName.fromString(args[0]);
            shortLinkManager.clearLinks();
            if (isLogout()) continue;
            switch (commandEnum) {
                case EMPTY_COMMAND -> {
                    continue;
                }
                case CREATE_LINK_COMMAND -> createLink(args);
                case LOGIN_COMMAND -> login(args);
                case LOGOUT_COMMAND -> logout(args);
                case EXIT_COMMAND, EXIT_Q_COMMAND -> exit(args);

                case USE_LINK_COMMAND -> useLink(args);
                case LIST_LINK_COMMAND -> list(args);
                case STATUS_LINK_COMMAND -> statusLink(args);
                case STATUS_COMMAND -> status(args);
                case CHANGE_COMMAND -> changeLink(args);
                case DELETE_COMMAND -> deleteLink(args);
                case HELP_COMMAND -> help(args);
                default -> System.out.println("Неизвестная команда! Используйте команды из списка.");
            }

        }
    }

    private boolean isLogout() {
        if (loggedUser != null) {
            shortLinkManager.getUserService().sendNotificationsToUser(loggedUser);
            shortLinkManager.removeUser(loggedUser);
            if (!shortLinkManager.getUserService().userExists(loggedUser)) {
                System.out.println("Все короткие ссылки пользователя удалены");
                System.out.println("Пользователь был удален!");
                System.out.println("Выполните создание новой короткой ссылки для создания нового UUID");
                loggedUser = null;
                return true;
            }
        }
        return false;
    }

    private void createLink(String[] args) {
        Map<String, String> params;
        try {
            params = commandParser.parseCommand(args,
                    commands.getCommands().get(CommandsName.CREATE_LINK_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        if (params == null)
            return;
        Integer count = null;
        try {
            if (params.getOrDefault("-c", null) != null) {
                count = Integer.parseInt(params.get("-c"));
                if (count != null && count <= 0) {
                    System.out.println("Значение количества обращений не может быть меньше или равно 0");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(
                    "Количество обращений к короткой ссылке не является числом.\nКороткая ссылка не будет создана");
            return;
        }
        try {
            String uuid = loggedUser == null ? commandParser.getUserUUID(null) : loggedUser.toString();
            UUID tempUUID = shortLinkManager.addLink(params.get("-l"), count, uuid);
            if (tempUUID != loggedUser) {
                loggedUser = tempUUID;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Короткая ссылка не будет создана");
        }
    }

    private void login(String[] args) {
        if (loggedUser != null) {
            System.out.println("Вы работаете за пользователя. Смените выйдите из UUID");
            return;
        }
        Map<String, String> params;
        try {
            params = commandParser.parseCommand(args, commands.getCommands().get(CommandsName.LOGIN_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        if (params == null)
            return;
        try {
            loggedUser = shortLinkManager.getUserService().validateUser(params.get("-u"));
            if (loggedUser != null) System.out.println("Успешный вход");
            isLogout();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Вход не будет выполнен");
            loggedUser = null;
        }
    }

    private void logout(String[] args) {
        if (loggedUser == null) {
            System.out.println("Вы не работаете за пользователя. " +
                    "Выполните вход по UUID или создание новой ссылки для того, чтобы получить UUID");
            return;
        }
        try {
            commandParser.parseCommand(args, commands.getCommands().get(CommandsName.LOGOUT_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        loggedUser = null;
        System.out.println("Выход из учетной записи");

    }

    private void status(String[] args) {
        if (loggedUser == null) {
            System.out.println(
                    "Вы не работаете за пользователя. Выполните вход по UUID или создание новой ссылки для того, чтобы получить UUID");
            return;
        }
        try {
            commandParser.parseCommand(args, commands.getCommands().get(CommandsName.STATUS_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Вы работаете с UUID: " + loggedUser.toString());
        System.out.println("Количество ссылок: " + shortLinkManager.getShortLinkService().getLinksForUser(loggedUser).size());

    }

    private void statusLink(String[] args) {
        if (Boolean.parseBoolean(Config.get("users.need_login")) && loggedUser == null) {
            System.out.println("Для использования ссылок необходимо зайти за пользователя. " +
                    "Выполните вход по UUID или создание новой ссылки для того, чтобы получить UUID");
            return;
        }
        Map<String, String> params;
        ShortLink shortLink;
        try {
            params = commandParser.parseCommand(args, commands.getCommands().get(CommandsName.STATUS_LINK_COMMAND.getCommand()));
            shortLink = shortLinkManager.validateShortLink(params.get("-s"));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        if (shortLink == null) {
            System.out.println("Короткой ссылки не существует");
            return;
        }
        System.out.println(String.format("%-20s | %-20s | %-20s | %-20s | %-200s", "Сокращенная ссылка",
                "Кол-во переходов", "Время создания", "Время удаления", "Ссылка для перехода"));

        System.out.println(shortLink);
    }

    private void deleteLink(String[] args) {
        if (Boolean.parseBoolean(Config.get("users.need_login")) && loggedUser == null) {
            System.out.println("Для использования ссылок необходимо зайти за пользователя. " +
                    "Выполните вход по UUID или создание новой ссылки для того, чтобы получить UUID");
            return;
        }
        Map<String, String> params;
        ShortLink shortLink;
        try {
            params = commandParser.parseCommand(args, commands.getCommands().get(CommandsName.DELETE_COMMAND.getCommand()));
            shortLinkManager.deleteLink(params.get("-s"), loggedUser);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    private void changeLink(String[] args) {
        if (loggedUser == null) {
            System.out.println("Для использования ссылок необходимо зайти за пользователя. " +
                    "Выполните вход по UUID или создание новой ссылки для того, чтобы получить UUID");
            return;
        }
        Map<String, String> params;
        ShortLink shortLink;
        try {
            params = commandParser.parseCommand(args, commands.getCommands().get(CommandsName.CHANGE_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        Integer count = null;
        try {
            if (params.getOrDefault("-c", null) != null) {
                count = Integer.parseInt(params.get("-c"));
                if (count != null && count <= 0) {
                    System.out.println("Значение количества обращений не может быть меньше или равно 0");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(
                    "Количество обращений к короткой ссылке не является числом");
            return;
        }
        try {
            LocalDateTime removeTime = UtilsCommandLineInterface.parseDateTime(LocalDateTime.now(), params.getOrDefault("-t", null));
            shortLinkManager.changeLink(params.get("-s"), loggedUser, count, removeTime);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    private void useLink(String[] args) {
        if (Boolean.parseBoolean(Config.get("users.need_login")) && loggedUser == null) {
            System.out.println("Для использования ссылок необходимо зайти за пользователя. " +
                    "Выполните вход по UUID или создание новой ссылки для того, чтобы получить UUID");
            return;
        }
        Map<String, String> params;
        try {
            params = commandParser.parseCommand(args,
                    commands.getCommands().get(CommandsName.USE_LINK_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        try {
            shortLinkManager.useLink(params.get("-s"), loggedUser);
        } catch (URISyntaxException e) {
            System.out.println("Ссылка имеет невалидный URL");
        } catch (IOException e) {
            System.out.println("Ошибка при открытии браузера");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void list(String[] args) {
        if (loggedUser == null) {
            System.out.println(
                    "Вы не работаете за пользователя. Выполните вход по UUID или создание новой ссылки для того, чтобы получить UUID");
            return;
        }
        Map<String, String> params;

        try {
            params = commandParser.parseCommand(args,
                    commands.getCommands().get(CommandsName.LIST_LINK_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        List<ShortLink> links = shortLinkManager.getShortLinkService().getLinksForUser(loggedUser);
        if (links.isEmpty()) {
            System.out.println("Для пользователя " + loggedUser.toString() + " нет сокращенных ссылок");
            return;
        }
        System.out.println(String.format("%-20s | %-20s | %-20s | %-20s | %-200s", "Сокращенная ссылка",
                "Кол-во переходов", "Время создания", "Время удаления", "Ссылка для перехода"));
        for (ShortLink link : links) {
            System.out.println(link.toString());
        }
    }

    private void help(String[] args) {
        if (args.length > 2) {
            System.out.println("Неверный синтаксис команды");
        }
        if (args.length == 1 || args[1].equals("help")) {
            CommandLineInterfaceText.getInfoAboutAllCommand(commands.getCommands());
            return;
        }
        CommandLineInterfaceText.getInfoAboutCommand(commands.getCommands().getOrDefault(args[1], null));
    }


    /**
     * Завершает работу программы.
     */
    public void exit(String[] args) {
        try {
            commandParser.parseCommand(args, commands.getCommands().get(CommandsName.EXIT_COMMAND.getCommand()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Выход из программы...");
        System.exit(0);
    }

}