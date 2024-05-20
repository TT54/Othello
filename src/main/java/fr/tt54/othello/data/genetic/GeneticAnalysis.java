package fr.tt54.othello.data.genetic;

import fr.tt54.othello.bots.AdvancedPatternEvalBot;
import fr.tt54.othello.bots.Bot;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GeneticAnalysis {

    public static GeneticAlgorithm.Individu getBestIndividuFromGeneticResults(String geneticResultFolder, Bot reference, int[] depths, int[] gamesPerDepth){
        JSONParser jsonParser = new JSONParser();
        File folder = new File(geneticResultFolder);

        GeneticAlgorithm.Individu[] botsArray = new GeneticAlgorithm.Individu[folder.listFiles().length];

        for(File file : folder.listFiles()){
            try (FileReader reader = new FileReader(file)) {
                Object obj = jsonParser.parse(reader);
                JSONObject object = (JSONObject) obj;

                JSONObject best = (JSONObject) object.get("best_individu");
                JSONObject botDatas = (JSONObject) best.get("bot_datas");
                double freedom = (double) botDatas.get("freedom");
                float[] patterns = getPatternsFromString((String) botDatas.get("pattern"));

                System.out.println(file.getName());
                botsArray[Integer.parseInt(file.getName().split("-")[1].split("\\.")[0])] = new GeneticAlgorithm.Individu(new AdvancedPatternEvalBot(true, depths[0], patterns, (float) freedom), 0);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }


        int i = 0;
        for(GeneticAlgorithm.Individu individu : botsArray){
            i++;
            System.out.println("evaluating new bot... " + i);
            Bot.BotEvaluationResult result = Bot.evaluateBotAsync(individu.bot.copy(), new Bot[] {reference.copy()}, depths, gamesPerDepth, 10, false);

            while(!result.isEvaluationFinished()){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            float fitness = 0;
            float[] depthResults = result.getDepthConfrontationResults()[0];
            for(float depthResult : depthResults){
                fitness += depthResult;
            }
            individu.fitness = fitness;
        }

        return GeneticAlgorithm.Individu.getTwoBests(botsArray)[0];
    }

    private static float[] getPatternsFromString(String patternsStr){
        List<Double> patternsList = Arrays.stream((patternsStr).substring(1, patternsStr.length() - 1).split(",")).map(Double::parseDouble).toList();
        float[] patterns = new float[patternsList.size()];
        for(int i = 0; i < patternsList.size(); i++){
            patterns[i] = patternsList.get(i).floatValue();
        }
        return patterns;
    }

}
