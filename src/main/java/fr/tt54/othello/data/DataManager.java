package fr.tt54.othello.data;

import fr.tt54.othello.data.complexity.AlphaBetaComplexityAnalysis;
import fr.tt54.othello.data.openings.OpeningLoader;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.data.patterns.PatternLoader;
import fr.tt54.othello.game.OthelloGame;

import java.io.FileNotFoundException;

public class DataManager {

    public static OpeningTree mainOpeningTree;

    private static boolean enabled = false;

    public static void main(String[] args) {
        DataManager.enable();
        AlphaBetaComplexityAnalysis.launchAnalysis(
                new int[] {1, 2, 3, 4, 5, 6, 7, 8},
                150,
                "D:\\Theo\\Programmation\\Othello\\src\\main\\resources\\alphabeta_complexity_analysis",
                "test3"
        );
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

/*        MultiProbCutAnalysis.getEvaluationComparisonAsync(
                new int[] {1, 2, 3, 4, 5, 6, 7, 8},
                100,
                15,
                60,
                "D:\\Theo\\Programmation\\Othello\\mpc",
                "eval_" + System.currentTimeMillis(),
                10);*/

        //MultiProbCutAnalysis.mergeCSV("D:\\Theo\\Perso\\Prog\\Othello\\mpc", "eval_1716219430009", "eval_1716219430009_final", 15, 59);
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

    private static void generatePatternsValues() {
        try {
            PatternLoader.generatePatterns(OpeningLoader.HUMAN_GAME_FOLDER);
            PatternLoader.generatePatterns(OpeningLoader.LOGISTELLO_GAME_FOLDER);

            PatternLoader.savePatterns();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
