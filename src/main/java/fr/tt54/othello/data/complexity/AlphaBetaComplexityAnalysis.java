package fr.tt54.othello.data.complexity;

import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.data.genetic.GeneticAlgorithm;
import fr.tt54.othello.game.OthelloGame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class AlphaBetaComplexityAnalysis {

    private static final Random random = new Random();

    public static void launchAnalysis(int[] depths, int positionsToCheck, String fileFolder, String fileName){
        File file = new File(fileFolder, fileName + ".csv");

        String datas = "Avancement";
        for(int depth : depths){
            datas += ";Profondeur " + depth;
        }

        for(int i = 0; i < positionsToCheck; i++){
            if(i % (positionsToCheck / 10) == 0){
                System.out.println(i + "/" + positionsToCheck);
            }
            int playedMoves = random.nextInt(40) + 10;
            OthelloGame game = OthelloGame.generateRandomPos(playedMoves);

            long elapsedTimes[] = new long[depths.length];
            for(int j = 0; j < depths.length; j++){
                long time = System.nanoTime();
                Bot.alphaBeta(game, depths[j], Integer.MIN_VALUE, Integer.MAX_VALUE, GeneticAlgorithm.FIRST_ATTEMPT_BOT::advancedEvaluation);
                elapsedTimes[j] = System.nanoTime() - time;
            }

            try {
                FileWriter writer = new FileWriter(file);

                datas += "\n" + playedMoves;
                for(int k = 0; k < depths.length; k++){
                    datas += ";" + elapsedTimes[k];
                }

                writer.write(datas);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
