package com.example.blockchain.old.users;

import com.example.blockchain.users.Vote;
import com.example.blockchain.utils.HashUtils;
import com.example.blockchain.old.View;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class UserView extends View {


    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWhat do you wish to be called?");
        String text = scanner.nextLine();
        try {
            oldNetworkUser = new OldUserOld(text);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);}
        super.run();
    }

    protected void processCommand(String text) {

        int endCMDIndex = text.indexOf(' ');
        if(endCMDIndex == -1)
            endCMDIndex = text.length();

        String command = text.substring(0, endCMDIndex).toLowerCase();
        String args = text.substring(text.indexOf(' ') + 1);

        OldUserOld user = ((OldUserOld) oldNetworkUser);
        Vote t = user.getVote();
        switch (command) {
            case "/help" -> displayHelp();
            case "/vote" -> {
                try {
                    user.vote(args);
                    System.out.println("Vote recorded. Type /view vote to view your vote.");
                    /**Check if vote has been received*/
                } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
			}
            case "/view" -> {
                if(args.equals("vote")) {
                    if (t != null) {
                        try {
                            System.out.println(t.displayVote());
                        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException |
								 InvalidKeySpecException | IOException e) {
                            throw new RuntimeException(e);
                        }
					}
                    else
                        System.out.println("You haven't voted yet!");
                } else if(args.equals("address")) {
                    System.out.println(HashUtils.toHexString(user.getAddress()));
                }
            }
            default -> super.processCommand(text);
        }
    }

    protected void displayHelp() {
        System.out.print("""
                /vote X: Votes for the candidate "X".
                /view vote: Shows who you voted for.
                /view address: Shows your address.
                """);
        super.displayHelp();
    }

    public static void main(String[] args) throws Exception {
        UserView uv = new UserView();
        uv.run();
    }

    private void testVote() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        OldUserOld a = new OldUserOld("Guille");
        OldUserOld b = new OldUserOld("Carlos");

        //a.vote(b.getAddress());
        Vote t1 = a.getVote();
        System.out.println(t1.displayVote());
    }
}
