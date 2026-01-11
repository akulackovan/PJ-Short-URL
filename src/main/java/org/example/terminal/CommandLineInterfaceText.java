package org.example.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class CommandLineInterfaceText {

    public static void getInfoAboutAllCommand(Map<String, CommandInfo> commands){
        for (Map.Entry<String, CommandInfo> entry : commands.entrySet()) {
            CommandInfo cmd = entry.getValue();
            getSyntaxCommand(cmd);
        }
    }

    public static void getInfoAboutCommand(CommandInfo commandInfo) {
        if (commandInfo == null) {
            System.out.println("Команды не существует");
            return;
        }
        System.out.println("--- " + commandInfo.getDescription() + " ---");

        getSyntaxCommand(commandInfo);

        List<CommandInfo.FlagInfo> flagInfos = commandInfo.getFlags();
        if (!flagInfos.isEmpty()){
            System.out.println("\nПараметры:");
            for (CommandInfo.FlagInfo flag : flagInfos){
                System.out.printf("\t%-2s %-20s - %s%n", flag.getName(), flag.getShortDescription(), flag.getDescription());
            }
        }
        if (commandInfo.getExample() != null){
            System.out.println("\nПримеры:");
            System.out.println(commandInfo.getExample());
        }
    }

    private static void getSyntaxCommand(CommandInfo cmd) {
        StringBuilder description = new StringBuilder(cmd.getName()).append(" ");

        if (cmd.getSubcommand() != null) {
            description.append("[<").append(cmd.getSubcommand()).append(">] ");
        }

        // Обязательные флаги
        cmd.getFlags().stream()
                .filter(CommandInfo.FlagInfo::isRequired)
                .forEach(flag -> description.append(flag.getName()).append(" <").append(flag.getShortDescription()).append(">").append(" "));

        // Опциональные флаги
        List<CommandInfo.FlagInfo> optionalFlags = cmd.getFlags().stream()
                .filter(flag -> !flag.isRequired())
                .toList();

        if (!optionalFlags.isEmpty()) {
            description.append("[");
            List<String> flagDescriptions = new ArrayList<>();
            optionalFlags.forEach(flag ->
                    flagDescriptions.add(flag.getName() + " <" + flag.getShortDescription() + ">")
            );
            description.append(String.join(" ", flagDescriptions));
            description.append("]");
        }

        System.out.printf("%-60s - %s%n", description.toString().trim(), cmd.getDescription());
    }
}
