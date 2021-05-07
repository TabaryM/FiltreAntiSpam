package filtre;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
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

    /**
     * Constructeur du filtre à partir d'un dictionnaire
     * Le dictionnaire chargé est fixe, il correspond au fichier dictionnaire1000en.txt
     */
    public Filtre(){
        chargerDictionnaire(loadRessource("dictionnaire1000en.txt"));
    }

    /**
     * Constructeur du filtre à partir d'un classifieur
     * @param filtreParam chemin vers le fichier correspondant au filtre à charger
     */
    public Filtre(String filtreParam) {
        chargeClassifieur(loadRessource(filtreParam));
    }

    /**
     * Constructeur de filtre à partir d'un classifieur et permettant d'ajouter un mail à la base d'apprentissage
     * @param pathToClassifieur chemin vers le fichier correspondant au filtre à charger
     * @param pathToMail chemin vers le fichier correspondant au mail à charger
     * @param spam indique si le mail fournit et un SPAM ou un HAM
     */
    // Bonus 2
    public Filtre(String pathToClassifieur, String pathToMail, String spam) {
        BufferedReader reader = loadRessource(pathToClassifieur);
        ArrayList<Integer> spamProbas = new ArrayList<>();
        ArrayList<Integer> hamProbas = new ArrayList<>();
        String line;
        int mSpam = 0, mHam = 0;
        try{
            String[] elems;
            line = reader.readLine();
            elems = line.split(";");
            assert(elems[0].equals("MOT") && elems[1].equals("Spam") && elems[2].equals("Ham")):"Mauvais format de fichier";

            line = reader.readLine();
            elems = line.split(";");
            mSpam = Integer.parseInt(elems[1]);
            mHam = Integer.parseInt(elems[2]);

            if (spam.equals("SPAM")) mSpam++;
            else if (spam.equals("HAM")) mHam++;
            else {
                System.err.println("Veuillez dire si le mail est un \"SPAM\" ou un \"HAM\"");
                System.exit(-1);
            }

            while((line = reader.readLine()) != null){
                elems = line.split(";");
                dictionnaire.add(elems[0]);
                spamProbas.add(Integer.parseInt(elems[1]));
                hamProbas.add(Integer.parseInt(elems[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        lireMessage(loadRessource(pathToMail));

        // On compte le nombre de spam qui contienne le mot X
        probasSpam = new double[spamProbas.size()];
        for (int i = 0; i < spamProbas.size(); i++){
            probasSpam[i] = spamProbas.get(i);
        }
        // On compte le nombre de ham qui contienne le mot X
        probasHam = new double[hamProbas.size()];
        for (int i = 0; i < hamProbas.size(); i++){
            probasHam[i] = hamProbas.get(i);
        }

        // Mise à jour des probas
        for (int i = 0; i < dictionnaire.size(); i++) {
            if (X[i] == 1) {
                if (spam.equals("SPAM")) probasSpam[i]++;
                else probasHam[i]++;
            }
        }

        saveApprentissage(mSpam, mHam, pathToClassifieur);
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

    /**
     * Récupère les informations d'un classifieur à partir d'un fichier donné
     * @param reader BufferedReader contenant toutes les informations du fichier classifieur
     */
    private void chargeClassifieur(BufferedReader reader) {
        System.out.println("Chargement des paramètres du filtre");
        ArrayList<Double> spamProbas = new ArrayList<>();
        ArrayList<Double> hamProbas = new ArrayList<>();
        String line;
        int mSpam = 0, mHam = 0;
        try{
            String[] elems;
            line = reader.readLine();
            elems = line.split(";");
            assert(elems[0].equals("MOT") && elems[1].equals("Spam") && elems[2].equals("Ham")):"Mauvais format de fichier";

            line = reader.readLine();
            elems = line.split(";");
            mSpam = Integer.parseInt(elems[1]);
            mHam = Integer.parseInt(elems[2]);

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
            probasSpam[i] = (spamProbas.get(i) + epsilon) / (mSpam + 2*epsilon);
        }
        probasHam = new double[hamProbas.size()];
        for (int i = 0; i < hamProbas.size(); i++){
            probasHam[i] = (hamProbas.get(i) + epsilon) / (mHam + 2*epsilon);
        }
        probaSpam = (double) mSpam/(mSpam+mHam);
        probaHam = 1-probaSpam;
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
    public void apprentissage(int mSpam, int mHam, String pathToBaseApp){
        probasSpam = new double[dictionnaire.size()];
        probasHam = new double[dictionnaire.size()];
        List<Integer> numMail = new ArrayList<>(mSpam+mHam);
        for(int i = 0; i< (mHam + mSpam) ; i++){
            numMail.add(i);
        }
//        Collections.shuffle(numMail);
        BufferedReader reader;
        //Estimation des paramètres de distribution
        //SPAM
        for(int j = 0; j < mSpam; j++) {
            reader = loadRessource(pathToBaseApp+"/spam/" + numMail.get(j) + ".txt");
            lireMessage(reader);
            for (int i = 0; i < dictionnaire.size(); i++) {
                if (X[i] == 1) {
                    probasSpam[i]++;
                }
            }
        }
        //HAM
        for(int j = 0; j < mHam; j++){
            reader = loadRessource(pathToBaseApp+"/ham/"+ numMail.get(j+mSpam) +".txt");
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
    }

    /**
     * Lance la classification d'un mail
     * @param pathToMail chemin vers le fichier correspondant au mail à étiqueter
     */
    public void testUnique(String pathToMail) {
        BufferedReader reader = loadRessource(pathToMail);
        lireMessage(reader);

        double probaPosterioriSpam = probaSpam;
        double probaPosterioriHam = probaHam;

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

        if(probaPosterioriSpam > probaPosterioriHam) {
            if (verbose) System.out.print("D'après le classifieur donné en paramètre, le message "+pathToMail+" est un SPAM !!\n");
        }
        else {
            if (verbose) System.out.print("D'après le classifieur donné en paramètre, le message "+pathToMail+" est un HAM\n");
        }
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
    private static BufferedReader loadRessource(String Url){
        BufferedReader reader = null;

        // Recherche l'Url dans les ressources du package
        URL path = Filtre.class.getClassLoader().getResource(Url);
        if (path != null) {
            // La ressource est trouvée, on retourne le bufferedReader demandé
            InputStream is = Filtre.class.getClassLoader().getResourceAsStream(Url);
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
     * Donne les paramètres mHam et mSpam fournissant la meilleur erreur globale d'apprentissage
     * @param saveApprentissage chemin vers le fichier où sera enregistrer le classfieur avec les meilleurs paramètres trouvés
     */
    public void maxFiltre(String saveApprentissage) {
        System.out.println("Recherche des meilleurs paramètres pour la base d'apprentissage...");
        int nbTestSpam, nbTestHam;
        nbTestHam = nbTestSpam = 5;
        int step = 50;
        double[] erreursTestGlobal = new double[nbTestHam * nbTestSpam];
        double[] erreursTestSpam = new double[nbTestHam * nbTestSpam];
        double[] erreursTestHam = new double[nbTestHam * nbTestSpam];
        int mHam;
        int mSpam;

        mHam = step;
        for (int i = 0; i < nbTestHam; i++) {
            mSpam = step;
            for (int j = 0; j < nbTestSpam; j++) {
                launch(mSpam, mHam, "basetest");
                erreursTestSpam[i*nbTestHam + j] = errTestSpam;
                erreursTestHam[i*nbTestHam + j] = errTestHam;
                erreursTestGlobal[i*nbTestHam + j] = errTestGlobale;
                System.out.printf("mSpam : %d\tmHam : %d\terrSpam : %.2f\terrHam : %.2f\terrTestGlobale : %.2f\n"
                        , mSpam, mHam, errTestSpam, errTestHam, errTestGlobale);
                mSpam += step;
            }
            mHam += step;
        }

        // On trouve les paramètres d'apprentissage qui ont le meilleur résultat sur la base de test
        int indiceMinSpam = -1, indiceMinHam = -1;
        double minErr = Double.MAX_VALUE;
        for (int i = 0; i < nbTestHam; i++) {
            for (int j = 0; j < nbTestSpam; j++) {
//                System.out.printf("i=%d\tj=%d\t(i*nbTestHam + j) = %d\terreursTestGlobal[i*nbTestHam + j] = %f\n",i , j, (i*nbTestHam + j), erreursTestGlobal[i*nbTestHam + j]);
                if(erreursTestGlobal[i*nbTestHam + j] < minErr){
                    indiceMinHam = i;
                    indiceMinSpam = j;
                    minErr = erreursTestGlobal[i*nbTestHam + j];
                }
            }
        }
        int indiceMin = indiceMinHam*nbTestHam + indiceMinSpam;

        mSpam = (indiceMinSpam + 1) * step;
        mHam = (indiceMinHam + 1) * step;
        System.out.printf("Meilleur configuration d'apprentissage trouvé :\tmSpam=%d\tmHam=%d\n\tErreur Spam : %.2f\tErreur Ham : %.2f\tErreur Globale : %.2f\n"
                , mSpam, mHam, erreursTestSpam[indiceMin], erreursTestHam[indiceMin], erreursTestGlobal[indiceMin]);

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
        apprentissage(mSpam, mHam, "baseapp");
        test(mSpam, mHam, cheminTest);
    }

    /**
     * Évalution k-fold du classfieur
     * @param pathToBaseEvaluation chemin vers le répertoire correspondant à la base d'évaluation
     */
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

    /**
     * Lance la classification pour un type de mail donné (ham/spam)
     * @param debut indice de mail où commencer le test
     * @param fin indice de mail où terminer le test
     * @param cheminTest chemin vers le répertoire correspondant à la base de test
     * @param spam true si l'on classifie des spams
     */
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

    // Bonus 2
    /**
     * Enregistre les données du classifieur dans un fichier
     * @param nbSpam nombre de mails spam pris en compte dans la base d'apprentissage
     * @param nbHam nombre de mails ham pris en compte dans la base d'apprentissage
     * @param out nom du fichier où enregistrer ce classifieur
     */
    public void saveApprentissage(int nbSpam, int nbHam, String out){
        File file = new File(out);
        System.out.println("Enregistrement du filtre avec les paramètres mSpam="+nbSpam+" mHam="+nbHam+" dans "+file.getAbsolutePath());

        try {
            FileWriter writer = new FileWriter(file);
            writer.write("MOT;Spam;Ham");
            writer.write("\nnb mail;"+nbSpam+';'+nbHam);
            for (int i = 0; i < dictionnaire.size(); i++){
                writer.write("\n"+dictionnaire.get(i));
                writer.write(";"+ (int)probasSpam[i]);
                writer.write(";"+ (int)probasHam[i]);
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Bonus 1

    /**
     * Lance un apprentissage puis enregistre les données du classifieur dans un fichier
     * @param nbSpam nombre de mails spam pris en compte dans la base d'apprentissage
     * @param nbHam nombre de mails ham pris en compte dans la base d'apprentissage
     * @param out nom du fichier où enregistrer ce classifieur
     * @param pathToBaseApp chemin vers le répertoire correspondant à la base d'apprentissage
     */
    public void saveApprentissage(int nbSpam, int nbHam, String out, String pathToBaseApp){
        File file = new File(out);
        apprentissage(nbSpam, nbHam, pathToBaseApp);
        System.out.println("Enregistrement du filtre avec les paramètres mSpam="+nbSpam+" mHam="+nbHam+" dans "+file.getAbsolutePath());

        try {
            FileWriter writer = new FileWriter(file);
            writer.write("MOT;Spam;Ham");
            writer.write("\nnb mail;"+nbSpam+';'+nbHam);
            for (int i = 0; i < dictionnaire.size(); i++){
                writer.write("\n"+dictionnaire.get(i));
                writer.write(";"+ (int)(probasSpam[i] * (nbSpam + 2*epsilon) - epsilon));
                writer.write(";"+ (int)( probasHam[i] * (nbSpam + 2*epsilon) - epsilon));
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
