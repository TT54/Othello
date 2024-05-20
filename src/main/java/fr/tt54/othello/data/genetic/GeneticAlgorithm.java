package fr.tt54.othello.data.genetic;

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
    public static final AdvancedPatternEvalBot SECOND_ATTEMPT_BOT = new AdvancedPatternEvalBot(true, new float[] {5.249243f, 3.4480486f, 2.6761231f, 2.7054176f, 9.367574f, 4.3268347f, 3.9276142f, 2.708558f, 6.8990593f}, 0.08751938f);

/*    public static final float mutationProba = 0.25f;
    public static final float crossOverProba = 0.70f;

    public static final int EVALUATION_AMOUNT_OF_GAMES = 4;
    public static final int[] EVALUATION_DEPTH = new int[] {1, 2, 3};
    public static final int POPULATION = 200;*/


    private static Individu[] individus;
    private static int generation;
    private static GeneticParameters geneticParameters;

    public static void launch(GeneticParameters parameters){
        geneticParameters = parameters;
        individus = new Individu[parameters.population()];
        for(int i = 0; i < parameters.population(); i++){
            individus[i] = Individu.generateRandomly();
        }

        generation = 0;

        evaluatePopulation();
        selectNextGen();
        nextGeneration();
    }

    public static void asyncLaunch(GeneticParameters parameters, int threadAmount){
        geneticParameters = parameters;
        individus = new Individu[parameters.population()];
        for(int i = 0; i < parameters.population(); i++){
            individus[i] = Individu.generateRandomly();
        }

        generation = 0;

        evaluatePopulation();
        saveCurrentGeneration();

        selectNextGen();
        asyncNextGeneration(threadAmount);
    }



    static int currentCrossOver = 0;
    static int finishedCrossOverAmount = 0;
    private static void asyncNextGeneration(int threadAmount){
        currentCrossOver = 0;
        finishedCrossOverAmount = 0;
        generation++;

        System.out.println("##### Generation " + generation + " #####");

        for(int i = 0; i < threadAmount; i++){
            Thread thread = new Thread(){
                @Override
                public void run() {
                    while(!this.isInterrupted()){
                        if(currentCrossOver <= ((int) (geneticParameters.crossOverProba() * geneticParameters.population())) / 2){
                            int threadCrossOver = currentCrossOver;
                            currentCrossOver++;

                            int parent1 = random.nextInt(geneticParameters.population());
                            int parent2 = random.nextInt(geneticParameters.population());
                            while (parent2 == parent1){
                                parent2 = random.nextInt(geneticParameters.population());
                            }

                            Individu[] children = Individu.generateCrossOver(individus[parent1], individus[parent2]);

                            Individu[] competitors = new Individu[] {individus[parent1], individus[parent2], children[0], children[1]};

                            for(int j = 0; j < competitors.length; j++){
                                Individu.evalFitness(competitors[j]);
                            }

                            Individu[] bests = Individu.getTwoBests(competitors);
                            individus[parent1] = bests[0];
                            individus[parent2] = bests[1];

                            System.out.println("Crossover " + threadCrossOver + " terminé");

                            finishedCrossOverAmount++;
                        } else {
                            this.interrupt();
                            if(finishedCrossOverAmount == ((int) (geneticParameters.crossOverProba() * geneticParameters.population())) / 2){

                            } else if(finishedCrossOverAmount > ((int) (geneticParameters.crossOverProba() * geneticParameters.population())) / 2){
                                System.out.println("problème : " + finishedCrossOverAmount);
                            }
                        }
                    }
                }
            };
            thread.start();
        }

        while(finishedCrossOverAmount <= ((int) (geneticParameters.crossOverProba() * geneticParameters.population())) / 2){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        saveCurrentGeneration();
        selectNextGen();
        asyncNextGeneration(threadAmount);
    }

    private static void nextGeneration(){
        generation++;

        System.out.println("##### Generation " + generation + " #####");

        // On réalise les cross overs
        for(int i = 0; i < ((int) (geneticParameters.crossOverProba() * geneticParameters.population())) / 2; i++){

            // Sélection des parents
            int parent1 = random.nextInt(geneticParameters.population());
            int parent2 = random.nextInt(geneticParameters.population());
            while (parent2 == parent1){
                parent2 = random.nextInt(geneticParameters.population());
            }
            Individu[] children = Individu.generateCrossOver(individus[parent1], individus[parent2]);

            // On évalue les enfants et les parents
            Individu[] competitors = new Individu[] {individus[parent1], individus[parent2], children[0], children[1]};
            for(int j = 0; j < competitors.length; j++){
                Individu.evalFitness(competitors[j]);
            }

            // On récupère les deux meilleurs parmi les parents et les enfants
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

        System.out.println("Meilleure fitness : " + bests[0].fitness);

        try (FileWriter file = new FileWriter("generation-" + generation + ".json")) {
            file.write(json.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluatePopulation(){
        for(int i = 0; i < geneticParameters.population(); i++){
            Individu.evalFitness(individus[i]);
        }
    }

    private static void selectNextGen(){
        float totalFitness = 0;
        for(int i = 0; i < geneticParameters.population(); i++){
            totalFitness += individus[i].fitness;
        }

        Individu[] nextPop = new Individu[geneticParameters.population()];
        for(int i = 0; i < geneticParameters.population(); i++){
            float selected = random.nextFloat() * totalFitness;

            float score = 0;
            for(int j = 0; j < geneticParameters.population(); j++){
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
            return new Individu(new AdvancedPatternEvalBot(true, patternsCoeffs, getRandomValue()), 0f);
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
            while (random.nextFloat() < geneticParameters.mutationProba()) {
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
            for (int i = 0; i < geneticParameters.evaluationGamesAmount(); i++) {
                for (int depth : geneticParameters.evaluationDepth()) {
                    individu.bot.depthSearch = depth;
                    OthelloGame beginingPosition = new OthelloGame();

                    for(int j = 1; j <= random.nextInt(11); j++){
                        List<Integer> availableMoves = new ArrayList<>(beginingPosition.getAvailablePlacements());
                        beginingPosition.playMove(availableMoves.get(random.nextInt(availableMoves.size())));
                    }

                    //AdvancedPatternEvalBot adversaryBot = new AdvancedPatternEvalBot(false, depth);
                    AdvancedPatternEvalBot adversaryBot = (AdvancedPatternEvalBot) FIRST_ATTEMPT_BOT.copy();
                    adversaryBot.depthSearch = depth;
                    adversaryBot.setWhite(false);

                    Bot.GameResults results = launchEvaluationGame(individu.bot, adversaryBot, beginingPosition);
                    eval += (results.getGlobalResult() + 1) / 2f;
                }
            }
            individu.fitness = (individu.fitness + eval / (geneticParameters.evaluationGamesAmount() * geneticParameters.evaluationDepth().length)) / 2;
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


        public static Bot.GameResults launchEvaluationGame(Bot bot1, Bot bot2, OthelloGame beginningPosition){
            int amount = 2;
            Bot.GameResult[] results = new Bot.GameResult[amount];

            boolean bot1Color = bot1.isWhite(); // true si bot1 est blanc, false s'il est noir

            for(int i = 0; i < amount; i++){
                OthelloGame game = beginningPosition.clone();

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
