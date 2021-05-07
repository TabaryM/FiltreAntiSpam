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

        else if(args.length == 3) {
            // Si les deux dernires paramètres sont des entiers, c'est qu'on fait l'analyse d'une base de mail
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
                // Sinon on ajoute un mail à la base d'apprentissage
                new Filtre(args[0], args[1], args[2]);
            }
        }

        //Nombre d'arguments incorrect
        else{
            // TODO : explication des trois possibilités de lancement
            System.err.println("Veuillez entrer des arguments en suivant un des schémas suivant : \n");
            System.err.println("Pour le test d'une base de test :" +
                    "\n\tchemin vers la base de test" +
                    "\n\tnombre de spam a tester" +
                    "\n\tnombre de ham a tester\n");
            System.err.println("Pour enregistrer un classifieur :" +
                    "\n\tchemin vers le fichier classifieur enregistré" +
                    "\n\tchemin vers la base d'apprentissage" +
                    "\n\tnombre de spam à utiliser dans la base d'apprentissage" +
                    "\n\tnmobre de ham à utiliser dans la base d'apprentissage\n");
            System.err.println("Pour ajouter un mail à la base d'apprentissage :" +
                    "\n\tchemin vers le classifieur existant" +
                    "\n\tchemin vers le mail a ajouter" +
                    "\n\ttype de mail (\"SPAM\" ou \"HAM\")\n");
            System.err.println("Pour la création du meilleur classifieur avec la base d'apprentissage interne: " +
                    "\n\tchemin vers le fichier classifieur enregistré\n");
            System.err.println("Pour l'évaluation k-fold d'un classifieur existant :" +
                    "\n\t-eval (pour indiquer que l'on effectue une evaluation" +
                    "\n\tchemin vers le classifieur à évaluer" +
                    "\n\tbase d'évaluation du classifieur (de préférence, plus de 3000 mails, dont 1500 SPAM et 1500 HAM minimum)\n");

            System.exit(-1);
        }
    }
}
