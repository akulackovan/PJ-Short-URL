package org.example.terminal;

import java.util.ArrayList;
import java.util.List;

public class CommandInfo {

    public static class FlagInfo{
        private final String name;
        private final boolean required;
        private final String description;
        private final String defaultValue;
        private final String shortDescription;

        public FlagInfo(String name, boolean required, String shortDescription, String description) {
            this.name = name;
            this.required = required;
            this.description = description;
            this.defaultValue = null;
            this.shortDescription = shortDescription;
        }

        public FlagInfo(String name, boolean required, String shortDescription, String description, String defaultValue) {
            this.name = name;
            this.required = required;
            this.description = description;
            this.defaultValue = defaultValue;
            this.shortDescription = shortDescription;
        }

        public String getName() { return name; }
        public boolean isRequired() { return required; }
        public String getDescription() { return description; }
        public String getDefaultValue() { return defaultValue; }
        public String getShortDescription() { return shortDescription; }

    }

    private final String name;
    private final String description;
    private List<FlagInfo> flags;
    private String subcommand;
    private String example;

    public CommandInfo(String name, String description, List<FlagInfo> flags) {
        this.name = name;
        this.description = description;
        this.flags = flags;
        this.subcommand = null;
        this.example = null;
    }

    public CommandInfo(String name, String description, List<FlagInfo> flags, String example) {
        this.name = name;
        this.description = description;
        this.flags = flags;
        this.subcommand = null;
        this.example = example;

    }

    public String getName() {
        return name;
    }

    public List<FlagInfo> getFlags() {
        return flags;
    }

    public String getDescription() {
        return description;
    }

    public String getSubcommand() {
        return subcommand;
    }

    public void setSubcommand(String subcommand) {
        this.subcommand = subcommand;
    }

    public String getExample() {
        return example;
    }

}
