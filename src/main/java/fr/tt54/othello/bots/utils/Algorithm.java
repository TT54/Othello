package fr.tt54.othello.bots.utils;

import fr.tt54.othello.bots.MinMaxBot;
import fr.tt54.othello.bots.OpeningTestBot;
import fr.tt54.othello.data.objects.PlayedPosition;
import fr.tt54.othello.game.OthelloGame;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class Algorithm {

    private final Map<PlayedPosition, OpeningTestBot.PositionValue> alphaBetaStorage = new HashMap<>();

    private MoveEvaluation mtdf(OthelloGame startingPosition, boolean isMaxPlayer, int defaultValue, int depth){
        MoveEvaluation currentValue = new MoveEvaluation(defaultValue, null, false);
        int beta;
        int upperBound = Integer.MAX_VALUE;
        int lowerBound = Integer.MIN_VALUE;
        do{
            if(currentValue.getEvaluation() == lowerBound){
                beta = currentValue.getEvaluation() + 1;
            } else {
                beta = currentValue.getEvaluation();
            }
            currentValue = alphaBetaWithMemory(startingPosition, depth, isMaxPlayer, beta - 1, beta);
            if(currentValue.getEvaluation() < beta){
                upperBound = currentValue.getEvaluation();
            } else {
                lowerBound = currentValue.getEvaluation();
            }
        } while (lowerBound < upperBound);

        return currentValue;
    }


    private MoveEvaluation alphaBetaWithMemory(OthelloGame startingPosition, int depth, boolean isMaxPlayer, int alpha, int beta) {
        PlayedPosition pos = PlayedPosition.convertPosition(startingPosition);
        OpeningTestBot.PositionValue value = new OpeningTestBot.PositionValue(Integer.MIN_VALUE, Integer.MAX_VALUE, null);

        if(alphaBetaStorage.containsKey(pos)){
            value = alphaBetaStorage.get(pos);
            if(value.lowerbound >= beta){
                return new MoveEvaluation(value.lowerbound, value.moveChain, false);
            } else if(value.upperbound <= alpha){
                return new MoveEvaluation(value.upperbound, value.moveChain, false);
            }
            alpha = Math.max(alpha, value.lowerbound);
            beta = Math.min(beta, value.upperbound);
        }

        if(startingPosition.isGameFinished() || depth <= 0){
            return new MoveEvaluation(MinMaxBot.evaluationFunction2(startingPosition), null, startingPosition.isGameFinished());
        }

        MoveChain bestMove = null;
        int currentEvaluation;

        if(isMaxPlayer){
            currentEvaluation = Integer.MIN_VALUE;
            int a = alpha;

            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();

                int[] movePos = OthelloGame.intToPosition(move);
                game.playMove(movePos[0], movePos[1]);

                MoveEvaluation nextEval = alphaBetaWithMemory(game, depth - 1, previousPlayer == game.isWhiteToPlay()  ? isMaxPlayer : !isMaxPlayer, a, beta);
                if(nextEval.getEvaluation() > currentEvaluation){
                    bestMove = new MoveChain(null, nextEval.getMoveChain(), move, previousPlayer);
                    currentEvaluation = nextEval.getEvaluation();
                }

                a = Math.max(a, currentEvaluation);

                if(currentEvaluation >= beta){
                    break;
                }
            }
        } else {
            currentEvaluation = Integer.MAX_VALUE;
            int b = beta;

            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();

                int[] movePos = OthelloGame.intToPosition(move);
                game.playMove(movePos[0], movePos[1]);

                MoveEvaluation nextEval = alphaBetaWithMemory(game, depth - 1, previousPlayer == game.isWhiteToPlay()  ? isMaxPlayer : !isMaxPlayer, alpha, b);
                if(nextEval.getEvaluation() < currentEvaluation){
                    bestMove = new MoveChain(null, nextEval.getMoveChain(), move, previousPlayer);
                    currentEvaluation = nextEval.getEvaluation();
                }

                b = Math.min(b, currentEvaluation);

                if(currentEvaluation <= alpha){
                    break;
                }
            }
        }

        if(currentEvaluation <= alpha){
            value.upperbound = currentEvaluation;
            value.moveChain = bestMove;
            this.alphaBetaStorage.put(pos, value);
        }
        if(currentEvaluation >= beta){
            value.lowerbound = currentEvaluation;
            value.moveChain = bestMove;
            this.alphaBetaStorage.put(pos, value);
        }

        return new MoveEvaluation(currentEvaluation, bestMove, false);
    }

}
