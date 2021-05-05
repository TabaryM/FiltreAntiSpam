import filtre.Filtre;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // Recherche meilleur apprentissage
        if(args.length == 1){
            String pathToFiltre = args[0];
            Filtre filtre = new Filtre();
            filtre.verbose = false;
            filtre.maxFiltre(pathToFiltre);
        }

        // Évaluation
        else if (args.length == 2){
            String pathToFiltre = args[0];
            String pathToBaseEval = args[1];
            Filtre filtre = new Filtre(pathToFiltre);
            filtre.verbose = false;
            filtre.validation(pathToBaseEval);
        }

        // Lancement du programme
        else if(args.length == 3) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Combien de SPAM dans la base d'apprentissage ? ");
            int mSpam = scanner.nextInt();
            System.out.print("Combien de HAM dans la base d'apprentissage ? ");
            int mHam = scanner.nextInt();

            Filtre filtre = new Filtre();
            filtre.verbose = true;
            System.out.println("Apprentissage...");
            filtre.apprentissage(mSpam, mHam);

            System.out.println("Test :");
            filtre.test(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0]);
        }

        //Nombre d'arguments incorrect
        else{
            // TODO : explication des trois possibilités de lancement
            System.err.println("Veuillez entrer les arguments suivants : \n- chemin de la base de test \n- nombre de spams dans la base d'apprentissage \n- nombre de hams dans la base d'apprentissage");
            System.exit(-1);
        }
    }
}
