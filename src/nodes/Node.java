package nodes;

import java.io.IOException;
import java.util.Scanner;

public class Node {

    private Listener listener = null;
    private String id;

    public Node(String id) {
        this.id = id;
    }

    protected void startListener(int port) throws IOException {
        listener = new Listener(port);
        listener.startListening();
    }

    protected void connectTo(String address) throws IOException {
        String ip = address.substring(0, address.indexOf(':'));
        int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
        connectTo(ip, port);
    }

    private void connectTo(String ip, int port) throws IOException {
        listener.connectTo(ip, port);
    }

    protected void sendMessage(String message) {
        listener.sendMessage(message);
    }

    protected boolean isListening() {
        return listener != null;
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
