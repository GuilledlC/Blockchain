package nodes;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Node {

    private Listener listener = null;
    private String id;

    public Node(String id) {
        this.id = id;

        ioSystem();
    }

    private void startListener(int port) throws IOException {
        listener = new Listener(port);
        listener.startListening();
    }

    public void ioSystem() {
        Scanner scanner = new Scanner(System.in);
        new Thread(new Runnable() {
            @Override
            public void run() {

                while(listener == null) {
                    System.out.println("What port do you want to host out of? ");
                    int port = scanner.nextInt();
                    try {
                        startListener(port);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                scanner.nextLine(); //Flushing the scanner

                String text = scanner.nextLine();
                while(text != "/close") {
                    switch (text.charAt(0)) {
                        case '/':
                            try {
                                processCommand(text.substring(1));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        default:
                            listener.sendMessage(text);
                            break;
                    }

                    text = scanner.nextLine();
                }
            }
        }).start();
    }

    private void processCommand(String text) throws IOException {

        int endCMDIndex = text.indexOf(' ');
        if(endCMDIndex == -1)
            endCMDIndex = text.length();

        String command = text.substring(0, endCMDIndex).toLowerCase();

        switch (command) {
            case "hello":
                System.out.println("Hello world!");
                break;
            case "connect":
                connectTo(text.substring(text.indexOf(' ') + 1));
                break;
            default:
                break;
        }
    }

    private void connectTo(String address) throws IOException {
        String ip = address.substring(0, address.indexOf(':'));
        int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
        connectTo(ip, port);
    }

    private void connectTo(String ip, int port) throws IOException {
        listener.connectTo(ip, port);
    }

    /**another 'ask' to send data*/

    /**another to maybe handshake,*/

    /**another to get version info to make sure they are all same version/etc*/

    /**Connect to other nodes**/

    /**Get connected by other nodes**/

    /**Propagate nodes*/

    /**Compare Ledgers**/

    /**Build Block**/

    /**Add Block to Ledger**/
}
