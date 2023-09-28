import nodes.Node;
import users.*;
import utils.HashUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Node n = new Node("Guille");

        /*Scanner s = new Scanner(System.in);

        System.out.println("ID: ");
        String id = s.nextLine();
        System.out.println("Port to host out of: ");
        int port = s.nextInt();
        s.nextLine();
        Node node = new Node(id, port);

        node.startListener();

        System.out.println("IP to connect to: ");
        id = s.nextLine();
        System.out.println("Port to connect to: ");
        port = s.nextInt();
        s.nextLine();

        //System.out.println(id + port);
        node.connectTo(id, port);*/
    }

    private void testTransaction() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        User a = new User("Guille");
        User b = new User("Carlos");

        Transaction t1 = a.vote(b.getAddress());

        System.out.println("\nTransaction : " + t1.getTransaction());
        System.out.println("\nSignature   : " + HashUtils.toHexString(t1.getSignature()));
        System.out.println("\nPublic Key  : " + t1.getKey());

        System.out.println("\nVerified : " + Transaction.verify(t1));
    }
}