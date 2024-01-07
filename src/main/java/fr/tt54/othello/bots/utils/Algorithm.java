package fr.tt54.othello.bots.utils;

import fr.tt54.othello.bots.MinMaxBot;
import fr.tt54.othello.data.objects.PlayedPosition;
import fr.tt54.othello.game.OthelloGame;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class Algorithm {

    private final Map<PlayedPosition, PositionValue> alphaBetaStorage = new HashMap<>();

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
        PositionValue value = new PositionValue(Integer.MIN_VALUE, Integer.MAX_VALUE, null);

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


    private static EvaluationTree alphaBeta2(OthelloGame startingPosition, int depth, int alpha, int beta){
        if(depth == 0 || startingPosition.isGameFinished()){
            return new EvaluationTree(new MoveEvaluation(MinMaxBot.evaluationFunction2(startingPosition), null, startingPosition.isGameFinished()));
        }

        EvaluationTree evaluationTree = new EvaluationTree();

        if(startingPosition.isWhiteToPlay()){
            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                EvaluationTree moveEvaluationTree = alphaBeta2(game, depth - 1, alpha, beta); // Arbre des évaluations réalisé récursivement après avoir joué le coup "move"

                MoveEvaluation currentBestMove = evaluationTree.getBestMove(previousPlayer); // Meilleur coup trouvé à partir de la position startingPosition
                MoveEvaluation playedMoveEval = moveEvaluationTree.getBestMove(game.isWhiteToPlay()); // Meilleur coup trouvé après avoir joué le coup "move"

                int max = currentBestMove == null ? playedMoveEval.getEvaluation() : Math.max(currentBestMove.getEvaluation(), playedMoveEval.getEvaluation());
                evaluationTree.addMove(new MoveEvaluation(max, new MoveChain(null, playedMoveEval.getMoveChain(), move, previousPlayer), playedMoveEval.isFinalEvaluation()), moveEvaluationTree);

                if(beta <= max){
                    // On fait une coupure beta
                    // ==> beta étant la plus petite valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre max sera plus grand que beta, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return evaluationTree;
                }

                alpha = Math.max(alpha, max);
            }
            return evaluationTree;
        } else {
            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                EvaluationTree moveEvaluationTree = alphaBeta2(game, depth - 1, alpha, beta); // Arbre des évaluations réalisé récursivement après avoir joué le coup "move"

                MoveEvaluation currentBestMove = evaluationTree.getBestMove(previousPlayer); // Meilleur coup trouvé à partir de la position startingPosition
                MoveEvaluation playedMoveEval = moveEvaluationTree.getBestMove(game.isWhiteToPlay()); // Meilleur coup trouvé après avoir joué le coup "move"

                int min = currentBestMove == null ? playedMoveEval.getEvaluation() : Math.min(currentBestMove.getEvaluation(), playedMoveEval.getEvaluation());
                evaluationTree.addMove(new MoveEvaluation(min, new MoveChain(null, playedMoveEval.getMoveChain(), move, previousPlayer), playedMoveEval.isFinalEvaluation()), moveEvaluationTree);

                if(alpha >= min){
                    // On fait une coupure alpha
                    // ==> alpha étant la plus grande valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre min sera plus petit qu'alpha, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return evaluationTree;
                }

                beta = Math.min(beta, min);
            }
            return evaluationTree;
        }
    }

    public static class PositionValue{

        public int upperbound;
        public int lowerbound;
        public MoveChain moveChain;

        public PositionValue(int upperbound, int lowerbound, MoveChain moveChain){
            this.upperbound = upperbound;
            this.lowerbound = lowerbound;
            this.moveChain = moveChain;
        }
    }

}
