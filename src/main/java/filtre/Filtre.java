package filtre;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Filtre {
    private final List<String> dictionnaire = new ArrayList<>();
    private int[] message;

    public void chargerDictionnaire(File file){
        dictionnaire.clear();
        BufferedReader reader = null ;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e){
            e.printStackTrace();
            System.exit(-1);
        }
        String line;
        try{
            do {
                line = reader.readLine();
                dictionnaire.add(line);
            } while(line != null);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void lireMessage(File file){
        message = new int[1000];
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
                        message[dictionnaire.indexOf(mot)] = 1;
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
