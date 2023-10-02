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
    }

    private void testTransaction() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        User a = new User("Guille");
        User b = new User("Carlos");

        a.vote(b.getAddress());
        Transaction t1 = a.getTransaction();
        Transaction.displayTransaction(t1);
    }
}