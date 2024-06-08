package com.example.blockchain.old;

import com.example.blockchain.old.sockets.OldNetworkUser;

import java.io.IOException;
import java.util.Scanner;

public class View implements Runnable {

    protected OldNetworkUser oldNetworkUser;

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String text;
        displayHelp();
        do {
            text = scanner.nextLine();
            processCommand(text);
        } while(!text.equals("/close"));

    }

    protected void processCommand(String text) {

        int endCMDIndex = text.indexOf(' ');
        if(endCMDIndex == -1)
            endCMDIndex = text.length();

        String command = text.substring(0, endCMDIndex).toLowerCase();
        String args = text.substring(text.indexOf(' ') + 1);

        switch (command) {
            case "/hello" -> System.out.println("Hello world!");
            case "/start" -> {
                int port = Integer.parseInt(args);
                try {
                    oldNetworkUser.startListener(port);
                    System.out.println("Listening at port " + port);
                } catch (IOException e) {
                    System.out.println("Unable to start listening on port " + port);
                }
            }
            case "/connect" -> {
                if (oldNetworkUser.isListening()) {
                    try {
                        oldNetworkUser.connectTo(args);
                    } catch (IOException e) {
                        System.out.println("Unable to connect to " + args);
                    }
                }
                else
                    System.out.println("You are currently offline! Start listening with /start \"port\"");
            }
            case "/close" -> System.out.println("Goodbye!");
        }
    }

    protected void displayHelp() {
        System.out.println("""
                /hello: Says "Hello world!".
                /start P: Starts listening on port "P".
                /connect X:Y: Connects to the port "Y" at the IP "X".
                /close: Closes the program.
                /help: Displays the help.
                """);
    }
}
