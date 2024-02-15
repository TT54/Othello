package fr.tt54.othello.data;

import fr.tt54.othello.bots.AdvancedPatternEvalBot;
import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.data.patterns.Pattern;

import java.util.Random;

public class GeneticAlgorithm {

    public static Random random = new Random();

    public static final float mutationProba = 0.15f;
    public static final float crossOverProba = 0.60f;

    public static final int EVALUATION_AMOUNT_OF_GAMES = 4;
    public static final int[] EVALUATION_DEPTH = new int[] {0, 1, 2};
    public static final int POPULATION = 60;


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

    public static void nextGeneration(){
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
        }

        //TODO Save datas

        selectNextGen();
        nextGeneration();
    }

    public static void evaluatePopulation(){
        for(int i = 0; i < POPULATION; i++){
            Individu.evalFitness(individus[i]);
        }
    }

    public static void selectNextGen(){
        float totalFitness = 0;
        for(int i = 0; i < POPULATION; i++){
            totalFitness += individus[i].fitness;
        }

        Individu[] nextPop = new Individu[POPULATION];
        for(int i = 0; i < POPULATION; i++){
            float selected = random.nextFloat(totalFitness);

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


        public static void mutate(Individu individu) {
            int length = individu.bot.patternCoeffs.length;
            if (random.nextFloat() < mutationProba) {
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
                    Bot.GameResults results = Bot.confrontBots(individu.bot, new AdvancedPatternEvalBot(false, depth), 2, 20, false);
                    eval += (results.getGlobalResult() + 1) / 2f;
                }
            }
            individu.fitness = (individu.fitness + eval) / 2;
        }

        public static float getRandomValue() {
            return 2 * random.nextFloat() - 1;
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
    }

}
