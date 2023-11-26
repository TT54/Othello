package fr.tt54.othello.bots;

import fr.tt54.othello.bots.utils.EvaluationTree;
import fr.tt54.othello.bots.utils.MoveChain;
import fr.tt54.othello.bots.utils.MoveEvaluation;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.game.OthelloGame;

import java.util.Map;

public class OpeningTestBot extends Bot{

    public static final double FACTEUR = 5.5d;

    private MoveChain winingSequence = null;

    public OpeningTestBot(boolean white) {
        super(white);
        DataManager.enable();
    }

    @Override
    public boolean playMove(OthelloGame game, long timeLeft) {
        int movesToPlayLeft = (60 - game.getMoveCount()) / 2;
        long timeToPlay = (timeLeft == -1) ? Long.MAX_VALUE : (movesToPlayLeft == 0) ? timeLeft : timeLeft / movesToPlayLeft;

        byte[] playedMoves = game.getPlayedMoves();

        OpeningTree.OpeningMove move = DataManager.mainOpeningTree.getMoveAfterSequence(playedMoves);

        if(playedMoves[0] == -1){
            game.playMove(4, 5);
            return true;
        } else {
            if (move != null) {
                Map<Byte, OpeningTree.OpeningMove> nextMoves = move.getNextMoves();

                if (nextMoves.size() > 0) {
                    float maxScore = -Float.MAX_VALUE;
                    OpeningTree.OpeningMove bestMove = null;

                    for (byte m : nextMoves.keySet()) {
                        OpeningTree.OpeningMove openingMove = nextMoves.get(m);
                        float openingScore = this.isWhite() ? openingMove.getScore() : -openingMove.getScore();
                        if (openingScore > maxScore) {
                            bestMove = openingMove;
                            maxScore = openingScore;
                        }
                    }


                    if (maxScore >= 0) {
                        int[] movePos = OthelloGame.intToPosition(bestMove.getMove());
                        game.playMove(movePos[0], movePos[1]);
                        return true;
                    }
                }
            }
        }

        iterativeSearch(game, timeToPlay, 0, 0);

        return true;
    }

    private long[] testIterative(OthelloGame startingPosition, int depth, int maxDepth){
        long[] values = new long[maxDepth];

        long time = System.nanoTime();
        MoveEvaluation bestMove = alphaBeta(startingPosition.clone(), depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        values[depth - 1] = System.nanoTime() - time;

        if(depth != maxDepth){
            long[] iterations = testIterative(startingPosition, depth + 1, maxDepth);
            for(int i = 0; i < iterations.length; i++){
                values[i] += iterations[i];
            }
        } else {
            startingPosition.playMove(bestMove.getMoveChain().getPosition());
        }

        return values;
    }


    /**
     *
     * @param startingPosition position initiale
     * @param timeLeft temps restant pour jouer le coup
     * @param previousDepth profondeur de la recherche précédente
     * @param previousIterationDuration durée de la recherche précédente
     * @return true si un coup a été joué, false sinon
     */
    private boolean iterativeSearch(OthelloGame startingPosition, long timeLeft, int previousDepth, long previousIterationDuration){
        long estimatedTime = (long) (previousIterationDuration * FACTEUR);

        if(estimatedTime < timeLeft){
            long time = System.currentTimeMillis();
            MoveEvaluation result = alphaBeta(startingPosition.clone(), previousDepth + 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
/*
            System.out.println("tree move : " + result2.getBestMove(startingPosition.isWhiteToPlay()).getMoveChain().getPosition());
            System.out.println("tree eval : " + result2.getBestMove(startingPosition.isWhiteToPlay()).getEvaluation());
            System.out.println("normal move : " + result.getMoveChain().getPosition());
            System.out.println("normal eval : " + result.getEvaluation());*/

            long elapsedTime = System.currentTimeMillis() - time;

            if (result.isFinalEvaluation() || !iterativeSearch(startingPosition, timeLeft - elapsedTime, previousDepth + 1, elapsedTime)) {
                startingPosition.playMove(result.getMoveChain().getPosition());

                if(result.isFinalEvaluation()){
                    System.out.println("Calcul exact à partir du coup " + startingPosition.getMoveCount());
                }
            }
            return true;
        }

        return false;
    }



    private static MoveEvaluation alphaBeta(OthelloGame startingPosition, int depth, int alpha, int beta){
        if(depth == 0 || startingPosition.isGameFinished()){
            return new MoveEvaluation(MinMaxBot.evaluationFunction2(startingPosition), null, startingPosition.isGameFinished());
        }

        if(startingPosition.isWhiteToPlay()){
            MoveEvaluation bestMove = new MoveEvaluation(Integer.MIN_VALUE, null, false);
            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                MoveEvaluation currentEval = alphaBeta(game, depth - 1, alpha, beta);
                int max = Math.max(bestMove.getEvaluation(), currentEval.getEvaluation());
                if(max != bestMove.getEvaluation()){
                    bestMove = new MoveEvaluation(max, new MoveChain(null, currentEval.getMoveChain(), move, previousPlayer), currentEval.isFinalEvaluation());
                }

                if(beta <= max){
                    // On fait une coupure beta
                    // ==> beta étant la plus petite valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre max sera plus grand que beta, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return bestMove;
                }

                alpha = Math.max(alpha, max);
            }
            return bestMove;
        } else {
            MoveEvaluation bestMove = new MoveEvaluation(Integer.MAX_VALUE, null, false);

            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                MoveEvaluation currentEval = alphaBeta(game, depth - 1, alpha, beta);
                int min = Math.min(bestMove.getEvaluation(), currentEval.getEvaluation());
                if(min != bestMove.getEvaluation()){
                    bestMove = new MoveEvaluation(min, new MoveChain(null, currentEval.getMoveChain(), move, previousPlayer), currentEval.isFinalEvaluation());
                }

                if(alpha >= min){
                    // On fait une coupure alpha
                    // ==> alpha étant la plus grande valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre min sera plus petit qu'alpha, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return bestMove;
                }

                beta = Math.min(beta, min);
            }
            return bestMove;
        }
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
