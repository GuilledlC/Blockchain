package com.example.blockchain.old.sockets.nodes;

import com.example.blockchain.ledger.Block;
import com.example.blockchain.users.Vote;
import com.example.blockchain.old.View;
import java.util.Scanner;

public class OldNodeView extends View {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWhat do you wish to be called?");
        String text = scanner.nextLine();
        oldNetworkUser = new OldNode(text);
        super.run();

    }

    protected void processCommand(String text) {

        int endCMDIndex = text.indexOf(' ');
        if(endCMDIndex == -1)
            endCMDIndex = text.length();

        String command = text.substring(0, endCMDIndex).toLowerCase();
        String args = text.substring(text.indexOf(' ') + 1);

        OldNode oldNode = (OldNode) oldNetworkUser;
        switch (command) {
            case "/help" -> displayHelp();
            case "/view" -> {
                if(args.equals("votes")) {
                    for (Vote t: oldNode.getVotes())
                        System.out.println(t.displayVoteShort());
                } else if(args.equals("blocks")) {
                    for(Block b: oldNode.getBlocks())
                        System.out.println(b.displayBlock());
                }
            }
            default -> super.processCommand(text);
        }
    }

    protected void displayHelp() {
        System.out.print("""
                /view votes: Shows a list of the votes received.
                /view blocks: Shows a list of the blocks created.
                """);
        super.displayHelp();
    }

    public static void main(String[] args) {
        OldNodeView nv = new OldNodeView();
        nv.run();
    }
}
