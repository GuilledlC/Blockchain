import users.*;
import utils.HashUtils;

public class Main {
    public static void main(String[] args) throws Exception {

        User a = new User("Guille");
        User b = new User("Carlos");

        Transaction t1 = a.vote(b.getAddress());

        System.out.println("\nTransaction : " + t1.getTransaction());
        System.out.println("\nSignature   : " + HashUtils.toHexString(t1.getSignature()));
        System.out.println("\nPublic Key  : " + t1.getKey());

        System.out.println("\nVerified : " + Transaction.verify(t1));
    }
}