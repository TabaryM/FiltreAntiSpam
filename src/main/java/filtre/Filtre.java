package filtre;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Filtre {
    private final List<String> dictionnaire = new ArrayList<>();
    private int[] X;
    private double[] probasSpam;
    private double[] probasHam;
    private double probaSpam;
    private double probaHam;
    private final ClassLoader classLoader = getClass().getClassLoader();

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
                    dictionnaire.add(line);
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
                    if(dictionnaire.contains(mot)){
                        X[dictionnaire.indexOf(mot)] = 1;
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
        File file;

        //Estimation des paramètres de distribution
        //SPAM
        for(int j = 0; j<mSpam; j++) {
            file = loadRessource("baseapp/spam/" + j + ".txt");
//                file = new File("baseapp/spam/"+j+".txt");
            lireMessage(file);
            for (int i = 0; i < dictionnaire.size(); i++) {
                if (X[i] == 1) {
                    probasSpam[i]++;
                }
            }
        }

        //HAM
        for(int j = 0; j<mHam; j++){
            file = loadRessource("baseapp/ham/"+j+".txt");
//                file = new File("baseapp/ham/"+j+".txt");
            lireMessage(file);
            for (int i = 0; i < dictionnaire.size(); i++) {
                if(X[i]==1){
                    probasHam[i]++;
                }
            }
        }

        for (int i = 0; i < dictionnaire.size(); i++) {
            probasSpam[i] = probasSpam[i] / mSpam;
            probasHam[i] = probasHam[i] / mHam;
        }

        //Estimation des probabilités a priori
        probaSpam = (double) mSpam/(mSpam+mHam);
        probaHam = 1-probaSpam;

        System.out.printf("Proba a priori\tSpam : %.15f\tHam : %.15f\n", probaSpam, probaHam);
    }

    public void test(int mSpam, int mHam, String cheminTest){
        testType(mSpam, cheminTest, true);
        testType(mHam, cheminTest, false);
    }

    private void testType(int m, String cheminTest, boolean spam){
        double probaPosterioriSpam;
        double probaPosterioriHam;
        String type;
        if(spam)
            type = "/spam/";
        else
            type = "/ham/";

        File file;

        for(int i = 0; i<m; i++){
            probaPosterioriSpam = probaSpam;
            probaPosterioriHam = probaHam;

//          file = new File(cheminTest+type+j+".txt");
            file = loadRessource(cheminTest+type+i+".txt");
            lireMessage(file);
            //Calcul des probabilités a posteriori
            for(int j = 0; j<dictionnaire.size(); j++){
                if(X[j] == 1) {
                    probaPosterioriSpam *= probasSpam[j];
                    probaPosterioriHam *= probasHam[j];
                }
                else {
                    probaPosterioriSpam *= 1 - probasSpam[j];
                    probaPosterioriHam *= 1 - probasHam[j];
                }
            }

            System.out.printf("\nProba a priori\tSpam : %.15f\tHam : %.15f\n", probaSpam, probaHam);
//            probaPosterioriSpam *= probaSpam;
//            probaPosterioriHam *= probaHam;
            System.out.printf("Proba a posteriori\tSpam : %.15f\tHam : %.15f\n", probaPosterioriSpam, probaPosterioriHam);

            //Évaluation
            if(probaPosterioriSpam > probaPosterioriHam) {
                System.out.print("Message " + i + " est prédit comme SPAM");
                if(!spam)
                    System.out.print("\t*** ERREUR ***");
            }

            else {
                System.out.print("Message " + i + " est prédit comme HAM");
                if(spam)
                    System.out.print("\t*** ERREUR ***");
            }
            System.out.println();
        }
    }

    private File loadRessource(String URL){
        return new File(classLoader.getResource(URL).getFile());
    }
}
