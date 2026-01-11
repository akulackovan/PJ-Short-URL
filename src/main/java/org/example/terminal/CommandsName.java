package org.example.terminal;

import java.util.Objects;

public enum CommandsName {
    CREATE_LINK_COMMAND("create"),
    USE_LINK_COMMAND("use"),
    STATUS_COMMAND("status"),
    STATUS_LINK_COMMAND("status-link"),
    LIST_LINK_COMMAND("list"),
    DELETE_COMMAND("delete"),
    CHANGE_COMMAND("change"),
    LOGIN_COMMAND("login"),
    LOGOUT_COMMAND("logout"),
    HELP_COMMAND("help"),
    EXIT_COMMAND("exit"),
    EXIT_Q_COMMAND("q"),
    EMPTY_COMMAND(""),
    UNKNOWN_COMMAND("");

    private final String command;

    CommandsName(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static CommandsName fromString(String text) {
        if (text.isEmpty()) {
            return EMPTY_COMMAND;
        }
        for (CommandsName cmd : CommandsName.values()) {
            if (cmd.command.equals(text)) {
                return cmd;
            }
        }
        return UNKNOWN_COMMAND;
    }
}