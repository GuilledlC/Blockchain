package nodes;

import ledger.Block;
import users.Transaction;
import utils.View;
import java.util.Scanner;

public class NodeView extends View {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWhat do you wish to be called?");
        String text = scanner.nextLine();
        networkUser = new Node(text);
        super.run();

    }

    protected void processCommand(String text) {

        int endCMDIndex = text.indexOf(' ');
        if(endCMDIndex == -1)
            endCMDIndex = text.length();

        String command = text.substring(0, endCMDIndex).toLowerCase();
        String args = text.substring(text.indexOf(' ') + 1);

        Node node = (Node)networkUser;
        switch (command) {
            case "/help" -> displayHelp();
            case "/viewvotes" -> {
                for (Transaction t: node.getTransactions()) {
                    System.out.println(t.displayTransactionShort());
                }
            }
            case "/viewblocks" -> {
                for(Block b: node.getBlocks()) {
                    System.out.println(b.displayBlock());
                }
            }
            default -> super.processCommand(text);
        }
    }

    protected void displayHelp() {
        System.out.print("""
                /viewvotes: Shows a list of the transactions received.
                /viewblocks: Shows a list of the blocks created.
                """);
        super.displayHelp();
    }

    public static void main(String[] args) {
        NodeView nv = new NodeView();
        nv.run();
    }
}
