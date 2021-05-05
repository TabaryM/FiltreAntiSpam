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

    double errTestSpam;
    double errTestHam;
    double errTestGlobale;

    private int nbErreurSpam = 0;
    private int nbErreurHam = 0;

    public boolean verbose;

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
        System.out.println("Dictionnaire chargé !");
    }

    public Filtre(String filtreParam) {
        chargeParam(loadRessource(filtreParam));
    }

    private void chargeParam(BufferedReader reader) {
        System.out.println("Chargement des paramètres du filtre");
        ArrayList<Double> spamProbas = new ArrayList<>();
        ArrayList<Double> hamProbas = new ArrayList<>();
        String line;
        try{
            String[] elems;
            line = reader.readLine();
            elems = line.split(";");
            assert(elems[0].equals("MOT") && elems[1].equals("ProbaSpam") && elems[2].equals("ProbaHam")):"Mauvais format de fichier";

            line = reader.readLine();
            elems = line.split(";");
            probaSpam = Double.parseDouble(elems[1]);
            probaHam = Double.parseDouble(elems[2]);

            while((line = reader.readLine()) != null){
                elems = line.split(";");
                dictionnaire.add(elems[0]);
                spamProbas.add(Double.parseDouble(elems[1]));
                hamProbas.add(Double.parseDouble(elems[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        probasSpam = new double[spamProbas.size()];
        for (int i = 0; i < spamProbas.size(); i++){
            probasSpam[i] = spamProbas.get(i);
        }
        probasHam = new double[hamProbas.size()];
        for (int i = 0; i < hamProbas.size(); i++){
            probasHam[i] = hamProbas.get(i);
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
//        Collections.shuffle(numMail);
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
        if(verbose) {
            System.out.printf("Erreur de test sur les %d SPAM : \t%.2f %s \n", mSpam, errTestSpam, '%');
            System.out.printf("Erreur de test sur les %d HAM : \t%.2f %s \n", mHam, errTestHam, '%');
            System.out.printf("Erreur de test globale sur %d mails : \t%.2f %s \n", (mHam + mSpam), errTestGlobale, '%');
        }
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

//        Collections.shuffle(numMail);

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
                if (verbose) System.out.printf("SPAM numéro %d : P(Y=SPAM | X=x) = %e, P(Y=HAM |X=x) = %e", i, probaPosterioriSpam, probaPosterioriHam);
                if(probaPosterioriSpam > probaPosterioriHam) {
                    if (verbose) System.out.print(" => identifié comme un SPAM\n");
                }
                else {
                    if (verbose) System.out.print(" => identifié comme un HAM \033[31m*** ERREUR***\033[0m\n");
                    nbErreurSpam++;
                }
            } else {
                if (verbose) System.out.printf("HAM numéro %d : P(Y=SPAM | X=x) = %e, P(Y=HAM |X=x) = %e", i, probaPosterioriSpam, probaPosterioriHam);
                if(probaPosterioriSpam > probaPosterioriHam) {
                    if (verbose) System.out.print(" => identifié comme un SPAM \033[31m*** ERREUR***\033[0m\n");
                    nbErreurHam++;
                }
                else {
                    if (verbose) System.out.print(" => identifié comme un HAM\n");
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
        URL path = getClass().getClassLoader().getResource(Url);
        if (path != null) {
            // La ressource est trouvée, on retourne le bufferedReader demandé
            InputStream is = getClass().getClassLoader().getResourceAsStream(Url);
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            return reader;
        }

        // La ressource n'est pas trouvée, on la cherche à la racine du programme
        String filePath = new File(Url).getAbsolutePath();
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            System.err.println("Le chemin vers la base de test n'est pas valide\n"+filePath);
            System.exit(-1);
        }
        return reader;
    }

    /**
     * Lance l'évaluation de la classification pour différentes nombre de mails dans les bases d'apprentissage et de test
     */
    public void maxFiltre(String saveApprentissage) {
        System.out.println("Recherche des meilleurs paramètres pour la base d'apprentissage...");
        int nbTestSpam, nbTestHam;
        nbTestHam = nbTestSpam = 5;
        int step = 50;
        double[] erreursTestGlobal = new double[nbTestHam * nbTestSpam];
        int mHam;
        int mSpam;

        mHam = step;
        for (int i = 0; i < nbTestHam; i++) {
            mSpam = step;
            for (int j = 0; j < nbTestSpam; j++) {
                launch(mSpam, mHam, "basetest");
                erreursTestGlobal[i*nbTestHam + j] = errTestGlobale;
//                System.out.printf("mSpam : %d\tmHam : %d\terrTestGlobale : %f\n", mSpam, mHam, errTestGlobale);
                mSpam += step;
            }
            mHam += step;
        }

        // On trouve les paramètres d'apprentissage qui ont le meilleur résultat sur la base de test
        int indiceMinSpam = -1, indiceMinHam = -1;
        double minErr = Double.MAX_VALUE;
        for (int i = 0; i < nbTestHam; i++) {
            for (int j = 0; j < nbTestSpam; j++) {
                System.out.printf("i=%d\tj=%d\t(i*nbTestHam + j) = %d\terreursTestGlobal[i*nbTestHam + j] = %f\n",i , j, (i*nbTestHam + j), erreursTestGlobal[i*nbTestHam + j]);
                if(erreursTestGlobal[i*nbTestHam + j] < minErr){
                    indiceMinHam = i;
                    indiceMinSpam = j;
                    minErr = erreursTestGlobal[i*nbTestHam + j];
                }
            }
        }

        mSpam = (indiceMinSpam + 1) * step;
        mHam = (indiceMinHam + 1) * step;
//        System.out.printf("Meilleur indice Spam=%d\tHam=%d\n", indiceMinSpam, indiceMinHam);
        System.out.printf("Meilleur indice : %d\tmSpam=%d\tmHam=%d\n",(indiceMinHam*nbTestHam + indiceMinSpam), mSpam, mHam);

        // On enregistre l'apprentissage avec ces paramètres
        saveApprentissage(mSpam, mHam, saveApprentissage);
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

    public void validation(String pathToBaseEvaluation){
        System.out.println("Début de l'évaluation par K-fold");
        File evalFile = new File("out/eval.tsv");
        int totalErrTestSpam = 0, totalErrTestHam = 0, totalErrTestGlobale = 0;
        File baseEvalHam = new File(pathToBaseEvaluation+"/ham");
        File baseEvalSpam = new File(pathToBaseEvaluation+"/spam");

        int maxSpam = baseEvalSpam.list().length;
        int maxHam = baseEvalHam.list().length;

        int k = 7;
        int debHam = 0;
        int debSpam = 0;
        int mHam = maxHam / k;
        int mSpam = maxSpam / k;

        for (int i = 0; i < k; i++) {
            System.out.printf("K = %d\n", i+1);

            nbErreurHam = 0;
            nbErreurSpam = 0;

            testType(debHam, mHam, pathToBaseEvaluation, false);
            testType(debSpam, mSpam, pathToBaseEvaluation, true);

            totalErrTestHam += nbErreurHam;
            totalErrTestSpam += nbErreurSpam;
            totalErrTestGlobale += nbErreurHam;
            totalErrTestGlobale += nbErreurSpam;

            debHam = mHam;
            debSpam = mSpam;
            mHam += maxHam/k;
            mSpam += maxSpam/k;
        }

        try {
            FileWriter writer = new FileWriter(evalFile);
            writer.write("ErreurSpam\tErreurHam\tErreurGlobale\n");
            writer.write(totalErrTestSpam/(float)mSpam + "\t" + totalErrTestHam/(float)mHam + "\t" + totalErrTestGlobale/(float)(mSpam+mHam) + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testType(int debut, int fin, String cheminTest, boolean spam){
        System.out.println("début : "+debut+" fin : "+fin+" spam : "+spam);
        double probaPosterioriSpam;
        double probaPosterioriHam;
        int nbmail = fin - debut;
        String type;
        if(spam) type = "/spam/";
        else type = "/ham/";
        List<Integer> numMail = new ArrayList<>(nbmail);
        for(int i = debut; i < fin; i++){
            numMail.add(i);
        }

        BufferedReader reader;

        for(int i = 0; i < nbmail; i++){
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
                if (verbose) System.out.printf("SPAM numéro %d : P(Y=SPAM | X=x) = %e, P(Y=HAM |X=x) = %e", i, probaPosterioriSpam, probaPosterioriHam);
                if(probaPosterioriSpam > probaPosterioriHam) {
                    if (verbose) System.out.print(" => identifié comme un SPAM\n");
                }
                else {
                    if (verbose) System.out.print(" => identifié comme un HAM \033[31m*** ERREUR***\033[0m\n");
                    nbErreurSpam++;
                }
            } else {
                if (verbose) System.out.printf("HAM numéro %d : P(Y=SPAM | X=x) = %e, P(Y=HAM |X=x) = %e", i, probaPosterioriSpam, probaPosterioriHam);
                if(probaPosterioriSpam > probaPosterioriHam) {
                    if (verbose) System.out.print(" => identifié comme un SPAM \033[31m*** ERREUR***\033[0m\n");
                    nbErreurHam++;
                }
                else {
                    if (verbose) System.out.print(" => identifié comme un HAM\n");
                }
            }
        }
    }

    private void saveApprentissage(int nbSpam, int nbHam, String out){
        File file = new File(out);
        apprentissage(nbSpam, nbHam);
        System.out.println("Enregistrement du filtre avec les paramètres mSpam="+nbSpam+" mHam="+nbHam+" dans "+file.getAbsolutePath());

        try {
            FileWriter writer = new FileWriter(file);
            writer.write("MOT;ProbaSpam;ProbaHam");
            writer.write("\na priori;"+probaSpam+';'+probaHam);
            for (int i = 0; i < dictionnaire.size(); i++){
                writer.write("\n"+dictionnaire.get(i));
                writer.write(";"+probasSpam[i]);
                writer.write(";"+probasHam[i]);
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
