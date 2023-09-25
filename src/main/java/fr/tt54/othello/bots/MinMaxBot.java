package fr.tt54.othello.bots;

import fr.tt54.othello.Main;
import fr.tt54.othello.game.OthelloGame;

import java.util.ArrayList;
import java.util.List;

public class MinMaxBot {


    /**
     *
     * @return true si le bot a gagné, false sinon
     */
    public static boolean playAgainstRandom(boolean isMinMaxWhite, int depth){
        OthelloGame game = new OthelloGame();

        while(!game.isGameFinished()){
            if(game.isWhiteToPlay() != isMinMaxWhite){
                List<Integer> moves = new ArrayList<>(game.getAvailablePlacements());

                if(moves.isEmpty()){
                    System.out.println("Moves empty ! " + game.getMoveCount());
                }

                int[] move = OthelloGame.intToPosition(moves.get(Main.random.nextInt(moves.size())));
                game.playMove(move[0], move[1]);
            } else {
                MinMaxBot.playBestMove(game, depth);
            }
        }

        int white = game.getWhitePiecesCount();
        int black = game.getBlackPiecesCount();
        System.out.println("Score : " + white + " / " + black);
        if(white > black){
            System.out.println("Les blancs ont gagné !");
        } else if(white == black){
            System.out.println("Il y a eu égalité !");
        } else {
            System.out.println("Les noirs ont gagné !");
        }

        return isMinMaxWhite ? white > black : black > white;
    }

    public static void playBestMove(OthelloGame position, int depth){
        int[] result = evaluatePosition(position, depth, position.isWhiteToPlay());
        int[] move = OthelloGame.intToPosition(result[1]);
        position.playMove(move[0], move[1]);
    }


    public static int[] evaluatePosition(OthelloGame startingPosition, int depth, boolean isMaxPlayer){
        if(depth == 0 || startingPosition.isGameFinished()){
            return new int[] {evaluateFunction(startingPosition)};
        }

        if(isMaxPlayer){
            int max = Integer.MIN_VALUE;
            int bestMove = -1;
            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                int previousMax = max;
                max = Math.max(max, evaluatePosition(game, depth - 1, canAdversaryPlay != isMaxPlayer)[0]);
                if(max != previousMax){
                    bestMove = move;
                }
            }
            return new int[] {max, bestMove};
        } else {
            int min = Integer.MAX_VALUE;
            int bestMove = -1;
            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                int previousMin = min;
                min = Math.min(min, evaluatePosition(game, depth - 1, canAdversaryPlay != isMaxPlayer)[0]);
                if(previousMin != min){
                    bestMove = move;
                }
            }
            return new int[] {min, bestMove};
        }
    }


    public static int evaluateFunction(OthelloGame game){
        if(game.isGameFinished()){
            if(game.getWhitePiecesCount() > game.getBlackPiecesCount()){
                return Integer.MAX_VALUE;
            } else if(game.getWhitePiecesCount() == game.getBlackPiecesCount()) {
                return 0;
            } else {
                return Integer.MAX_VALUE;
            }
        }

        int points = 0;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                points += game.getPiece(i, j);
            }
        }
        return points;
    }

}
