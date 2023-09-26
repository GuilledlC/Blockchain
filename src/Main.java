public class Main {
    public static void main(String[] args) throws Exception {

        User a = new User("Guille");
        User b = new User("B");

        Transaction t1 = a.Send(15, b.getAddress());

        System.out.println("\nTransaction : " + t1.getTransaction());
        System.out.println("\nSignature : " + t1.getSignature());
        System.out.println("\nPublic Key : " + t1.getKey());

        System.out.println("\nVerified : " + Transaction.Verify(t1));
    }
}