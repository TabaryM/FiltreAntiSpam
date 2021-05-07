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
        else if (args.length == 3 && args[0].equals("-eval")){
            String pathToClassifieur = args[1];
            String pathToBaseEval = args[2];
            Filtre filtre = new Filtre(pathToClassifieur);
            filtre.verbose = false;
            filtre.validation(pathToBaseEval);
        }

        // Bonus 1 Création classifieur
        else if(args.length == 4) {
            Filtre filtre = new Filtre();
            filtre.verbose = true;
            System.out.println("Apprentissage...");
            filtre.saveApprentissage(Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[0], args[1]);
        }

        // Bonus 1 : utilisation du classifieur sur un mail
        else if (args.length == 2){
            String pathToClassifieur = args[0];
            String mail = args[1];
            Filtre filtre = new Filtre(pathToClassifieur);
            filtre.verbose = true;
            filtre.testUnique(mail);
        }

        // Lancement du programme
        else if(args.length == 3) {
            // Si les deux dernires paramètres sont des entiers, c'est qu'on l'analyse d'une base de mail
            if(args[1].matches("[0-9]*") && args[2].matches("[0-9]*")){
                Scanner scanner = new Scanner(System.in);
                System.out.print("Combien de SPAM dans la base d'apprentissage ? ");
                int mSpam = scanner.nextInt();
                System.out.print("Combien de HAM dans la base d'apprentissage ? ");
                int mHam = scanner.nextInt();

                Filtre filtre = new Filtre();
                filtre.verbose = true;
                System.out.println("Apprentissage...");
                filtre.apprentissage(mSpam, mHam, "baseapp");

                System.out.println("Test :");
                filtre.test(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0]);
            } else {
                //Sinon on ajoute un mail à la base d'apprentissage
                new Filtre(args[0], args[1], args[2]);
            }
        }

        //Nombre d'arguments incorrect
        else{
            // TODO : explication des trois possibilités de lancement
            System.err.println("Veuillez entrer les arguments suivants : \n- chemin de la base de test \n- nombre de spams dans la base d'apprentissage \n- nombre de hams dans la base d'apprentissage");
            System.exit(-1);
        }
    }
}
