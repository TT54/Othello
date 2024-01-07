package fr.tt54.othello.data.patterns;

import fr.tt54.othello.bots.AdvancedPatternEvalBot;
import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.bots.RandomBot;
import fr.tt54.othello.data.openings.OpeningLoader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PatternBotTraining {

    public static final double BOT_MUTATION_PROBA = 0.2d; // Probabilité qu'un bot subisse une mutation d'un précédent bot
    public static final int NEW_BOTS = 5; // Nombre de bots totalement nouveau à chaque génération

    public static JSONObject createdBotsJson;


    private static int generation = 0;


    public static void start(){
        loadCreatedBotsJson();

        List<AdvancedPatternEvalBot> bots = new ArrayList<>();
        Random random = RandomBot.random;

        for(int i = 0; i < Population.GROUP_AMOUNT * Population.GROUP_SIZE; i++){
            bots.add(new AdvancedPatternEvalBot(true,
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000,
                    UUID.randomUUID()));
        }

        Population population = new Population(bots);
        population.evaluate();
    }

    private static void loadCreatedBotsJson() {
        JSONParser jsonParser = new JSONParser();
        if(OpeningLoader.class.getResource("/created_bots/bots.json") == null){
            createdBotsJson = new JSONObject();
        } else {
            File file = new File(OpeningLoader.class.getResource("/created_bots/bots.json").getFile());
            if (!file.exists()) {
                createdBotsJson = new JSONObject();
            } else {
                try {
                    FileReader reader = new FileReader(file);
                    Object obj = jsonParser.parse(reader);
                    createdBotsJson = (JSONObject) obj;
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                    System.out.println("une erreur est survenue");
                    createdBotsJson = new JSONObject();
                }
            }
        }
    }

    public static void nextGen(Population previousPopulation){
        List<AdvancedPatternEvalBot> bots = new ArrayList<>();
        Random random = RandomBot.random;

        AdvancedPatternEvalBot[][] bests = previousPopulation.findTwoBestsPerGroup();
        for(int i = 0; i < Population.GROUP_AMOUNT; i++){
            bots.add(bests[i][0]);
            bots.add(bests[i][1]);
        }

        // Ajout de nouveaux bots aléatoires
        for(int i = 0; i < NEW_BOTS; i++){
            bots.add(new AdvancedPatternEvalBot(true,
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000,
                    random.nextDouble() * 1000,
                    UUID.randomUUID()));
        }

        for(int i = bots.size(); i < Population.GROUP_AMOUNT * Population.GROUP_SIZE; i++){
            AdvancedPatternEvalBot toAdd;

            int bot1 = random.nextInt(Population.GROUP_AMOUNT),
                    bot2 = random.nextInt(Population.GROUP_AMOUNT),
                    bot3 = random.nextInt(Population.GROUP_AMOUNT),
                    bot4 = random.nextInt(Population.GROUP_AMOUNT);

            toAdd = new AdvancedPatternEvalBot(true,
                    previousPopulation.getFinalGroup()[bot1].getCornerCoeff(),
                    previousPopulation.getFinalGroup()[bot2].getBorderCoeff(),
                    previousPopulation.getFinalGroup()[bot3].getTableCoeff(),
                    previousPopulation.getFinalGroup()[bot4].getFreedomCoeff(),
                    UUID.randomUUID());

            if(random.nextDouble() <= BOT_MUTATION_PROBA){
                toAdd = mutate(toAdd);
            }

            bots.add(toAdd);
        }

        generation++;

        Population population = new Population(bots);
        population.evaluate();
    }

    private static AdvancedPatternEvalBot mutate(AdvancedPatternEvalBot bot){
        Random random = RandomBot.random;
        int mutatedParameter = random.nextInt(4);
        double mutationValue = random.nextDouble() * 1000;

        AdvancedPatternEvalBot mutation = new AdvancedPatternEvalBot(true,
                mutatedParameter == 0 ? mutationValue : bot.getCornerCoeff(),
                mutatedParameter == 1 ? mutationValue : bot.getBorderCoeff(),
                mutatedParameter == 2 ? mutationValue : bot.getTableCoeff(),
                mutatedParameter == 3 ? mutationValue : bot.getFreedomCoeff(),
                UUID.randomUUID());

        return random.nextDouble() <= BOT_MUTATION_PROBA ? mutate(mutation) : mutation;
    }

    private static void saveBot(AdvancedPatternEvalBot bot) {
        JSONObject botJson = new JSONObject();
        botJson.put("corner", bot.getCornerCoeff());
        botJson.put("border", bot.getBorderCoeff());
        botJson.put("table", bot.getTableCoeff());
        botJson.put("freedom", bot.getFreedomCoeff());
        createdBotsJson.put(bot.getUUID().toString(), botJson);
    }



    public static class Population{

        public static final int GROUP_SIZE = 5;
        public static final int GROUP_AMOUNT = 10;


        private final List<AdvancedPatternEvalBot> bots;
        private boolean[] finishedThread = new boolean[GROUP_AMOUNT];
        private JSONObject populationJson = new JSONObject();



        private AdvancedPatternEvalBot[][] groups;
        private double[][] groupsScores;

        private AdvancedPatternEvalBot[] finalGroup;
        private double[] finalScores;



        public Population(List<AdvancedPatternEvalBot> bots) {
            this.bots = bots;
            Arrays.fill(finishedThread, false);
        }

        public void evaluate(){
            runGroupStage();
        }

        private void runGroupStage(){
            groups = new AdvancedPatternEvalBot[GROUP_AMOUNT][GROUP_SIZE];
            groupsScores = new double[GROUP_AMOUNT][GROUP_SIZE];

            for(int i = 0; i < bots.size(); i++){
                AdvancedPatternEvalBot bot = bots.get(i);
                groups[i%GROUP_AMOUNT][i/GROUP_AMOUNT] = bot;
                bot.setBotNumber(i);
                if(!PatternBotTraining.createdBotsJson.containsKey(bot.getUUID().toString())){
                    PatternBotTraining.saveBot(bot);
                }
            }

            try (FileWriter file = new FileWriter("src/main/resources/created_bots/bots.json")) {
                file.write(createdBotsJson.toJSONString());
                file.flush();
                System.out.println("created bots saved");
            } catch (IOException e) {
                e.printStackTrace();
            }


            JSONObject botsJson = new JSONObject();
            JSONObject groupsJson = new JSONObject();
            for(int i = 0; i < groups.length; i++){
                JSONArray groupArray = new JSONArray();
                for(int j = 0; j < groups[i].length; j++){
                    AdvancedPatternEvalBot bot = groups[i][j];
                    botsJson.put("Bot" + bot.getBotNumber(), bot.getUUID().toString());
                    groupArray.add("Bot" + bot.getBotNumber());
                }
                groupsJson.put("group" + i, groupArray);
            }
            populationJson.put("botsInfos", botsJson);
            populationJson.put("groupsInfos", groupsJson);


            JSONObject groupStageJson = new JSONObject();
            JSONObject groupScoreJson = new JSONObject();
            for(int i = 0; i < GROUP_AMOUNT; i++){
                final int group = i;
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        JSONObject groupResultJson = new JSONObject();
                        for(int j = 0; j < GROUP_SIZE; j++){
                            AdvancedPatternEvalBot bot1 = groups[group][j];
                            bot1.setWhite(true);
                            for(int k = 0; k < GROUP_SIZE; k++){
                                if(j != k){
                                    System.out.println("Match en cours...");
                                    int[] result = Bot.confrontBots(bot1, groups[group][k], 1, 10 * 1000, false);
                                    groupsScores[group][j] += result[0] + 0.5d * result[1];
                                    groupsScores[group][k] += result[2] + 0.5d * result[1];
                                    groupResultJson.put("Bot" + bot1.getBotNumber() + " - Bot" + groups[group][k].getBotNumber(), result[0] + 0.5d * result[1] - result[2]);
                                }
                            }


                            // On fait également affronter à chaque bot une référence (le bot se basant uniquement sur la table), pour s'assurer que les bots réussissant à les battre soient mis en avant
                            int[] result = Bot.confrontBots(bot1, new AdvancedPatternEvalBot(false, 0, 0, 1, 0, UUID.randomUUID()), 2, 10 * 1000, false);
                            groupsScores[group][j] += 10 * (result[0] + 0.5d * result[1]);
                            groupResultJson.put("Bot" + bot1.getBotNumber() + " - Reference", result[0] + 0.5d * result[1] - result[2]);
                        }

                        groupStageJson.put("group" + group, groupResultJson);
                        groupScoreJson.put("group" + group, Arrays.toString(groupsScores[group]));

                        populationJson.put("groupStageResults", groupStageJson);
                        populationJson.put("groupStageScores", groupScoreJson);

                        finishedThread[group] = true;

                        System.out.println("Un thread terminé");

                        runFinalStage();

                        this.interrupt();
                    }
                };

                thread.start();
            }
        }

        private void runFinalStage(){
            for(boolean b : finishedThread){
                if(!b)
                    return;
            }

            System.out.println("Début de la phase finale");

            finalGroup = generateFinalGroup();
            finishedThread = new boolean[finalGroup.length];
            Arrays.fill(finishedThread, false);
            finalScores = new double[finalGroup.length];

            JSONObject finalStageJson = new JSONObject();
            JSONObject finalScoreJson = new JSONObject();

            for(int i = 0; i < finishedThread.length; i++){
                final int botIndex = i;
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        AdvancedPatternEvalBot bot = (AdvancedPatternEvalBot) finalGroup[botIndex].copy();
                        bot.setWhite(true);
                        for(int j = 0; j < finalGroup.length; j++){
                            if(botIndex != j){
                                int[] result = Bot.confrontBots(bot, finalGroup[j], 1, 10 * 1000, false);
                                finalScores[botIndex] += result[0] + 0.5d * result[1];
                                finalScores[j] += result[2] + 0.5d * result[1];
                                finalStageJson.put(botIndex + " | Bot" + bot.getBotNumber() + " - Bot" + finalGroup[j].getBotNumber(), result[0] + 0.5d * result[1] - result[2]);
                            }
                        }

                        // On fait également affronter à chaque bot une référence (le bot se basant uniquement sur la table), pour s'assurer que les bots réussissant à les battre soient mis en avant
                        int[] result = Bot.confrontBots(bot, new AdvancedPatternEvalBot(false, 0, 0, 1, 0, UUID.randomUUID()), 2, 10 * 1000, false);
                        finalScores[botIndex] += 10 * (result[0] + 0.5d * result[1]);
                        finalStageJson.put("Bot" + bot.getBotNumber() + " - Reference", result[0] + 0.5d * result[1] - result[2]);


                        finishedThread[botIndex] = true;

                        finalScoreJson.put("final", Arrays.toString(finalScores));

                        populationJson.put("finalResults", finalStageJson);
                        populationJson.put("finalScores", finalScoreJson);

                        finishEvaluation();
                    }
                };

                thread.start();
            }
        }

        private void finishEvaluation(){
            for(boolean b : finishedThread){
                if(!b)
                    return;
            }

            int maxIndex = 0;
            for(int k = 1; k < finalGroup.length; k++){
                if(finalScores[k] > finalScores[maxIndex]){
                    maxIndex = k;
                }
            }

            AdvancedPatternEvalBot winner = finalGroup[maxIndex];

            JSONObject winnerJson = new JSONObject();
            winnerJson.put("corner", winner.getCornerCoeff());
            winnerJson.put("border", winner.getBorderCoeff());
            winnerJson.put("table", winner.getTableCoeff());
            winnerJson.put("freedom", winner.getFreedomCoeff());
            winnerJson.put("uuid", winner.getUUID().toString());
            winnerJson.put("number", winner.getBotNumber());
            populationJson.put("winner", winnerJson);


            System.out.println("saving file");

            try (FileWriter file = new FileWriter("generation - " + PatternBotTraining.generation + ".json")) {
                file.write(populationJson.toJSONString());
                file.flush();
                System.out.println("file saved");
            } catch (IOException e) {
                e.printStackTrace();
            }

            PatternBotTraining.nextGen(this);
        }


        private AdvancedPatternEvalBot[] generateFinalGroup(){
            AdvancedPatternEvalBot[] group = new AdvancedPatternEvalBot[GROUP_AMOUNT];
            for(int i = 0; i < GROUP_AMOUNT; i++){
                int maxIndex = 0;
                for(int k = 1; k < groups[i].length; k++){
                    if(groupsScores[i][k] > groupsScores[i][maxIndex]){
                        maxIndex = k;
                    }
                }
                group[i] = groups[i][maxIndex];
            }
            return group;
        }

        public AdvancedPatternEvalBot[][] findTwoBestsPerGroup(){
            AdvancedPatternEvalBot[][] group = new AdvancedPatternEvalBot[GROUP_AMOUNT][2];
            for(int i = 0; i < GROUP_AMOUNT; i++){
                int maxIndex = 0;
                int secondIndex = 0;
                for(int k = 1; k < groups[i].length; k++){
                    if(groupsScores[i][k] > groupsScores[i][maxIndex]){
                        secondIndex = maxIndex;
                        maxIndex = k;
                    } else if(groupsScores[i][k] > groupsScores[i][secondIndex]){
                        secondIndex = k;
                    }
                }
                group[i][0] = groups[i][maxIndex];
                group[i][1] = groups[i][secondIndex];
            }
            return group;
        }

        public AdvancedPatternEvalBot[][] getGroups() {
            return groups;
        }

        public double[][] getGroupsScores() {
            return groupsScores;
        }

        public AdvancedPatternEvalBot[] getFinalGroup() {
            return finalGroup;
        }

        public double[] getFinalScores() {
            return finalScores;
        }
    }

}
