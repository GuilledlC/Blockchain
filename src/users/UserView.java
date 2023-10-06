package users;

import utils.View;

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
            networkUser = new User(text);
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

        User user = ((User)networkUser);
        Transaction t = user.getTransaction();
        switch (command) {
            case "/help" -> displayHelp();
            case "/vote" -> {
                try {
                    user.vote(args);
                    System.out.println("Vote recorded. Type /viewvote to view your vote.");
                    /**Check if vote has been received*/
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (SignatureException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
            case "/viewvote" -> {
                if (t != null) {
                    try {
                        System.out.println(Transaction.displayTransaction(t));
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    } catch (SignatureException e) {
                        throw new RuntimeException(e);
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                    System.out.println("You haven't voted yet!");
            }
            default -> super.processCommand(text);
        }
    }

    protected void displayHelp() {
        System.out.print("""
                /vote X: Votes for the candidate "X".
                /viewvote: Shows who you voted for.""");
        super.displayHelp();
    }

    public static void main(String[] args) throws Exception {
        UserView uv = new UserView();
        uv.run();
    }

    private void testTransaction() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        User a = new User("Guille");
        User b = new User("Carlos");

        a.vote(b.getAddress());
        Transaction t1 = a.getTransaction();
        System.out.println(Transaction.displayTransaction(t1));
    }
}
