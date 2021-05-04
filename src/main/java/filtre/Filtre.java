package filtre;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Filtre {
    private final List<String> dictionnaire = new ArrayList<>();
    private int[] X;
    private double[] probasSpam;
    private double[] probasHam;
    private double probaSpam;
    private double probaHam;
    private final double epsilon = 1.0;

    private final ClassLoader classLoader = getClass().getClassLoader();
    double errTestSpam;
    double errTestHam;
    double errTestGlobale;
    private final String erreur = "\033[31m *** ERREUR *** \033[0m";

    private int nbErreurSpam = 0;
    private int nbErreurHam = 0;

    public Filtre(){
        chargerDictionnaire(loadRessource("dictionnaire1000en.txt"));
    }

    /**
     * Charge le dictionnaire donné en paramètre
     * @param reader fichier texte comprenant les mots du dictionnaire
     */
    public void chargerDictionnaire(BufferedReader reader){
        dictionnaire.clear();
        System.out.println("Chargement du dictionnaire...");
        String line;
        try{
            while((line = reader.readLine()) != null){
                if (line.length() >= 3){
                    dictionnaire.add(line.toUpperCase());
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Parcours un mail et retranscrit cette lecture par un vecteur binaire X
     * X[i]=1 --> le mot i apparaît dans ce mail
     * @param reader fichier texte correspondant au mail
     */
    public void lireMessage(BufferedReader reader){
        X = new int[dictionnaire.size()];

        String[] mots;
        String ligne;

        try{
            while((ligne = reader.readLine()) != null){
                mots = ligne.split("[^a-zA-Z]");
                for(String mot : mots){
                    if(dictionnaire.contains(mot.toUpperCase())){
                        X[dictionnaire.indexOf(mot.toUpperCase())] = 1;
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Lance l'apprentissage sur le nombre de mails donné
     * Le dossier de la base d'apprentissage est ressources/baseapp
     * @param mSpam nombre de mails spams dans la base d'apprentissage
     * @param mHam nombre de mails hams dans la base d'apprentissage
     */
    public void apprentissage(int mSpam, int mHam){
        probasSpam = new double[dictionnaire.size()];
        probasHam = new double[dictionnaire.size()];
        List<Integer> numMail = new ArrayList<>(mSpam+mHam);
        for(int i = 0; i<mHam+mSpam; i++){
            numMail.add(i);
        }
        Collections.shuffle(numMail);
        BufferedReader reader;

        //Estimation des paramètres de distribution
        //SPAM
        for(int j = 0; j<mSpam; j++) {
            reader = loadRessource("baseapp/spam/" + numMail.get(j) + ".txt");
            lireMessage(reader);
            for (int i = 0; i < dictionnaire.size(); i++) {
                if (X[i] == 1) {
                    probasSpam[i] = probasSpam[i] + 1;
                }
            }
        }

        //HAM
        for(int j = 0; j<mHam; j++){
            reader = loadRessource("baseapp/ham/"+ numMail.get(j+mSpam) +".txt");
            lireMessage(reader);
            for (int i = 0; i < dictionnaire.size(); i++) {
                if(X[i]==1){
                    probasHam[i]++;
                }
            }
        }

        for (int i = 0; i < dictionnaire.size(); i++) {
            probasSpam[i] = (probasSpam[i] + epsilon) / (mSpam + 2*epsilon);
            probasHam[i] = (probasHam[i] + epsilon) / (mHam + 2*epsilon);
        }

        //Estimation des probabilités a priori
        probaSpam = (double) mSpam/(mSpam+mHam);
        probaHam = 1-probaSpam;

//        System.out.printf("Proba a priori\tSpam : %.15f\tHam : %.15f\n", probaSpam, probaHam);
    }

    /**
     * Lance la classification pour les spams et les hams sur un nombre de mail donné
     * Affiche les erreurs de test
     * @param mSpam nombre de mails spams dans la base de test
     * @param mHam nombre de mails hams dans la base de test
     * @param cheminTest chemin vers le dossier de la base de test
     */
    public void test(int mSpam, int mHam, String cheminTest){
        nbErreurHam = 0;
        nbErreurSpam = 0;
        testType(mSpam, cheminTest, true);
        testType(mHam, cheminTest, false);
        errTestSpam = ((float) nbErreurSpam/mSpam)*100;
        errTestHam = ((float) nbErreurHam/mHam)*100;
        errTestGlobale = ((float) (nbErreurHam + nbErreurSpam)/(mHam + mSpam))*100;
        System.out.printf("Erreur de test sur les %d SPAM : \t%.2f %s \n", mSpam, errTestSpam, '%');
        System.out.printf("Erreur de test sur les %d HAM : \t%.2f %s \n", mHam, errTestHam, '%');
        System.out.printf("Erreur de test globale sur %d mails : \t%.2f %s \n", (mHam+mSpam), errTestGlobale, '%');
    }

    /**
     * Lance la classification pour un type de mail donné (ham/spam)
     * @param m nombre de mails de cette catégorie dans la base de test
     * @param cheminTest chemin vers le dossier de la base de test
     * @param spam true si l'on classifie des spams
     */
    private void testType(int m, String cheminTest, boolean spam){
        double probaPosterioriSpam;
        double probaPosterioriHam;
        String type;
        if(spam) type = "/spam/";
        else type = "/ham/";
        List<Integer> numMail = new ArrayList<>(m);
        for(int i = 0; i<m; i++){
            numMail.add(i);
        }

        Collections.shuffle(numMail);

        BufferedReader reader;

        for(int i = 0; i<m; i++){
            probaPosterioriSpam = probaSpam;
            probaPosterioriHam = probaHam;

            reader = loadRessource(cheminTest+type+numMail.get(i)+".txt");
            lireMessage(reader);
            //Calcul des probabilités a posteriori
            for(int j = 0; j<dictionnaire.size(); j++){
                if(X[j] == 1) {
                    probaPosterioriSpam *= probasSpam[j];
                    probaPosterioriHam *= probasHam[j];
                }
                else {
                    probaPosterioriSpam *= (1 - probasSpam[j]);
                    probaPosterioriHam *= (1 - probasHam[j]);
                }
            }

            //Évaluation
            if (spam) {
                System.out.printf("SPAM numéro %d : P(Y=SPAM | X=x) = %e, P(Y=HAM |X=x) = %e", i, probaPosterioriSpam, probaPosterioriHam);
                if(probaPosterioriSpam > probaPosterioriHam) {
                    System.out.print(" => identifié comme un SPAM\n");
                }
                else {
                    System.out.print(" => identifié comme un HAM \033[31m*** ERREUR***\033[0m\n");
                    nbErreurSpam++;
                }
            } else {
                System.out.printf("HAM numéro %d : P(Y=SPAM | X=x) = %e, P(Y=HAM |X=x) = %e", i, probaPosterioriSpam, probaPosterioriHam);
                if(probaPosterioriSpam > probaPosterioriHam) {
                    System.out.print(" => identifié comme un SPAM \033[31m*** ERREUR***\033[0m\n");
                    nbErreurHam++;
                }
                else {
                    System.out.print(" => identifié comme un HAM\n");
                }
            }
        }
    }

    /**
     * Charge le fichier correspondant au chemin donné
     * @param Url chemin vers le fichier désiré
     * @return le File correspondant au fichier
     */
    private BufferedReader loadRessource(String Url){
        BufferedReader reader = null;

        // Recherche l'Url dans les ressources du package
        URL path = classLoader.getResource(Url);
        if (path != null) {
            // La ressource est trouvée, on retourne le bufferedReader demandé
            InputStream is = classLoader.getResourceAsStream(Url);
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            return reader;
        }

        // La ressource n'est pas trouvée, on la cherche à la racine du programme
        try {
            reader = new BufferedReader(new FileReader(new File(Url).getAbsolutePath()));
        } catch (FileNotFoundException e) {
            System.err.println("Le chemin vers la base de test n'est pas valide");
            System.exit(-1);
        }
        return reader;
    }

    /**
     * Lance l'évaluation de la classification pour différentes nombre de mails dans les bases d'apprentissage et de test
     * @param cheminTest chemin vers le dossier de la base de test
     */
    public void evaluation(String cheminTest) {
        File evalRep = new File("evaluation");
        if(!evalRep.exists())
            evalRep.mkdir();
        File evalFile = new File("evaluation/eval.txt");
        try {
            FileWriter writer = new FileWriter(evalFile);
            writer.write("mSpam\tmHam\tm\tErreurSpam\tErreurHam\tErreurGlobale\n");
            int mHam = 50;
            while(mHam<=250) {
                int mSpam = 0;
                for (int i = 0; i < 5; i++) {
                    mSpam += 50;
                    launch(mSpam, mHam, cheminTest);
                    writer.write(mSpam + "\t" + mHam + "\t" + (mSpam + mHam) + "\t" + errTestSpam + "\t" + errTestHam + "\t" + errTestGlobale + "\n");
                }
                mHam +=50;
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lance l'apprentissage suivie de la classification
     * @param mSpam nombre de spams dans les bases d'apprentissage et de test
     * @param mHam nombre de hams dans les bases d'apprentissage et de test
     * @param cheminTest chemin vers le dossier de la base de test
     */
    private void launch(int mSpam, int mHam, String cheminTest){
        apprentissage(mSpam, mHam);
        test(mSpam, mHam, cheminTest);
    }
}
