package fr.tt54.othello.data;

import fr.tt54.othello.data.complexity.AlphaBetaComplexityLoader;
import fr.tt54.othello.data.openings.OpeningLoader;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.data.patterns.PatternLoader;
import fr.tt54.othello.game.OthelloGame;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DataManager {

    public static OpeningTree mainOpeningTree;

    private static boolean enabled = false;

    public static void main(String[] args) {
        DataManager.enable();
        //analyseComplexityDatas();

        //generatePatternsValues();

        //GeneticAlgorithm.asynchLaunch(8);

        /*GeneticAlgorithm.Individu individu = GeneticAnalysis.getBestIndividuFromGeneticResults(
                "D:\\Theo\\Programmation\\Othello\\genetic\\launch-6",
                GeneticAlgorithm.FIRST_ATTEMPT_BOT,
                new int[] {1, 2, 3, 4},
                new int[] {10, 10, 10, 10});
        System.out.println(individu.saveIndividu().toJSONString());*/

        /*Bot.evaluateBotAsync(GeneticAlgorithm.SECOND_ATTEMPT_BOT,
                new Bot[] {GeneticAlgorithm.FIRST_ATTEMPT_BOT},
                new int[] {1, 2, 3, 4, 5},
                new int[] {500, 500, 500, 500, 500},
                10, true);*/

/*        Bot.evaluateBotAsync(GeneticAlgorithm.SECOND_ATTEMPT_BOT,
                new Bot[] {GeneticAlgorithm.FIRST_ATTEMPT_BOT},
                new int[] {6, 7},
                new int[] {400, 400},
                10, true);*/

/*        MultiProbCutAnalysis.formatDatas(
                new int[] {1, 2, 3, 4, 5, 6, 7, 8},
                100,
                15,
                16,
                "D:\\Theo\\Programmation\\Othello\\mpc",
                "eval_1_1716200314749",
                "format_2"
                );*/

        MultiProbCutAnalysis.getEvaluationComparisonAsync(
                new int[] {1, 2, 3, 4, 5, 6, 7, 8},
                100,
                15,
                60,
                "D:\\Theo\\Programmation\\Othello\\mpc",
                "eval_" + System.currentTimeMillis(),
                10);
    }


    public static void enable(){
        if(enabled) return;
        enabled = true;

        mainOpeningTree = new OpeningTree((byte) OthelloGame.positionToInt(4, 5));
        try {
            OpeningLoader.loadGames(OpeningLoader.HUMAN_GAME_FOLDER);
            OpeningLoader.loadGames(OpeningLoader.LOGISTELLO_GAME_FOLDER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        PatternLoader.loadPatterns(PatternLoader.PATTERNS_REWORK);
        System.out.println("Data Manager Enabled");
    }

    public static void analyseComplexityDatas(){
        try {
            List<long[]> datas = AlphaBetaComplexityLoader.loadDatas(AlphaBetaComplexityLoader.DATA_FOLDER);

            int[] readValues = new int[datas.get(0).length];
            double[] moyennes = new double[readValues.length];

            for(long[] data : datas){
                for(int i = 0; i < data.length; i++){
                    if(i == 0) {
                        moyennes[i] += data[i];
                    } else {
                        // On retire toutes les valeurs pouvant fausser nos résultats
                        // L'idée est donc de retirer notamment la fin de partie où une recherche à 9 de profondeur est la même qu'à 8 (ou moins) de profondeur
                        if(data[i] / (double) data[i - 1] < 4){
                            continue;
                        }

                        moyennes[i] += data[i] / (double) data[i - 1];
                        readValues[i]++;
                    }
                }
            }

            for(int i = 0; i < moyennes.length; i++){
                moyennes[i] /= readValues[i];
            }

            System.out.println(Arrays.toString(moyennes));
            System.out.println(Arrays.toString(readValues));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generatePatternsValues(){
        try {
            PatternLoader.generatePatterns(OpeningLoader.HUMAN_GAME_FOLDER);
            PatternLoader.generatePatterns(OpeningLoader.LOGISTELLO_GAME_FOLDER);

            PatternLoader.savePatterns();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
