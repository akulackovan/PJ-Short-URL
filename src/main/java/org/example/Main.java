package org.example;

import java.security.NoSuchAlgorithmException;

import org.example.terminal.CommandLineInterface;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        while (true){
            commandLineInterface.run();
        }
    }
}
