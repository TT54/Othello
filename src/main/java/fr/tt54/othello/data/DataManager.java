package fr.tt54.othello.data;

import fr.tt54.othello.data.complexity.AlphaBetaComplexityLoader;
import fr.tt54.othello.data.objects.Pattern;
import fr.tt54.othello.data.openings.OpeningLoader;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.game.OthelloGame;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DataManager {

    public static OpeningTree mainOpeningTree;

    private static boolean enabled = false;

    public static void main(String[] args) {
        DataManager.enable();
        //analyseComplexityDatas();

        generatePatternsValues();
        System.out.println("enabled");
    }


    public static void enable(){
        if(enabled) return;
        enabled = true;

        mainOpeningTree = new OpeningTree((byte) OthelloGame.positionToInt(5, 4));
        try {
            OpeningLoader.loadGames(OpeningLoader.HUMAN_GAME_FOLDER);
            OpeningLoader.loadGames(OpeningLoader.LOGISTELLO_GAME_FOLDER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
    
    
    private static void testPatterns(){
        OthelloGame game = new OthelloGame("a1b1a2b2g1g2h1h2a8a7b8b7h8g8h7g7");

        System.out.println(Arrays.toString(game.getTopLeftPattern()));
        System.out.println(Arrays.toString(game.getTopRightPattern()));
        System.out.println(Arrays.toString(game.getBottomLeftPattern()));
        System.out.println(Arrays.toString(game.getBottomRightPattern()));
    }

    private static void generatePatternsValues(){
        OpeningTree tree = DataManager.mainOpeningTree;
        readPattersFromMoves(new OthelloGame(), tree.getFirstMove());
    }

    /**
     * Stock les données des patterns obtenus à partir d'un certain coup chargé depuis la base de données
     * @param game
     * @param currentMove
     */
    private static void readPattersFromMoves(OthelloGame game, OpeningTree.OpeningMove currentMove){
        if(game.getMoveCount() > 60 - 24){
            // End Game
            Pattern.addPatternOccurrences(Pattern.GameStage.ENDGAME, game.getTopLeftPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.ENDGAME, game.getTopRightPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.ENDGAME, game.getBottomLeftPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.ENDGAME, game.getBottomRightPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
        } else if(game.getMoveCount() > 20){
            // Mid Game
            Pattern.addPatternOccurrences(Pattern.GameStage.MID_GAME, game.getTopLeftPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.MID_GAME, game.getTopRightPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.MID_GAME, game.getBottomRightPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.MID_GAME, game.getBottomLeftPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
        } else {
            // Opening
            Pattern.addPatternOccurrences(Pattern.GameStage.OPENING, game.getTopLeftPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.OPENING, game.getBottomLeftPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.OPENING, game.getTopRightPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
            Pattern.addPatternOccurrences(Pattern.GameStage.OPENING, game.getBottomRightPattern(), game.isWhiteToPlay(), currentMove.getScore(), currentMove.getGamesAmount());
        }


        if(!currentMove.getNextMoves().isEmpty()){
            for(Map.Entry<Byte, OpeningTree.OpeningMove> entry : currentMove.getNextMoves().entrySet()){
                OthelloGame copy = game.clone();

                copy.playMove(entry.getKey());

                readPattersFromMoves(copy, entry.getValue());
            }
        }
    }

}
