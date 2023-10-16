package fr.tt54.othello.bots;

import fr.tt54.othello.Main;
import fr.tt54.othello.game.OthelloGame;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaBot {

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
                AlphaBetaBot.playBestMove(game, depth);
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
        //System.out.println("on joue un coup");
        int[] result = evaluatePosition(position, depth, position.isWhiteToPlay(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        int[] move = OthelloGame.intToPosition(result[1]);
        position.playMove(move[0], move[1]);
    }


    public static int[] evaluatePosition(OthelloGame startingPosition, int depth, boolean isMaxPlayer, int alpha, int beta){
        if(depth == 0 || startingPosition.isGameFinished()){
            return new int[] {MinMaxBot.evaluationFunction2(startingPosition)};
        }

        if(isMaxPlayer){
            int max = Integer.MIN_VALUE;
            int bestMove = -1;

            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                int previousMax = max;
                max = Math.max(max, evaluatePosition(game, depth - 1, canAdversaryPlay != isMaxPlayer, alpha, beta)[0]);
                if(max != previousMax){
                    bestMove = move;
                }

                if(beta <= max){
                    // On fait une coupure beta
                    // ==> beta étant la plus petite valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre max sera plus grand que beta, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return new int[]{max, bestMove};
                }

                alpha = Math.max(alpha, max);
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
                min = Math.min(min, evaluatePosition(game, depth - 1, canAdversaryPlay != isMaxPlayer, alpha, beta)[0]);
                if(min != previousMin){
                    bestMove = move;
                }

                if(alpha >= min){
                    // On fait une coupure alpha
                    // ==> alpha étant la plus grande valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre min sera plus petit qu'alpha, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return new int[] {min, bestMove};
                }

                beta = Math.min(beta, min);
            }
            return new int[] {min, bestMove};
        }
    }

}
