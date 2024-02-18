package fr.tt54.othello.data;

import fr.tt54.othello.bots.AdvancedPatternEvalBot;
import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.data.patterns.Pattern;
import fr.tt54.othello.game.OthelloGame;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {

    public static Random random = new Random();

    public static final AdvancedPatternEvalBot FIRST_ATTEMPT_BOT = new AdvancedPatternEvalBot(true, new float[] {5.585513f, 4.4577804f, 2.6828187f, 4.4562306f, 7.4148116f, 4.2731333f, 4.333627f, 1.5871282f, 8.166409f}, 0.05614471f);

    public static final float mutationProba = 0.15f;
    public static final float crossOverProba = 0.60f;

    public static final int EVALUATION_AMOUNT_OF_GAMES = 4;
    public static final int[] EVALUATION_DEPTH = new int[] {1, 2, 3};
    public static final int POPULATION = 150;


    private static Individu[] individus = new Individu[POPULATION];
    private static int generation;

    public static void launch(){
        for(int i = 0; i < POPULATION; i++){
            individus[i] = Individu.generateRandomly();
        }

        generation = 0;

        evaluatePopulation();
        selectNextGen();
        nextGeneration();
    }

    public static void evaluateBot(Bot bot, Bot[] adversaries, int[] evaluationDepths, int[] gamesAmountPerDepth){
        int[][] depthBotVictories = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties gagnées contre chaque bot à chaque profondeur
        int[][] depthBotDraw = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties nulles contre chaque bot à chaque profondeur
        int[][] depthBotLooses = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties perdues contre chaque bot à chaque profondeur
        float[][] depthConfrontationResults = new float[adversaries.length][evaluationDepths.length]; // Contient le "score" des confrontations contre chaque adversaire à chaque profondeur

        for (int i = 0; i < evaluationDepths.length; i++) {
            int depth = evaluationDepths[i];
            bot.depthSearch = depth;

            System.out.println("#### DEPTH " + depth + " ####");

            for(int k = 0; k < gamesAmountPerDepth[i]; k++) {
                System.out.println("#### Game " + (k+1) + "/" + gamesAmountPerDepth[i] +" ####");

                OthelloGame beginningPosition = new OthelloGame();

                for (int j = 1; j <= random.nextInt(11); j++) {
                    List<Integer> availableMoves = new ArrayList<>(beginningPosition.getAvailablePlacements());
                    beginningPosition.playMove(availableMoves.get(random.nextInt(availableMoves.size())));
                }

                for (int j = 0; j < adversaries.length; j++) {
                    adversaries[j].depthSearch = depth;
                    Bot.GameResults results = Individu.launchEvaluationGame(bot.copy(), adversaries[j].copy(), beginningPosition);

                    depthBotVictories[j][i] += results.getScore()[0];
                    depthBotDraw[j][i] += results.getScore()[1];
                    depthBotLooses[j][i] += results.getScore()[2];
                    depthConfrontationResults[j][i] += (results.getGlobalResult() + 1f) / 2;
                }
            }
        }

        System.out.println("");

        for(int i = 0; i < depthBotVictories.length; i++){
            System.out.println("Bot VS Adversaire " + i);
            System.out.println("Victoires : " + Arrays.toString(depthBotVictories[i]));
            System.out.println("Nulles : " + Arrays.toString(depthBotDraw[i]));
            System.out.println("Défaites : " + Arrays.toString(depthBotLooses[i]));
            System.out.println("Score total des confrontations : " + Arrays.toString(depthConfrontationResults[i]));
            System.out.println(" ------ ");
        }
    }



    static int currentDepth = 0;
    static int gamesPlayedAtCurrentDepth = 0;
    static int currentAdversary = 0;

    public static void evaluateBotAsync(Bot bot, Bot[] adversaries, int[] evaluationDepths, int[] gamesAmountPerDepth, int threadAmount){
        int[][] depthBotVictories = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties gagnées contre chaque bot à chaque profondeur
        int[][] depthBotDraw = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties nulles contre chaque bot à chaque profondeur
        int[][] depthBotLooses = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties perdues contre chaque bot à chaque profondeur
        float[][] depthConfrontationResults = new float[adversaries.length][evaluationDepths.length]; // Contient le "score" des confrontations contre chaque adversaire à chaque profondeur

        for(int i = 0; i < threadAmount; i++){
            Thread thread = new Thread(){

                private int threadDepth = 0;
                private int threadAdversary = 0;

                @Override
                public void run() {
                    while(!this.isInterrupted()) {
                        gamesPlayedAtCurrentDepth++;

                        if (gamesPlayedAtCurrentDepth >= gamesAmountPerDepth[threadDepth]) {
                            gamesPlayedAtCurrentDepth = 0;
                            currentDepth++;

                            if(currentDepth < evaluationDepths.length) {
                                System.out.println("#### DEPTH " + evaluationDepths[currentDepth] + " ####");
                            }
                        }

                        if (currentDepth >= evaluationDepths.length) {
                            currentAdversary++;
                            currentDepth = 0;
                        }

                        if (currentAdversary >= adversaries.length) {
                            System.out.println("");

                            for (int i = 0; i < depthBotVictories.length; i++) {
                                System.out.println("Bot VS Adversaire " + i);
                                System.out.println("Victoires : " + Arrays.toString(depthBotVictories[i]));
                                System.out.println("Nulles : " + Arrays.toString(depthBotDraw[i]));
                                System.out.println("Défaites : " + Arrays.toString(depthBotLooses[i]));
                                System.out.println("Score total des confrontations : " + Arrays.toString(depthConfrontationResults[i]));
                                System.out.println(" ------ ");
                            }

                            interrupt();
                            return;
                        }

                        this.threadAdversary = currentAdversary;
                        this.threadDepth = currentDepth;

                        int depth = evaluationDepths[threadDepth];

                        OthelloGame beginningPosition = new OthelloGame();
                        for (int j = 1; j <= random.nextInt(11); j++) {
                            List<Integer> availableMoves = new ArrayList<>(beginningPosition.getAvailablePlacements());
                            beginningPosition.playMove(availableMoves.get(random.nextInt(availableMoves.size())));
                        }

                        adversaries[threadAdversary].depthSearch = depth;
                        bot.depthSearch = depth;
                        Bot.GameResults results = Individu.launchEvaluationGame(bot.copy(), adversaries[threadAdversary].copy(), beginningPosition);

                        depthBotVictories[threadAdversary][threadDepth] += results.getScore()[0];
                        depthBotDraw[threadAdversary][threadDepth] += results.getScore()[1];
                        depthBotLooses[threadAdversary][threadDepth] += results.getScore()[2];
                        depthConfrontationResults[threadAdversary][threadDepth] += (results.getGlobalResult() + 1f) / 2;
                    }
                }
            };
            thread.start();
        }
    }

    private static void nextGeneration(){
        generation++;

        System.out.println("##### Generation " + generation + " #####");

        for(int i = 0; i < ((int) (crossOverProba * POPULATION)) / 2; i++){
            int parent1 = random.nextInt(POPULATION);
            int parent2 = random.nextInt(POPULATION);
            while (parent2 == parent1){
                parent2 = random.nextInt(POPULATION);
            }

            Individu[] children = Individu.generateCrossOver(individus[parent1], individus[parent2]);

            Individu[] competitors = new Individu[] {individus[parent1], individus[parent2], children[0], children[1]};

            for(int j = 0; j < competitors.length; j++){
                Individu.evalFitness(competitors[j]);
            }

            Individu[] bests = Individu.getTwoBests(competitors);
            individus[parent1] = bests[0];
            individus[parent2] = bests[1];

            System.out.println("Crossover " + i + " terminé");
        }

        saveCurrentGeneration();

        selectNextGen();
        nextGeneration();
    }

    private static void saveCurrentGeneration() {
        JSONObject json = new JSONObject();

        JSONArray individusArray = new JSONArray();
        for(Individu individu : individus){
            individusArray.add(individu.saveIndividu());
        }

        Individu[] bests = Individu.getTwoBests(individus);

        json.put("individus", individusArray);
        json.put("best_individu", bests[0].saveIndividu());
        json.put("second_individu", bests[1].saveIndividu());

        try (FileWriter file = new FileWriter("generation-" + generation + ".json")) {
            file.write(json.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluatePopulation(){
        for(int i = 0; i < POPULATION; i++){
            Individu.evalFitness(individus[i]);
        }
    }

    private static void selectNextGen(){
        float totalFitness = 0;
        for(int i = 0; i < POPULATION; i++){
            totalFitness += individus[i].fitness;
        }

        Individu[] nextPop = new Individu[POPULATION];
        for(int i = 0; i < POPULATION; i++){
            float selected = random.nextFloat() * totalFitness;

            float score = 0;
            for(int j = 0; j < POPULATION; j++){
                score += individus[j].fitness;
                if(score >= selected){
                    nextPop[i] = individus[j];
                    break;
                }
            }
        }

        individus = nextPop;
    }


    public static class Individu {

        AdvancedPatternEvalBot bot;
        float fitness;

        public Individu(AdvancedPatternEvalBot bot, float fitness) {
            this.bot = bot;
            this.fitness = fitness;
        }

        public Individu copy(){
            return new Individu((AdvancedPatternEvalBot) this.bot.copy(), fitness);
        }

        public JSONObject saveIndividu(){
            JSONObject individu = new JSONObject();

            JSONObject botDatas = new JSONObject();
            botDatas.put("pattern", Arrays.toString(this.bot.patternCoeffs));
            botDatas.put("freedom", this.bot.freedomCoeff);

            individu.put("bot_datas", botDatas);
            individu.put("fitness", this.fitness);

            return individu;
        }

        public static Individu generateRandomly() {
            float[] patternsCoeffs = new float[Pattern.PatternType.values().length];
            for (int i = 0; i < patternsCoeffs.length; i++) {
                patternsCoeffs[i] = getRandomValue();
            }
            return new Individu(new AdvancedPatternEvalBot(true, patternsCoeffs, getRandomValue()), 0.5f);
        }

        public static Individu[] generateCrossOver(Individu individu1, Individu individu2) {
            float[] patternCoeffs1 = new float[individu1.bot.patternCoeffs.length];
            float[] patternCoeffs2 = new float[individu1.bot.patternCoeffs.length];

            for (int i = 0; i < patternCoeffs1.length; i++) {
                float alpha = random.nextFloat();
                patternCoeffs1[i] = alpha * individu1.bot.patternCoeffs[i] + (1 - alpha) * individu2.bot.patternCoeffs[i];
                patternCoeffs2[i] = alpha * individu2.bot.patternCoeffs[i] + (1 - alpha) * individu1.bot.patternCoeffs[i];
            }

            float alpha = random.nextFloat();
            float freedomCoeff1 = alpha * individu1.bot.freedomCoeff + (1 - alpha) * individu2.bot.freedomCoeff;
            float freedomCoeff2 = alpha * individu2.bot.freedomCoeff + (1 - alpha) * individu1.bot.freedomCoeff;

            Individu[] individus = new Individu[2];
            individus[0] = new Individu(new AdvancedPatternEvalBot(true, patternCoeffs1, freedomCoeff1), (individu1.fitness + individu2.fitness) / 2);
            individus[1] = new Individu(new AdvancedPatternEvalBot(true, patternCoeffs2, freedomCoeff2), (individu1.fitness + individu2.fitness) / 2);

            mutate(individus[0]);
            mutate(individus[1]);

            return individus;
        }


        private static void mutate(Individu individu) {
            int length = individu.bot.patternCoeffs.length;
            while (random.nextFloat() < mutationProba) {
                int i = random.nextInt(length + 1);
                if (i == length) {
                    individu.bot.freedomCoeff = getRandomValue();
                } else {
                    individu.bot.patternCoeffs[i] = getRandomValue();
                }
            }
        }

        public static void evalFitness(Individu individu) {
            float eval = 0;
            for (int i = 0; i < EVALUATION_AMOUNT_OF_GAMES; i++) {
                for (int depth : EVALUATION_DEPTH) {
                    individu.bot.depthSearch = depth;
                    OthelloGame beginingPosition = new OthelloGame();

                    for(int j = 1; j <= random.nextInt(11); j++){
                        List<Integer> availableMoves = new ArrayList<>(beginingPosition.getAvailablePlacements());
                        beginingPosition.playMove(availableMoves.get(random.nextInt(availableMoves.size())));
                    }

                    AdvancedPatternEvalBot adversaryBot = new AdvancedPatternEvalBot(false, depth);

                    Bot.GameResults results = launchEvaluationGame(individu.bot, adversaryBot, beginingPosition);
                    eval += (results.getGlobalResult() + 1) / 2f;
                }
            }
            individu.fitness = (individu.fitness + eval / (EVALUATION_AMOUNT_OF_GAMES * EVALUATION_DEPTH.length)) / 2;
        }

        private static float getRandomValue() {
            return random.nextFloat() * 10;
        }

        public static Individu[] getTwoBests(Individu... individus){
            int best = 0;
            int second = 1;
            if(individus.length <= 1){
                throw new IllegalArgumentException("Il faut au moins 2 enfants !");
            }

            for(int i = 1; i < individus.length; i++){
                if(individus[best].fitness < individus[i].fitness){
                    second = best;
                    best = i;
                } else if(individus[second].fitness < individus[i].fitness){
                    second = i;
                }
            }
            return new Individu[] {individus[best], individus[second]};
        }


        private static Bot.GameResults launchEvaluationGame(Bot bot1, Bot bot2, OthelloGame beginingPosition){
            int[] score = new int[3];
            int amount = 2;
            Bot.GameResult[] results = new Bot.GameResult[amount];

            boolean bot1Color = bot1.isWhite(); // true si bot1 est blanc, false s'il est noir

            for(int i = 0; i < amount; i++){
                OthelloGame game = beginingPosition.clone();

                bot1.setWhite(bot1Color);
                bot2.setWhite(!bot1Color);

                while(!game.isGameFinished()){
                    if(bot1Color == game.isWhiteToPlay()){
                        bot1.playMove(game, -1);
                    } else {
                        bot2.playMove(game, -1);
                    }
                }

                Bot.GameResult result = new Bot.GameResult(bot1.isWhite() ? game.getWhitePiecesCount() : game.getBlackPiecesCount(),
                        bot2.isWhite() ? game.getWhitePiecesCount() : game.getBlackPiecesCount());
                results[i] = result;

                bot1Color = !bot1Color;
            }

            return new Bot.GameResults(results);
        }
    }

}
