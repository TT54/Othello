package fr.tt54.othello.bots.utils;

import fr.tt54.othello.OthelloGame;
import fr.tt54.othello.data.patterns.Pattern;

public class Evaluation {

    public static float patternEval(OthelloGame game){
        if(game.isGameFinished()){
            if(game.getWhitePiecesCount() > game.getBlackPiecesCount()){
                return 500 + game.getWhitePiecesCount() - game.getBlackPiecesCount();
            } else if(game.getWhitePiecesCount() == game.getBlackPiecesCount()) {
                return 0;
            } else {
                return -500 + (game.getWhitePiecesCount() - game.getBlackPiecesCount());
            }
        }

        float value = 0;

        for(Pattern.PatternType patternType : Pattern.PatternType.values()){
            for(int[][] patternLocation : patternType.getPatternsLocations()){
                value += Pattern.getPatternFromPosition(patternType, Pattern.GameStage.MID_GAME, game.getPattern(patternLocation), game.isWhiteToPlay()).getPatternValue();
            }
        }

        if(!game.isWhiteToPlay()){
            return value * -1;
        }

        return value;
    }

    private static final int[][] casesValuation = {
            {500, -150, 30, 10, 10, 30, -150, 500},
            {-150, -250, 0, 0, 0, 0, -250, -150},
            {30, 0, 1, 2, 2, 1, 0, 30},
            {10, 0, 2, 16, 16, 2, 0, 10},
            {10, 0, 2, 16, 16, 2, 0, 10},
            {30, 0, 1, 2, 2, 1, 0, 30},
            {-150, -250, 0, 0, 0, 0, -250, -150},
            {500, -150, 30, 10, 10, 30, -150, 500}
    };

    public static int tableEval(OthelloGame game){
        long time = System.nanoTime();

        if(game.isGameFinished()){
            if(game.getWhitePiecesCount() > game.getBlackPiecesCount()){
                return 500 + game.getWhitePiecesCount() - game.getBlackPiecesCount();
            } else if(game.getWhitePiecesCount() == game.getBlackPiecesCount()) {
                return 0;
            } else {
                return -500 + (game.getWhitePiecesCount() - game.getBlackPiecesCount());
            }
        }

        int points = 0;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                points += game.getPiece(i, j) * casesValuation[i][j];
            }
        }

        return points;
    }

    public static int standardEval(OthelloGame game){
        int points = 0;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                points += game.getPiece(i, j);
            }
        }
        return points;
    }

}
