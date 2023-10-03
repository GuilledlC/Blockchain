package nodes;

import java.io.IOException;
import java.util.Scanner;

public class NodeView implements Runnable {

    private Node node;

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWhat do you wish to call yourself");
        String text = scanner.nextLine();
        try {
            node = new Node(text);

            displayHelp();
            do {
                text = scanner.nextLine();
                processCommand(text);
            } while(!text.equals("/close"));

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void processCommand(String text) throws IOException {

        int endCMDIndex = text.indexOf(' ');
        if(endCMDIndex == -1)
            endCMDIndex = text.length();

        String command = text.substring(0, endCMDIndex).toLowerCase();

        switch (command) {
            case "/help":
                displayHelp();
                break;
            case "/hello":
                System.out.println("Hello world!");
                break;
            case "/start":
                int port = Integer.parseInt(text.substring(text.indexOf(' ') + 1));
                node.startListener(port);
                break;
            case "/connect":
                if(node.isListening())
                    node.connectTo(text.substring(text.indexOf(' ') + 1));
                else
                    System.out.println("You are currently offline! Start listening with /start \"port\"");
                break;
            case "/close":
                System.out.println("Goodbye!");
                break;
            default:
                node.sendMessage(command);
                break;
        }
    }

    private void displayHelp() {
        System.out.println("""
                        /help: Displays the help.
                        /hello: Says "Hello world!".
                        /start P: Starts listening on port "P".
                        /connect X:Y: Connects to the port "Y" at the IP "X".
                        /close: Closes the program.
                        Anything else: sends everything to all the nodes
                        """);
    }

    public static void main(String[] args) {
        NodeView nv = new NodeView();
        nv.run();
    }
}
