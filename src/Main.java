import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Client c = new Client();

        Scanner scanner = new Scanner(System.in);
        String string;
        while(true) {
            string = scanner.nextLine();
            System.out.println(string + " : " + HashUtils.Hash(string));
        }
    }
}