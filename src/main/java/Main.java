import filtre.Filtre;

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Combien de SPAM dans la base d'apprentissage ?");
        int mSpam = scanner.nextInt();
        System.out.println("Combien de HAM dans la base d'apprentissage ?");
        int mHam = scanner.nextInt();

        Filtre filtre = new Filtre();
        System.out.println("Apprentissage...");
        filtre.apprentissage(mSpam, mHam);

        System.out.println("Test :");
        filtre.test(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0]);

    }
}
