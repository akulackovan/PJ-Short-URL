package org.example.terminal;

import java.util.*;

import org.example.storage.Config;

public class CommandRegistry {
    private final Map<String, CommandInfo> commands = new LinkedHashMap<>();

    public CommandRegistry() {
        registerHelpCommand();
        registerCreateLinkCommand();
        registerUseCommand();
        registerLoginCommand();
        registerLogoutCommand();
        registerStatusCommand();
        registerListCommand();
        registerStatusLinkCommand();
        registerChangeLinkCommand();
        registerDeleteLinkCommand();
        registerExitCommand();
    }

    public Map<String, CommandInfo> getCommands() {
        return commands;
    }

    private void registerCreateLinkCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        flagInfos.add(new CommandInfo.FlagInfo("-l", true, "ссылка", "Ссылка, которая будет сокращена"));

        flagInfos.add(new CommandInfo.FlagInfo("-c", false, "кол-во обращений", "Количество обращений. При отсутствии будет использовано значение по умолчанию "
                + Config.get("links.count"), Config.get("links.count")));
        String example = "\tcreate -l https://student-lk.skillfactory.ru/ -c 10\n" +
                "\tcreate";
        commands.put(CommandsName.CREATE_LINK_COMMAND.getCommand(), new CommandInfo(CommandsName.CREATE_LINK_COMMAND.getCommand(), "Создание ссылки", flagInfos, example));
    }

    private void registerLoginCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        flagInfos.add(new CommandInfo.FlagInfo("-u", true, "UUID", "UUID пользователя полученный при создании ссылки"));
        commands.put(CommandsName.LOGIN_COMMAND.getCommand(), new CommandInfo(CommandsName.LOGIN_COMMAND.getCommand(), "Вход за пользователя", flagInfos));
    }

    private void registerUseCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        flagInfos.add(new CommandInfo.FlagInfo("-s", true, "короткая ссылка", "Сокращенная ссылка, полученная при ее создании"));
        String example = "\tuse -s clck.ru/7pNMLy\n" +
                "\tuse";
        commands.put(CommandsName.USE_LINK_COMMAND.getCommand(), new CommandInfo(CommandsName.USE_LINK_COMMAND.getCommand(), "Переход по короткой ссылке", flagInfos, example));
    }

    private void registerLogoutCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        commands.put(CommandsName.LOGOUT_COMMAND.getCommand(), new CommandInfo(CommandsName.LOGOUT_COMMAND.getCommand(), "Выход за пользователя", flagInfos));
    }

    private void registerExitCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        commands.put(CommandsName.EXIT_COMMAND.getCommand(), new CommandInfo(CommandsName.EXIT_COMMAND.getCommand(), "Выход", flagInfos));
        commands.put(CommandsName.EXIT_Q_COMMAND.getCommand(), new CommandInfo(CommandsName.EXIT_Q_COMMAND.getCommand(), "Выход", flagInfos));
    }

    private void registerStatusCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        commands.put(CommandsName.STATUS_COMMAND.getCommand(), new CommandInfo(CommandsName.STATUS_COMMAND.getCommand(), "Статус пользователя", flagInfos));
    }

    private void registerStatusLinkCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        flagInfos.add(new CommandInfo.FlagInfo("-s", true, "короткая ссылка", "Сокращенная ссылка, полученная при ее создании"));
        String example = "\tstatus-link -s clck.ru/7pNMLy\n" +
                "\tstatus-link";
        commands.put(CommandsName.STATUS_LINK_COMMAND.getCommand(), new CommandInfo(CommandsName.STATUS_LINK_COMMAND.getCommand(), "Статус ссылки", flagInfos, example));
    }

    private void registerListCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        commands.put(CommandsName.LIST_LINK_COMMAND.getCommand(), new CommandInfo(CommandsName.LIST_LINK_COMMAND.getCommand(), "Список команд", flagInfos));
    }

    private void registerDeleteLinkCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        flagInfos.add(new CommandInfo.FlagInfo("-s", true, "короткая ссылка", "Сокращенная ссылка, полученная при ее создании"));
        String example = "\tdelete -s clck.ru/7pNMLy\n" +
                "\tdelete";
        commands.put(CommandsName.DELETE_COMMAND.getCommand(), new CommandInfo(CommandsName.DELETE_COMMAND.getCommand(), "Удаление короткой ссылки", flagInfos, example));
    }

    private void registerHelpCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo(CommandsName.HELP_COMMAND.getCommand(), "Справка команд", flagInfos);
        commandInfo.setSubcommand("команда");
        commands.put(CommandsName.HELP_COMMAND.getCommand(), commandInfo);
    }

    private void registerChangeLinkCommand() {
        List<CommandInfo.FlagInfo> flagInfos = new ArrayList<>();
        flagInfos.add(new CommandInfo.FlagInfo("-s", true, "короткая ссылка", "Сокращенная ссылка, полученная при ее создании"));
        if (Boolean.parseBoolean(Config.get("users.allow_time_change", "false"))) {
            flagInfos.add(new CommandInfo.FlagInfo("-t", false, "время", "Время жизни ссылки в формате чч:мм:cc (часы могут иметь значение больше 24, данное время будет прибавлено от текущего) или до даты в формате дд.мм.гггг чч:мм:cc." +
                    "При отсутствии будет использовано значение ссылки", null));
        }
        flagInfos.add(new CommandInfo.FlagInfo("-c", false, "кол-во обращений", "Количество обращений, которое будет у данной ссылки. При отсутствии будет использовано текущее значение обращений к ссылке", null));
        String example = "\tchange -s clck.ru/7pNMLy -c 10\n" +
                "\tchange -s clck.ru/7pNMLy\n" +
                "\tchange";
        commands.put(CommandsName.CHANGE_COMMAND.getCommand(), new CommandInfo(CommandsName.CHANGE_COMMAND.getCommand(), "Изменение ссылки", flagInfos, example));
    }
}
