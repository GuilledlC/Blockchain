package utils;

import nodes.Node;
import sockets.NetworkUser;

import java.io.IOException;
import java.util.Scanner;

public class View implements Runnable {

    protected NetworkUser networkUser;

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
                    networkUser.startListener(port);
                } catch (IOException e) {
                    System.out.println("Unable to start listening on port " + port);
                }
            }
            case "/connect" -> {
                if (networkUser.isListening()) {
                    try {
                        networkUser.connectTo(args);
                    } catch (IOException e) {
                        System.out.println("Unable to connect to " + args);
                    }
                }
                else
                    System.out.println("You are currently offline! Start listening with /start \"port\"");
            }
            case "/close" -> System.out.println("Goodbye!");
            default -> networkUser.sendMessage(command);
        }
    }

    protected void displayHelp() {
        System.out.println("""
                /hello: Says "Hello world!".
                /start P: Starts listening on port "P".
                /connect X:Y: Connects to the port "Y" at the IP "X".
                /close: Closes the program.
                /help: Displays the help.
                Anything else: sends everything to all the connected nodes.
                """);
    }
}
