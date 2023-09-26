import Utils.HashUtils;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Client a = new Client("Guille");
        Client b = new Client("B");

        Transaction t1 = a.Send(15, b.getAddress());

        System.out.println("\nTransaction : " + t1.getTransaction());
        System.out.println("\nSignature : " + t1.getSignature());
        System.out.println("\nPublic Key : " + t1.getKey());

        System.out.println("\nVerified : " + Transaction.Verify(t1.getTransaction(), t1.getSignature(), t1.getKey()));
    }
}