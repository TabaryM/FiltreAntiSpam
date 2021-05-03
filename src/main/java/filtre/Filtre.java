package filtre;

import java.io.*;
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
    private final ClassLoader classLoader = getClass().getClassLoader();
    private final double epsilon = 1.0;
    private final String erreur = "\033[31m *** ERREUR *** \033[0m";

    private int nbErreurSpam = 0;
    private int nbErreurHam = 0;

    public Filtre(){
        chargerDictionnaire(loadRessource("dictionnaire1000en.txt"));
    }

    public void chargerDictionnaire(File file){
        dictionnaire.clear();
        System.out.println("Chargement du dictionnaire...");
        BufferedReader reader = null ;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e){
            e.printStackTrace();
            System.exit(-1);
        }
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

    public void lireMessage(File file){
        X = new int[dictionnaire.size()];
        BufferedReader reader = null ;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e){
            e.printStackTrace();
            System.exit(-1);
        }

        String[] mots;
        String ligne;

        try{
            while((ligne = reader.readLine()) != null){
                mots = ligne.split(" ");
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

    public void apprentissage(int mSpam, int mHam){
        probasSpam = new double[dictionnaire.size()];
        probasHam = new double[dictionnaire.size()];
        List<Integer> numMail = new ArrayList<>(mSpam+mHam);
        for(int i = 0; i<mHam+mSpam; i++){
            numMail.add(i);
        }
        Collections.shuffle(numMail);
        File file;

        //Estimation des paramètres de distribution
        //SPAM
        for(int j = 0; j<mSpam; j++) {
            file = loadRessource("baseapp/spam/" + numMail.get(j) + ".txt");
//                file = new File("baseapp/spam/"+j+".txt");
            lireMessage(file);
            for (int i = 0; i < dictionnaire.size(); i++) {
                if (X[i] == 1) {
                    probasSpam[i] = probasSpam[i] + 1;
                }
            }
        }

        //HAM
        for(int j = 0; j<mHam; j++){
            file = loadRessource("baseapp/ham/"+ numMail.get(j+mSpam) +".txt");
//                file = new File("baseapp/ham/"+j+".txt");
            lireMessage(file);
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

        System.out.printf("Proba a priori\tSpam : %.15f\tHam : %.15f\n", probaSpam, probaHam);
    }

    public void test(int mSpam, int mHam, String cheminTest){
        testType(mSpam, cheminTest, true);
        testType(mHam, cheminTest, false);
        System.out.printf("Erreur de test sur les %d SPAM : \t%.15f\n", mSpam, ((float) nbErreurSpam/mSpam));
        System.out.printf("Erreur de test sur les %d HAM : \t%.15f\n", mHam, ((float) nbErreurHam/mHam));
        System.out.printf("Erreur de test globale sur %d mails : \t%.15f\n", (mHam+mSpam), ((float) (nbErreurHam + nbErreurSpam)/(mHam + mSpam)));
    }

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

        File file;

        for(int i = 0; i<m; i++){
            probaPosterioriSpam = probaSpam;
            probaPosterioriHam = probaHam;

            file = loadRessource(cheminTest+type+numMail.get(i)+".txt");
            lireMessage(file);
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
            if(probaPosterioriSpam > probaPosterioriHam) {
//                System.out.print("Message " + i + " est prédit comme SPAM");
                if(!spam) {
                    nbErreurHam++;
//                    System.out.print("\t"+erreur);
                }
            }
            else {
//                System.out.print("Message " + i + " est prédit comme HAM");
                if(spam) {
                    nbErreurSpam++;
//                    System.out.print("\t"+erreur);
                }
            }
//            System.out.println();
        }
    }

    private File loadRessource(String URL){
        return new File(classLoader.getResource(URL).getFile());
    }
}
