package users;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class UserView implements Runnable {

    private User user;

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nWhat do you wish to be called?");
        String text = scanner.nextLine();

        try {
            user = new User(text);

            displayHelp();
            do {
                text = scanner.nextLine();
                processCommand(text);
            } while(!text.equals("/close"));

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);}
    }

    private void processCommand(String text) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        int endCMDIndex = text.indexOf(' ');
        if(endCMDIndex == -1)
            endCMDIndex = text.length();

        String command = text.substring(0, endCMDIndex).toLowerCase();
        Transaction t = user.getTransaction();;
        switch (command) {
            case "/help":
                displayHelp();
                break;
            case "/vote":
                if(t != null)
                    System.out.println("You have already voted!");
                else
                    user.vote(text.substring(endCMDIndex + 1));
                break;
            case "/viewvote":
                if(t != null)
                    System.out.println(Transaction.displayTransaction(t));
                else
                    System.out.println("You haven't voted yet!");
                break;
            case "/start":
                int port = Integer.parseInt(text.substring(text.indexOf(' ') + 1));
                user.startListener(port);
                break;
            case "/connect":
                if(user.isListening())
                    user.connectTo(text.substring(text.indexOf(' ') + 1));
                else
                    System.out.println("You are currently offline! Start listening with /start \"port\"");
                break;
            case "/close":
                System.out.println("Goodbye!");
                break;
            default:
                user.sendMessage(command);
                break;
        }
    }

    private void displayHelp() {
        System.out.println("""
                /help: Displays the help.
                /vote X: Votes for the candidate "X".
                /viewvote: Shows who you voted for.
                /start P: Starts listening on port "P".
                /connect X:Y: Connects to the port "Y" at the IP "X".
                /close: Closes the program.
                Anything else: sends everything to all the connected users.
                """);
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
