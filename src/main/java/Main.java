import filtre.Filtre;

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        //Evaluation
        if(args.length == 1){
            Filtre filtre = new Filtre();
            filtre.evaluation(args[0]);
        }

        //Lancement du programme
        else if(args.length == 3) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Combien de SPAM dans la base d'apprentissage ? ");
            int mSpam = scanner.nextInt();
            System.out.print("Combien de HAM dans la base d'apprentissage ? ");
            int mHam = scanner.nextInt();

            Filtre filtre = new Filtre();
            System.out.println("Apprentissage...");
            filtre.apprentissage(mSpam, mHam);

            System.out.println("Test :");
            filtre.test(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0]);
        }

        //Nombre d'arguments incorrect
        else{
            System.err.println("Veuillez entrer les arguments suivants : \n- chemin de la base de test \n- nombre de spams dans la base d'apprentissage \n- nombre de hams dans la base d'apprentissage");
            System.exit(-1);
        }
    }
}
