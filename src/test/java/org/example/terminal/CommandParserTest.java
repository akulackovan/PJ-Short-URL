package org.example.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandParserTest {

    private Scanner scanner;
    private CommandParser parser;
    private CommandInfo commandInfo;

    @BeforeEach
    void setUp() {
        // Создаем команду с двумя флагами
        List<CommandInfo.FlagInfo> flags = new ArrayList<>();
        flags.add(new CommandInfo.FlagInfo("-l", true, "ссылка", "Ссылка"));
        flags.add(new CommandInfo.FlagInfo("-c", false, "количество", "Количество", "5"));

        commandInfo = new CommandInfo("create", "Создание ссылки", flags);
    }

    public void inputCommand(String input) {
        scanner = new Scanner(input);
        parser = new CommandParser(scanner);
    }

    @Test
    public void testParseCommandWithQuotes() {
        inputCommand("");
        String[] args = {"create", "-l", "\"https://example.com\"", "-c", "10"};
        Map<String, String> result = parser.parseCommand(args, commandInfo);

        assertEquals("https://example.com", result.get("-l"));
        assertEquals("10", result.get("-c"));
    }

    @Test
    public void testParseCommandWithInsertEmptyUnrequiredField() {
        inputCommand("10");
        String[] args = {"create", "-l", "\"https://example.com\""};
        Map<String, String> result = parser.parseCommand(args, commandInfo);

        assertEquals("https://example.com", result.get("-l"));
        assertEquals("10", result.get("-c"));
    }

    @Test
    public void testParseCommandWithEmptyUnrequiredField() {
        inputCommand(" ");
        String[] args = {"create", "-l", "\"https://example.com\""};
        Map<String, String> result = parser.parseCommand(args, commandInfo);

        assertEquals("https://example.com", result.get("-l"));
        assertEquals("5", result.get("-c"));
    }


    @Test
    public void testParseCommandWithInsertEmptyRequiredField() {
        inputCommand("https://example.com\n10");
        String[] args = {"create"};
        Map<String, String> result = parser.parseCommand(args, commandInfo);

        assertEquals("https://example.com", result.get("-l"));
        assertEquals("10", result.get("-c"));
    }

    @Test
    void testParseCommandWithMultiWordQuotedArguments() {
        inputCommand("");
        String[] args = {"create", "-l", "\"https://example.com", "path", "with", "spaces\"", "-c", "10"};

        Map<String, String> result = parser.parseCommand(args, commandInfo);

        assertNotNull(result);
        assertEquals("https://example.com path with spaces", result.get("-l"));
        assertEquals("10", result.get("-c"));
    }

    @Test
    void testParseCommandWithQuotedArgumentsAndSpace() {
        inputCommand("");
        String[] args = {"create", "-l", "\"", "https://example.com", "\"", "-c", "10"};

        Map<String, String> result = parser.parseCommand(args, commandInfo);

        assertNotNull(result);
        assertEquals(" https://example.com ", result.get("-l"));
        assertEquals("10", result.get("-c"));
    }

    @Test
    void testParseCommandWithArgumentsAndSpace() {
        inputCommand("");
        String[] args = {"create", "-l", "", "https://example.com", "", "-c", "", "", "10", "", ""};

        Map<String, String> result = parser.parseCommand(args, commandInfo);

        assertNotNull(result);
        assertEquals("https://example.com", result.get("-l"));
        assertEquals("10", result.get("-c"));
    }

}
