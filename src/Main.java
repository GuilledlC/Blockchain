import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String string;
        while(true) {
            string = scanner.nextLine();
            System.out.println(string + " : " + Utils.Hash(string));
        }
    }
}