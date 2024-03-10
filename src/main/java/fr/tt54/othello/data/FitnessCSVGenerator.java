package fr.tt54.othello.data;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FitnessCSVGenerator {

    public static final String geneticResultFolder = "D:\\Theo\\Programmation\\Othello\\genetic\\launch-6";
    public static final String csvFolder = "D:\\Theo\\Programmation\\Othello\\genetic";
    public static final String csvFileName = "launch-6";

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        File folder = new File(geneticResultFolder);

        double[] fitnessArray = new double[folder.listFiles().length];

        for(File file : folder.listFiles()){
            try (FileReader reader = new FileReader(file)) {
                Object obj = jsonParser.parse(reader);
                JSONObject object = (JSONObject) obj;

                JSONObject best = (JSONObject) object.get("best_individu");
                double fitness = (double) best.get("fitness");

                System.out.println(file.getName());
                fitnessArray[Integer.parseInt(file.getName().split("-")[1].split("\\.")[0])] = fitness;
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

            File result = new File(csvFolder, csvFileName + ".csv");
            try {
                FileWriter writer = new FileWriter(result);
                String str = "Generation;Fitness\n";
                for(int i = 0; i < fitnessArray.length; i++){
                    str += "" + i + ";" + String.valueOf(fitnessArray[i]).replace(".", ",") + "\n";
                }
                writer.write(str);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
